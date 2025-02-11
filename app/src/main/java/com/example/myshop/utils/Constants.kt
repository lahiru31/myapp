package com.example.myshop.utils

object Constants {
    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_CARTS = "carts"
    const val COLLECTION_ADDRESSES = "addresses"

    // Order Status
    const val ORDER_STATUS_PENDING = "PENDING"
    const val ORDER_STATUS_CONFIRMED = "CONFIRMED"
    const val ORDER_STATUS_SHIPPED = "SHIPPED"
    const val ORDER_STATUS_DELIVERED = "DELIVERED"
    const val ORDER_STATUS_CANCELLED = "CANCELLED"

    // Notification Types
    const val NOTIFICATION_TYPE = "type"
    const val NOTIFICATION_TYPE_ORDER_STATUS = "order_status"
    
    // Notification Data Keys
    const val ORDER_ID = "order_id"
    const val ORDER_STATUS = "status"
    
    // Intent Extras
    const val EXTRA_ORDER_ID = "extra_order_id"
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"
    
    // Broadcast Actions
    const val ACTION_ORDER_UPDATED = "com.example.myshop.ORDER_UPDATED"

    // User Roles
    const val USER_ROLE_CUSTOMER = "CUSTOMER"
    const val USER_ROLE_ADMIN = "ADMIN"

    // Shared Preferences
    const val PREF_NAME = "MyshopPrefs"
    const val PREF_FCM_TOKEN = "fcm_token"
    const val PREF_USER_ROLE = "user_role"
    const val PREF_SELECTED_ADDRESS = "selected_address"

    // Request Codes
    const val RC_SIGN_IN = 100
    const val RC_LOCATION_PERMISSION = 101
    const val RC_IMAGE_PICK = 102

    // Pagination
    const val PAGE_SIZE = 20

    // Other Constants
    const val DEFAULT_SHIPPING_COST = 5.99
    const val MIN_ORDER_AMOUNT = 10.0
    const val MAX_CART_QUANTITY = 99
}
