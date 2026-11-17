package com.vidi.global.response

import com.vidi.global.error.ErrorCode
import com.vidi.global.response.ErrorResponse.FieldError
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.nio.file.Files
import java.nio.file.Path

class ApiResponseTest {
    private val mapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()
    private val examples = readContractExamples()

    @Test
    fun `성공 응답은 문서 예시와 같다`() {
        val response = ApiResponse.ok(mapOf("videoId" to "dQw4w9WgXcQ", "commentCount" to 120))

        serialize(response) shouldBe normalize(examples.getValue("성공 응답"))
    }

    @Test
    fun `실패 응답은 문서 예시와 같다`() {
        val response = ErrorResponse.of(ErrorCode.NOT_FOUND)

        serialize(response) shouldBe normalize(examples.getValue("실패 응답"))
    }

    @Test
    fun `검증 실패 응답은 필드 오류를 담고 문서 예시와 같다`() {
        val response =
            ErrorResponse.of(
                ErrorCode.INVALID_INPUT,
                listOf(FieldError("videoUrl", "유튜브 영상 URL 형식이 아닙니다.")),
            )

        serialize(response) shouldBe normalize(examples.getValue("검증 실패 응답"))
    }

    private fun serialize(value: Any): String = mapper.writeValueAsString(value)

    // Re-serializing the documented JSON keeps field order, so order drift is also caught.
    private fun normalize(json: String): String = mapper.writeValueAsString(mapper.readTree(json))

    private fun readContractExamples(): Map<String, String> {
        val document = Files.readString(Path.of("docs/rules/10-api-contract.md"))
        val pattern = Regex("""### (.+?)\n.*?```json\n(.*?)```""", RegexOption.DOT_MATCHES_ALL)
        return pattern.findAll(document.replace("\r\n", "\n")).associate { match ->
            match.groupValues[1].trim() to match.groupValues[2]
        }
    }
}
