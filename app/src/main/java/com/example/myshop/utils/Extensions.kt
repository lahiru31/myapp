package com.example.myshop.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

// Context Extensions
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableRes)
}

// View Extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    duration: Int = Snackbar.LENGTH_LONG,
    action: () -> Unit
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText) { action() }
        .show()
}

// Activity Extensions
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let {
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

inline fun <reified T : Activity> Activity.startActivityWithAnimation(
    noinline init: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.init()
    startActivity(intent)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
}

// Fragment Extensions
fun Fragment.hideKeyboard() {
    activity?.hideKeyboard()
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    context?.toast(message, duration)
}

// ImageView Extensions
fun ImageView.loadImage(
    url: String?,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes error: Int? = null
) {
    Glide.with(this)
        .load(url)
        .apply {
            placeholder?.let { placeholder(it) }
            error?.let { error(it) }
        }
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.loadImageWithCallback(
    url: String?,
    onSuccess: (Bitmap) -> Unit,
    onError: () -> Unit
) {
    Glide.with(this)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                onSuccess(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // Do nothing
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                onError()
            }
        })
}

// String Extensions
fun String.isValidEmail(): Boolean = Utils.isValidEmail(this)

fun String.isValidPassword(): Boolean = Utils.isValidPassword(this)

fun String.isValidPhone(): Boolean = Utils.isValidPhone(this)

fun String.formatAsPrice(): String {
    return try {
        val number = this.toDouble()
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(number)
    } catch (e: NumberFormatException) {
        this
    }
}

// Double Extensions
fun Double.formatAsPrice(): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(this)
}

// LiveData Extensions
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
    observe(lifecycleOwner, object : androidx.lifecycle.Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}

fun <T> MutableLiveData<T>.asLiveData() = this as LiveData<T>

// Bundle Extensions
fun Bundle.putAny(key: String, value: Any?) {
    when (value) {
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Long -> putLong(key, value)
        is Boolean -> putBoolean(key, value)
        is Float -> putFloat(key, value)
        is Double -> putDouble(key, value)
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        // Add more types as needed
    }
}

// Uri Extensions
fun Uri.isImageFile(): Boolean {
    val mimeType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(toString()))
    return mimeType?.startsWith("image/") == true
}

// Int Extensions
fun Int.dpToPx(context: Context): Int = Utils.dpToPx(context, this.toFloat())

fun Int.pxToDp(context: Context): Int = Utils.pxToDp(context, this.toFloat())

// List Extensions
fun <T> List<T>.toArrayList(): ArrayList<T> = ArrayList(this)

// Map Extensions
fun <K, V> Map<K, V>.toBundle(): Bundle = Bundle().apply {
    forEach { (key, value) ->
        putAny(key.toString(), value)
    }
}
