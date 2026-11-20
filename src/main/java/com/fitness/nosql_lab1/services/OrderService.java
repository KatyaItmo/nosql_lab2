package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.db.EtcdService;
import com.fitness.nosql_lab1.db.repos.data.DaoResponse;
import com.fitness.nosql_lab1.dtos.*;
import com.fitness.nosql_lab1.dtos.userDto.UserData;
import com.fitness.nosql_lab1.objects.*;
import com.fitness.nosql_lab1.db.repos.CartDao;
import com.fitness.nosql_lab1.db.repos.OrderDao;
import com.fitness.nosql_lab1.db.repos.UserDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final CartDao cartDao;
    private final EtcdService etcdService;

    public OrderService(OrderDao orderDao, UserDao userDao, CartDao cartDao, EtcdService etcdService) {
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.cartDao = cartDao;
        this.etcdService = etcdService;
    }

    public ResponseEntity<Code> createOrder(String sessionToken, OrderDto orderDto) throws Exception {
        UserData user = userDao.findBySession(sessionToken);
        String orderId = UUID.randomUUID().toString();

        List<OrderItem> items = orderDto.getItems().stream()
                .map(itemDto -> new OrderItem(
                        UUID.randomUUID().toString(),
                        itemDto.getProductId(),
                        itemDto.getProductName(),
                        itemDto.getProductPrice(),
                        itemDto.getAmount(),
                        itemDto.getCost()
                ))
                .toList();

        Status status = Status.NEW;
        int changeCounter = 0;

        Order order = new Order(
                orderId,
                status,
                user,
                changeCounter,
                items,
                orderDto.getCost(),
                LocalDateTime.now()
        );

        orderDao.createOrder(order);

        cartDao.clearCart(sessionToken);

        Random random = new Random();
        int confirmCode = random.nextInt(9000) + 1000;
        etcdService.putTemporary("code:" + orderId, Integer.toString(confirmCode), 240);

        Code code = new Code(
                "создан заказ №" + orderId,
                orderId,
                confirmCode
        );
        return ResponseEntity.ok(code);
    }

    public ResponseEntity<List<OrderDto>> getUserOrders(String sessionToken, int limit, int skip) throws Exception {
        UserData user = userDao.findBySession(sessionToken);
        List<OrderDto> orders = convertOrders(orderDao.getUserOrders(user.getUsername(), limit, skip));
        return ResponseEntity.ok(orders);
    }

    public ResponseEntity<List<OrderDto>> getAllOrders(Status status, int limit, int skip) {
        List<OrderDto> orders = convertOrders(orderDao.getOrders(status, limit, skip));
        return ResponseEntity.ok(orders);
    }

    public ResponseEntity<String> confirmOrder(Confirmation confirmation) throws Exception {
        String dbCodeJson = etcdService.get("code:" + confirmation.getOrderId());
        etcdService.delete("code:" + confirmation.getOrderId());

        Order order = orderDao.getOrderById(confirmation.getOrderId());

        if (dbCodeJson == null) {
            order.setStatus(Status.DELETED);
            order.setChangeCounter(order.getChangeCounter() + 1);
            DaoResponse response = orderDao.updateOrder(order);
            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("время действия кода истекло!");
        }

        int dbCode = Integer.parseInt(dbCodeJson);

        if (confirmation.getCode() != dbCode) {
            order.setStatus(Status.DELETED);
            order.setChangeCounter(order.getChangeCounter() + 1);
            DaoResponse response = orderDao.updateOrder(order);
            if (!response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("вы ввели неверный код, заказ не подтвержден");
        }

        order.setStatus(Status.CONFIRMED);
        order.setChangeCounter(order.getChangeCounter() + 1);
        DaoResponse response = orderDao.updateOrder(order);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage());
        }
        return ResponseEntity.ok("заказ №" + confirmation.getOrderId() + " успешно подтвержден");
    }

    public ResponseEntity<String> moderateOrder(Moderation moderation) throws Exception {
        Status newStatus = Status.valueOf(moderation.getNewStatus());
        Order order = orderDao.getOrderById(moderation.getOrderId());

        order.setStatus(newStatus);
        order.setChangeCounter(order.getChangeCounter() + 1);
        DaoResponse response = orderDao.updateOrder(order);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage());
        }
        return ResponseEntity.ok("вы изменили статус заказа " + moderation.getOrderId());
    }

    public ResponseEntity<String> deleteOrder(String orderId) throws Exception {
        Order order = orderDao.getOrderById(orderId);
        order.setStatus(Status.DELETED);
        order.setChangeCounter(order.getChangeCounter() + 1);
        DaoResponse response = orderDao.updateOrder(order);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage());
        }
        return ResponseEntity.ok("заказ №" + orderId + " отменен");
    }

    private List<OrderDto> convertOrders(List<Order> orders) {
        return orders.stream()
                .map(order -> new OrderDto(
                        order.getId(),
                        order.getStatus().toString(),
                        order.getUser().getUsername(),
                        order.getItems().stream()
                                .map(item -> new OrderItemDto(
                                        item.getId(),
                                        item.getProductId(),
                                        item.getProductName(),
                                        item.getProductPrice(),
                                        item.getAmount(),
                                        item.getCost()
                                ))
                                .toList(),
                        order.getCost(),
                        order.getCreatedAt()
                ))
                .toList();
    }
}