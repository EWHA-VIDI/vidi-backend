package com.vidi.global.security

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.Base64

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Import(SecurityConfigTests.ProtectedTestController::class)
class SecurityConfigTests
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val jwtProvider: JwtProvider,
        private val jwtProperties: JwtProperties,
        private val passwordEncoder: PasswordEncoder,
    ) {
        @Test
        fun `토큰 없이 보호된 API를 호출하면 UNAUTHORIZED 에러 응답을 반환한다`() {
            mockMvc
                .perform(get(PROTECTED_PATH))
                .andExpect(status().isUnauthorized)
                .andExpectError("UNAUTHORIZED", "인증이 필요합니다.")
        }

        @Test
        fun `유효한 토큰으로 보호된 API를 호출하면 인증된 subject로 응답한다`() {
            val token = jwtProvider.createAccessToken("42")

            mockMvc
                .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
                .andExpect(status().isOk)
                .andExpect(content().string("42"))
        }

        @Test
        fun `만료된 토큰으로 호출하면 EXPIRED_TOKEN 에러 응답을 반환한다`() {
            val token = expiredToken()

            mockMvc
                .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
                .andExpect(status().isUnauthorized)
                .andExpectError("EXPIRED_TOKEN", "로그인이 만료되었습니다. 다시 로그인해 주세요.")
        }

        @Test
        fun `서명이 변조된 토큰으로 호출하면 INVALID_TOKEN 에러 응답을 반환한다`() {
            val (header, payload, signature) = jwtProvider.createAccessToken("42").split(".")
            val forgedPayload = String(Base64.getUrlDecoder().decode(payload)).replace("\"42\"", "\"1\"")
            val encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(forgedPayload.toByteArray())
            val tampered = "$header.$encodedPayload.$signature"

            mockMvc
                .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer $tampered"))
                .andExpect(status().isUnauthorized)
                .andExpectError("INVALID_TOKEN", "인증 정보가 올바르지 않습니다. 다시 로그인해 주세요.")
        }

        @Test
        fun `형식이 잘못된 토큰으로 호출하면 INVALID_TOKEN 에러 응답을 반환한다`() {
            mockMvc
                .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized)
                .andExpectError("INVALID_TOKEN", "인증 정보가 올바르지 않습니다. 다시 로그인해 주세요.")
        }

        @Test
        fun `Bearer 형식이 아닌 Authorization 헤더는 인증하지 않는다`() {
            val token = jwtProvider.createAccessToken("42")

            mockMvc
                .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Basic $token"))
                .andExpect(status().isUnauthorized)
                .andExpectError("UNAUTHORIZED", "인증이 필요합니다.")
        }

        @Test
        fun `권한이 없는 사용자가 호출하면 FORBIDDEN 에러 응답을 반환한다`() {
            val token = jwtProvider.createAccessToken("42")

            mockMvc
                .perform(get(ADMIN_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer $token"))
                .andExpect(status().isForbidden)
                .andExpectError("FORBIDDEN", "접근 권한이 없습니다.")
        }

        @Test
        fun `인증이 필요 없는 경로는 만료된 토큰이 있어도 호출한다`() {
            mockMvc
                .perform(get("/actuator/health").header(HttpHeaders.AUTHORIZATION, "Bearer ${expiredToken()}"))
                .andExpect(status().isOk)
        }

        @Test
        fun `health는 인증 없이 호출한다`() {
            mockMvc
                .perform(get("/actuator/health"))
                .andExpect(status().isOk)
        }

        @Test
        fun `PasswordEncoder는 원문을 해시로 저장하고 일치 여부를 확인한다`() {
            val encoded = passwordEncoder.encode("raw-password")

            encoded shouldNotBe "raw-password"
            passwordEncoder.matches("raw-password", encoded) shouldBe true
            passwordEncoder.matches("other-password", encoded) shouldBe false
        }

        @RestController
        class ProtectedTestController {
            @GetMapping(PROTECTED_PATH)
            fun me(
                @AuthenticationPrincipal subject: String,
            ): String = subject

            @GetMapping(ADMIN_PATH)
            @PreAuthorize("hasRole('ADMIN')")
            fun admin(
                @AuthenticationPrincipal subject: String,
            ): String = subject
        }

        private fun expiredToken(): String =
            JwtProvider(jwtProperties.copy(accessTokenExpiration = Duration.ofSeconds(-1))).createAccessToken("42")

        private fun ResultActions.andExpectError(
            code: String,
            message: String,
        ): ResultActions =
            andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().encoding(Charsets.UTF_8))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.errors").isEmpty)

        private companion object {
            const val PROTECTED_PATH = "/api/test/security/me"
            const val ADMIN_PATH = "/api/test/security/admin"
        }
    }
