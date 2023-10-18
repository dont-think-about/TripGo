package com.nbcamp.tripgo.util

// Retrofit 응답 상태에 따라 UI 분기 처리를 위해 상태를 가질 수 있게 하는 클래스
sealed class APIResponse<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T? = null) : APIResponse<T>(data)
    class Error<T>(message: String, data: T? = null) : APIResponse<T>(data, message)
}
