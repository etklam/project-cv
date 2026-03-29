package me.hker.module.cv.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import me.hker.common.AccessDeniedException
import me.hker.common.ResourceNotFoundException
import me.hker.config.AppBusinessProperties
import me.hker.module.credit.service.CreditService
import me.hker.module.cv.dto.CreateCvRequest
import me.hker.module.cv.dto.CvDetailDto
import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.cv.dto.CvSectionDto
import me.hker.module.cv.dto.CvSectionPayload
import me.hker.module.cv.dto.CvSummaryDto
import me.hker.module.cv.dto.SaveCvDraftRequest
import me.hker.module.cv.dto.UpdateCvSectionsRequest
import me.hker.module.cv.dto.UpdateCvRequest
import me.hker.module.cv.entity.Cv
import me.hker.module.cv.entity.CvSection
import me.hker.module.cv.mapper.CvMapper
import me.hker.module.cv.mapper.CvSectionMapper
import me.hker.module.cv.service.CvService
import me.hker.module.template.service.TemplateService
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CvServiceImpl(
    private val cvMapper: CvMapper,
    private val cvSectionMapper: CvSectionMapper,
    private val templateService: TemplateService,
    private val creditService: CreditService,
    private val appBusinessProperties: AppBusinessProperties,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
) : CvService {
    override fun listByUser(userId: Long): List<CvSummaryDto> =
        cvMapper.selectList(
            QueryWrapper<Cv>()
                .eq("user_id", userId)
                .eq("is_deleted", false)
                .orderByDesc("updated_at", "id"),
        ).map(::toSummaryDto)

    override fun getById(userId: Long, cvId: Long): CvDetailResponse = buildCvDetail(findOwnedCv(userId, cvId))

    override fun create(userId: Long, request: CreateCvRequest): CvDetailResponse {
        val normalizedTitle = request.title.trim().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("cv title cannot be blank")
        val normalizedTemplateKey = request.templateKey.trim().lowercase().takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("template key cannot be blank")
        val template = templateService.getActiveTemplate(normalizedTemplateKey)
            ?: throw IllegalArgumentException("template not found")
        val normalizedSlug = normalizeSlug(request.slug ?: "")
        if (request.isPublic && normalizedSlug.isNullOrBlank()) {
            throw IllegalArgumentException("slug is required when cv is public")
        }
        if (!normalizedSlug.isNullOrBlank()) {
            ensureSlugAvailable(normalizedSlug, 0L)
        }

        val cv = Cv().apply {
            this.userId = userId
            this.title = normalizedTitle
            this.templateKey = template.key
            this.isPublic = request.isPublic
            this.slug = normalizedSlug
        }
        cvMapper.insert(cv)

        val newCvId = cv.id ?: throw IllegalStateException("cv id missing after insert")

        // Replace sections first - this could fail
        replaceSections(newCvId, if (request.sections.isNotEmpty()) request.sections else defaultSections())

        // Deduct credits AFTER all DB operations succeed
        val createCost = appBusinessProperties.credit.createCvCost
        if (createCost > 0) {
            creditService.deduct(userId, createCost, "CREATE_CV", "CV", newCvId, "create cv")
        }

        return buildCvDetail(findOwnedCv(userId, newCvId))
    }

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
        val nextTemplate = normalizedTemplateKey?.let {
            templateService.getActiveTemplate(it) ?: throw IllegalArgumentException("template not found")
        }

        val normalizedSlug = if (request.slug != null) normalizeSlug(request.slug) else cv.slug
        val nextIsPublic = request.isPublic ?: cv.isPublic
        if (nextIsPublic && normalizedSlug.isNullOrBlank()) {
            throw IllegalArgumentException("slug is required when cv is public")
        }
        if (!normalizedSlug.isNullOrBlank()) {
            ensureSlugAvailable(normalizedSlug, cv.id ?: 0L)
        }

        if (
            nextTemplate != null &&
            nextTemplate.key != cv.templateKey &&
            nextTemplate.creditCost > 0
        ) {
            creditService.deduct(userId, nextTemplate.creditCost, "SWITCH_TEMPLATE", "CV", cv.id, "switch template")
        }

        normalizedTitle?.let { cv.title = it }
        nextTemplate?.let { cv.templateKey = it.key }
        cv.isPublic = nextIsPublic
        cv.slug = normalizedSlug
        cvMapper.updateById(cv)

        return buildCvDetail(findOwnedCv(userId, cvId))
    }

    override fun updateSections(userId: Long, cvId: Long, request: UpdateCvSectionsRequest): CvDetailResponse {
        findOwnedCv(userId, cvId)
        replaceSections(cvId, request.sections)
        return buildCvDetail(findOwnedCv(userId, cvId))
    }

    override fun saveDraft(userId: Long, cvId: Long, request: SaveCvDraftRequest): CvDetailResponse {
        updateMetadata(
            userId,
            cvId,
            UpdateCvRequest(
                title = request.title,
                templateKey = request.templateKey,
                isPublic = request.isPublic,
                slug = request.slug,
            ),
        )
        replaceSections(cvId, request.sections)
        return buildCvDetail(findOwnedCv(userId, cvId))
    }

    override fun delete(userId: Long, cvId: Long) {
        val cv = findOwnedCv(userId, cvId)
        cv.isDeleted = true
        cvMapper.updateById(cv)

        cvSectionMapper.selectList(
            QueryWrapper<CvSection>()
                .eq("cv_id", cvId)
                .eq("is_deleted", false),
        ).forEach { section ->
            section.isDeleted = true
            cvSectionMapper.updateById(section)
        }
    }

    private fun findOwnedCv(userId: Long, cvId: Long): Cv {
        // First check if CV exists (regardless of owner)
        val cv = cvMapper.selectOne(
            QueryWrapper<Cv>()
                .eq("id", cvId)
                .eq("is_deleted", false)
                .last("LIMIT 1"),
        )

        if (cv == null) {
            throw ResourceNotFoundException("cv not found")
        }

        // Explicit ownership check
        if (cv.userId != userId) {
            throw AccessDeniedException("You don't own this CV")
        }

        return cv
    }

    private fun buildCvDetail(cv: Cv): CvDetailResponse {
        val cvId = cv.id ?: throw ResourceNotFoundException("cv not found")
        val owner = userService.findById(cv.userId ?: 0L)
            ?: throw ResourceNotFoundException("cv owner not found")
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
                email = owner.email,
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

    private fun replaceSections(cvId: Long, sections: List<CvSectionPayload>) {
        cvSectionMapper.selectList(
            QueryWrapper<CvSection>()
                .eq("cv_id", cvId)
                .eq("is_deleted", false),
        ).forEach { section ->
            section.isDeleted = true
            cvSectionMapper.updateById(section)
        }

        sections.sortedBy { it.sortOrder }.forEach { payload ->
            val section = CvSection().apply {
                this.cvId = cvId
                this.sectionType = payload.sectionType
                this.sortOrder = payload.sortOrder
                this.title = payload.title
                this.content = objectMapper.writeValueAsString(payload.content)
            }
            cvSectionMapper.insert(section)
        }
    }

    private fun defaultSections(): List<CvSectionPayload> = listOf(
        CvSectionPayload(
            sectionType = "summary",
            sortOrder = 0,
            title = "Summary",
            content = objectMapper.readTree("""{"text":""}"""),
        ),
        CvSectionPayload(
            sectionType = "experience",
            sortOrder = 1,
            title = "Experience",
            content = objectMapper.readTree("""{"items":[]}"""),
        ),
        CvSectionPayload(
            sectionType = "education",
            sortOrder = 2,
            title = "Education",
            content = objectMapper.readTree("""{"items":[]}"""),
        ),
        CvSectionPayload(
            sectionType = "skills",
            sortOrder = 3,
            title = "Skills",
            content = objectMapper.readTree("""{"items":[]}"""),
        ),
    )

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
