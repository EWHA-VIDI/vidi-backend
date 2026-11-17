package com.vidi.global.security

import com.vidi.global.error.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JsonAccessDeniedHandler(
    private val errorResponseWriter: SecurityErrorResponseWriter,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        errorResponseWriter.write(response, ErrorCode.FORBIDDEN)
    }
}
