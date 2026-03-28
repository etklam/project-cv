package me.hker.module.export.service

interface PdfExportService {
    fun exportPdf(userId: Long, cvId: Long): ByteArray
}
