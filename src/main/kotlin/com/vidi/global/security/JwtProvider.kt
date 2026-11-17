package com.vidi.global.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
@EnableConfigurationProperties(JwtProperties::class)
class JwtProvider(
    private val properties: JwtProperties,
) {
    private val key: SecretKey =
        properties.secret.toByteArray(Charsets.UTF_8).let { bytes ->
            require(bytes.size >= MIN_SECRET_BYTES) { "JWT secret must be at least $MIN_SECRET_BYTES bytes." }
            Keys.hmacShaKeyFor(bytes)
        }

    fun createAccessToken(subject: String): String {
        val now = Date()
        return Jwts
            .builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(Date(now.time + properties.accessTokenExpiration.toMillis()))
            .signWith(key)
            .compact()
    }

    fun getSubject(token: String): String? =
        try {
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
                ?.takeIf { it.isNotBlank() }
        } catch (_: JwtException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    private companion object {
        const val MIN_SECRET_BYTES = 32
    }
}
