package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.db.EtcdService;
import com.fitness.nosql_lab1.db.repos.data.DaoResponse;
import com.fitness.nosql_lab1.dtos.userDto.AttachmentDto;
import com.fitness.nosql_lab1.dtos.userDto.UserAuth;
import com.fitness.nosql_lab1.dtos.userDto.UserData;
import com.fitness.nosql_lab1.dtos.userDto.UserRegistration;
import com.fitness.nosql_lab1.objects.Role;
import com.fitness.nosql_lab1.objects.user.AttachmentMetadata;
import com.fitness.nosql_lab1.objects.user.User;
import com.fitness.nosql_lab1.db.repos.UserDao;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserDao userDao;
    private final SessionService sessionService;
    private final EtcdService etcdService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao,
                       SessionService sessionService, EtcdService etcdService,
                       PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.sessionService = sessionService;
        this.etcdService = etcdService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<String> registerUser(UserRegistration userDto) {
        if (userDto.getRole().equals(Role.MODER)) {
            if (!userDto.getSecreteCode().equals("cheremsha")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("неверный код модератора");
            }
        }

        if (checkUser(userDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(userDto.getUsername() + " уже зарегистрирован");
        }

        String hashedPassword = passwordEncoder.encode(userDto.getPassword());

        User user = new User(
                userDto.getUsername(),
                userDto.getUsername(),
                hashedPassword,
                userDto.getRole(),
                Map.of(),
                LocalDateTime.now()
        );

        userDao.createUser(user);

        return ResponseEntity.ok("зарегистрирован пользователь " + user.getUsername());
    }


    public ResponseEntity<UserData> loginUser(UserAuth userDto) throws Exception {
        if (!checkUser(userDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new UserData("нет такого пользователя"));
        }

        User userDb = userDao.getUserById(userDto.getUsername());

        if (!passwordEncoder.matches(userDto.getPassword(), userDb.getPassword())) {
            if (etcdService.processCounter(userDto.getUsername(), 100, 5, false) >= 5) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new UserData("вы превысили число попыток входа"));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new UserData("неверный пароль"));
        }

        if (etcdService.processCounter(userDto.getUsername(), 100, 5, true) >= 5) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new UserData("ваш аккаунт временно недоступен (превышено число попыток входа)"));
        }

        UserData userData = new UserData(
                userDb.getUsername(),
                userDb.getRole(),
                userDb.getAttachments().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> {
                                    AttachmentMetadata model = entry.getValue();
                                    return new AttachmentDto(
                                            model.getContentType(),
                                            model.getLength(),
                                            model.getDigest(),
                                            model.getRevpos(),
                                            model.isStub()
                                    );
                                }
                        ))
        );

        String cookie = sessionService.login(userData).toString();
        userData.setAuthMessage("выполнен вход под логином " + userDto.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie)
                .body(userData);
    }


    public ResponseEntity<String> logoutUser(String sessionToken) throws Exception {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionService.logout(sessionToken).toString())
                .body("вы вышли из аккаунта");
    }

    public ResponseEntity<String> deleteUser(String username) {
        DaoResponse daoResponse = userDao.deleteUser(username);
        if (!daoResponse.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(daoResponse.getMessage());
        }
        return ResponseEntity.ok(daoResponse.getMessage());
    }

    private boolean checkUser(String username) {
        return userDao.getUserById(username) != null;
    }
}