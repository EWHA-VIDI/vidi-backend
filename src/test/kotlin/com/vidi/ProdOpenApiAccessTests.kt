package com.vidi

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class ProdOpenApiAccessTests
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val context: ApplicationContext,
        private val environment: Environment,
    ) {
        @Test
        fun `운영에서는 문서와 로컬 인증 예외를 활성화하지 않는다`() {
            environment.getProperty("springdoc.api-docs.enabled", Boolean::class.java) shouldBe false
            environment.getProperty("springdoc.swagger-ui.enabled", Boolean::class.java) shouldBe false
            context.containsBean("localOpenApiSecurity") shouldBe false
            mockMvc
                .perform(get("/v3/api-docs"))
                .andExpect(status().isUnauthorized)
        }
    }
