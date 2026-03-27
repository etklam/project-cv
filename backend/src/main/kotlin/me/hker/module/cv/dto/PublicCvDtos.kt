package me.hker.module.cv.dto

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

data class PublicProfileUserDto(
    val username: String,
    val displayName: String,
    val avatarPath: String?,
)

data class PublicCvSummaryDto(
    val id: Long,
    val title: String,
    val slug: String,
    val templateKey: String,
    val updatedAt: LocalDateTime?,
)

data class PublicProfileResponse(
    val user: PublicProfileUserDto,
    val cvs: List<PublicCvSummaryDto>,
)

data class PublicCvDto(
    val id: Long,
    val title: String,
    val slug: String,
    val templateKey: String,
    val updatedAt: LocalDateTime?,
)

data class PublicCvSectionDto(
    val id: Long,
    val sectionType: String,
    val sortOrder: Int,
    val title: String?,
    val content: JsonNode,
)

data class PublicCvDetailResponse(
    val user: PublicProfileUserDto,
    val cv: PublicCvDto,
    val sections: List<PublicCvSectionDto>,
)
