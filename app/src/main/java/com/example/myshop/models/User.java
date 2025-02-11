package com.example.myshop.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private List<Order.ShippingAddress> addresses;
    private String userType; // "CUSTOMER" or "ADMIN"
    private List<String> favoriteProducts;
    private String profileImageUrl;

    // Empty constructor for Firebase
    public User() {
        this.addresses = new ArrayList<>();
        this.favoriteProducts = new ArrayList<>();
        this.userType = "CUSTOMER"; // Default user type
    }

    public User(String userId, String email, String fullName, String phoneNumber) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.addresses = new ArrayList<>();
        this.favoriteProducts = new ArrayList<>();
        this.userType = "CUSTOMER";
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<Order.ShippingAddress> getAddresses() { return addresses; }
    public void setAddresses(List<Order.ShippingAddress> addresses) { this.addresses = addresses; }
    public void addAddress(Order.ShippingAddress address) {
        if (this.addresses == null) {
            this.addresses = new ArrayList<>();
        }
        this.addresses.add(address);
    }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public boolean isAdmin() { return "ADMIN".equals(userType); }

    public List<String> getFavoriteProducts() { return favoriteProducts; }
    public void setFavoriteProducts(List<String> favoriteProducts) { 
        this.favoriteProducts = favoriteProducts; 
    }
    public void addFavoriteProduct(String productId) {
        if (this.favoriteProducts == null) {
            this.favoriteProducts = new ArrayList<>();
        }
        if (!this.favoriteProducts.contains(productId)) {
            this.favoriteProducts.add(productId);
        }
    }
    public void removeFavoriteProduct(String productId) {
        if (this.favoriteProducts != null) {
            this.favoriteProducts.remove(productId);
        }
    }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { 
        this.profileImageUrl = profileImageUrl; 
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}
