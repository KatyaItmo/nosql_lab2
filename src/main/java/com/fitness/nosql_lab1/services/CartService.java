package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.objects.CartItem;
import com.fitness.nosql_lab1.objects.Product;
import com.fitness.nosql_lab1.repos.CartRepository;
import com.fitness.nosql_lab1.repos.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.util.*;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public ResponseEntity<List<CartItem>> getCart(String sessionToken) throws Exception {
        Map<String, CartItem> cartMap = cartRepository.getCart(sessionToken);
        for (Map.Entry<String, CartItem> entry : cartMap.entrySet()) {
            String productId = entry.getKey();
            CartItem item = entry.getValue();
            Product cartProduct = item.getProduct();
            Product dbProduct = productRepository.findById(productId);

            if (dbProduct != null) {
                if (!cartProduct.equals(dbProduct)) {
                    CartItem newItem = new CartItem(
                            item.getId(),
                            dbProduct,
                            item.getAmount(),
                            dbProduct.getPrice() * item.getAmount()
                    );
                    cartRepository.updateItem(sessionToken, productId, newItem);
                }
            } else {
                cartRepository.deleteItem(sessionToken, productId);
            }
        }

        cartMap = cartRepository.getCart(sessionToken);
        return ResponseEntity.ok(cartMap.values().stream().toList());
    }

    public ResponseEntity<String> addItem(String sessionToken, CartItem item) throws Exception {
        String productId = item.getProduct().getId();
        cartRepository.addItem(sessionToken, productId, item);
        return ResponseEntity.ok("товар " + item.getProduct().getName() + " добавлен в корзину");
    }


    public ResponseEntity<String> deleteItem(String sessionToken, String productId) throws Exception {
        cartRepository.deleteItem(sessionToken, productId);
        return ResponseEntity.ok("товар убран из корзины");
    }


    public ResponseEntity<String> clearCart(String sessionToken) throws Exception {
        cartRepository.clearCart(sessionToken);
        return ResponseEntity.ok("корзина очищена");
    }
}
