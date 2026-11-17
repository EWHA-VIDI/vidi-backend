package com.vidi.global.security

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration

class JwtProviderTests {
    private val provider = JwtProvider(JwtProperties(SECRET, Duration.ofMinutes(30)))

    @Test
    fun `발급한 토큰에서 subject를 읽는다`() {
        val token = provider.createAccessToken("1")

        provider.parse(token) shouldBe JwtParseResult.Valid("1")
    }

    @Test
    fun `다른 키로 서명한 토큰은 잘못된 토큰으로 판단한다`() {
        val otherProvider = JwtProvider(JwtProperties(OTHER_SECRET, Duration.ofMinutes(30)))
        val token = otherProvider.createAccessToken("1")

        provider.parse(token) shouldBe JwtParseResult.Invalid
    }

    @Test
    fun `만료된 토큰은 만료로 판단한다`() {
        val expiredProvider = JwtProvider(JwtProperties(SECRET, Duration.ofSeconds(-1)))
        val token = expiredProvider.createAccessToken("1")

        provider.parse(token) shouldBe JwtParseResult.Expired
    }

    @Test
    fun `다른 키로 서명한 만료 토큰은 만료가 아니라 잘못된 토큰으로 판단한다`() {
        val otherExpiredProvider = JwtProvider(JwtProperties(OTHER_SECRET, Duration.ofSeconds(-1)))
        val token = otherExpiredProvider.createAccessToken("1")

        provider.parse(token) shouldBe JwtParseResult.Invalid
    }

    @Test
    fun `형식이 잘못된 토큰은 잘못된 토큰으로 판단한다`() {
        provider.parse("not-a-jwt") shouldBe JwtParseResult.Invalid
        provider.parse("") shouldBe JwtParseResult.Invalid
    }

    @Test
    fun `32바이트보다 짧은 비밀키는 사용하지 않는다`() {
        shouldThrow<IllegalArgumentException> {
            JwtProvider(JwtProperties("too-short", Duration.ofMinutes(30)))
        }
    }

    private companion object {
        const val SECRET = "test-only-jwt-secret-that-is-at-least-32-bytes"
        const val OTHER_SECRET = "other-test-only-jwt-secret-at-least-32-bytes"
    }
}
