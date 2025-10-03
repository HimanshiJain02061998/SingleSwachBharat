package com.appynitty.kotlinsbalibrary.common.utils.retrofit

/**
 *  CREATED BY SANATH GOSAVI
 */
sealed class ApiResponseListener<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : ApiResponseListener<T>(data)
    class Failure<T>(message: String, data: T? = null) : ApiResponseListener<T>(data, message)
    class Loading<T> : ApiResponseListener<T>()
}