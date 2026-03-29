package me.hker.cv

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.hker.common.ResourceNotFoundException
import me.hker.module.cv.entity.Cv
import me.hker.module.cv.entity.CvSection
import me.hker.module.cv.mapper.CvMapper
import me.hker.module.cv.mapper.CvSectionMapper
import me.hker.module.cv.service.impl.PublicCvServiceImpl
import me.hker.module.user.entity.User
import me.hker.module.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class PublicCvServiceTest {
    private val userService = mock<UserService>()
    private val cvMapper = mock<CvMapper>()
    private val cvSectionMapper = mock<CvSectionMapper>()
    private val service = PublicCvServiceImpl(
        userService = userService,
        cvMapper = cvMapper,
        cvSectionMapper = cvSectionMapper,
        objectMapper = jacksonObjectMapper(),
    )

    @Test
    fun `get public profile returns only public cv summaries`() {
        whenever(userService.findByEmail("alice@example.com")).thenReturn(
            user(id = 1L, email = "alice@example.com", displayName = "Alice"),
        )
        whenever(cvMapper.selectList(any<QueryWrapper<Cv>>())).thenReturn(
            listOf(
                cv(
                    id = 10L,
                    title = "Alice Product Resume",
                    slug = "product-resume",
                    templateKey = "minimal",
                ),
            ),
        )

        val profile = service.getPublicProfile("alice@example.com")

        assertEquals("alice@example.com", profile.user.email)
        assertEquals("Alice", profile.user.displayName)
        assertEquals(1, profile.cvs.size)
        assertEquals("product-resume", profile.cvs.first().slug)
        assertEquals("minimal", profile.cvs.first().templateKey)
    }

    @Test
    fun `get public cv returns typed sections with parsed json content`() {
        whenever(userService.findByEmail("alice@example.com")).thenReturn(
            user(id = 1L, email = "alice@example.com", displayName = "Alice"),
        )
        whenever(cvMapper.selectOne(any<QueryWrapper<Cv>>())).thenReturn(
            cv(
                id = 10L,
                title = "Alice Product Resume",
                slug = "product-resume",
                templateKey = "modern",
            ),
        )
        whenever(cvSectionMapper.selectList(any<QueryWrapper<CvSection>>())).thenReturn(
            listOf(
                CvSection().apply {
                    id = 20L
                    cvId = 10L
                    sectionType = "summary"
                    sortOrder = 0
                    title = "Summary"
                    content = """{"text":"Product-minded full-stack engineer"}"""
                },
            ),
        )

        val detail = service.getPublicCv("alice@example.com", "product-resume")

        assertEquals("alice@example.com", detail.user.email)
        assertEquals("modern", detail.cv.templateKey)
        assertEquals(1, detail.sections.size)
        assertEquals("summary", detail.sections.first().sectionType)
        assertEquals("Product-minded full-stack engineer", detail.sections.first().content["text"].asText())
    }

    @Test
    fun `get public cv throws not found when email is unknown`() {
        whenever(userService.findByEmail("ghost@example.com")).thenReturn(null)

        val error = assertThrows(ResourceNotFoundException::class.java) {
            service.getPublicCv("ghost@example.com", "missing")
        }

        assertEquals("public user not found", error.message)
    }

    private fun user(
        id: Long,
        email: String,
        displayName: String,
    ) = User().apply {
        this.id = id
        this.displayName = displayName
        this.email = email
        inviteCode = "INV-ALICE"
    }

    private fun cv(
        id: Long,
        title: String,
        slug: String,
        templateKey: String,
    ) = Cv().apply {
        this.id = id
        this.title = title
        this.slug = slug
        this.templateKey = templateKey
        isPublic = true
        updatedAt = LocalDateTime.of(2026, 3, 28, 9, 0)
    }
}
