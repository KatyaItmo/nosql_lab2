package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.db.repos.data.DaoResponse;
import com.fitness.nosql_lab1.dtos.ProductDto;
import com.fitness.nosql_lab1.objects.Product;
import com.fitness.nosql_lab1.db.repos.ProductDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public ResponseEntity<String> createProduct(ProductDto dto) throws Exception {
        String id = dto.getName() + UUID.randomUUID();

        Product product = new Product(
                id,
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getTags(),
                LocalDateTime.now()
        );

        productDao.createProduct(product);

        return ResponseEntity.ok("добавлен новый товар " + "'" + product.getName() + "'");
    }

    public ResponseEntity<String> updateProduct(ProductDto dto) throws Exception {
        Product product = new Product(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getTags(),
                LocalDateTime.now()
        );

        DaoResponse response = productDao.updateProduct(product);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage());
        }
        return ResponseEntity.ok(response.getMessage());
    }

    public ResponseEntity<String> deleteProduct(String productId) {
        DaoResponse response = productDao.deleteProduct(productId);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage());
        }
        return ResponseEntity.ok(response.getMessage());
    }

    public ResponseEntity<List<ProductDto>> getProducts(String tag, LocalDateTime fromDate, LocalDateTime toDate, int limit, int skip) throws Exception {
        List<Product> productsDb = productDao.getProducts(tag, fromDate, toDate, limit, skip);
        List<ProductDto> products = convertProducts(productsDb);
        return ResponseEntity.ok(products);
    }

    private List<ProductDto> convertProducts(List<Product> products) {
        return products.stream()
                .map(product -> new ProductDto(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getTags()
                ))
                .toList();
    }
}