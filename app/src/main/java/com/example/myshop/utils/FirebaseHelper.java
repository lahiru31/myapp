package com.example.myshop.utils;

import com.example.myshop.models.User;
import com.example.myshop.models.Product;
import com.example.myshop.models.Order;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String PRODUCTS_COLLECTION = "products";
    private static final String ORDERS_COLLECTION = "orders";

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    // Authentication Methods
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public Task<Void> signOut() {
        auth.signOut();
        return Task.forResult(null);
    }

    // User Methods
    public Task<Void> createUserProfile(User user) {
        return db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user);
    }

    public Task<User> getUserProfile(String userId) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(User.class);
                    }
                    return null;
                });
    }

    // Product Methods
    public Task<Void> addProduct(Product product) {
        return db.collection(PRODUCTS_COLLECTION)
                .document(product.getId())
                .set(product);
    }

    public Task<QuerySnapshot> getAllProducts() {
        return db.collection(PRODUCTS_COLLECTION).get();
    }

    public Task<QuerySnapshot> getProductsByCategory(String category) {
        return db.collection(PRODUCTS_COLLECTION)
                .whereEqualTo("category", category)
                .get();
    }

    public Task<Product> getProductById(String productId) {
        return db.collection(PRODUCTS_COLLECTION)
                .document(productId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(Product.class);
                    }
                    return null;
                });
    }

    // Order Methods
    public Task<DocumentReference> createOrder(Order order) {
        return db.collection(ORDERS_COLLECTION).add(order);
    }

    public Task<QuerySnapshot> getUserOrders(String userId) {
        return db.collection(ORDERS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get();
    }

    public Task<Void> updateOrderStatus(String orderId, String newStatus) {
        return db.collection(ORDERS_COLLECTION)
                .document(orderId)
                .update("status", newStatus);
    }

    // Storage Methods
    public StorageReference getProductImageRef(String productId) {
        return storage.getReference()
                .child("products")
                .child(productId + ".jpg");
    }

    public StorageReference getUserProfileImageRef(String userId) {
        return storage.getReference()
                .child("profile_images")
                .child(userId + ".jpg");
    }

    // Admin Methods
    public Task<QuerySnapshot> getAllOrders() {
        return db.collection(ORDERS_COLLECTION).get();
    }

    public Task<Void> deleteProduct(String productId) {
        return db.collection(PRODUCTS_COLLECTION)
                .document(productId)
                .delete();
    }

    public Task<Void> updateProduct(Product product) {
        return db.collection(PRODUCTS_COLLECTION)
                .document(product.getId())
                .set(product);
    }

    // User Management Methods
    public Task<Void> updateUserProfile(User user) {
        return db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .set(user);
    }

    public Task<QuerySnapshot> searchProducts(String query) {
        String searchQuery = query.toLowerCase();
        return db.collection(PRODUCTS_COLLECTION)
                .whereGreaterThanOrEqualTo("name", searchQuery)
                .whereLessThanOrEqualTo("name", searchQuery + "\uf8ff")
                .get();
    }
}
