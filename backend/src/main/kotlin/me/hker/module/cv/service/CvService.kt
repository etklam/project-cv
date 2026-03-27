package me.hker.module.cv.service

interface CvService {
    fun listByUser(userId: Long): List<Map<String, Any?>>
}
