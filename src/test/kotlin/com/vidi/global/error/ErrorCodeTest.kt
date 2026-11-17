package com.vidi.global.error

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ErrorCodeTest {
    @Test
    fun `공통 에러 코드는 HTTP 상태와 연결된다`() {
        ErrorCode.INVALID_INPUT.status shouldBe HttpStatus.BAD_REQUEST
        ErrorCode.UNAUTHORIZED.status shouldBe HttpStatus.UNAUTHORIZED
        ErrorCode.FORBIDDEN.status shouldBe HttpStatus.FORBIDDEN
        ErrorCode.NOT_FOUND.status shouldBe HttpStatus.NOT_FOUND
        ErrorCode.INTERNAL_ERROR.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    }

    @Test
    fun `에러 코드 문자열은 enum 이름과 같다`() {
        ErrorCode.entries.forEach { errorCode ->
            errorCode.code shouldBe errorCode.name
        }
    }
}
