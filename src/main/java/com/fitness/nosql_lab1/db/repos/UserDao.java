package com.fitness.nosql_lab1.db.repos;

import com.fitness.nosql_lab1.db.repos.data.CouchSaveResponse;
import com.fitness.nosql_lab1.db.repos.data.DaoResponse;
import com.fitness.nosql_lab1.dtos.userDto.UserData;
import com.fitness.nosql_lab1.objects.user.File;
import com.fitness.nosql_lab1.objects.user.User;
import com.fitness.nosql_lab1.db.EtcdService;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Repository
public class UserDao {
    private final RestClient restClient;
    private final EtcdService etcdService;
    private final ObjectMapper objectMapper;

    private static final String DB_NAME = "users";

    public UserDao(RestClient couchDbRestClient, EtcdService etcdService, ObjectMapper objectMapper) {
        this.restClient = couchDbRestClient;
        this.etcdService = etcdService;
        this.objectMapper = objectMapper;
    }

    public User getUserById(String userId) {
        try {
            return restClient.get()
                    .uri("/{db}/{id}", DB_NAME, userId)
                    .retrieve()
                    .body(User.class);

        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public void createUser(User user) {
        CouchSaveResponse response = restClient.put()
                .uri("/{db}/{id}", DB_NAME, user.getId())
                .body(user)
                .retrieve()
                .body(CouchSaveResponse.class);
    }

    public DaoResponse deleteUser(String userId) {
        try {
            User user = getUserById(userId);
            if (user == null) {
                return new DaoResponse(false, "юзер с id=" + userId + " не найден в базе данных");
            }

            restClient.delete()
                    .uri("/{db}/{id}?rev={rev}", DB_NAME, userId, user.getRev())
                    .retrieve()
                    .toBodilessEntity();

            return new DaoResponse(true, "юзер с id=" + userId + " удален");

        } catch (HttpClientErrorException.NotFound e) {
            return new DaoResponse(false, "юзер с id=" + userId + " не найден в базе данных");
        } catch (HttpClientErrorException.Conflict e) {
            return new DaoResponse(false, "конфликт при попытке удаления юзера с id=" + userId);
        }
    }

    public void createSession(String sessionToken, UserData user) throws Exception {
        String userJson = objectMapper.writeValueAsString(user);
        etcdService.putTemporary("session:" + sessionToken, userJson, 3600);
    }

    public UserData findBySession(String sessionToken) throws Exception {
        String userJson = etcdService.get("session:" + sessionToken);
        if (!(userJson == null)) {
            return objectMapper.readValue(userJson, UserData.class);
        }
        return null;
    }

    public void deleteSession(String sessionToken) throws Exception {
        String userJson = etcdService.get("session:" + sessionToken);
        if (!(userJson == null)) {
            etcdService.delete("session:" + sessionToken);
        }
    }

    public DaoResponse uploadAttachment(String userId, File file) {
        User user = getUserById(userId);
        if (user == null) {
            return new DaoResponse(false, "нельзя добавить вложение, юзер с id=" + userId + " не найден в базе данных");
        }

        CouchSaveResponse response = restClient.put()
                .uri("/{db}/{id}/{filename}?rev={rev}", DB_NAME, userId, file.getFilename(), user.getRev())
                .header("Content-Type", file.getContentType())
                .body(file.getFileBytes())
                .retrieve()
                .body(CouchSaveResponse.class);

        if (response == null) {
            return new DaoResponse(false, "не удалось добавить вложение " + file.getFilename());
        }
        return new DaoResponse(true, "добавлено вложение " + file.getFilename());
    }

    public byte[] downloadAttachment(String userId, String filename) {
        return restClient.get()
                .uri("/{db}/{id}/{filename}", DB_NAME, userId, filename)
                .retrieve()
                .body(byte[].class);
    }
}
