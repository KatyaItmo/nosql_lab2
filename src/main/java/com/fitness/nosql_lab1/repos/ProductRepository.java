package com.fitness.nosql_lab1.repos;

import com.fitness.nosql_lab1.objects.Product;
import com.fitness.nosql_lab1.services.EtcdService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {
    private final RestTemplate restTemplate;
    private final EtcdService etcdService;
    private final ObjectMapper objectMapper;

    private final String couchDbUrl = "http://localhost:5984/products";

    public ProductRepository(RestTemplate restTemplate, EtcdService etcdService, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.etcdService = etcdService;
        this.objectMapper = objectMapper;
    }

    public Product findById(String id) {
        try {
            String url = couchDbUrl + "/" + id;
            return restTemplate.getForObject(url, Product.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    public void putProductsPage(int page, List<Product> products) throws Exception {
        String productsJson = objectMapper.writeValueAsString(products);
        etcdService.put("cache:products:" + page, productsJson);
    }

    public List<Product> findCachedProductsPaginated(int limit, int skip) throws Exception {
        int page = skip / limit + 1;
        String jsonProducts = etcdService.get("cache:products:" + page);
        if (jsonProducts == null) {
             return findProductsPaginated(limit, skip);
        }
        return findCachedProductsPaginated(limit, skip);
    }

    public List<Product> findProductsPaginated(int limit, int skip) {
        try {
            String url = couchDbUrl + "/_find";

            Map<String, Object> requestBody = Map.of(
                    "limit", limit,
                    "skip", skip
            );

            ResponseEntity<CouchDbResponse<Product>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody),
                    new ParameterizedTypeReference<CouchDbResponse<Product>>() {}
            );

            if (response.getBody() != null && response.getBody().getDocs() != null) {
                List<Product> products = response.getBody().getDocs();
                int page = skip / limit + 1;
                putProductsPage(page, products);
                return products;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public void save(Product product) throws Exception {
        String url = couchDbUrl + "/" + product.getId();

        Product existing = findById(product.getId());
        if (existing != null && existing.getRev() != null) {
            product.setRev(existing.getRev());
        }
        etcdService.deleteByPrefix("cache:products:");
        restTemplate.put(url, product);
    }

    public boolean delete(String id) {
        Product product = findById(id);
        if (product == null || product.getRev() == null) {
            return false;
        }

        String url = couchDbUrl + "/" + id + "?rev=" + product.getRev();

        try {
            restTemplate.delete(url);
            etcdService.deleteByPrefix("cache:products:");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}