package me.hker.export

import me.hker.common.InsufficientCreditsException
import me.hker.config.AppBusinessProperties
import me.hker.module.auth.JwtUtil
import me.hker.module.credit.service.CreditService
import me.hker.module.cv.dto.CvDetailDto
import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.cv.service.CvService
import me.hker.module.export.integration.PdfRendererClient
import me.hker.module.export.service.impl.PdfExportServiceImpl
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class PdfExportServiceTest {
    private val cvService = mock<CvService>()
    private val creditService = mock<CreditService>()
    private val pdfRendererClient = mock<PdfRendererClient>()
    private val jwtUtil = mock<JwtUtil>()
    private val service = PdfExportServiceImpl(
        cvService = cvService,
        creditService = creditService,
        pdfRendererClient = pdfRendererClient,
        jwtUtil = jwtUtil,
        appBusinessProperties = AppBusinessProperties(
            credit = AppBusinessProperties.Credit(pdfExportCost = 15),
            export = AppBusinessProperties.Export(
                rendererBaseUrl = "http://renderer:3100",
                frontendBaseUrl = "http://frontend:5173",
            ),
        ),
    )

    @Test
    fun `export rejects insufficient credits before calling renderer`() {
        whenever(cvService.getById(1L, 3L)).thenReturn(detail(3L))
        whenever(creditService.hasEnoughCredits(1L, 15)).thenReturn(false)

        assertThrows(InsufficientCreditsException::class.java) {
            service.exportPdf(1L, 3L)
        }

        verify(pdfRendererClient, never()).renderPdf(any())
        verify(creditService, never()).deduct(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `export does not deduct credits when renderer fails`() {
        whenever(cvService.getById(1L, 3L)).thenReturn(detail(3L))
        whenever(creditService.hasEnoughCredits(1L, 15)).thenReturn(true)
        whenever(jwtUtil.generateExportToken(1L, 3L)).thenReturn("signed-export-token")
        whenever(pdfRendererClient.renderPdf("http://frontend:5173/print/cvs/3?token=signed-export-token"))
            .thenThrow(IllegalStateException("renderer down"))

        assertThrows(RuntimeException::class.java) {
            service.exportPdf(1L, 3L)
        }

        verify(creditService, never()).deduct(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `export deducts credits after successful renderer response`() {
        val pdfBytes = "%PDF-test".toByteArray()
        whenever(cvService.getById(1L, 3L)).thenReturn(detail(3L))
        whenever(creditService.hasEnoughCredits(1L, 15)).thenReturn(true)
        whenever(jwtUtil.generateExportToken(1L, 3L)).thenReturn("signed-export-token")
        whenever(pdfRendererClient.renderPdf("http://frontend:5173/print/cvs/3?token=signed-export-token"))
            .thenReturn(pdfBytes)

        val result = service.exportPdf(1L, 3L)

        assertArrayEquals(pdfBytes, result)
        verify(creditService).deduct(1L, 15, "PDF_EXPORT", "CV", 3L, "pdf export")
    }

    private fun detail(cvId: Long) = CvDetailResponse(
        cv = CvDetailDto(
            id = cvId,
            title = "Resume",
            templateKey = "minimal",
            isPublic = false,
            slug = null,
            username = "alice",
            updatedAt = LocalDateTime.of(2026, 3, 28, 12, 0),
        ),
        sections = emptyList(),
    )
}
