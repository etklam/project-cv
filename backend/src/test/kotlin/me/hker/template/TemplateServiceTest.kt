package me.hker.template

import me.hker.module.template.entity.Template
import me.hker.module.template.mapper.TemplateMapper
import me.hker.module.template.service.impl.TemplateServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.context.i18n.LocaleContextHolder
import java.util.Locale

class TemplateServiceTest {
    private val mapper: TemplateMapper = mock(TemplateMapper::class.java)
    private val service = TemplateServiceImpl(mapper, ObjectMapper())

    @BeforeEach
    fun setUp() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("zh-TW"))
    }

    @AfterEach
    fun tearDown() {
        LocaleContextHolder.resetLocaleContext()
    }

    @Test
    fun `listActiveTemplates returns localized values for zh-TW`() {
        val active = Template().apply {
            componentKey = "minimal"
            displayNameI18n = """{"en":"Minimal","zh-TW":"簡約"}"""
            descriptionI18n = """{"en":"Simple layout","zh-TW":"強調內容"}"""
            creditCost = 0
            previewImage = "/images/minimal.png"
            isActive = true
        }
        `when`(mapper.selectActiveTemplates()).thenReturn(listOf(active))

        val results = service.listActiveTemplates()

        assertEquals(1, results.size)
        val dto = results.first()
        assertEquals("minimal", dto.key)
        assertEquals("簡約", dto.displayName)
        assertEquals("強調內容", dto.description)
        assertEquals(0, dto.creditCost)
        assertEquals("/images/minimal.png", dto.previewImagePath)
    }

    @Test
    fun `listActiveTemplates falls back to first available locale value`() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("fr"))
        val active = Template().apply {
            componentKey = "modern"
            displayNameI18n = """{"en":"Modern"}"""
            descriptionI18n = """{"en":"Modern layout"}"""
            creditCost = 5
            previewImage = "/images/modern.png"
        }
        `when`(mapper.selectActiveTemplates()).thenReturn(listOf(active))

        val results = service.listActiveTemplates()

        assertEquals("Modern", results.first().displayName)
        assertEquals("Modern layout", results.first().description)
    }

    @Test
    fun `existsActiveTemplate returns mapper existence result`() {
        val active = Template().apply {
            componentKey = "modern"
        }
        `when`(mapper.selectActiveTemplateByKey("modern")).thenReturn(active)
        `when`(mapper.selectActiveTemplateByKey("ghost")).thenReturn(null)

        assertEquals(true, service.existsActiveTemplate("modern"))
        assertEquals(false, service.existsActiveTemplate("ghost"))
    }
}
