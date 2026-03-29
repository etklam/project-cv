package me.hker.module.cv.service

import me.hker.module.cv.dto.CvDetailResponse
import me.hker.module.cv.dto.CvSummaryDto
import me.hker.module.cv.dto.CreateCvRequest
import me.hker.module.cv.dto.SaveCvDraftRequest
import me.hker.module.cv.dto.UpdateCvSectionsRequest
import me.hker.module.cv.dto.UpdateCvRequest
import org.springframework.transaction.annotation.Transactional

interface CvService {
    fun listByUser(userId: Long): List<CvSummaryDto>
    fun getById(userId: Long, cvId: Long): CvDetailResponse

    @Transactional
    fun create(userId: Long, request: CreateCvRequest): CvDetailResponse

    @Transactional
    fun updateMetadata(userId: Long, cvId: Long, request: UpdateCvRequest): CvDetailResponse

    @Transactional
    fun updateSections(userId: Long, cvId: Long, request: UpdateCvSectionsRequest): CvDetailResponse

    @Transactional
    fun saveDraft(userId: Long, cvId: Long, request: SaveCvDraftRequest): CvDetailResponse

    @Transactional
    fun delete(userId: Long, cvId: Long)
}
