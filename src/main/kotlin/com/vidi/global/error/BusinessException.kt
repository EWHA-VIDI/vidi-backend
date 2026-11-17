package com.vidi.global.error

import com.vidi.global.response.ErrorResponse.FieldError

// message is for logs only; the response always uses errorCode.message.
open class BusinessException(
    val errorCode: ErrorCode,
    val errors: List<FieldError> = emptyList(),
    message: String = errorCode.message,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
