package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.dtos.ProductDto;
import com.fitness.nosql_lab1.objects.Product;
import com.fitness.nosql_lab1.repos.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final SessionService sessionService;

    public ProductService(ProductRepository productRepository,
                          SessionService sessionService) {
        this.productRepository = productRepository;
        this.sessionService = sessionService;
    }

    public ResponseEntity<String> createProduct(ProductDto dto) {
        String id = UUID.randomUUID().toString();

        Product product = new Product(
                id,
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                LocalDateTime.now()
        );

        productRepository.save(product);

        return ResponseEntity.ok("добавлен новый товар " + id);
    }

    public ResponseEntity<String> updateProduct(ProductDto dto) {
        Product product = new Product(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                LocalDateTime.now()
        );

        productRepository.save(product);

        return ResponseEntity.ok("изменен товар с " + dto.getId());
    }

    public ResponseEntity<String> deleteProduct(String productId) {
        if (!productRepository.delete(productId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("не удалось удалить товар");
        }
        return ResponseEntity.ok("товар с id " + productId + " удален");
    }

    public ResponseEntity<List<ProductDto>> getProducts() {
        List<Product> productsDb = productRepository.findAll();
        List<ProductDto> products = convertProducts(productsDb);
        return ResponseEntity.ok(products);
    }

    private List<ProductDto> convertProducts(List<Product> products) {
        return products.stream()
                .map(product -> new ProductDto(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice()
                ))
                .toList();
    }
}