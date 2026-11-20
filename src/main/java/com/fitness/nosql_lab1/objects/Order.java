package com.fitness.nosql_lab1.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitness.nosql_lab1.db.repos.UserDao;
import com.fitness.nosql_lab1.dtos.userDto.UserData;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String rev;

    private Status status;
    private UserData user;

    @JsonProperty("change_counter")
    private int changeCounter;

    private List<OrderItem> items;
    private int cost;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public Order() {}

    public Order(String id, Status status, UserData user, int changeCounter, List<OrderItem> items, int cost, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.user = user;
        this.changeCounter = changeCounter;
        this.items = items;
        this.cost = cost;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public int getChangeCounter() {
        return changeCounter;
    }

    public void setChangeCounter(int changeCounter) {
        this.changeCounter = changeCounter;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
