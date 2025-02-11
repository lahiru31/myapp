package com.example.myshop.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myshop.R
import com.example.myshop.ui.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            sendNotification(notification.title, notification.body)
        }

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            handleDataPayload(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server
        sendRegistrationTokenToServer(token)
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle data payload based on your app's requirements
        when (data["type"]) {
            "order_status" -> {
                val orderId = data["order_id"]
                val status = data["status"]
                // Handle order status update
                updateOrderStatus(orderId, status)
            }
            "new_product" -> {
                val productId = data["product_id"]
                // Handle new product notification
                handleNewProduct(productId)
            }
            "promotion" -> {
                val promoCode = data["promo_code"]
                val discount = data["discount"]
                // Handle promotion notification
                handlePromotion(promoCode, discount)
            }
        }
    }

    private fun sendRegistrationTokenToServer(token: String) {
        // Send the FCM registration token to your server
        // This could be used to target specific devices for notifications
    }

    private fun updateOrderStatus(orderId: String?, status: String?) {
        // Update local order status and notify UI if needed
    }

    private fun handleNewProduct(productId: String?) {
        // Handle new product notification
        // Maybe update local cache or notify UI
    }

    private fun handlePromotion(promoCode: String?, discount: String?) {
        // Handle promotion notification
        // Maybe store promotion details locally or show special notification
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
