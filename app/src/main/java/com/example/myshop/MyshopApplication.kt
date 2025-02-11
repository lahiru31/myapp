package com.example.myshop

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MyshopApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore settings
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Enable offline persistence
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

        // Initialize other app-wide configurations
        setupGlideConfiguration()
    }

    private fun setupGlideConfiguration() {
        // Configure Glide for image loading
        // You can add Glide configuration here if needed
        // For example, setting default placeholder, error images, etc.
    }

    companion object {
        private const val TAG = "MyshopApplication"
    }
}
