package com.fitness.nosql_lab1.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Product {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_rev")
    private String rev;

    private String name;
    private String description;
    private int price;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public Product() {}

    public Product(String id, String name, String description, int price, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
