package me.hker.module.cv.service

import me.hker.module.cv.dto.PublicCvDetailResponse
import me.hker.module.cv.dto.PublicProfileResponse

interface PublicCvService {
    fun getPublicProfile(email: String): PublicProfileResponse
    fun getPublicCv(email: String, slug: String): PublicCvDetailResponse
}
