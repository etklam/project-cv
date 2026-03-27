package me.hker.module.cv.dto

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

data class CvSummaryDto(
    val id: Long,
    val title: String,
    val templateKey: String,
    val isPublic: Boolean,
    val slug: String?,
    val updatedAt: LocalDateTime?,
)

data class CvListResponse(
    val cvs: List<CvSummaryDto>,
)

data class CvDetailDto(
    val id: Long,
    val title: String,
    val templateKey: String,
    val isPublic: Boolean,
    val slug: String?,
    val updatedAt: LocalDateTime?,
)

data class CvSectionDto(
    val id: Long,
    val sectionType: String,
    val sortOrder: Int,
    val title: String?,
    val content: JsonNode,
)

data class CvDetailResponse(
    val cv: CvDetailDto,
    val sections: List<CvSectionDto>,
)

data class UpdateCvRequest(
    val title: String? = null,
    val templateKey: String? = null,
    val isPublic: Boolean? = null,
    val slug: String? = null,
)
