package com.fitness.nosql_lab1.db.repos;

import com.fitness.nosql_lab1.db.repos.data.CouchSaveResponse;
import com.fitness.nosql_lab1.objects.Order;
import com.fitness.nosql_lab1.objects.Status;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.*;

@Repository
public class OrderDao {
    private final RestClient restClient;

    private static final String DB_NAME = "orders";

    public OrderDao(RestClient restClient) {
        this.restClient = restClient;
    }

    @PostConstruct
    public void ensureUserOrdersIndexExists() {
        Map<String, Object> indexBody = Map.of(
                "index", Map.of("fields", List.of("user.username", "created_at")),
                "type", "json",
                "name", "user-username-created-at-index"
        );

        restClient.post()
                .uri("/{db}/_index", DB_NAME)
                .body(indexBody)
                .retrieve()
                .toBodilessEntity();
    }

    @PostConstruct
    public void ensureStatusOrdersIndexExists() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("fields", Arrays.asList("status", "created_at"));

        Map<String, Object> indexBody = new HashMap<>();
        indexBody.put("index", fields);
        indexBody.put("type", "json");
        indexBody.put("name", "status-created-at-index");

        restClient.post()
                .uri("/{db}/_index", DB_NAME)
                .body(indexBody)
                .retrieve()
                .toBodilessEntity();
    }

    @PostConstruct
    public void ensureAnalyticsViewExists() {
        String mapFunction = """
            function (doc) {
              if (typeof doc.change_counter === 'number') {
                emit(null, doc.change_counter);
              }
            }
            """;

        Map<String, Object> viewDef = new HashMap<>();
        viewDef.put("map", mapFunction);
        viewDef.put("reduce", "_stats");

        Map<String, Object> views = new HashMap<>();
        views.put("avg_changes", viewDef);

        Map<String, Object> designDoc = new HashMap<>();
        designDoc.put("_id", "_design/orders_analytics");
        designDoc.put("views", views);

        try {
            restClient.put()
                    .uri("/{db}/_design/orders_analytics", DB_NAME)
                    .body(designDoc)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict ignored) {
        }
    }

    public Order getOrderById(String orderId) {
        try {
            return restClient.get()
                    .uri("/{db}/{id}", DB_NAME, orderId)
                    .retrieve()
                    .body(Order.class);

        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public void createOrder(Order order) throws Exception {
        CouchSaveResponse response = restClient.put()
                .uri("/{db}/{id}", DB_NAME, order.getId())
                .body(order)
                .retrieve()
                .body(CouchSaveResponse.class);

    }

    public boolean updateOrder(Order order) throws Exception {
        CouchSaveResponse response = restClient.put()
                .uri("/{db}/{id}", DB_NAME, order.getId())
                .body(order)
                .retrieve()
                .body(CouchSaveResponse.class);

        return response != null;
    }

    public List<Order> getOrders(Status status, int limit, int skip) {
        Map<String, Object> dateCondition = new HashMap<>();
        dateCondition.put("$gt", null);

        Map<String, Object> selector = new HashMap<>();
        selector.put("status", status.name());
        selector.put("created_at", dateCondition);

        Map<String, Object> queryBody = new HashMap<>();
        queryBody.put("selector", selector);
        queryBody.put("sort", List.of(Collections.singletonMap("created_at", "desc")));
        queryBody.put("limit", limit);
        queryBody.put("skip", skip);

        FindResponse response = restClient.post()
                .uri("/{db}/_find", DB_NAME)
                .body(queryBody)
                .retrieve()
                .body(FindResponse.class);

        if (response == null || response.getDocs() == null) {
            return List.of();
        }
        return response.getDocs();
    }

    public List<Order> getUserOrders(String username, int limit, int skip) {
        Map<String, Object> dateCondition = new HashMap<>();
        dateCondition.put("$gt", null);

        Map<String, Object> selector = Map.of(
                "user.username", username,
                "created_at", dateCondition
        );

        Map<String, Object> queryBody = Map.of(
                "selector", selector,
                "sort", List.of(Collections.singletonMap("created_at", "desc")),
                "limit", limit,
                "skip", skip
        );

        FindResponse response = restClient.post()
                .uri("/{db}/_find", DB_NAME)
                .body(queryBody)
                .retrieve()
                .body(FindResponse.class);

        return (response == null || response.getDocs() == null)
                ? List.of()
                : response.getDocs();
    }

    public double getAverageChanges() {
        JsonNode root = restClient.get()
                .uri("/{db}/_design/orders_analytics/_view/avg_changes?group=false", DB_NAME)
                .retrieve()
                .body(JsonNode.class);

        if (root == null || !root.has("rows") || root.path("rows").isEmpty()) {
            return 0.0;
        }

        JsonNode stats = root.path("rows").get(0).path("value");
        double sum = stats.path("sum").asDouble(0.0);
        long count = stats.path("count").asLong(0L);

        return count == 0 ? 0.0 : sum / count;
    }

    public static class FindResponse {
        private List<Order> docs;

        public List<Order> getDocs() {
            return docs;
        }

        public void setDocs(List<Order> docs) {
            this.docs = docs;
        }
    }
}