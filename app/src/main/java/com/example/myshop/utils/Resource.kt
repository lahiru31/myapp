package com.example.myshop.utils

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val error: Throwable? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(
        message: String,
        error: Throwable? = null,
        data: T? = null
    ) : Resource<T>(data, message, error)

    val isSuccess: Boolean get() = this is Success
    val isLoading: Boolean get() = this is Loading
    val isError: Boolean get() = this is Error

    fun <R> map(transform: (T?) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(message!!, error, transform(data))
            is Loading -> Loading(transform(data))
        }
    }

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)

        fun <T> loading(data: T? = null): Resource<T> = Loading(data)

        fun <T> error(
            message: String,
            error: Throwable? = null,
            data: T? = null
        ): Resource<T> = Error(message, error, data)

        fun <T> networkError(data: T? = null): Resource<T> =
            Error("Network error occurred. Please check your internet connection.", null, data)

        fun <T> serverError(data: T? = null): Resource<T> =
            Error("Server error occurred. Please try again later.", null, data)

        fun <T> unknownError(error: Throwable? = null, data: T? = null): Resource<T> =
            Error("An unknown error occurred.", error, data)

        fun <T> authError(message: String = "Authentication error occurred.", data: T? = null): Resource<T> =
            Error(message, null, data)
    }

    override fun toString(): String {
        return when (this) {
            is Success -> "Success[data=$data]"
            is Error -> "Error[message=$message, error=$error, data=$data]"
            is Loading -> "Loading[data=$data]"
        }
    }

    suspend fun onSuccess(action: suspend (T) -> Unit): Resource<T> {
        if (this is Success) {
            action(data!!)
        }
        return this
    }

    suspend fun onError(action: suspend (String, Throwable?) -> Unit): Resource<T> {
        if (this is Error) {
            action(message!!, error)
        }
        return this
    }

    suspend fun onLoading(action: suspend (T?) -> Unit): Resource<T> {
        if (this is Loading) {
            action(data)
        }
        return this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(defaultValue: T): T = when (this) {
        is Success -> data ?: defaultValue
        else -> defaultValue
    }

    fun requireData(): T = when (this) {
        is Success -> data!!
        is Error -> throw IllegalStateException("Cannot access data in Error state: $message")
        is Loading -> throw IllegalStateException("Cannot access data in Loading state")
    }

    fun errorMessage(): String? = when (this) {
        is Error -> message
        else -> null
    }
}

// Extension function to convert a nullable value to Resource
fun <T> T?.asResource(): Resource<T> = when {
    this != null -> Resource.success(this)
    else -> Resource.error("Data is null")
}

// Extension function to handle exceptions and convert to Resource
inline fun <T> tryAsResource(block: () -> T): Resource<T> = try {
    Resource.success(block())
} catch (e: Exception) {
    Resource.error(e.message ?: "Unknown error occurred", e)
}

// Extension function to handle suspending functions and convert to Resource
suspend inline fun <T> tryAsResourceSuspend(crossinline block: suspend () -> T): Resource<T> = try {
    Resource.success(block())
} catch (e: Exception) {
    Resource.error(e.message ?: "Unknown error occurred", e)
}

// Extension function to convert a Result to Resource
fun <T> Result<T>.asResource(): Resource<T> = fold(
    onSuccess = { Resource.success(it) },
    onFailure = { Resource.error(it.message ?: "Unknown error occurred", it) }
)
