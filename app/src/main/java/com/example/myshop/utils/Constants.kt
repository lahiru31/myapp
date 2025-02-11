package com.example.myshop.utils

object Constants {
    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_CART = "cart"
    const val COLLECTION_CATEGORIES = "categories"

    // User Types
    const val USER_TYPE_CUSTOMER = "CUSTOMER"
    const val USER_TYPE_ADMIN = "ADMIN"

    // Order Status
    const val ORDER_STATUS_PENDING = "PENDING"
    const val ORDER_STATUS_CONFIRMED = "CONFIRMED"
    const val ORDER_STATUS_SHIPPED = "SHIPPED"
    const val ORDER_STATUS_DELIVERED = "DELIVERED"
    const val ORDER_STATUS_CANCELLED = "CANCELLED"

    // Payment Methods
    const val PAYMENT_METHOD_COD = "CASH_ON_DELIVERY"
    const val PAYMENT_METHOD_CARD = "CREDIT_CARD"
    const val PAYMENT_METHOD_UPI = "UPI"

    // Intent Keys
    const val KEY_PRODUCT_ID = "product_id"
    const val KEY_ORDER_ID = "order_id"
    const val KEY_CATEGORY = "category"
    const val KEY_USER_ID = "user_id"

    // Bundle Keys
    const val BUNDLE_PRODUCT = "bundle_product"
    const val BUNDLE_ORDER = "bundle_order"
    const val BUNDLE_USER = "bundle_user"

    // Shared Preferences
    const val PREF_NAME = "myshop_preferences"
    const val PREF_USER_LOGGED_IN = "user_logged_in"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_TYPE = "user_type"
    const val PREF_FCM_TOKEN = "fcm_token"

    // Request Codes
    const val RC_SIGN_IN = 100
    const val RC_PICK_IMAGE = 101
    const val RC_LOCATION_PERMISSION = 102

    // Notification Channels
    const val CHANNEL_ORDERS = "orders_channel"
    const val CHANNEL_PROMOTIONS = "promotions_channel"
    const val CHANNEL_GENERAL = "general_channel"

    // Notification IDs
    const val NOTIFICATION_ORDER_UPDATE = 1001
    const val NOTIFICATION_NEW_PRODUCT = 1002
    const val NOTIFICATION_PROMOTION = 1003

    // Time Constants
    const val SPLASH_DELAY = 2000L
    const val CART_UPDATE_DELAY = 500L
    const val SEARCH_DEBOUNCE = 300L

    // Pagination
    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40

    // Image Constants
    const val MAX_IMAGE_SIZE = 1024 * 1024 // 1MB
    const val IMAGE_QUALITY = 80
    const val THUMBNAIL_SIZE = 200

    // Validation Constants
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PRODUCT_QUANTITY = 99
    const val MIN_PRODUCT_PRICE = 0.01
    const val MAX_DESCRIPTION_LENGTH = 500

    // Error Messages
    const val ERROR_NETWORK = "Network error occurred"
    const val ERROR_UNKNOWN = "An unknown error occurred"
    const val ERROR_INVALID_CREDENTIALS = "Invalid credentials"
    const val ERROR_WEAK_PASSWORD = "Password is too weak"
    const val ERROR_EMAIL_EXISTS = "Email already exists"

    // Storage Paths
    const val STORAGE_PRODUCTS = "products"
    const val STORAGE_PROFILES = "profiles"
    const val STORAGE_CATEGORIES = "categories"

    // File Extensions
    const val EXTENSION_JPG = ".jpg"
    const val EXTENSION_PNG = ".png"

    // Default Values
    const val DEFAULT_CURRENCY = "USD"
    const val DEFAULT_LANGUAGE = "en"
    const val DEFAULT_COUNTRY = "US"
    
    // API Endpoints (if using external APIs)
    const val BASE_URL = "https://api.myshop.com/"
    const val ENDPOINT_PAYMENT = "payment"
    const val ENDPOINT_SHIPPING = "shipping"
}
