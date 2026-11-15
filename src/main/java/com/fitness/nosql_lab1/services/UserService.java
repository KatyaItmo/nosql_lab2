package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.dtos.UserDto;
import com.fitness.nosql_lab1.objects.Role;
import com.fitness.nosql_lab1.objects.User;
import com.fitness.nosql_lab1.repos.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final EtcdService etcdService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       SessionService sessionService, EtcdService etcdService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.etcdService = etcdService;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<String> registerUser(UserDto userDto) {
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
                LocalDateTime.now()
        );

        userRepository.save(user);

        return ResponseEntity.ok("зарегистрирован пользователь " + user.getUsername());
    }


    public ResponseEntity<String> loginUser(UserDto userDto) throws Exception {
        if (!checkUser(userDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("нет такого пользователя");
        }

        User userDb = userRepository.findById(userDto.getUsername());

        if (!passwordEncoder.matches(userDto.getPassword(), userDb.getPassword())) {
            if (etcdService.processCounter(userDto.getUsername(), 100, 5, false) >= 5) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("вы превысили число попыток входа");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body("неверный пароль");
        }

        if (etcdService.processCounter(userDto.getUsername(), 100, 5, true) >= 5) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ваш аккаунт временно недоступен (превышено число попыток входа)");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionService.login(userDb).toString())
                .body("выполнен вход под логином " + userDto.getUsername());
    }


    public ResponseEntity<String> logoutUser(String sessionToken) throws Exception {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionService.logout(sessionToken).toString())
                .body("вы вышли из аккаунта");
    }

    public ResponseEntity<String> deleteUser(String username) {
        if (!userRepository.delete(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("не удалось удалить аккаунт");
        }
        return ResponseEntity.ok("аккаунт " + username + " удален");
    }

    private boolean checkUser(String username) {
        return userRepository.findById(username) != null;
    }
}