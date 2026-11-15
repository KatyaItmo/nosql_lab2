package com.fitness.nosql_lab1.services;

import com.fitness.nosql_lab1.dtos.*;
import com.fitness.nosql_lab1.objects.Order;
import com.fitness.nosql_lab1.objects.OrderItem;
import com.fitness.nosql_lab1.objects.User;
import com.fitness.nosql_lab1.repos.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final EtcdService etcdService;
    private final ObjectMapper objectMapper;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        EtcdService etcdService,
                        ObjectMapper objectMapper,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.etcdService = etcdService;
        this.objectMapper = objectMapper;
        this.cartService = cartService;
    }

    // Вспомогательный метод для получения текущего залогиненного юзера из Spring Security
    private User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    public ResponseEntity<Code> createOrder(OrderDto orderDto) throws Exception {
        User user = getCurrentUser(); // Безопасно берем юзера из контекста безопасности

        String orderId = UUID.randomUUID().toString();
        String status = "new";

        Order order = new Order(
                orderId,
                status,
                user,
                orderDto.getCost()
        );

        List<OrderItem> items = orderDto.getOrderList().stream()
                .map(itemDto -> new OrderItem(
                        UUID.randomUUID().toString(),
                        order,
                        itemDto.getProductId(),
                        itemDto.getProductName(),
                        itemDto.getProductPrice(),
                        itemDto.getAmount()
                ))
                .toList();

        order.setOrderList(items);

        orderRepository.save(order);

        // Очищаем корзину (если требуется по логике, передаем id юзера или токен,
        // но здесь лучше адаптировать под твой метод удаления корзины)
        // cartService.delete(user.getId());

        Random random = new Random();
        int confirmCode = random.nextInt(9000) + 1000;
        etcdService.putTemporary("code:" + orderId, Integer.toString(confirmCode), 240);

        Code code = new Code(
                "создан заказ " + orderId,
                orderId,
                confirmCode
        );
        return ResponseEntity.ok(code);
    }

    public ResponseEntity<List<OrderDto>> getUserOrders() {
        User user = getCurrentUser();
        List<Order> userOrders = orderRepository.findByUserId(user.getId());
        List<OrderDto> orders = fromOrdersToOrdersDto(userOrders);
        return ResponseEntity.ok(orders);
    }

    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> allOrders = orderRepository.findAll();
        List<OrderDto> orders = fromOrdersToOrdersDto(allOrders);
        return ResponseEntity.ok(orders);
    }

    public ResponseEntity<String> confirmOrder(Confirmation confirmation) throws Exception {
        User user = getCurrentUser();

        if (confirmation.getCode() == 676767) {
            orderRepository.delete(confirmation.getOrderId());
            return ResponseEntity.ok("вы отменили заказ " + confirmation.getOrderId());
        }

        if (confirmation.getCode() == 198467) {
            changeStatus(confirmation.getOrderId(), "not_confirmed");
            return ResponseEntity.ok("заказ " + confirmation.getOrderId() + " перешел в статус \"не подтвержден\"");
        }

        String codeJson = etcdService.get("code:" + confirmation.getOrderId());
        if (codeJson == null) {
            changeStatus(confirmation.getOrderId(), "not_confirmed");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("время действия кода истекло, заказ " + confirmation.getOrderId() + " перешел в статус \"не подтвержден\"");
        }

        int code = objectMapper.readValue(codeJson, Integer.class);

        if (confirmation.getCode() == code) {
            changeStatus(confirmation.getOrderId(), "confirmed");
            return ResponseEntity.ok("вы подтвердили заказ " + confirmation.getOrderId());
        }

        changeStatus(confirmation.getOrderId(), "not_confirmed");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("неверный код подтверждения, заказ " + confirmation.getOrderId() + " перешел в статус \"не подтвержден\"");
    }

    public ResponseEntity<String> moderateOrder(Moderation moderation) {
        changeStatus(moderation.getOrderId(), moderation.getNewStatus());
        return ResponseEntity.ok("вы изменили статус заказа " + moderation.getOrderId());
    }

    public ResponseEntity<Code> repeatOrder(String oldOrderId) {
        User user = getCurrentUser();

        Order oldOrder = orderRepository.findById(oldOrderId);
        if (oldOrder == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Code("к сожалению, нельзя повторить данный заказ", "", 0));
        }

        // Проверяем, что чужой заказ нельзя повторить (если это принципиально)
        if (oldOrder.getUser() != null && !oldOrder.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Code("действие запрещено", "", 0));
        }

        String newOrderId = UUID.randomUUID().toString();
        String newOrderStatus = "new";

        Order newOrder = new Order(
                newOrderId,
                newOrderStatus,
                user,
                oldOrder.getOrderList(),
                oldOrder.getCost()
        );

        orderRepository.save(newOrder);

        Random random = new Random();
        int confirmCode = random.nextInt(9000) + 1000;
        etcdService.putTemporary("code:" + newOrderId, Integer.toString(confirmCode), 240);

        Code code = new Code(
                "создан заказ " + newOrderId,
                newOrderId,
                confirmCode
        );
        return ResponseEntity.ok(code);
    }

    private void changeStatus(String orderId, String newStatus) {
        Order order = orderRepository.findById(orderId);
        if (order != null) {
            order.setStatus(newStatus);
            orderRepository.save(order);
        }
    }

    private List<OrderDto> convertOrders(List<Order> orders) {
        return orders.stream()
                .map(order -> new OrderDto(
                        order.getId(),
                        order.getStatus(),
                        order.getUserId(),
                        order.getItems().stream().map(item -> new OrderItemDto()).toList(),

                ))
                .toList();
    }
}