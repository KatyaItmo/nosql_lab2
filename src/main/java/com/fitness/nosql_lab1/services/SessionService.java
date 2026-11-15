package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.objects.User;
import com.fitness.nosql_lab1.repos.UserRepository;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class SessionService {
    private final UserRepository userRepository;

    public SessionService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        }

        return ResponseCookie.from("SESSION", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
    }
}
