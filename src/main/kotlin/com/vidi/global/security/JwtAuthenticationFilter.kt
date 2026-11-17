package com.vidi.global.security

import com.vidi.global.error.ErrorCode
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // 유효하지 않은 토큰은 인증 없이 통과시키고, EntryPoint가 기록된 사유로 401 코드를 정한다.
        resolveToken(request)?.let { token ->
            when (val result = jwtProvider.parse(token)) {
                is JwtParseResult.Valid -> authenticate(request, result.subject)
                JwtParseResult.Expired -> request.setAttribute(AUTH_ERROR_ATTRIBUTE, ErrorCode.EXPIRED_TOKEN)
                JwtParseResult.Invalid -> request.setAttribute(AUTH_ERROR_ATTRIBUTE, ErrorCode.INVALID_TOKEN)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun authenticate(
        request: HttpServletRequest,
        subject: String,
    ) {
        val authentication = UsernamePasswordAuthenticationToken.authenticated(subject, null, emptyList())
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun resolveToken(request: HttpServletRequest): String? =
        request
            .getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX, ignoreCase = true) }
            ?.substring(BEARER_PREFIX.length)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    companion object {
        val AUTH_ERROR_ATTRIBUTE: String = JwtAuthenticationFilter::class.java.name + ".AUTH_ERROR"
        private const val BEARER_PREFIX = "Bearer "
    }
}
