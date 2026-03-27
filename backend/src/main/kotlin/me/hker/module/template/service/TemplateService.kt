package me.hker.module.template.service

import me.hker.module.template.dto.TemplateDto

interface TemplateService {
    fun listActiveTemplates(): List<TemplateDto>
}
