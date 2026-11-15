package com.fitness.nosql_lab1.repos;

import com.fitness.nosql_lab1.objects.User;
import com.fitness.nosql_lab1.services.EtcdService;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@Repository
public class UserRepository {
    private final RestTemplate restTemplate;
    private final EtcdService etcdService;
    private final ObjectMapper objectMapper;

    private final String couchDbUrl = "http://localhost:5984/users";

    public UserRepository(RestTemplate restTemplate, EtcdService etcdService, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.etcdService = etcdService;
        this.objectMapper = objectMapper;
    }

    public User findById(String username) {
        try {
            String url = couchDbUrl + "/" + username;
            return restTemplate.getForObject(url, User.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public void save(User user) {
        String url = couchDbUrl + "/" + user.getUsername();
        restTemplate.put(url, user);
    }

    public boolean delete(String username) {
        User user = findById(username);
        if (user == null || user.getRev() == null) {
            return false;
        }

        String url = couchDbUrl + "/" + username + "?rev=" + user.getRev();

        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void createSession(String sessionToken, User user) throws Exception {
        String userJson = objectMapper.writeValueAsString(user);
        etcdService.putTemporary("session:" + sessionToken, userJson, 3600);
    }

    public User findBySession(String sessionToken) throws Exception {
        String userJson = etcdService.get("session:" + sessionToken);
        if (!(userJson == null)) {
            return objectMapper.readValue(userJson, User.class);
        }
        return null;
    }

    public void deleteSession(String sessionToken) throws Exception {
        String userJson = etcdService.get("session:" + sessionToken);
        if (!(userJson == null)) {
            etcdService.delete("session:" + sessionToken);
        }
    }
}
