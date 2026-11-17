package com.vidi.global.error

import com.vidi.global.response.ErrorResponse.FieldError

// message는 로그에만 남기고 응답에는 항상 errorCode.message를 쓴다.
open class BusinessException(
    val errorCode: ErrorCode,
    val errors: List<FieldError> = emptyList(),
    message: String = errorCode.message,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
