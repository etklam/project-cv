package me.hker.module.admin.service

import me.hker.module.admin.dto.AdminTemplateDto
import me.hker.module.admin.dto.CreateTemplateRequest
import me.hker.module.admin.dto.ToggleTemplateStatusRequest
import me.hker.module.admin.dto.UpdateTemplateRequest

interface AdminTemplateService {
    fun listAllTemplates(): List<AdminTemplateDto>
    fun createTemplate(request: CreateTemplateRequest): AdminTemplateDto
    fun updateTemplate(id: Long, request: UpdateTemplateRequest): AdminTemplateDto
    fun deleteTemplate(id: Long)
    fun toggleTemplateStatus(id: Long, request: ToggleTemplateStatusRequest): AdminTemplateDto
}
