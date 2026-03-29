package me.hker.cv

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.hker.common.InsufficientCreditsException
import me.hker.config.AppBusinessProperties
import me.hker.common.ResourceNotFoundException
import me.hker.module.credit.service.CreditService
import me.hker.module.cv.dto.CreateCvRequest
import me.hker.module.cv.dto.CvSectionPayload
import me.hker.module.cv.dto.UpdateCvSectionsRequest
import me.hker.module.cv.dto.UpdateCvRequest
import me.hker.module.cv.entity.Cv
import me.hker.module.cv.entity.CvSection
import me.hker.module.cv.mapper.CvMapper
import me.hker.module.cv.mapper.CvSectionMapper
import me.hker.module.cv.service.impl.CvServiceImpl
import me.hker.module.template.dto.TemplateDto
import me.hker.module.template.service.TemplateService
import me.hker.module.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class CvServiceTest {
    private val cvMapper = mock<CvMapper>()
    private val cvSectionMapper = mock<CvSectionMapper>()
    private val templateService = mock<TemplateService>()
    private val creditService = mock<CreditService>()
    private val userService = mock<UserService>()
    private val service = CvServiceImpl(
        cvMapper = cvMapper,
        cvSectionMapper = cvSectionMapper,
        templateService = templateService,
        creditService = creditService,
        appBusinessProperties = AppBusinessProperties(),
        objectMapper = jacksonObjectMapper(),
        userService = userService,
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
        whenever(templateService.getActiveTemplate("modern")).thenReturn(template("modern", 5))
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
    fun `create deducts create cv credits and persists default sections`() {
        whenever(templateService.getActiveTemplate("minimal")).thenReturn(template("minimal", 0))
        whenever(cvMapper.insert(any<Cv>())).thenAnswer { invocation ->
            invocation.getArgument<Cv>(0).id = 9L
            1
        }
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(
            cv(
                id = 9L,
                title = "New CV",
                templateKey = "minimal",
                isPublic = false,
                slug = null,
            ),
        )
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(
            listOf(
                section(1L, 9L, "summary", 0, "Summary", """{"text":""}"""),
                section(2L, 9L, "experience", 1, "Experience", """{"items":[]}"""),
                section(3L, 9L, "education", 2, "Education", """{"items":[]}"""),
                section(4L, 9L, "skills", 3, "Skills", """{"items":[]}"""),
            ),
        )

        val created = service.create(1L, CreateCvRequest(title = "New CV"))

        verify(creditService).deduct(1L, 10, "CREATE_CV", "CV", 9L, "create cv")
        assertEquals("New CV", created.cv.title)
        assertEquals(4, created.sections.size)
    }

    @Test
    fun `updateMetadata deducts paid template cost when switching template`() {
        val existing = cv(
            id = 1L,
            title = "Resume",
            templateKey = "minimal",
            isPublic = false,
            slug = null,
        )
        val updated = cv(
            id = 1L,
            title = "Resume",
            templateKey = "modern",
            isPublic = false,
            slug = null,
        )
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(existing, updated)
        whenever(templateService.getActiveTemplate("modern")).thenReturn(template("modern", 5))
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(emptyList())

        service.updateMetadata(1L, 1L, UpdateCvRequest(templateKey = "modern"))

        verify(creditService).deduct(1L, 5, "SWITCH_TEMPLATE", "CV", 1L, "switch template")
    }

    @Test
    fun `updateMetadata skips credit deduction when template does not change`() {
        val existing = cv(
            id = 1L,
            title = "Resume",
            templateKey = "modern",
            isPublic = false,
            slug = null,
        )
        val updated = cv(
            id = 1L,
            title = "Resume",
            templateKey = "modern",
            isPublic = false,
            slug = null,
        )
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(existing, updated)
        whenever(templateService.getActiveTemplate("modern")).thenReturn(template("modern", 5))
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(emptyList())

        service.updateMetadata(1L, 1L, UpdateCvRequest(templateKey = "modern"))

        verify(creditService, never()).deduct(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `updateSections replaces existing sections`() {
        val existing = cv(
            id = 1L,
            title = "Resume",
            templateKey = "minimal",
            isPublic = false,
            slug = null,
        )
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(existing, existing)
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(
            listOf(section(9L, 1L, "summary", 0, "Summary", """{"text":"Old"}""")),
            listOf(section(10L, 1L, "summary", 0, "Summary", """{"text":"New"}""")),
        )

        val result = service.updateSections(
            1L,
            1L,
            UpdateCvSectionsRequest(
                sections = listOf(
                    CvSectionPayload(
                        sectionType = "summary",
                        sortOrder = 0,
                        title = "Summary",
                        content = jacksonObjectMapper().readTree("""{"text":"New"}"""),
                    ),
                ),
            ),
        )

        verify(cvSectionMapper).updateById(any<CvSection>())
        verify(cvSectionMapper).insert(any<CvSection>())
        assertEquals("New", result.sections.first().content["text"].asText())
    }

    @Test
    fun `delete marks cv and sections as deleted`() {
        val existing = cv(
            id = 1L,
            title = "Resume",
            templateKey = "minimal",
            isPublic = false,
            slug = null,
        )
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(existing)
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(
            listOf(section(9L, 1L, "summary", 0, "Summary", """{"text":"Old"}""")),
        )

        service.delete(1L, 1L)

        verify(cvMapper).updateById(any<Cv>())
        verify(cvSectionMapper).updateById(any<CvSection>())
    }

    @Test
    fun `create surfaces insufficient credits from credit service`() {
        whenever(templateService.getActiveTemplate("minimal")).thenReturn(template("minimal", 0))
        whenever(cvMapper.insert(any<Cv>())).thenAnswer { invocation ->
            invocation.getArgument<Cv>(0).id = 9L
            1
        }
        whenever(creditService.deduct(1L, 10, "CREATE_CV", "CV", 9L, "create cv"))
            .thenThrow(InsufficientCreditsException())

        assertThrows(InsufficientCreditsException::class.java) {
            service.create(1L, CreateCvRequest(title = "New CV"))
        }
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
        whenever(templateService.getActiveTemplate("ghost")).thenReturn(null)

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

    private fun section(
        id: Long,
        cvId: Long,
        sectionType: String,
        sortOrder: Int,
        title: String?,
        content: String,
    ) = CvSection().apply {
        this.id = id
        this.cvId = cvId
        this.sectionType = sectionType
        this.sortOrder = sortOrder
        this.title = title
        this.content = content
    }

    private fun template(key: String, creditCost: Int) = TemplateDto(
        key = key,
        displayName = key,
        description = key,
        creditCost = creditCost,
        previewImagePath = null,
    )
}
