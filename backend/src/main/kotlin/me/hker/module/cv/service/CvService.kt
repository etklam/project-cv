package me.hker.module.cv.service

import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.cv.dto.CvSummaryDto
import me.hker.module.cv.dto.UpdateCvRequest
import org.springframework.transaction.annotation.Transactional

interface CvService {
    fun listByUser(userId: Long): List<CvSummaryDto>
    fun getById(userId: Long, cvId: Long): CvDetailResponse

    @Transactional
    fun updateMetadata(userId: Long, cvId: Long, request: UpdateCvRequest): CvDetailResponse
}
