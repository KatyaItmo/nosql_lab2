package com.fitness.nosql_lab1.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    private String id;
    private String status;
    private String username;
    private List<OrderItemDto> items;
    private int cost;
    private LocalDateTime date;

    public OrderDto() {}

    public OrderDto(String id, String status, String username, List<OrderItemDto> items, int cost, LocalDateTime date) {
        this.id = id;
        this.status = status;
        this.username = username;
        this.items = items;
        this.cost = cost;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
