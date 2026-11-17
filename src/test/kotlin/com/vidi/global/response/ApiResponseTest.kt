package com.vidi.global.response

import com.vidi.global.error.ErrorCode
import com.vidi.global.response.ErrorResponse.FieldError
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

class ApiResponseTest {
    private val mapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    @Test
    fun `성공 응답은 success와 data 순서로 직렬화된다`() {
        val response = ApiResponse.ok(mapOf("videoId" to "dQw4w9WgXcQ"))

        mapper.writeValueAsString(response) shouldBe
            """{"success":true,"data":{"videoId":"dQw4w9WgXcQ"}}"""
    }

    @Test
    fun `실패 응답은 enum 이름을 code로 쓰고 errors는 빈 배열이다`() {
        val response = ErrorResponse.of(ErrorCode.NOT_FOUND)

        mapper.writeValueAsString(response) shouldBe
            """{"success":false,"code":"NOT_FOUND","message":"요청한 리소스를 찾을 수 없습니다.","errors":[]}"""
    }

    @Test
    fun `검증 실패 응답은 필드 오류를 담는다`() {
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT, listOf(FieldError("videoUrl", "형식 오류")))

        mapper.writeValueAsString(response) shouldBe
            """{"success":false,"code":"INVALID_INPUT","message":"입력값이 올바르지 않습니다.",""" +
            """"errors":[{"field":"videoUrl","reason":"형식 오류"}]}"""
    }

    @Test
    fun `목록 응답은 data에 페이지 정보를 담는다`() {
        val response = ApiResponse.ok(PageResponse.from(PageImpl(listOf("a", "b"), PageRequest.of(0, 2), 5)))

        mapper.writeValueAsString(response) shouldBe
            """{"success":true,"data":{"content":["a","b"],"page":0,"size":2,""" +
            """"totalElements":5,"totalPages":3,"hasNext":true}}"""
    }
}
