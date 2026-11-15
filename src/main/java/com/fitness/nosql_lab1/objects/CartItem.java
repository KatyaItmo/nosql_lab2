package com.fitness.nosql_lab1.objects;

public class CartItem {
    private String id;
    private Product product;
    private int amount;
    private int cost;

    public CartItem() {}

    public CartItem(String id, Product product, int amount, int cost) {
        this.id = id;
        this.product = product;
        this.amount = amount;
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
