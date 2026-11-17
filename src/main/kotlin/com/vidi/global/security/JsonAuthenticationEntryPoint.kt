package com.vidi.global.security

import com.vidi.global.error.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JsonAuthenticationEntryPoint(
    private val errorResponseWriter: SecurityErrorResponseWriter,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorCode =
            request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE) as? ErrorCode
                ?: ErrorCode.UNAUTHORIZED
        errorResponseWriter.write(response, errorCode)
    }
}
