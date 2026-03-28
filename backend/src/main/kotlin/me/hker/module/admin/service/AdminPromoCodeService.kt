package me.hker.module.admin.service

import me.hker.module.admin.dto.*

interface AdminPromoCodeService {
    fun listPromoCodes(page: Int, size: Int): PaginatedPromoCodeResponse
    fun createPromoCode(request: CreatePromoCodeRequest): AdminPromoCodeDto
    fun updatePromoCode(id: Long, request: UpdatePromoCodeRequest): AdminPromoCodeDto
    fun deletePromoCode(id: Long)
    fun getPromoCodeStats(id: Long): PromoCodeStatsDto
}
