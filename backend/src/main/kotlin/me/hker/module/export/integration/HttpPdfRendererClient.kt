package me.hker.module.export.integration

import com.fasterxml.jackson.databind.ObjectMapper
import me.hker.config.AppBusinessProperties
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class HttpPdfRendererClient(
    private val objectMapper: ObjectMapper,
    private val appBusinessProperties: AppBusinessProperties,
) : PdfRendererClient {
    private val httpClient: HttpClient = HttpClient.newHttpClient()

    override fun renderPdf(url: String): ByteArray {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${appBusinessProperties.export.rendererBaseUrl.removeSuffix("/")}/render"))
            .header("Content-Type", "application/json")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(mapOf("url" to url)),
                ),
            )
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
        if (response.statusCode() != 200) {
            throw IllegalStateException("pdf renderer failed")
        }
        return response.body()
    }
}
