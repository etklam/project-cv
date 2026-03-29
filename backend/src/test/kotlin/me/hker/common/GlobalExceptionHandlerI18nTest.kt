package me.hker.common

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.Locale
import me.hker.module.auth.JwtAuthFilter

@WebMvcTest(controllers = [I18nErrorTestController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(TestI18nConfig::class, GlobalExceptionHandler::class, I18nMessageHelper::class)
class GlobalExceptionHandlerI18nTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

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
