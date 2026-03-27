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
        whenever(userService.findByUsername("alice")).thenReturn(
            user(id = 1L, username = "alice", displayName = "Alice"),
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

        val profile = service.getPublicProfile("alice")

        assertEquals("alice", profile.user.username)
        assertEquals("Alice", profile.user.displayName)
        assertEquals(1, profile.cvs.size)
        assertEquals("product-resume", profile.cvs.first().slug)
        assertEquals("minimal", profile.cvs.first().templateKey)
    }

    @Test
    fun `get public cv returns typed sections with parsed json content`() {
        whenever(userService.findByUsername("alice")).thenReturn(
            user(id = 1L, username = "alice", displayName = "Alice"),
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

        val detail = service.getPublicCv("alice", "product-resume")

        assertEquals("alice", detail.user.username)
        assertEquals("modern", detail.cv.templateKey)
        assertEquals(1, detail.sections.size)
        assertEquals("summary", detail.sections.first().sectionType)
        assertEquals("Product-minded full-stack engineer", detail.sections.first().content["text"].asText())
    }

    @Test
    fun `get public cv throws not found when username is unknown`() {
        whenever(userService.findByUsername("ghost")).thenReturn(null)

        val error = assertThrows(ResourceNotFoundException::class.java) {
            service.getPublicCv("ghost", "missing")
        }

        assertEquals("public user not found", error.message)
    }

    private fun user(
        id: Long,
        username: String,
        displayName: String,
    ) = User().apply {
        this.id = id
        this.username = username
        this.displayName = displayName
        email = "$username@example.com"
        inviteCode = "INV-${username.uppercase()}"
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
