package me.hker.common

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.http.converter.HttpMessageNotReadableException

@RestControllerAdvice
class GlobalExceptionHandler(
    private val messageHelper: I18nMessageHelper? = null,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(InsufficientCreditsException::class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    fun handleInsufficientCredits(ex: InsufficientCreditsException): R<Nothing> {
        log.warn("Payment required: {}", ex.message)
        val localized = messageHelper?.get("error.payment_required") ?: (ex.message ?: "insufficient credits")
        return R.fail(HttpStatus.PAYMENT_REQUIRED.value(), localized)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: IllegalArgumentException): R<Nothing> {
        log.warn("Request failed: {}", ex.message)
        val localized = messageHelper?.get("error.bad_request") ?: (ex.message ?: "bad request")
        return R.fail(400, localized)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: ResourceNotFoundException): R<Nothing> {
        log.warn("Resource not found: {}", ex.message)
        val localized = messageHelper?.get("error.not_found") ?: (ex.message ?: "not found")
        return R.fail(404, localized)
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDenied(ex: AccessDeniedException): R<Nothing> {
        log.warn("Access denied: {}", ex.message)
        val localized = messageHelper?.get("error.forbidden") ?: (ex.message ?: "access denied")
        return R.fail(403, localized)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): R<Nothing> {
        log.warn("Type mismatch: {}", ex.message)
        val message = "Invalid parameter: ${ex.name}"
        return R.fail(400, message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): R<Nothing> {
        log.warn("Message not readable: {}", ex.message)
        return R.fail(400, "Invalid request body")
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(ex: Exception): R<Nothing> {
        log.error("Unhandled server error", ex)
        val localized = messageHelper?.get("error.internal") ?: (ex.message ?: "internal server error")
        return R.fail(500, localized)
    }
}
