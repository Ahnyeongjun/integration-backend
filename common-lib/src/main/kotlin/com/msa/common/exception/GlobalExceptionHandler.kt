package com.msa.common.exception

import com.msa.common.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Business exception: code={}, message={}", e.code, e.message)
        return ResponseEntity
            .status(e.status)
            .body(ApiResponse.error(e.code, e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = e.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")
        log.warn("Validation exception: {}", errors)
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("VALIDATION_ERROR", errors))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Nothing>> {
        val message = "잘못된 파라미터 타입: ${e.name}"
        log.warn("Type mismatch exception: parameter={}, value={}", e.name, e.value)
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("TYPE_MISMATCH", message))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameterException(e: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Nothing>> {
        val message = "필수 파라미터 누락: ${e.parameterName}"
        log.warn("Missing parameter exception: {}", e.parameterName)
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("MISSING_PARAMETER", message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Illegal argument exception: {}", e.message)
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("BAD_REQUEST", e.message ?: "잘못된 요청입니다"))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unexpected exception occurred", e)
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error("INTERNAL_ERROR", "서버 오류가 발생했습니다"))
    }
}
