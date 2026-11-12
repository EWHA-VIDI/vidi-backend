package com.vidi

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LocalOpenApiAccessTests
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @Test
        fun `로컬 OpenAPI는 인증 없이 읽는다`() {
            mockMvc
                .perform(get("/v3/api-docs"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.openapi").exists())
        }

        @Test
        fun `문서 외 경로는 기본 인증을 유지한다`() {
            mockMvc
                .perform(get("/api/private"))
                .andExpect(status().isUnauthorized)
        }
    }
