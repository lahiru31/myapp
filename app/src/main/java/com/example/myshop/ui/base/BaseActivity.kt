package com.example.myshop.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.myshop.R
import com.example.myshop.ui.activities.AuthActivity
import com.example.myshop.utils.PreferenceHelper
import com.example.myshop.utils.Resource
import com.example.myshop.utils.showSnackbar
import com.example.myshop.utils.showSnackbarWithAction
import com.example.myshop.utils.toast
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseActivity(@LayoutRes private val layoutId: Int) : AppCompatActivity() {

    protected lateinit var preferenceHelper: PreferenceHelper
    private var progressView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        preferenceHelper = PreferenceHelper.getInstance(this)
        
        setupUI()
        setupObservers()
    }

    // Abstract methods that child activities must implement
    protected abstract fun setupUI()
    protected abstract fun setupObservers()

    // Setup toolbar with back button
    protected fun setupToolbar(toolbar: Toolbar, showBackButton: Boolean = true) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(showBackButton)
            setDisplayShowHomeEnabled(showBackButton)
        }
    }

    // Handle toolbar back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handle loading state
    protected fun handleLoading(isLoading: Boolean) {
        progressView?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    // Set progress view
    protected fun setProgressView(view: View) {
        progressView = view
    }

    // Handle error state
    protected fun handleError(message: String?, action: (() -> Unit)? = null) {
        message?.let {
            if (action != null) {
                showSnackbarWithAction(
                    message = it,
                    actionText = getString(R.string.dialog_ok),
                    action = action
                )
            } else {
                showSnackbar(it)
            }
        }
    }

    // Show toast message
    protected fun showToast(message: String) {
        toast(message)
    }

    // Show snackbar
    protected fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT
    ) {
        findViewById<View>(android.R.id.content).showSnackbar(message, duration)
    }

    // Show snackbar with action
    protected fun showSnackbarWithAction(
        message: String,
        actionText: String,
        action: () -> Unit,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        findViewById<View>(android.R.id.content).showSnackbarWithAction(
            message,
            actionText,
            duration,
            action
        )
    }

    // Handle Resource state
    protected fun <T> handleResource(
        resource: Resource<T>,
        onSuccess: (T) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        when (resource) {
            is Resource.Loading -> handleLoading(true)
            is Resource.Success -> {
                handleLoading(false)
                resource.data?.let(onSuccess)
            }
            is Resource.Error -> {
                handleLoading(false)
                if (onError != null) {
                    onError(resource.message ?: "Unknown error occurred")
                } else {
                    handleError(resource.message)
                }
            }
        }
    }

    // Extension function to observe LiveData
    protected fun <T> LiveData<T>.observe(action: (T) -> Unit) {
        lifecycleScope.launch {
            observe(this@BaseActivity) { action(it) }
        }
    }

    // Extension function to collect Flow
    protected fun <T> Flow<T>.collect(action: suspend (T) -> Unit) {
        lifecycleScope.launch {
            collect { action(it) }
        }
    }

    // Authentication helpers
    protected fun isUserLoggedIn(): Boolean {
        return preferenceHelper.isUserLoggedIn
    }

    protected fun getCurrentUserId(): String? {
        return preferenceHelper.userId
    }

    protected fun isAdmin(): Boolean {
        return preferenceHelper.userType == "ADMIN"
    }

    protected fun logout() {
        preferenceHelper.clearUserData()
        startActivity(Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // Navigation helpers
    protected inline fun <reified T : AppCompatActivity> startActivity(
        noinline init: Intent.() -> Unit = {}
    ) {
        val intent = Intent(this, T::class.java)
        intent.init()
        startActivity(intent)
    }

    protected inline fun <reified T : AppCompatActivity> startActivityWithAnimation(
        noinline init: Intent.() -> Unit = {}
    ) {
        startActivity<T>(init)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any resources
    }

    companion object {
        private const val TAG = "BaseActivity"
    }
}
