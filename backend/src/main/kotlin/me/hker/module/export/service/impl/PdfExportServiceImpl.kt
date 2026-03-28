package me.hker.module.export.service.impl

import me.hker.common.InsufficientCreditsException
import me.hker.config.AppBusinessProperties
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
            pdfRendererClient.renderPdf(
                "${appBusinessProperties.export.frontendBaseUrl.removeSuffix("/")}/print/cvs/$cvId",
            )
        } catch (e: Exception) {
            logger.error("PDF generation failed for cvId=$cvId", e)
            if (exportCost > 0) {
                // Refund the credits if PDF generation failed
                creditService.credit(userId, exportCost, "PDF_EXPORT_REFUND", "CV", cvId, "refund failed pdf export")
            }
            throw RuntimeException("Failed to generate PDF: ${e.message}", e)
        }

        if (exportCost > 0) {
            creditService.deduct(userId, exportCost, "PDF_EXPORT", "CV", cvId, "pdf export")
        }

        return pdfBytes
    }
}
