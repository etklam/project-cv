package me.hker.export

import me.hker.common.CurrentUserResolver
import me.hker.common.InsufficientCreditsException
import me.hker.module.auth.JwtAuthFilter
import me.hker.module.export.PdfExportController
import me.hker.module.export.service.PdfExportService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(PdfExportController::class)
@AutoConfigureMockMvc(addFilters = false)
class PdfExportControllerContractTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var pdfExportService: PdfExportService

    @MockBean
    private lateinit var currentUserResolver: CurrentUserResolver

    @MockBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @Test
    fun `export endpoint returns pdf bytes`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(pdfExportService.exportPdf(1L, 7L)).thenReturn("%PDF-test".toByteArray())

        mockMvc.post("/api/v1/me/cvs/7/export/pdf")
            .andExpect {
                status { isOk() }
                header { string("Content-Type", "application/pdf") }
            }
    }

    @Test
    fun `export endpoint returns 402 when credits are insufficient`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(pdfExportService.exportPdf(1L, 7L)).thenThrow(InsufficientCreditsException())

        mockMvc.post("/api/v1/me/cvs/7/export/pdf")
            .andExpect {
                status { isPaymentRequired() }
            }
    }
}
