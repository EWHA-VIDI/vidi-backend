package com.vidi.global.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CorsConfigTests
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @Test
        fun `로컬 FE 주소의 preflight 요청은 인증 없이 허용한다`() {
            mockMvc
                .perform(
                    options("/api/any")
                        .header(HttpHeaders.ORIGIN, LOCAL_FE_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"),
                ).andExpect(status().isOk)
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LOCAL_FE_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
        }

        @Test
        fun `허용하지 않은 주소의 preflight 요청은 거부한다`() {
            mockMvc
                .perform(
                    options("/api/any")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"),
                ).andExpect(status().isForbidden)
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
        }

        @Test
        fun `로컬 FE 주소의 일반 요청 응답에도 CORS 헤더를 포함한다`() {
            mockMvc
                .perform(get("/actuator/health").header(HttpHeaders.ORIGIN, LOCAL_FE_ORIGIN))
                .andExpect(status().isOk)
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LOCAL_FE_ORIGIN))
        }

        private companion object {
            const val LOCAL_FE_ORIGIN = "http://localhost:5173"
        }
    }
