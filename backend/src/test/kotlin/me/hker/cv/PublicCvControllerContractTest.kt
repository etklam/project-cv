package me.hker.cv

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import me.hker.module.auth.JwtAuthFilter
import me.hker.module.cv.PublicCvController
import me.hker.module.cv.dto.PublicCvDetailResponse
import me.hker.module.cv.dto.PublicCvDto
import me.hker.module.cv.dto.PublicCvSectionDto
import me.hker.module.cv.dto.PublicCvSummaryDto
import me.hker.module.cv.dto.PublicProfileResponse
import me.hker.module.cv.dto.PublicProfileUserDto
import me.hker.module.cv.service.PublicCvService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime

@WebMvcTest(PublicCvController::class)
@AutoConfigureMockMvc(addFilters = false)
class PublicCvControllerContractTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var publicCvService: PublicCvService

    @MockBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @Test
    fun `public profile endpoint returns typed payload`() {
        whenever(publicCvService.getPublicProfile("alice")).thenReturn(
            PublicProfileResponse(
                user = PublicProfileUserDto(
                    username = "alice",
                    displayName = "Alice",
                    avatarPath = "/uploads/alice.png",
                ),
                cvs = listOf(
                    PublicCvSummaryDto(
                        id = 1L,
                        title = "Alice Product Resume",
                        slug = "product-resume",
                        templateKey = "minimal",
                        updatedAt = LocalDateTime.of(2026, 3, 28, 9, 0),
                    ),
                ),
            ),
        )

        mockMvc.get("/api/v1/public/alice")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value(0) }
                jsonPath("$.data.user.username") { value("alice") }
                jsonPath("$.data.cvs[0].slug") { value("product-resume") }
                jsonPath("$.data.cvs[0].templateKey") { value("minimal") }
            }
    }

    @Test
    fun `public cv endpoint returns cv with sections`() {
        whenever(publicCvService.getPublicCv("alice", "product-resume")).thenReturn(
            PublicCvDetailResponse(
                user = PublicProfileUserDto(
                    username = "alice",
                    displayName = "Alice",
                    avatarPath = null,
                ),
                cv = PublicCvDto(
                    id = 1L,
                    title = "Alice Product Resume",
                    slug = "product-resume",
                    templateKey = "minimal",
                    updatedAt = LocalDateTime.of(2026, 3, 28, 9, 0),
                ),
                sections = listOf(
                    PublicCvSectionDto(
                        id = 11L,
                        sectionType = "summary",
                        sortOrder = 0,
                        title = "Summary",
                        content = ObjectNode(JsonNodeFactory.instance).put(
                            "text",
                            "Product-minded full-stack engineer",
                        ),
                    ),
                ),
            ),
        )

        mockMvc.get("/api/v1/public/alice/product-resume")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value(0) }
                jsonPath("$.data.cv.slug") { value("product-resume") }
                jsonPath("$.data.cv.templateKey") { value("minimal") }
                jsonPath("$.data.sections[0].sectionType") { value("summary") }
                jsonPath("$.data.sections[0].content.text") { value("Product-minded full-stack engineer") }
            }
    }
}
