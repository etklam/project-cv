package me.hker.module.template.service

import me.hker.module.template.dto.TemplateDto

interface TemplateService {
    fun listActiveTemplates(): List<TemplateDto>
    fun existsActiveTemplate(templateKey: String): Boolean
    fun getActiveTemplate(templateKey: String): TemplateDto?
}
