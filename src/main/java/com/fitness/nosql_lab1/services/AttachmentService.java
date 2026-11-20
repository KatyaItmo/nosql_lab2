package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.db.repos.UserDao;
import com.fitness.nosql_lab1.db.repos.data.DaoResponse;
import com.fitness.nosql_lab1.dtos.userDto.UserData;
import com.fitness.nosql_lab1.objects.user.File;
import org.springframework.http.*;
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("вы не можете прикрепить вложение к юзеру " + userId);
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("нельзя прикрепить пустой файл");
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
        if (fileBytes == null || fileBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(fileBytes);
    }
}
