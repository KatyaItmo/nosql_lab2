package com.fitness.nosql_lab1.repos;

import com.fitness.nosql_lab1.objects.Order;
import com.fitness.nosql_lab1.objects.Status;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {
    private final RestTemplate restTemplate;

    private final String couchDbUrl = "http://localhost:5984/orders";

    public OrderRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Order findById(String id) {
        try {
            String url = couchDbUrl + "/" + id;
            return restTemplate.getForObject(url, Order.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public List<Order> findAllPaginatedByStatus(Status status, int limit, int skip) {
        try {
            String url = couchDbUrl + "/_find";

            Map<String, Object> requestBody = Map.of(
                    "selector", Map.of("status", status.name()),
                    "limit", limit,
                    "skip", skip
            );

            ResponseEntity<CouchDbResponse<Order>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody),
                    new ParameterizedTypeReference<CouchDbResponse<Order>>() {}
            );

            if (response.getBody() != null && response.getBody().getDocs() != null) {
                return response.getBody().getDocs();
            }

            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<Order> findUserPaginated(Status status, String userId, int limit, int skip) {
        try {
            String url = couchDbUrl + "/_find";

            Map<String, Object> requestBody = Map.of(
                    "selector", Map.of(
                            "userId", userId,
                            "status", status.name()
                    ),
                    "limit", limit,
                    "skip", skip
            );

            ResponseEntity<CouchDbResponse<Order>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody),
                    new ParameterizedTypeReference<CouchDbResponse<Order>>() {}
            );

            if (response.getBody() != null && response.getBody().getDocs() != null) {
                return response.getBody().getDocs();
            }

            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void save(Order order) {
        String url = couchDbUrl + "/" + order.getId();

        Order existing = findById(order.getId());
        if (existing != null && existing.getRev() != null) {
            order.setRev(existing.getRev());
        }

        restTemplate.put(url, order);
    }

    public boolean delete(String id) {
        Order order = findById(id);
        if (order == null || order.getRev() == null) {
            return false;
        }

        String url = couchDbUrl + "/" + id + "?rev=" + order.getRev();

        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}