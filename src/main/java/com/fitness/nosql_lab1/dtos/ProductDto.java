package com.fitness.nosql_lab1.dtos;

import com.fitness.nosql_lab1.dtos.userDto.AttachmentDto;

import java.util.List;

public class ProductDto {
    private String id;
    private String name;
    private String description;
    private int price;
    private List<String> tags;

    public ProductDto(String id, String name, String description, int price, List<String> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.tags = tags;
    }

    public ProductDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
