package com.fitness.nosql_lab1.db.repos;

import com.fitness.nosql_lab1.db.repos.data.CouchSaveResponse;
import com.fitness.nosql_lab1.db.repos.data.DaoResponse;
import com.fitness.nosql_lab1.objects.Product;
import com.fitness.nosql_lab1.db.EtcdService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class ProductDao {
    private final RestClient restClient;
    private final EtcdService etcdService;
    private final ObjectMapper objectMapper;

    private static final String DB_NAME = "products";

    public ProductDao(RestClient couchDbRestClient, EtcdService etcdService, ObjectMapper objectMapper) {
        this.restClient = couchDbRestClient;
        this.etcdService = etcdService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void ensureTagsAndCreatedAtIndexExists() {
        Map<String, Object> indexBody = Map.of(
                "index", Map.of("fields", List.of("tags", "created_at")),
                "type", "json",
                "name", "tags-created-at-index"
        );

        restClient.post()
                .uri("/{db}/_index", DB_NAME)
                .body(indexBody)
                .retrieve()
                .toBodilessEntity();
    }

    public Product getProductById(String productId) {
        try {
            return restClient.get()
                    .uri("/{db}/{id}", DB_NAME, productId)
                    .retrieve()
                    .body(Product.class);

        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public void createProduct(Product product) throws Exception {
        CouchSaveResponse response = restClient.put()
                .uri("/{db}/{id}", DB_NAME, product.getId())
                .body(product)
                .retrieve()
                .body(CouchSaveResponse.class);

        deleteProductsCache();
    }

    public boolean updateProduct(Product product) throws Exception {
        CouchSaveResponse response = restClient.put()
                .uri("/{db}/{id}", DB_NAME, product.getId())
                .body(product)
                .retrieve()
                .body(CouchSaveResponse.class);

        deleteProductsCache();

        return response != null;
    }

    public DaoResponse deleteProduct(String productId) {
        try {
            Product product = getProductById(productId);
            if (product == null) {
                return new DaoResponse(false, "товар с id=" + productId + " не найден в базе данных");
            }

            restClient.delete()
                    .uri("/{db}/{id}?rev={rev}", DB_NAME, productId, product.getRev())
                    .retrieve()
                    .toBodilessEntity();

            deleteProductsCache();

            return new DaoResponse(true, "товар с id=" + productId + " удален");

        } catch (HttpClientErrorException.NotFound e) {
            return new DaoResponse(false, "товар с id=" + productId + " не найден в базе данных");
        } catch (HttpClientErrorException.Conflict e) {
            return new DaoResponse(false, "конфликт при попытке удаления товара с id=" + productId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Product> getCachedProductsPage(
            String tag,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int limit,
            int skip
    ) throws Exception {
        int page = skip / limit + 1;
        String key = "cache:products:";
        key += tag != null && !tag.isBlank() ? tag + ":" : "";
        key += fromDate != null ? fromDate.toString() + ":" : "";
        key += toDate != null ?toDate.toString() + ":" : "";
        key += page;
        String jsonProducts = etcdService.get(key);
        if (jsonProducts == null) {
             return getProducts(tag, fromDate, toDate, limit, skip);
        }
        return objectMapper.readValue(jsonProducts, new TypeReference<List<Product>>() {});
    }

    public List<Product> getProducts(
            String tag,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int limit,
            int skip
    ) throws Exception {
        Map<String, Object> dateCondition = new HashMap<>();
        if (fromDate != null) {
            dateCondition.put("$gte", fromDate.toString());
        }
        if (toDate != null) {
            dateCondition.put("$lte", toDate.toString());
        }

        Map<String, Object> selector = new HashMap<>();
        if (tag != null && !tag.isBlank()) {
            selector.put("tags", Map.of("$elemMatch", Map.of("$eq", tag)));
        }
        if (!dateCondition.isEmpty()) {
            selector.put("created_at", dateCondition);
        }

        Map<String, Object> queryBody = new HashMap<>();
        queryBody.put("selector", selector);
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

        List<Product> products = response.getDocs();
        String filter = tag != null && !tag.isBlank() ? tag + ":" : "";
        filter += fromDate != null ? fromDate.toString() + ":" : "";
        filter += toDate != null ?toDate.toString() + ":" : "";
        cacheProductsPage(filter, skip / limit + 1, products);
        return products;
    }

    private void cacheProductsPage(String filter, int page, List<Product> products) throws Exception {
        String productsJson = objectMapper.writeValueAsString(products);
        etcdService.putTemporary("cache:products:" + filter + page, productsJson, 600);
    }

    private void deleteProductsCache() throws Exception {
        etcdService.deleteByPrefix("cache:products:");
    }

    public static class FindResponse {
        private List<Product> docs;

        public List<Product> getDocs() {
            return docs;
        }

        public void setDocs(List<Product> docs) {
            this.docs = docs;
        }
    }

}