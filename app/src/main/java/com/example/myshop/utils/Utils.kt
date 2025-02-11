package com.example.myshop.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.TextUtils
import android.util.Patterns
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.myshop.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Utils {

    // Format currency based on locale
    fun formatPrice(price: Double, locale: Locale = Locale.getDefault()): String {
        return NumberFormat.getCurrencyInstance(locale).format(price)
    }

    // Email validation
    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Password validation
    fun isValidPassword(password: String): Boolean {
        return password.length >= Constants.MIN_PASSWORD_LENGTH
    }

    // Phone number validation
    fun isValidPhone(phone: String): Boolean {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches()
    }

    // Check network connectivity
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    // Load image using Glide
    fun loadImage(context: Context, url: String?, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.placeholder_product)
            .error(R.drawable.error_product)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    // Show toast message
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    // Format date
    fun formatDate(date: Date, pattern: String = "dd MMM yyyy, HH:mm"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    // Generate order ID
    fun generateOrderId(): String {
        return "ORD${System.currentTimeMillis()}"
    }

    // Calculate discount
    fun calculateDiscount(originalPrice: Double, discountPercentage: Int): Double {
        return originalPrice - (originalPrice * discountPercentage / 100)
    }

    // Format file size
    fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> String.format("%d Bytes", size)
        }
    }

    // Validate image size
    fun isValidImageSize(size: Long): Boolean {
        return size <= Constants.MAX_IMAGE_SIZE
    }

    // Get file extension from URL
    fun getFileExtension(url: String): String {
        return url.substring(url.lastIndexOf("."))
    }

    // Generate unique filename
    fun generateUniqueFileName(extension: String): String {
        return "file_${System.currentTimeMillis()}$extension"
    }

    // Format phone number
    fun formatPhoneNumber(phone: String): String {
        // Assuming US phone number format
        if (phone.length == 10) {
            return String.format("(%s) %s-%s",
                phone.substring(0, 3),
                phone.substring(3, 6),
                phone.substring(6, 10)
            )
        }
        return phone
    }

    // Mask credit card number
    fun maskCreditCard(cardNumber: String): String {
        if (cardNumber.length < 4) return cardNumber
        val lastFour = cardNumber.takeLast(4)
        return "****-****-****-$lastFour"
    }

    // Calculate total cart value
    fun calculateCartTotal(prices: List<Double>): Double {
        return prices.sum()
    }

    // Check if string contains only numbers
    fun isNumeric(str: String): Boolean {
        return str.all { it.isDigit() }
    }

    // Truncate long text
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) text
        else "${text.take(maxLength)}..."
    }

    // Convert dp to pixels
    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    // Convert pixels to dp
    fun pxToDp(context: Context, px: Float): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }
}
