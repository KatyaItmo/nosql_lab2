package com.fitness.nosql_lab1.rest;

import com.fitness.nosql_lab1.dtos.userDto.UserAuth;
import com.fitness.nosql_lab1.dtos.userDto.UserData;
import com.fitness.nosql_lab1.dtos.userDto.UserRegistration;
import com.fitness.nosql_lab1.services.AttachmentService;
import com.fitness.nosql_lab1.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final AttachmentService attachmentService;

    public UserController(UserService userService, AttachmentService attachmentService) {
        this.userService = userService;
        this.attachmentService = attachmentService;
    }

    @PostMapping("/user")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistration user) {
        return userService.registerUser(user);
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestBody String username) {
        return userService.deleteUser(username);
    }

    @PostMapping("/session")
    public ResponseEntity<UserData> login(@RequestBody UserAuth user) throws Exception {
        return userService.loginUser(user);
    }

    @DeleteMapping("/session")
    public ResponseEntity<String> logout(
            @CookieValue(name = "SESSION", required = false) String session
    ) throws Exception {
        return userService.logoutUser(session);
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<String> uploadAttachment(
            @CookieValue(name = "SESSION", required = false) String session,
            @PathVariable("id") String userId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        return attachmentService.uploadAttachment(session, userId, file);
    }

    @GetMapping("/{id}/{filename}")
    public ResponseEntity<byte[]> downloadAttachment(
            @CookieValue(name = "SESSION", required = false) String session,
            @PathVariable("id") String userId,
            @RequestParam("filename") String filename
    ) throws Exception {
        return attachmentService.downloadAttachment(session, userId, filename);
    }
}