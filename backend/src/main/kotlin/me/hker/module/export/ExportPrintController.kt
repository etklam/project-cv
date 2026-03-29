package me.hker.module.export

import me.hker.common.R
import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.export.service.PdfExportService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/export/print/cvs")
class ExportPrintController(
    private val pdfExportService: PdfExportService,
) {
    @GetMapping("/{cvId}")
    fun getExportCv(
        @PathVariable cvId: Long,
        @RequestParam token: String,
    ): R<CvDetailResponse> = R.ok(pdfExportService.getExportCv(token, cvId))
}
