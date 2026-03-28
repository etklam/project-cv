package me.hker.module.admin.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.common.ResourceNotFoundException
import me.hker.module.admin.dto.*
import me.hker.module.admin.service.AdminTemplateService
import me.hker.module.template.entity.Template
import me.hker.module.template.mapper.TemplateMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminTemplateServiceImpl(
    private val templateMapper: TemplateMapper,
) : AdminTemplateService {

    override fun listAllTemplates(): List<AdminTemplateDto> {
        val templates = templateMapper.selectList(
            QueryWrapper<Template>().orderByAsc("sort_order", "id")
        )

        return templates.map { template ->
            AdminTemplateDto(
                id = template.id ?: 0L,
                componentKey = template.componentKey,
                displayNameI18n = template.displayNameI18n,
                descriptionI18n = template.descriptionI18n,
                previewImage = template.previewImage,
                creditCost = template.creditCost,
                isActive = template.isActive,
                sortOrder = template.sortOrder,
                createdAt = template.createdAt ?: java.time.LocalDateTime.now(),
            )
        }
    }

    @Transactional
    override fun createTemplate(request: CreateTemplateRequest): AdminTemplateDto {
        val existing = templateMapper.selectOne(
            QueryWrapper<Template>().eq("component_key", request.componentKey).eq("is_deleted", false)
        )
        if (existing != null) {
            throw IllegalArgumentException("Template already exists: ${request.componentKey}")
        }

        val template = Template().apply {
            componentKey = request.componentKey
            displayNameI18n = request.displayNameI18n
            descriptionI18n = request.descriptionI18n
            previewImage = request.previewImage
            creditCost = request.creditCost
            sortOrder = request.sortOrder
            isActive = true
        }

        templateMapper.insert(template)

        return AdminTemplateDto(
            id = template.id ?: 0L,
            componentKey = template.componentKey,
            displayNameI18n = template.displayNameI18n,
            descriptionI18n = template.descriptionI18n,
            previewImage = template.previewImage,
            creditCost = template.creditCost,
            isActive = template.isActive,
            sortOrder = template.sortOrder,
            createdAt = template.createdAt ?: java.time.LocalDateTime.now(),
        )
    }

    @Transactional
    override fun updateTemplate(id: Long, request: UpdateTemplateRequest): AdminTemplateDto {
        val template = templateMapper.selectById(id)
            ?: throw ResourceNotFoundException("Template not found: $id")

        request.displayNameI18n?.let { template.displayNameI18n = it }
        request.descriptionI18n?.let { template.descriptionI18n = it }
        request.previewImage?.let { template.previewImage = it }
        request.creditCost?.let { template.creditCost = it }
        request.sortOrder?.let { template.sortOrder = it }

        templateMapper.updateById(template)

        return AdminTemplateDto(
            id = template.id ?: 0L,
            componentKey = template.componentKey,
            displayNameI18n = template.displayNameI18n,
            descriptionI18n = template.descriptionI18n,
            previewImage = template.previewImage,
            creditCost = template.creditCost,
            isActive = template.isActive,
            sortOrder = template.sortOrder,
            createdAt = template.createdAt ?: java.time.LocalDateTime.now(),
        )
    }

    @Transactional
    override fun deleteTemplate(id: Long) {
        val template = templateMapper.selectById(id)
            ?: throw ResourceNotFoundException("Template not found: $id")

        template.isDeleted = true
        templateMapper.updateById(template)
    }

    @Transactional
    override fun toggleTemplateStatus(id: Long, request: ToggleTemplateStatusRequest): AdminTemplateDto {
        val template = templateMapper.selectById(id)
            ?: throw ResourceNotFoundException("Template not found: $id")

        template.isActive = request.active
        templateMapper.updateById(template)

        return AdminTemplateDto(
            id = template.id ?: 0L,
            componentKey = template.componentKey,
            displayNameI18n = template.displayNameI18n,
            descriptionI18n = template.descriptionI18n,
            previewImage = template.previewImage,
            creditCost = template.creditCost,
            isActive = template.isActive,
            sortOrder = template.sortOrder,
            createdAt = template.createdAt ?: java.time.LocalDateTime.now(),
        )
    }
}
