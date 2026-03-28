package me.hker.module.export.integration

interface PdfRendererClient {
    fun renderPdf(url: String): ByteArray
}
