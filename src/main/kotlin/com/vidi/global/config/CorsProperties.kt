package com.vidi.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "vidi.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = emptyList(),
)
