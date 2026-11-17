package com.vidi.global.security

sealed interface JwtParseResult {
    data class Valid(
        val subject: String,
    ) : JwtParseResult

    data object Expired : JwtParseResult

    data object Invalid : JwtParseResult
}
