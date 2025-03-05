package it.unipd.milan.padovaquest.core.util

sealed class Resource<out T> {
    data class Success<T>(val result: T) : Resource<T>()
    data class Error<T>(val exception: Exception) : Resource<T>()
    data object Loading : Resource<Nothing>()
}