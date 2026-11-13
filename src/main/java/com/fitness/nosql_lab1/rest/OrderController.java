package com.fitness.nosql_lab1.rest;

import com.fitness.nosql_lab1.dtos.Code;
import com.fitness.nosql_lab1.dtos.Confirmation;
import com.fitness.nosql_lab1.dtos.Moderation;
import com.fitness.nosql_lab1.dtos.OrderDto;
import com.fitness.nosql_lab1.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Code> createOrder(
            @CookieValue(name = "SESSION", required = false) String session,
            @RequestBody OrderDto orderDto
    ) {
        return orderService.createOrder(orderDto);
    }

    @PutMapping
    public ResponseEntity<String> confirmOrder(
            @CookieValue(name = "SESSION", required = false) String session,
            @RequestBody Confirmation confirmation
    ) throws Exception {
        return orderService.confirmOrder(confirmation);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteOrder(
            @CookieValue(name = "SESSION", required = false) String session,
            @RequestBody String orderId
    ) throws Exception {
        return orderService.deleteOrder(orderId);
    }

    @PostMapping("/moder")
    public ResponseEntity<String> moderateOrder(
            @CookieValue(name = "SESSION", required = false) String session,
            @RequestBody Moderation moderation
    ) throws Exception {
        return orderService.moderateOrder(moderation);
    }

    @GetMapping("/moder")
    public ResponseEntity<List<OrderDto>> getAllOrders(
            @CookieValue(name = "SESSION", required = false) String session
    ) throws Exception {
        return orderService.getAllOrders();
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(
            @CookieValue(name = "SESSION", required = false) String session
    ) throws Exception {
        return orderService.getUserOrders();
    }
}