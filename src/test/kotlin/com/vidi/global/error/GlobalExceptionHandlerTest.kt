package com.vidi.global.error

import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.Validation
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(controllers = [GlobalExceptionHandlerTest.ErrorTestController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandlerTest.ErrorTestController::class)
class GlobalExceptionHandlerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @Test
        fun `BusinessException은 ErrorCode의 상태와 메시지로 응답한다`() {
            mockMvc
                .perform(get("/test/errors/business"))
                .andExpectError(404, ErrorCode.NOT_FOUND)
                .andExpect(jsonPath("$.errors").isEmpty)
                .andExpect(content().string(not(containsString("video-42"))))
        }

        @Test
        fun `본문 검증 실패는 INVALID_INPUT과 필드 오류로 응답한다`() {
            mockMvc
                .perform(
                    post("/test/errors/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"videoUrl":"not-a-url"}"""),
                ).andExpectError(400, ErrorCode.INVALID_INPUT)
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("videoUrl"))
                .andExpect(jsonPath("$.errors[0].reason").value(VIDEO_URL_REASON))
        }

        @Test
        fun `파라미터 제약 위반은 INVALID_INPUT과 파라미터 오류로 응답한다`() {
            mockMvc
                .perform(get("/test/errors/param").param("size", "0"))
                .andExpectError(400, ErrorCode.INVALID_INPUT)
                .andExpect(jsonPath("$.errors[0].field").value("size"))
                .andExpect(jsonPath("$.errors[0].reason").value(SIZE_REASON))
        }

        @Test
        fun `ConstraintViolationException은 INVALID_INPUT과 필드 오류로 응답한다`() {
            mockMvc
                .perform(get("/test/errors/constraint"))
                .andExpectError(400, ErrorCode.INVALID_INPUT)
                .andExpect(jsonPath("$.errors[0].field").value("keyword"))
                .andExpect(jsonPath("$.errors[0].reason").value(KEYWORD_REASON))
        }

        @Test
        fun `예상하지 못한 예외는 내부 정보 없이 INTERNAL_ERROR로 응답한다`() {
            mockMvc
                .perform(get("/test/errors/unexpected"))
                .andExpectError(500, ErrorCode.INTERNAL_ERROR)
                .andExpect(jsonPath("$.errors").isEmpty)
                .andExpect(content().string(not(containsString("SELECT"))))
        }

        private fun ResultActions.andExpectError(
            status: Int,
            errorCode: ErrorCode,
        ): ResultActions =
            andExpect(status().`is`(status))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(errorCode.name))
                .andExpect(jsonPath("$.message").value(errorCode.message))

        data class VideoRequest(
            @field:Pattern(regexp = "https://(www\\.)?youtube\\.com/.+", message = VIDEO_URL_REASON)
            val videoUrl: String,
        )

        data class SearchCondition(
            @field:NotBlank(message = KEYWORD_REASON)
            val keyword: String,
        )

        @RestController
        @RequestMapping("/test/errors")
        class ErrorTestController {
            private val validator = Validation.buildDefaultValidatorFactory().validator

            @GetMapping("/business")
            fun business(): Unit = throw BusinessException(ErrorCode.NOT_FOUND, message = "video-42 not found")

            @PostMapping("/body")
            fun body(
                @RequestBody @Valid request: VideoRequest,
            ): VideoRequest = request

            @GetMapping("/param")
            fun param(
                @RequestParam @Min(1, message = SIZE_REASON) size: Int,
            ): Int = size

            @GetMapping("/constraint")
            fun constraint(): Unit = throw ConstraintViolationException(validator.validate(SearchCondition("")))

            @GetMapping("/unexpected")
            fun unexpected(): Unit = error("SELECT * FROM member failed")
        }

        companion object {
            const val VIDEO_URL_REASON = "유튜브 영상 URL 형식이 아닙니다."
            const val SIZE_REASON = "1 이상이어야 합니다."
            const val KEYWORD_REASON = "검색어를 입력해야 합니다."
        }
    }
