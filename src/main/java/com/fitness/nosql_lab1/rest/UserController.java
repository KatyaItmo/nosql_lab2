package com.fitness.nosql_lab1.rest;

import com.fitness.nosql_lab1.dtos.UserDto;
import com.fitness.nosql_lab1.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<String> registerUser(@RequestBody UserDto user) {
        return userService.registerUser(user);
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestBody String username) {
        return userService.deleteUser(username);
    }

    @PostMapping("/session")
    public ResponseEntity<String> login(@RequestBody UserDto user) throws Exception {
        return userService.loginUser(user);
    }

    @DeleteMapping("/session")
    public ResponseEntity<String> logout(
            @CookieValue(name = "SESSION", required = false) String session
    ) throws Exception {
        return userService.logoutUser(session);
    }
}