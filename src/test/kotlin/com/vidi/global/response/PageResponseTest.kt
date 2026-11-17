package com.vidi.global.response

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PageResponseTest {
    @Test
    fun `Page의 페이지 정보를 그대로 옮긴다`() {
        val page = PageImpl(listOf("a", "b"), PageRequest.of(1, 2), 5)

        PageResponse.from(page) shouldBe
            PageResponse(
                content = listOf("a", "b"),
                page = 1,
                size = 2,
                totalElements = 5,
                totalPages = 3,
                hasNext = true,
            )
    }

    @Test
    fun `마지막 페이지는 다음 페이지가 없다`() {
        val page = PageImpl(listOf("e"), PageRequest.of(2, 2), 5)

        PageResponse.from(page).hasNext shouldBe false
    }

    @Test
    fun `변환 함수로 content를 응답 DTO로 바꾼다`() {
        val page = PageImpl(listOf(1L, 2L), PageRequest.of(0, 10), 2)

        val response = PageResponse.from(page) { id -> "comment-$id" }

        response.content shouldBe listOf("comment-1", "comment-2")
        response.totalElements shouldBe 2
        response.hasNext shouldBe false
    }

    @Test
    fun `빈 페이지는 빈 content와 0개의 전체 개수를 가진다`() {
        val response = PageResponse.from(Page.empty<String>(PageRequest.of(0, 20)))

        response.content shouldBe emptyList()
        response.totalElements shouldBe 0
        response.totalPages shouldBe 0
        response.hasNext shouldBe false
    }
}
