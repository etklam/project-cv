package me.hker.common

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import org.springframework.context.annotation.Bean
import java.util.Locale
import org.springframework.context.annotation.Configuration
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [I18nErrorTestController::class])
@Import(TestI18nConfig::class, GlobalExceptionHandler::class, I18nMessageHelper::class)
class GlobalExceptionHandlerI18nTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @Test
    fun `bad request message uses locale from Accept-Language`() {
        mockMvc.get("/error/illegal") {
            header("Accept-Language", "zh-TW")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("參數不正確") }
        }

        mockMvc.get("/error/illegal") {
            header("Accept-Language", "en")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("Invalid request") }
        }
    }

    @Test
    fun `not found message uses locale from Accept-Language`() {
        mockMvc.get("/error/notfound") {
            header("Accept-Language", "zh-CN")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.message") { value("找不到资源") }
        }
    }
}

@RestController
class I18nErrorTestController {
    @GetMapping("/error/illegal")
    fun illegal(): String = throw IllegalArgumentException("should be localized")

    @GetMapping("/error/notfound")
    fun notfound(): String = throw ResourceNotFoundException("missing")
}

@Configuration
class TestI18nConfig {
    @Bean
    fun localeResolver(): LocaleResolver = AcceptHeaderLocaleResolver().apply {
        setDefaultLocale(Locale.forLanguageTag("en"))
    }

    @Bean
    fun messageSource(): MessageSource {
        val source = ResourceBundleMessageSource()
        source.setBasename("messages")
        source.setDefaultEncoding("UTF-8")
        source.setFallbackToSystemLocale(false)
        return source
    }
}
