package me.hker.module.cv.service

import me.hker.module.cv.dto.PublicCvDetailResponse
import me.hker.module.cv.dto.PublicProfileResponse

interface PublicCvService {
    fun getPublicProfile(username: String): PublicProfileResponse
    fun getPublicCv(username: String, slug: String): PublicCvDetailResponse
}
