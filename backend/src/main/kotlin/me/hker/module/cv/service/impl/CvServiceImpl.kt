package me.hker.module.cv.service.impl

import me.hker.module.cv.service.CvService
import org.springframework.stereotype.Service

@Service
class CvServiceImpl : CvService {
    override fun listByUser(userId: Long): List<Map<String, Any?>> = emptyList()
}
