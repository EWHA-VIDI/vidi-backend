package com.vidi.global.response

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.data.domain.Page

@JsonPropertyOrder("content", "page", "size", "totalElements", "totalPages", "hasNext")
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
) {
    companion object {
        fun <T : Any> from(page: Page<T>): PageResponse<T> = from(page) { it }

        fun <T : Any, R> from(
            page: Page<T>,
            transform: (T) -> R,
        ): PageResponse<R> =
            PageResponse(
                content = page.content.map(transform),
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                hasNext = page.hasNext(),
            )
    }
}
