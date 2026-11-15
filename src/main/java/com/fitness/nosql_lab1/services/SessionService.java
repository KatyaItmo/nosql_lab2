package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.objects.User;
import com.fitness.nosql_lab1.repos.CartRepository;
import com.fitness.nosql_lab1.repos.UserRepository;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class SessionService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    public SessionService(UserRepository userRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    public ResponseCookie login(User user) throws Exception {
        String sessionToken = UUID.randomUUID().toString();

        userRepository.createSession(sessionToken, user);

        return ResponseCookie.from("SESSION", sessionToken)
                .httpOnly(true)
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie logout(String sessionToken) throws Exception {
        if (sessionToken != null) {
            userRepository.deleteSession(sessionToken);
            cartRepository.clearCart(sessionToken);
        }

        return ResponseCookie.from("SESSION", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
    }
}
