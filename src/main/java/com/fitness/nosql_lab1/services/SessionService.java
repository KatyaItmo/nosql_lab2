package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.dtos.userDto.UserData;
import com.fitness.nosql_lab1.db.repos.CartDao;
import com.fitness.nosql_lab1.db.repos.UserDao;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionService {
    private final UserDao userDao;
    private final CartDao cartDao;

    public SessionService(UserDao userDao, CartDao cartDao) {
        this.userDao = userDao;
        this.cartDao = cartDao;
    }

    public ResponseCookie login(UserData user) throws Exception {
        String sessionToken = UUID.randomUUID().toString();

        userDao.createSession(sessionToken, user);

        return ResponseCookie.from("SESSION", sessionToken)
                .httpOnly(true)
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie logout(String sessionToken) throws Exception {
        if (sessionToken != null) {
            userDao.deleteSession(sessionToken);
            cartDao.clearCart(sessionToken);
        }

        return ResponseCookie.from("SESSION", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
    }
}
