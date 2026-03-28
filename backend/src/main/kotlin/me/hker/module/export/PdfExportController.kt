package me.hker.module.export

import me.hker.common.CurrentUserResolver
import me.hker.module.export.service.PdfExportService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me/cvs")
class PdfExportController(
    private val pdfExportService: PdfExportService,
    private val currentUserResolver: CurrentUserResolver,
) {
    @PostMapping("/{cvId}/export/pdf")
    fun exportPdf(@PathVariable cvId: Long): ResponseEntity<ByteArray> {
        val userId = currentUserResolver.resolve(null)
        val pdfBytes = pdfExportService.exportPdf(userId, cvId)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline().filename("cv-$cvId.pdf").build().toString(),
            )
            .body(pdfBytes)
    }
}
