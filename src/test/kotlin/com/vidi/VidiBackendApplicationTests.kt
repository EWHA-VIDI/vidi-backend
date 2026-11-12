package com.vidi

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import javax.sql.DataSource

@SpringBootTest
class VidiBackendApplicationTests
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val dataSource: DataSource,
    ) {
        @Test
        fun `날짜는 UTC ISO 문자열로 직렬화한다`() {
            objectMapper.writeValueAsString(Instant.parse("2026-01-01T00:00:00Z")) shouldBe
                "\"2026-01-01T00:00:00Z\""
        }

        @Test
        fun `MySQL 세션은 UTC로 연결한다`() {
            dataSource.connection.use { connection ->
                connection.metaData.databaseProductName shouldBe "MySQL"
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT @@session.time_zone").use { result ->
                        result.next() shouldBe true
                        result.getString(1) shouldBe "+00:00"
                    }
                }
            }
        }
    }
