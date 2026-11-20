package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.db.repos.UserDao;
import com.fitness.nosql_lab1.db.repos.data.DaoResponse;
import com.fitness.nosql_lab1.dtos.userDto.UserData;
import com.fitness.nosql_lab1.objects.user.File;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AttachmentService {
    private final UserDao userDao;

    public AttachmentService(UserDao userDao) {
        this.userDao = userDao;
    }

    public ResponseEntity<String> uploadAttachment(String sessionToken, String userId, MultipartFile file) throws Exception {
        UserData sessionUser = userDao.findBySession(sessionToken);
        if (!sessionUser.getUsername().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("вы не может прикрепить вложение к юзеру " + userId);
        }

        try {
            File fileData = new File(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );

            DaoResponse daoResponse = userDao.uploadAttachment(
                    userId,
                    fileData
            );
             if (!daoResponse.isSuccess()) {
                 return ResponseEntity.status(HttpStatus.CONFLICT).body(daoResponse.getMessage());
             }
             return ResponseEntity.ok(daoResponse.getMessage());

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("ошибка при чтении файла " + file.getOriginalFilename());
        }
    }

    public ResponseEntity<byte[]> downloadAttachment(String sessionToken, String userId, String filename) throws Exception {
        UserData sessionUser = userDao.findBySession(sessionToken);
        if (!sessionUser.getUsername().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] fileBytes = userDao.downloadAttachment(userId, filename);
        if (fileBytes == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(fileBytes);
    }
}
