package me.hker.module.template.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import me.hker.module.template.dto.TemplateDto
import me.hker.module.template.entity.Template
import me.hker.module.template.mapper.TemplateMapper
import me.hker.module.template.service.TemplateService
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class TemplateServiceImpl(
    private val templateMapper: TemplateMapper,
    private val objectMapper: ObjectMapper,
) : TemplateService {
    private val mapTypeReference = object : TypeReference<Map<String, String>>() {}
    private val fallbackLocaleTags = listOf("zh-TW", "en")

    override fun listActiveTemplates(): List<TemplateDto> {
        val locale = LocaleContextHolder.getLocale()
        return templateMapper.selectActiveTemplates().map { toDto(it, locale) }
    }

    private fun toDto(template: Template, locale: Locale): TemplateDto {
        val displayName = resolveLocalizedText(template.displayNameI18n, locale)
        val description = resolveLocalizedText(template.descriptionI18n, locale).ifBlank { displayName }
        return TemplateDto(
            key = template.componentKey,
            displayName = displayName,
            description = description,
            creditCost = template.creditCost,
            previewImagePath = template.previewImage,
        )
    }

    private fun resolveLocalizedText(json: String?, locale: Locale): String {
        val values = parseLocaleMap(json)
        val candidates = listOf(locale.toLanguageTag(), locale.language)
        candidates.forEach { key ->
            values[key]?.takeIf { it.isNotBlank() }?.let { return it }
        }
        fallbackLocaleTags.forEach { key ->
            values[key]?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return values.values.firstOrNull { it.isNotBlank() } ?: ""
    }

    private fun parseLocaleMap(json: String?): Map<String, String> {
        if (json.isNullOrBlank()) return emptyMap()
        return try {
            objectMapper.readValue(json, mapTypeReference)
        } catch (ex: Exception) {
            emptyMap()
        }
    }
}
