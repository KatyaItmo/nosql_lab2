package com.fitness.nosql_lab1.repos;

import com.fitness.nosql_lab1.objects.User;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Repository
public class UserRepository {
    private final RestTemplate restTemplate;

    private final String couchDbUrl = "http://localhost:5984/users";

    public UserRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
}
