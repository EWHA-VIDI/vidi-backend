package com.vidi.global.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TraceIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId =
            request
                .getHeader(TRACE_ID_HEADER)
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it.length <= MAX_TRACE_ID_LENGTH }
                ?: UUID.randomUUID().toString()

        try {
            MDC.put(TRACE_ID_KEY, traceId)
            response.setHeader(TRACE_ID_HEADER, traceId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_ID_KEY)
        }
    }

    companion object {
        const val TRACE_ID_HEADER = "X-Trace-Id"
        const val TRACE_ID_KEY = "traceId"
        private const val MAX_TRACE_ID_LENGTH = 128
    }
}
