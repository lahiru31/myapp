package com.example.myshop.utils;

import com.example.myshop.models.User;
import com.example.myshop.models.Product;
import com.example.myshop.models.Order;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String PRODUCTS_COLLECTION = "products";
    private static final String ORDERS_COLLECTION = "orders";
    private static final String CART_COLLECTION = "carts";

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
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get();
    }

    public Task<DocumentSnapshot> getOrderById(String orderId) {
        return db.collection(ORDERS_COLLECTION)
                .document(orderId)
                .get();
    }

    public Task<Void> updateOrderStatus(String orderId, String newStatus) {
        return db.collection(ORDERS_COLLECTION)
                .document(orderId)
                .update("status", newStatus);
    }

    public Task<QuerySnapshot> getOrdersByStatus(String status) {
        return db.collection(ORDERS_COLLECTION)
                .whereEqualTo("status", status)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get();
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
        return db.collection(ORDERS_COLLECTION)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get();
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

    // FCM Token Management
    public Task<Void> updateFCMToken(String userId, String token) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .update("fcmToken", token);
    }

    public Task<String> getFCMToken(String userId) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().getString("fcmToken");
                    }
                    return null;
                });
    }

    // Order Status Management
    public Task<Void> updateOrderStatus(String orderId, String newStatus, String notificationTitle, String notificationMessage) {
        DocumentReference orderRef = db.collection(ORDERS_COLLECTION).document(orderId);
        
        return db.runTransaction(transaction -> {
            DocumentSnapshot orderSnapshot = transaction.get(orderRef);
            if (!orderSnapshot.exists()) {
                throw new Exception("Order not found");
            }

            String userId = orderSnapshot.getString("userId");
            if (userId == null) {
                throw new Exception("User ID not found in order");
            }

            // Update order status
            transaction.update(orderRef, "status", newStatus);

            // Get user's FCM token
            DocumentSnapshot userSnapshot = transaction.get(
                db.collection(USERS_COLLECTION).document(userId)
            );

            String fcmToken = userSnapshot.getString("fcmToken");
            if (fcmToken != null) {
                // Send notification using Cloud Functions
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("type", "order_status");
                notificationData.put("orderId", orderId);
                notificationData.put("status", newStatus);
                notificationData.put("title", notificationTitle);
                notificationData.put("message", notificationMessage);
                notificationData.put("token", fcmToken);

                // Add to notifications collection for tracking
                transaction.set(
                    db.collection("notifications").document(),
                    notificationData
                );
            }

            return null;
        });
    }

    // Get orders by date range
    public Task<QuerySnapshot> getOrdersByDateRange(Date startDate, Date endDate) {
        return db.collection(ORDERS_COLLECTION)
                .whereGreaterThanOrEqualTo("orderDate", startDate)
                .whereLessThanOrEqualTo("orderDate", endDate)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get();
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

    // Cart Methods
    public Task<Void> addToCart(String userId, CartItem cartItem) {
        return db.collection(CART_COLLECTION)
                .document(userId)
                .collection("items")
                .document(cartItem.getProductId())
                .set(cartItem);
    }

    public Task<Void> updateCartItemQuantity(String userId, String productId, int quantity) {
        return db.collection(CART_COLLECTION)
                .document(userId)
                .collection("items")
                .document(productId)
                .update("quantity", quantity);
    }

    public Task<Void> removeFromCart(String userId, String productId) {
        return db.collection(CART_COLLECTION)
                .document(userId)
                .collection("items")
                .document(productId)
                .delete();
    }

    public Task<QuerySnapshot> getCartItems(String userId) {
        return db.collection(CART_COLLECTION)
                .document(userId)
                .collection("items")
                .get();
    }

    public Task<Void> clearCart(String userId) {
        return db.collection(CART_COLLECTION)
                .document(userId)
                .delete();
    }
}
