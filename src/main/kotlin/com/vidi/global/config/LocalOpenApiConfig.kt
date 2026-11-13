package com.vidi.global.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer

@Configuration(proxyBeanMethods = false)
@Profile("local & !prod")
@ConditionalOnProperty(prefix = "springdoc.api-docs", name = ["enabled"], havingValue = "true")
class LocalOpenApiConfig {
    // TODO(security): SecurityFilterChain 구현 시 문서 경로를 permitAll로 옮기고 이 Customizer를 제거한다.
    // Keep Boot's default security chain; only local documentation bypasses authentication.
    @Bean
    fun localOpenApiSecurity(): WebSecurityCustomizer =
        WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
            )
        }
}
