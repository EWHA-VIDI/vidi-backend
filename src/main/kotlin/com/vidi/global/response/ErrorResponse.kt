package com.vidi.global.response

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.vidi.global.error.ErrorCode

@JsonPropertyOrder("success", "code", "message", "errors")
data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: List<FieldError> = emptyList(),
) {
    val success: Boolean = false

    data class FieldError(
        val field: String,
        val reason: String,
    )

    companion object {
        fun of(
            errorCode: ErrorCode,
            errors: List<FieldError> = emptyList(),
        ): ErrorResponse = ErrorResponse(errorCode.code, errorCode.message, errors)
    }
}
