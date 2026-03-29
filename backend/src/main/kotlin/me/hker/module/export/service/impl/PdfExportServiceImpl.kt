package me.hker.module.export.service.impl

import me.hker.common.InsufficientCreditsException
import me.hker.config.AppBusinessProperties
import me.hker.module.auth.JwtUtil
import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.credit.service.CreditService
import me.hker.module.cv.service.CvService
import me.hker.module.export.integration.PdfRendererClient
import me.hker.module.export.service.PdfExportService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PdfExportServiceImpl(
    private val cvService: CvService,
    private val creditService: CreditService,
    private val pdfRendererClient: PdfRendererClient,
    private val appBusinessProperties: AppBusinessProperties,
    private val jwtUtil: JwtUtil,
) : PdfExportService {
    private val logger = LoggerFactory.getLogger(PdfExportServiceImpl::class.java)

    @Transactional
    override fun exportPdf(userId: Long, cvId: Long): ByteArray {
        cvService.getById(userId, cvId)

        val exportCost = appBusinessProperties.credit.pdfExportCost
        if (exportCost > 0 && !creditService.hasEnoughCredits(userId, exportCost)) {
            throw InsufficientCreditsException()
        }

        val pdfBytes = try {
            val exportToken = jwtUtil.generateExportToken(userId, cvId)
            pdfRendererClient.renderPdf(
                "${appBusinessProperties.export.frontendBaseUrl.removeSuffix("/")}/print/cvs/$cvId?token=$exportToken",
            )
        } catch (e: Exception) {
            logger.error("PDF generation failed for cvId=$cvId", e)
            throw RuntimeException("Failed to generate PDF: ${e.message}", e)
        }

        if (exportCost > 0) {
            creditService.deduct(userId, exportCost, "PDF_EXPORT", "CV", cvId, "pdf export")
        }

        return pdfBytes
    }

    override fun getExportCv(token: String, cvId: Long): CvDetailResponse {
        val userId = jwtUtil.getUserId(token)
        require(jwtUtil.isExportToken(token, userId, cvId)) { "invalid export token" }
        return cvService.getById(userId, cvId)
    }
}
