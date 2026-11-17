package com.vidi.global.response

import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder("success", "data")
data class ApiResponse<T>(
    val data: T,
) {
    val success: Boolean = true

    companion object {
        fun <T> ok(data: T): ApiResponse<T> = ApiResponse(data)
    }
}
