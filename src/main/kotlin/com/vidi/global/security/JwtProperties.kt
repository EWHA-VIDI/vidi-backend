package com.vidi.global.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "vidi.security.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Duration,
)
