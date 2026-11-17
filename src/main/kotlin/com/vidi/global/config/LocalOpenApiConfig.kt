package com.vidi.global.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration(proxyBeanMethods = false)
@Profile("local & !prod")
@ConditionalOnProperty(prefix = "springdoc.api-docs", name = ["enabled"], havingValue = "true")
class LocalOpenApiConfig {
    // Runs before the main chain so only local documentation paths skip authentication.
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun localOpenApiSecurity(http: HttpSecurity): SecurityFilterChain =
        http
            .securityMatcher("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .csrf { it.disable() }
            .build()
}
