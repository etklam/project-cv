package me.hker.cv

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.hker.common.ResourceNotFoundException
import me.hker.module.cv.dto.UpdateCvRequest
import me.hker.module.cv.entity.Cv
import me.hker.module.cv.entity.CvSection
import me.hker.module.cv.mapper.CvMapper
import me.hker.module.cv.mapper.CvSectionMapper
import me.hker.module.cv.service.impl.CvServiceImpl
import me.hker.module.template.service.TemplateService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class CvServiceTest {
    private val cvMapper = mock<CvMapper>()
    private val cvSectionMapper = mock<CvSectionMapper>()
    private val templateService = mock<TemplateService>()
    private val service = CvServiceImpl(
        cvMapper = cvMapper,
        cvSectionMapper = cvSectionMapper,
        templateService = templateService,
        objectMapper = jacksonObjectMapper(),
    )

    @Test
    fun `listByUser returns typed summaries`() {
        whenever(cvMapper.selectList(any<QueryWrapper<Cv>>())).thenReturn(
            listOf(
                cv(
                    id = 1L,
                    title = "Alice Product Resume",
                    templateKey = "minimal",
                    isPublic = true,
                    slug = "product-resume",
                ),
            ),
        )

        val cvs = service.listByUser(1L)

        assertEquals(1, cvs.size)
        assertEquals("Alice Product Resume", cvs.first().title)
        assertEquals(true, cvs.first().isPublic)
    }

    @Test
    fun `getById returns cv detail with parsed sections`() {
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(
            cv(
                id = 1L,
                title = "Alice Product Resume",
                templateKey = "minimal",
                isPublic = false,
                slug = null,
            ),
        )
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(
            listOf(
                CvSection().apply {
                    id = 2L
                    cvId = 1L
                    sectionType = "summary"
                    sortOrder = 0
                    title = "Summary"
                    content = """{"text":"Draft"}"""
                },
            ),
        )

        val detail = service.getById(1L, 1L)

        assertEquals("Alice Product Resume", detail.cv.title)
        assertEquals("summary", detail.sections.first().sectionType)
        assertEquals("Draft", detail.sections.first().content["text"].asText())
    }

    @Test
    fun `updateMetadata validates public slug requirement and persists normalized values`() {
        val existing = cv(
            id = 1L,
            title = "Old Resume",
            templateKey = "minimal",
            isPublic = false,
            slug = null,
        )
        val updated = cv(
            id = 1L,
            title = "Public Resume",
            templateKey = "modern",
            isPublic = true,
            slug = "public-resume",
        )
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(existing, null, updated)
        whenever(templateService.existsActiveTemplate("modern")).thenReturn(true)
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(emptyList())

        val result = service.updateMetadata(
            1L,
            1L,
            UpdateCvRequest(
                title = "Public Resume",
                templateKey = "modern",
                isPublic = true,
                slug = "Public-Resume",
            ),
        )

        val updatedCv = argumentCaptor<Cv>()
        verify(cvMapper).updateById(updatedCv.capture())
        assertEquals("Public Resume", updatedCv.firstValue.title)
        assertEquals("modern", updatedCv.firstValue.templateKey)
        assertEquals(true, updatedCv.firstValue.isPublic)
        assertEquals("public-resume", updatedCv.firstValue.slug)
        assertEquals("Public Resume", result.cv.title)
        assertEquals("modern", result.cv.templateKey)
        assertEquals(true, result.cv.isPublic)
        assertEquals("public-resume", result.cv.slug)
    }

    @Test
    fun `updateMetadata rejects invalid slug or unknown template`() {
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(
            cv(
                id = 1L,
                title = "Resume",
                templateKey = "minimal",
                isPublic = false,
                slug = null,
            ),
        )
        whenever(templateService.existsActiveTemplate("ghost")).thenReturn(false)

        val badTemplate = assertThrows(IllegalArgumentException::class.java) {
            service.updateMetadata(1L, 1L, UpdateCvRequest(templateKey = "ghost"))
        }
        assertEquals("template not found", badTemplate.message)

        val badSlug = assertThrows(IllegalArgumentException::class.java) {
            service.updateMetadata(1L, 1L, UpdateCvRequest(isPublic = true, slug = "bad slug"))
        }
        assertEquals("invalid slug", badSlug.message)
    }

    @Test
    fun `updateMetadata allows clearing slug while private`() {
        val existing = cv(
            id = 1L,
            title = "Resume",
            templateKey = "minimal",
            isPublic = false,
            slug = "old-slug",
        )
        val updated = cv(
            id = 1L,
            title = "Resume",
            templateKey = "minimal",
            isPublic = false,
            slug = null,
        )
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(existing, updated, null)
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(emptyList())

        val result = service.updateMetadata(1L, 1L, UpdateCvRequest(slug = ""))

        assertNull(result.cv.slug)
    }

    @Test
    fun `getById throws when cv is missing`() {
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(null)

        val error = assertThrows(ResourceNotFoundException::class.java) {
            service.getById(1L, 99L)
        }

        assertEquals("cv not found", error.message)
    }

    private fun cv(
        id: Long,
        title: String,
        templateKey: String,
        isPublic: Boolean,
        slug: String?,
    ) = Cv().apply {
        this.id = id
        userId = 1L
        this.title = title
        this.templateKey = templateKey
        this.isPublic = isPublic
        this.slug = slug
        updatedAt = LocalDateTime.of(2026, 3, 28, 12, 0)
    }
}
