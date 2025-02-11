package com.example.myshop.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String productId;
    private String productName;
    private double productPrice;
    private String productImage;
    private int quantity;
    private double totalPrice;

    // Empty constructor for Firebase
    public CartItem() {}

    public CartItem(String productId, String productName, double productPrice, 
                   String productImage, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.quantity = quantity;
        this.totalPrice = productPrice * quantity;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { 
        this.productPrice = productPrice;
        this.totalPrice = this.productPrice * this.quantity;
    }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        this.totalPrice = this.productPrice * this.quantity;
    }

    public double getTotalPrice() { return totalPrice; }

    public void updateTotalPrice() {
        this.totalPrice = this.productPrice * this.quantity;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
