package com.vidi.global.error

import com.vidi.global.response.ErrorResponse
import com.vidi.global.response.ErrorResponse.FieldError
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

private const val INVALID_VALUE_REASON = "올바르지 않은 값입니다."
private const val TYPE_MISMATCH_REASON = "형식이 올바르지 않습니다."
private const val MISSING_VALUE_REASON = "필수 값입니다."

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusiness(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.warn("BusinessException: code={}, message={}", e.errorCode.name, e.message)
        return respond(e.errorCode, e.errors)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors =
            e.bindingResult.fieldErrors.map { error ->
                FieldError(error.field, error.defaultMessage ?: INVALID_VALUE_REASON)
            }
        return respond(ErrorCode.INVALID_INPUT, errors)
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidation(e: HandlerMethodValidationException): ResponseEntity<ErrorResponse> {
        val errors =
            e.parameterValidationResults.flatMap { result ->
                val field = result.methodParameter.parameterName.orEmpty()
                result.resolvableErrors.map { error ->
                    FieldError(field, error.defaultMessage ?: INVALID_VALUE_REASON)
                }
            }
        return respond(ErrorCode.INVALID_INPUT, errors)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors =
            e.constraintViolations.map { violation ->
                FieldError(
                    violation.propertyPath
                        .lastOrNull()
                        ?.name
                        .orEmpty(),
                    violation.message,
                )
            }
        return respond(ErrorCode.INVALID_INPUT, errors)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> =
        respond(ErrorCode.INVALID_INPUT, listOf(FieldError(e.name, TYPE_MISMATCH_REASON)))

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> =
        respond(ErrorCode.INVALID_INPUT, listOf(FieldError(e.parameterName, MISSING_VALUE_REASON)))

    @ExceptionHandler(HttpMessageNotReadableException::class, HttpMediaTypeNotSupportedException::class)
    fun handleUnreadableBody(): ResponseEntity<ErrorResponse> = respond(ErrorCode.INVALID_INPUT)

    @ExceptionHandler(NoResourceFoundException::class, NoHandlerFoundException::class)
    fun handleNotFound(): ResponseEntity<ErrorResponse> = respond(ErrorCode.NOT_FOUND)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(): ResponseEntity<ErrorResponse> = respond(ErrorCode.METHOD_NOT_ALLOWED)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        // Spring Security의 EntryPoint와 AccessDeniedHandler가 401·403 응답을 만들도록 넘긴다.
        if (e is AccessDeniedException || e is AuthenticationException) throw e
        log.error("Unhandled exception: {} {}", request.method, request.requestURI, e)
        return respond(ErrorCode.INTERNAL_ERROR)
    }
}

private fun respond(
    errorCode: ErrorCode,
    errors: List<FieldError> = emptyList(),
): ResponseEntity<ErrorResponse> = ResponseEntity.status(errorCode.status).body(ErrorResponse.of(errorCode, errors))
