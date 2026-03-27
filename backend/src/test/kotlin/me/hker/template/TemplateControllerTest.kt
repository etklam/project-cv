package me.hker.template

import me.hker.module.template.TemplateController
import me.hker.module.template.dto.TemplateDto
import me.hker.module.template.service.TemplateService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(TemplateController::class)
@AutoConfigureMockMvc(addFilters = false)
class TemplateControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var templateService: TemplateService

    @Test
    fun `list templates returns response contract`() {
        val templates = listOf(
            TemplateDto(
                key = "minimal",
                displayName = "Minimal",
                description = "Simple layout",
                creditCost = 0,
                previewImagePath = "/images/minimal.png",
            ),
        )
        `when`(templateService.listActiveTemplates()).thenReturn(templates)

        mockMvc.get("/api/v1/templates")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value(0) }
                jsonPath("$.data.templates[0].key") { value("minimal") }
                jsonPath("$.data.templates[0].creditCost") { value(0) }
                jsonPath("$.data.templates[0].displayName") { value("Minimal") }
            }
    }
}
