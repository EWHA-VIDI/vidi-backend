package com.vidi.global.security

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Import(SecurityConfigTests.ProtectedTestController::class)
class SecurityConfigTests
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val jwtProvider: JwtProvider,
        private val passwordEncoder: PasswordEncoder,
    ) {
        @Test
        fun `토큰 없이 보호된 API를 호출하면 401을 반환한다`() {
            mockMvc
                .perform(get(PROTECTED_PATH))
                .andExpect(status().isUnauthorized)
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
        fun `유효하지 않은 토큰으로 호출하면 401을 반환한다`() {
            mockMvc
                .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `Bearer 형식이 아닌 Authorization 헤더는 인증하지 않는다`() {
            val token = jwtProvider.createAccessToken("42")

            mockMvc
                .perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Basic $token"))
                .andExpect(status().isUnauthorized)
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
        }

        private companion object {
            const val PROTECTED_PATH = "/api/test/security/me"
        }
    }
