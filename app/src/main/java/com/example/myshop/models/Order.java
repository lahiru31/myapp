package com.example.myshop.models;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private double totalAmount;
    private String status; // "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED"
    private Date orderDate;
    private ShippingAddress shippingAddress;
    private String paymentMethod;

    // Nested ShippingAddress class
    public static class ShippingAddress implements Serializable {
        private String fullName;
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
        private String phone;

        public ShippingAddress() {}

        public ShippingAddress(String fullName, String streetAddress, String city, 
                             String state, String zipCode, String phone) {
            this.fullName = fullName;
            this.streetAddress = streetAddress;
            this.city = city;
            this.state = state;
            this.zipCode = zipCode;
            this.phone = phone;
        }

        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    // Empty constructor for Firebase
    public Order() {
        this.orderDate = new Date();
        this.status = "PENDING";
    }

    public Order(String orderId, String userId, List<CartItem> items, 
                double totalAmount, ShippingAddress shippingAddress, 
                String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
        this.orderDate = new Date();
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { 
        this.shippingAddress = shippingAddress; 
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
}
