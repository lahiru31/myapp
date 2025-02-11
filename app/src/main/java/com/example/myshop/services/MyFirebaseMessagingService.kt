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
import com.example.myshop.utils.Constants
import com.example.myshop.utils.FirebaseHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save the new token to Firestore for the current user
        val currentUser = FirebaseHelper.getInstance().getCurrentUser()
        if (currentUser != null) {
            FirebaseHelper.getInstance().updateFCMToken(currentUser.uid, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle notification data
        remoteMessage.data.let { data ->
            when (data[Constants.NOTIFICATION_TYPE]) {
                Constants.NOTIFICATION_TYPE_ORDER_STATUS -> {
                    handleOrderStatusNotification(
                        data[Constants.ORDER_ID] ?: "",
                        data[Constants.ORDER_STATUS] ?: "",
                        remoteMessage.notification?.title,
                        remoteMessage.notification?.body
                    )
                }
            }
        }
    }

    private fun handleOrderStatusNotification(
        orderId: String,
        status: String,
        title: String?,
        message: String?
    ) {
        // Create an intent to open the order details screen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_ORDER_ID, orderId)
            putExtra(Constants.EXTRA_NOTIFICATION_TYPE, Constants.NOTIFICATION_TYPE_ORDER_STATUS)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            pendingIntentFlags
        )

        val channelId = getString(R.string.order_status_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: getString(R.string.order_status_update))
            .setContentText(message ?: getString(R.string.order_status_changed, status))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.order_status_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.order_status_channel_description)
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Show notification
        notificationManager.notify(orderId.hashCode(), notificationBuilder.build())

        // Update local order status if needed
        FirebaseHelper.getInstance().getOrderById(orderId)
            .addOnSuccessListener { documentSnapshot ->
                // Broadcast order update to update UI if the order details screen is open
                sendBroadcast(Intent(Constants.ACTION_ORDER_UPDATED).apply {
                    putExtra(Constants.EXTRA_ORDER_ID, orderId)
                })
            }
    }

    companion object {
        private const val TAG = "FCMService"
    }
}
