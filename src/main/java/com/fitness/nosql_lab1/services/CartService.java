package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.objects.CartItem;
import com.fitness.nosql_lab1.objects.Product;
import com.fitness.nosql_lab1.db.repos.CartDao;
import com.fitness.nosql_lab1.db.repos.ProductDao;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartService {
    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public ResponseEntity<List<CartItem>> getCart(String sessionToken) throws Exception {
        Map<String, CartItem> cartMap = cartDao.getCart(sessionToken);
        for (Map.Entry<String, CartItem> entry : cartMap.entrySet()) {
            String productId = entry.getKey();
            CartItem item = entry.getValue();
            Product cartProduct = item.getProduct();
            Product dbProduct = productDao.findById(productId);

            if (dbProduct != null) {
                if (!cartProduct.equals(dbProduct)) {
                    CartItem newItem = new CartItem(
                            item.getId(),
                            dbProduct,
                            item.getAmount(),
                            dbProduct.getPrice() * item.getAmount()
                    );
                    cartDao.updateItem(sessionToken, productId, newItem);
                }
            } else {
                cartDao.deleteItem(sessionToken, productId);
            }
        }

        cartMap = cartDao.getCart(sessionToken);
        return ResponseEntity.ok(cartMap.values().stream().toList());
    }

    public ResponseEntity<String> addItem(String sessionToken, CartItem item) throws Exception {
        String productId = item.getProduct().getId();
        cartDao.addItem(sessionToken, productId, item);
        return ResponseEntity.ok("товар " + item.getProduct().getName() + " добавлен в корзину");
    }


    public ResponseEntity<String> deleteItem(String sessionToken, String productId) throws Exception {
        cartDao.deleteItem(sessionToken, productId);
        return ResponseEntity.ok("товар убран из корзины");
    }


    public ResponseEntity<String> clearCart(String sessionToken) throws Exception {
        cartDao.clearCart(sessionToken);
        return ResponseEntity.ok("корзина очищена");
    }
}
