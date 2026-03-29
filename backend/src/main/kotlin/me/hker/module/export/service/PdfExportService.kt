package me.hker.module.export.service

import me.hker.module.cv.dto.CvDetailResponse

interface PdfExportService {
    fun exportPdf(userId: Long, cvId: Long): ByteArray
    fun getExportCv(token: String, cvId: Long): CvDetailResponse
}
