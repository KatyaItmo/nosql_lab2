package com.fitness.nosql_lab1.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String rev;

    private Status status;

    @JsonProperty("user_id")
    private String userId;

    private List<HistoryStatusItem> statusHistory;

    private List<OrderItem> items;

    public Order() {}

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<HistoryStatusItem> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<HistoryStatusItem> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
