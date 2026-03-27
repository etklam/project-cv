package me.hker.module.cv.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import me.hker.common.ResourceNotFoundException
import me.hker.module.cv.dto.CvDetailDto
import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.cv.dto.CvSectionDto
import me.hker.module.cv.dto.CvSummaryDto
import me.hker.module.cv.dto.UpdateCvRequest
import me.hker.module.cv.entity.Cv
import me.hker.module.cv.entity.CvSection
import me.hker.module.cv.mapper.CvMapper
import me.hker.module.cv.mapper.CvSectionMapper
import me.hker.module.cv.service.CvService
import me.hker.module.template.service.TemplateService
import org.springframework.stereotype.Service

@Service
class CvServiceImpl(
    private val cvMapper: CvMapper,
    private val cvSectionMapper: CvSectionMapper,
    private val templateService: TemplateService,
    private val objectMapper: ObjectMapper,
) : CvService {
    override fun listByUser(userId: Long): List<CvSummaryDto> =
        cvMapper.selectList(
            QueryWrapper<Cv>()
                .eq("user_id", userId)
                .eq("is_deleted", false)
                .orderByDesc("updated_at", "id"),
        ).map(::toSummaryDto)

    override fun getById(userId: Long, cvId: Long): CvDetailResponse = buildCvDetail(findOwnedCv(userId, cvId))

    override fun updateMetadata(userId: Long, cvId: Long, request: UpdateCvRequest): CvDetailResponse {
        val cv = findOwnedCv(userId, cvId)
        val normalizedTitle = request.title?.trim()?.takeIf { it.isNotEmpty() }
        if (request.title != null && normalizedTitle == null) {
            throw IllegalArgumentException("cv title cannot be blank")
        }

        val normalizedTemplateKey = request.templateKey?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        if (request.templateKey != null && normalizedTemplateKey == null) {
            throw IllegalArgumentException("template key cannot be blank")
        }
        if (normalizedTemplateKey != null && !templateService.existsActiveTemplate(normalizedTemplateKey)) {
            throw IllegalArgumentException("template not found")
        }

        val normalizedSlug = if (request.slug != null) normalizeSlug(request.slug) else cv.slug
        val nextIsPublic = request.isPublic ?: cv.isPublic
        if (nextIsPublic && normalizedSlug.isNullOrBlank()) {
            throw IllegalArgumentException("slug is required when cv is public")
        }
        if (!normalizedSlug.isNullOrBlank()) {
            ensureSlugAvailable(normalizedSlug, cv.id ?: 0L)
        }

        normalizedTitle?.let { cv.title = it }
        normalizedTemplateKey?.let { cv.templateKey = it }
        cv.isPublic = nextIsPublic
        cv.slug = normalizedSlug
        cvMapper.updateById(cv)

        return buildCvDetail(findOwnedCv(userId, cvId))
    }

    private fun findOwnedCv(userId: Long, cvId: Long): Cv =
        cvMapper.selectOne(
            QueryWrapper<Cv>()
                .eq("id", cvId)
                .eq("user_id", userId)
                .eq("is_deleted", false)
                .last("LIMIT 1"),
        ) ?: throw ResourceNotFoundException("cv not found")

    private fun buildCvDetail(cv: Cv): CvDetailResponse {
        val cvId = cv.id ?: throw ResourceNotFoundException("cv not found")
        val sections = cvSectionMapper.selectList(
            QueryWrapper<CvSection>()
                .eq("cv_id", cvId)
                .eq("is_deleted", false)
                .orderByAsc("sort_order", "id"),
        )

        return CvDetailResponse(
            cv = CvDetailDto(
                id = cvId,
                title = cv.title,
                templateKey = cv.templateKey,
                isPublic = cv.isPublic,
                slug = cv.slug,
                updatedAt = cv.updatedAt,
            ),
            sections = sections.map { section ->
                CvSectionDto(
                    id = section.id ?: 0L,
                    sectionType = section.sectionType,
                    sortOrder = section.sortOrder,
                    title = section.title,
                    content = objectMapper.readTree(section.content),
                )
            },
        )
    }

    private fun normalizeSlug(rawSlug: String): String? {
        val normalized = rawSlug.trim().lowercase()
        if (normalized.isEmpty()) return null
        if (!SLUG_PATTERN.matches(normalized)) {
            throw IllegalArgumentException("invalid slug")
        }
        return normalized
    }

    private fun ensureSlugAvailable(slug: String, currentCvId: Long) {
        val existing = cvMapper.selectOne(
            QueryWrapper<Cv>()
                .eq("slug", slug)
                .eq("is_deleted", false)
                .ne("id", currentCvId)
                .last("LIMIT 1"),
        )
        if (existing != null) {
            throw IllegalArgumentException("slug is already in use")
        }
    }

    private fun toSummaryDto(cv: Cv) = CvSummaryDto(
        id = cv.id ?: 0L,
        title = cv.title,
        templateKey = cv.templateKey,
        isPublic = cv.isPublic,
        slug = cv.slug,
        updatedAt = cv.updatedAt,
    )

    companion object {
        private val SLUG_PATTERN = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")
    }
}
