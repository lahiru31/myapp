package com.example.myshop.ui.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.myshop.utils.PreferenceHelper
import com.example.myshop.utils.Resource
import com.example.myshop.utils.showSnackbar
import com.example.myshop.utils.showSnackbarWithAction
import com.example.myshop.utils.toast
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    protected lateinit var preferenceHelper: PreferenceHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceHelper = PreferenceHelper.getInstance(requireContext())
        setupUI()
        setupObservers()
    }

    // Abstract methods that child fragments must implement
    protected abstract fun setupUI()
    protected abstract fun setupObservers()

    // Handle loading state
    protected fun handleLoading(isLoading: Boolean) {
        // Override in child fragments if needed
    }

    // Handle error state
    protected fun handleError(message: String?, action: (() -> Unit)? = null) {
        message?.let {
            if (action != null) {
                view?.showSnackbarWithAction(
                    message = it,
                    actionText = "Retry",
                    action = action
                )
            } else {
                view?.showSnackbar(it)
            }
        }
    }

    // Show toast message
    protected fun showToast(message: String) {
        requireContext().toast(message)
    }

    // Show snackbar
    protected fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT
    ) {
        view?.showSnackbar(message, duration)
    }

    // Show snackbar with action
    protected fun showSnackbarWithAction(
        message: String,
        actionText: String,
        action: () -> Unit,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        view?.showSnackbarWithAction(message, actionText, duration, action)
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
        viewLifecycleOwner.lifecycleScope.launch {
            observe(viewLifecycleOwner) { action(it) }
        }
    }

    // Extension function to collect Flow
    protected fun <T> Flow<T>.collect(action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            collect { action(it) }
        }
    }

    // Check if user is logged in
    protected fun isUserLoggedIn(): Boolean {
        return preferenceHelper.isUserLoggedIn
    }

    // Get current user ID
    protected fun getCurrentUserId(): String? {
        return preferenceHelper.userId
    }

    // Check if user is admin
    protected fun isAdmin(): Boolean {
        return preferenceHelper.userType == "ADMIN"
    }

    // Navigation helper methods
    protected fun navigateBack() {
        requireActivity().onBackPressed()
    }

    // Lifecycle methods
    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up any resources
    }

    companion object {
        private const val TAG = "BaseFragment"
    }
}
