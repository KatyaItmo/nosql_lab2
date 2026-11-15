package com.fitness.nosql_lab1.rest;

import com.fitness.nosql_lab1.dtos.ProductDto;
import com.fitness.nosql_lab1.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getProducts(
            @CookieValue(name = "SESSION", required = false) String session
    ) {
        return productService.getProducts();
    }

    @PostMapping
    public ResponseEntity<String> createProduct(
            @CookieValue(name = "SESSION", required = false) String session,
            @RequestBody ProductDto product
    ) throws Exception {
        return productService.createProduct(product);
    }

    @PutMapping
    public ResponseEntity<String> updateProduct(
            @CookieValue(name = "SESSION", required = false) String session,
            @RequestBody ProductDto product
    ) throws Exception {
        return productService.updateProduct(product);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteProduct(
            @CookieValue(name = "SESSION", required = false) String session,
            @RequestBody String productId
    ) throws Exception {
        return productService.deleteProduct(productId);
    }
}