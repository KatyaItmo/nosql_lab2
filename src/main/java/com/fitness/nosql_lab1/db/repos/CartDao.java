package com.fitness.nosql_lab1.db.repos;

import com.fitness.nosql_lab1.objects.CartItem;
import com.fitness.nosql_lab1.db.EtcdService;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Repository
public class CartDao {
    private final EtcdService etcdService;
    private final ObjectMapper objectMapper;

    public CartDao(EtcdService etcdService, ObjectMapper objectMapper) {
        this.etcdService = etcdService;
        this.objectMapper = objectMapper;
    }

    public Map<String, CartItem> getCart(String sessionToken) throws Exception {
        String cartJson = etcdService.get("cart:" + sessionToken);
        Map<String, CartItem> cart = new HashMap<>();
        if (cartJson != null) {
            cart = objectMapper.readValue(cartJson, new TypeReference<Map<String, CartItem>>() {});
        }
        return cart;
    }

    private void saveCart(String sessionToken, Map<String, CartItem> cart) throws Exception {
        String cartJson = objectMapper.writeValueAsString(cart);
        etcdService.putTemporary("cart:" + sessionToken, cartJson, 1800);
    }

    public void addItem(String sessionToken, String productId, CartItem item) throws Exception {
        Map<String, CartItem> cart = getCart(sessionToken);

        if (cart.get(productId) != null) {
            item.setAmount(item.getAmount() + cart.get(productId).getAmount());
            item.setCost(item.getCost() + cart.get(productId).getCost());
        } else {
            String itemId = UUID.randomUUID().toString();
            item.setId(itemId);
        }
        cart.put(productId, item);
        saveCart(sessionToken, cart);
    }

    public void updateItem(String sessionToken, String productId, CartItem item) throws Exception {
        Map<String, CartItem> cart = getCart(sessionToken);
        cart.put(productId, item);
        saveCart(sessionToken, cart);
    }

    public void deleteItem(String sessionToken, String productId) throws Exception {
        Map<String, CartItem> cart = getCart(sessionToken);
        if (cart.get(productId) != null) {
            cart.remove(productId);
        }
        saveCart(sessionToken, cart);
    }

    public void clearCart(String sessionToken) throws Exception {
        etcdService.delete("cart:" + sessionToken);
    }
}
