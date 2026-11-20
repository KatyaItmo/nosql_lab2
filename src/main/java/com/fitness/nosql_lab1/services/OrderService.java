package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.db.EtcdService;
import com.fitness.nosql_lab1.dtos.*;
import com.fitness.nosql_lab1.objects.*;
import com.fitness.nosql_lab1.db.repos.CartDao;
import com.fitness.nosql_lab1.db.repos.OrderDao;
import com.fitness.nosql_lab1.db.repos.UserDao;
import com.fitness.nosql_lab1.objects.user.User;
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
        User user = userDao.findBySession(sessionToken);
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
        List<HistoryStatusItem> historyStatus = new ArrayList<>();
        historyStatus.add(new HistoryStatusItem(
                Status.NEW,
                LocalDateTime.now()
        ));

        Order order = new Order(
                orderId,
                status,
                user.getId(),
                historyStatus,
                items,
                orderDto.getCost(),
                LocalDateTime.now()
        );

        orderDao.save(order);

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

    public ResponseEntity<List<OrderDto>> getUserOrders(String sessionToken, Status status, int limit, int skip) throws Exception {
        User user = userDao.findBySession(sessionToken);
        List<OrderDto> orders = convertOrders(orderDao.findUserPaginated(status, user.getId(), limit, skip));
        return ResponseEntity.ok(orders);
    }

    public ResponseEntity<List<OrderDto>> getAllOrders(Status status, int limit, int skip) {
        List<OrderDto> orders = convertOrders(orderDao.findAllPaginatedByStatus(status, limit, skip));
        return ResponseEntity.ok(orders);
    }

    public ResponseEntity<String> confirmOrder(Confirmation confirmation) throws Exception {
        String dbCodeJson = etcdService.get("code:" + confirmation.getOrderId());
        etcdService.delete("code:" + confirmation.getOrderId());

        Order order = orderDao.findById(confirmation.getOrderId());

        if (dbCodeJson == null) {
            order.setStatus(Status.NOT_CONFIRMED);
            List<HistoryStatusItem> history = order.getStatusHistory();
            history.add(new HistoryStatusItem(
                    Status.NOT_CONFIRMED,
                    LocalDateTime.now()
            ));
            order.setStatusHistory(history);
            orderDao.save(order);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("время действия кода истекло!");
        }

        int dbCode = Integer.parseInt(dbCodeJson);

        if (confirmation.getCode() != dbCode) {
            order.setStatus(Status.NOT_CONFIRMED);
            List<HistoryStatusItem> history = order.getStatusHistory();
            history.add(new HistoryStatusItem(
                    Status.NOT_CONFIRMED,
                    LocalDateTime.now()
            ));
            orderDao.save(order);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("вы ввели неверный код, заказ перешел в статус 'не подтвержден'");
        }

        order.setStatus(Status.CONFIRMED);
        List<HistoryStatusItem> history = order.getStatusHistory();
        history.add(new HistoryStatusItem(
                Status.CONFIRMED,
                LocalDateTime.now()
        ));
        orderDao.save(order);
        return ResponseEntity.ok("заказ №" + confirmation.getOrderId() + " подтвержден, следите за его статусом");
    }

    public ResponseEntity<String> moderateOrder(Moderation moderation) {
        Status newStatus = Status.valueOf(moderation.getNewStatus());
        Order order = orderDao.findById(moderation.getOrderId());

        order.setStatus(newStatus);
        List<HistoryStatusItem> history = order.getStatusHistory();
        history.add(new HistoryStatusItem(
                newStatus,
                LocalDateTime.now()
        ));
        orderDao.save(order);

        return ResponseEntity.ok("вы изменили статус заказа " + moderation.getOrderId());
    }

    public ResponseEntity<String> deleteOrder(String orderId) {
        orderDao.delete(orderId);
        return ResponseEntity.ok("вы удалили заказ №" + orderId);
    }

    private List<OrderDto> convertOrders(List<Order> orders) {
        return orders.stream()
                .map(order -> new OrderDto(
                        order.getId(),
                        order.getStatus().toString(),
                        order.getUserId(),
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