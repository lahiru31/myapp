package com.example.myshop.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myshop.utils.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentJob: Job? = null

    protected fun launchCoroutine(
        context: CoroutineContext = Dispatchers.IO,
        block: suspend () -> Unit
    ): Job {
        return viewModelScope.launch(context) {
            try {
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    protected fun <T> launchWithLoading(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null
    ) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                _loading.value = true
                val result = block()
                _loading.value = false
                onSuccess(result)
            } catch (e: Exception) {
                _loading.value = false
                onError?.invoke(e) ?: handleError(e)
            }
        }
    }

    protected fun <T> Flow<Resource<T>>.asLiveData(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): LiveData<Resource<T>> {
        val result = MutableLiveData<Resource<T>>()

        viewModelScope.launch {
            this@asLiveData
                .flowOn(dispatcher)
                .onStart { result.value = Resource.loading() }
                .catch { e -> result.value = Resource.error(e.message ?: "Unknown error occurred", e) }
                .collect { resource -> result.value = resource }
        }

        return result
    }

    protected suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        apiCall: suspend () -> T
    ): Resource<T> {
        return try {
            Resource.success(apiCall())
        } catch (throwable: Throwable) {
            handleApiError(throwable)
        }
    }

    protected fun handleError(throwable: Throwable) {
        _error.value = throwable.message ?: "An unknown error occurred"
    }

    protected fun handleApiError(throwable: Throwable): Resource.Error<Nothing> {
        return when (throwable) {
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    401 -> Resource.Error("Unauthorized. Please login again.")
                    403 -> Resource.Error("Forbidden. You don't have permission.")
                    404 -> Resource.Error("Resource not found.")
                    500 -> Resource.Error("Server error. Please try again later.")
                    else -> Resource.Error("Network error: ${throwable.message()}")
                }
            }
            is java.net.SocketTimeoutException -> {
                Resource.Error("Connection timed out. Please try again.")
            }
            is java.io.IOException -> {
                Resource.Error("Network error. Please check your internet connection.")
            }
            else -> {
                Resource.Error(throwable.message ?: "An unknown error occurred")
            }
        }
    }

    protected fun clearError() {
        _error.value = null
    }

    protected fun showLoading() {
        _loading.value = true
    }

    protected fun hideLoading() {
        _loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}
