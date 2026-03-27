package me.hker.module.template.dto

data class TemplateDto(
    val key: String,
    val displayName: String,
    val description: String,
    val creditCost: Int,
    val previewImagePath: String?,
)

data class TemplateListResponse(
    val templates: List<TemplateDto>,
)
