package com.vidi.global.security

import io.jsonwebtoken.ExpiredJwtException
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
            require(bytes.size >= MIN_SECRET_BYTES) { "JWT 비밀키는 ${MIN_SECRET_BYTES}바이트 이상이어야 합니다." }
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

    fun parse(token: String): JwtParseResult =
        try {
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
                ?.takeIf { it.isNotBlank() }
                ?.let { JwtParseResult.Valid(it) }
                ?: JwtParseResult.Invalid
        } catch (_: ExpiredJwtException) {
            JwtParseResult.Expired
        } catch (_: JwtException) {
            JwtParseResult.Invalid
        } catch (_: IllegalArgumentException) {
            JwtParseResult.Invalid
        }

    private companion object {
        const val MIN_SECRET_BYTES = 32
    }
}
