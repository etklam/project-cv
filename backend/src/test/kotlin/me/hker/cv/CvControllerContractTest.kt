package me.hker.cv

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import me.hker.common.CurrentUserResolver
import me.hker.module.auth.JwtAuthFilter
import me.hker.common.InsufficientCreditsException
import me.hker.module.cv.CvController
import me.hker.module.cv.dto.CvDetailDto
import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.cv.dto.CvSectionDto
import me.hker.module.cv.dto.CvSummaryDto
import me.hker.module.cv.dto.CreateCvRequest
import me.hker.module.cv.dto.UpdateCvSectionsRequest
import me.hker.module.cv.dto.UpdateCvRequest
import me.hker.module.cv.service.CvService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDateTime

@WebMvcTest(CvController::class)
@AutoConfigureMockMvc(addFilters = false)
class CvControllerContractTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var cvService: CvService

    @MockBean
    private lateinit var currentUserResolver: CurrentUserResolver

    @MockBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @Test
    fun `list cvs returns typed response`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(cvService.listByUser(1L)).thenReturn(
            listOf(
                CvSummaryDto(
                    id = 1L,
                    title = "Alice Product Resume",
                    templateKey = "minimal",
                    isPublic = true,
                    slug = "product-resume",
                    updatedAt = LocalDateTime.of(2026, 3, 28, 10, 0),
                ),
            ),
        )

        mockMvc.get("/api/v1/me/cvs")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.cvs[0].title") { value("Alice Product Resume") }
                jsonPath("$.data.cvs[0].isPublic") { value(true) }
                jsonPath("$.data.cvs[0].slug") { value("product-resume") }
            }
    }

    @Test
    fun `get cv returns detail with sections`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(cvService.getById(1L, 1L)).thenReturn(detailResponse())

        mockMvc.get("/api/v1/me/cvs/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.cv.templateKey") { value("minimal") }
                jsonPath("$.data.sections[0].sectionType") { value("summary") }
                jsonPath("$.data.sections[0].content.text") { value("Draft") }
            }
    }

    @Test
    fun `update cv returns updated metadata`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(cvService.updateMetadata(1L, 1L, UpdateCvRequest(title = "Public Resume", isPublic = true, slug = "public-resume")))
            .thenReturn(
                detailResponse(
                    title = "Public Resume",
                    isPublic = true,
                    slug = "public-resume",
                ),
            )

        mockMvc.put("/api/v1/me/cvs/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Public Resume","isPublic":true,"slug":"public-resume"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.cv.title") { value("Public Resume") }
            jsonPath("$.data.cv.isPublic") { value(true) }
            jsonPath("$.data.cv.slug") { value("public-resume") }
        }
    }

    @Test
    fun `create cv returns created detail payload`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(
            cvService.create(
                1L,
                CreateCvRequest(
                    title = "New CV",
                    templateKey = "minimal",
                    isPublic = false,
                    slug = null,
                    sections = emptyList(),
                ),
            ),
        ).thenReturn(detailResponse(title = "New CV"))

        mockMvc.post("/api/v1/me/cvs") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"New CV","templateKey":"minimal"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.cv.title") { value("New CV") }
            jsonPath("$.data.cv.templateKey") { value("minimal") }
        }
    }

    @Test
    fun `update sections returns refreshed cv detail`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(
            cvService.updateSections(
                1L,
                1L,
                UpdateCvSectionsRequest(
                    sections = listOf(
                        me.hker.module.cv.dto.CvSectionPayload(
                            sectionType = "summary",
                            sortOrder = 0,
                            title = "Summary",
                            content = ObjectNode(JsonNodeFactory.instance).put("text", "Updated"),
                        ),
                    ),
                ),
            ),
        ).thenReturn(
            detailResponse().copy(
                sections = listOf(
                    CvSectionDto(
                        id = 11L,
                        sectionType = "summary",
                        sortOrder = 0,
                        title = "Summary",
                        content = ObjectNode(JsonNodeFactory.instance).put("text", "Updated"),
                    ),
                ),
            ),
        )

        mockMvc.put("/api/v1/me/cvs/1/sections") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"sections":[{"sectionType":"summary","sortOrder":0,"title":"Summary","content":{"text":"Updated"}}]}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.sections[0].content.text") { value("Updated") }
        }
    }

    @Test
    fun `delete cv returns unified ok response`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)

        mockMvc.delete("/api/v1/me/cvs/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value(0) }
                jsonPath("$.message") { value("OK") }
            }
    }

    @Test
    fun `create cv returns 402 when credits are insufficient`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(
            cvService.create(
                1L,
                CreateCvRequest(
                    title = "New CV",
                    templateKey = "minimal",
                    isPublic = false,
                    slug = null,
                    sections = emptyList(),
                ),
            ),
        ).thenThrow(InsufficientCreditsException())

        mockMvc.post("/api/v1/me/cvs") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"New CV","templateKey":"minimal"}"""
        }.andExpect {
            status { isPaymentRequired() }
        }
    }

    private fun detailResponse(
        title: String = "Alice Product Resume",
        isPublic: Boolean = false,
        slug: String? = null,
    ) = CvDetailResponse(
        cv = CvDetailDto(
            id = 1L,
            title = title,
            templateKey = "minimal",
            isPublic = isPublic,
            slug = slug,
            username = "alice",
            updatedAt = LocalDateTime.of(2026, 3, 28, 10, 0),
        ),
        sections = listOf(
            CvSectionDto(
                id = 11L,
                sectionType = "summary",
                sortOrder = 0,
                title = "Summary",
                content = ObjectNode(JsonNodeFactory.instance).put("text", "Draft"),
            ),
        ),
    )
}
