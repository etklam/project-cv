package me.hker.module.admin.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.common.ResourceNotFoundException
import me.hker.module.admin.dto.*
import me.hker.module.admin.service.AdminPromoCodeService
import me.hker.module.reward.entity.PromoCode
import me.hker.module.reward.mapper.PromoCodeMapper
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminPromoCodeServiceImpl(
    private val promoCodeMapper: PromoCodeMapper,
    private val promoCodeRedemptionMapper: PromoCodeRedemptionMapper,
) : AdminPromoCodeService {

    override fun listPromoCodes(page: Int, size: Int): PaginatedPromoCodeResponse {
        val query = QueryWrapper<PromoCode>().eq("is_deleted", false)
            .orderByDesc("created_at")

        val promoCodePage: IPage<PromoCode> = promoCodeMapper.selectPage(Page(page.toLong(), size.toLong()), query)

        val promoCodes = promoCodePage.records.map { promoCode ->
            val redemptionCount = promoCodeRedemptionMapper.selectCount(
                QueryWrapper().eq("promo_code_id", promoCode.id)
            ).toInt()

            AdminPromoCodeDto(
                id = promoCode.id ?: 0L,
                code = promoCode.code,
                campaignKey = promoCode.campaignKey,
                rewardType = promoCode.rewardType,
                rewardValue = promoCode.rewardValue,
                maxRedemptions = promoCode.maxRedemptions,
                currentRedemptions = redemptionCount,
                startsAt = promoCode.startsAt,
                expiresAt = promoCode.expiresAt,
                isActive = promoCode.isActive,
                createdAt = promoCode.createdAt ?: java.time.LocalDateTime.now(),
            )
        }

        return PaginatedPromoCodeResponse(
            promoCodes = promoCodes,
            total = promoCodePage.total,
            page = page,
            size = size,
        )
    }

    @Transactional
    override fun createPromoCode(request: CreatePromoCodeRequest): AdminPromoCodeDto {
        val existing = promoCodeMapper.selectOne(
            QueryWrapper<PromoCode>().eq("code", request.code.uppercase()).eq("is_deleted", false)
        )
        if (existing != null) {
            throw IllegalArgumentException("PromoCode already exists: ${request.code}")
        }

        val promoCode = PromoCode().apply {
            code = request.code.uppercase()
            campaignKey = request.campaignKey
            rewardType = request.rewardType
            rewardValue = request.rewardValue
            maxRedemptions = request.maxRedemptions
            startsAt = request.startsAt
            expiresAt = request.expiresAt
            isActive = true
        }

        promoCodeMapper.insert(promoCode)

        return AdminPromoCodeDto(
            id = promoCode.id ?: 0L,
            code = promoCode.code,
            campaignKey = promoCode.campaignKey,
            rewardType = promoCode.rewardType,
            rewardValue = promoCode.rewardValue,
            maxRedemptions = promoCode.maxRedemptions,
            currentRedemptions = 0,
            startsAt = promoCode.startsAt,
            expiresAt = promoCode.expiresAt,
            isActive = promoCode.isActive,
            createdAt = promoCode.createdAt ?: java.time.LocalDateTime.now(),
        )
    }

    @Transactional
    override fun updatePromoCode(id: Long, request: UpdatePromoCodeRequest): AdminPromoCodeDto {
        val promoCode = promoCodeMapper.selectById(id)
            ?: throw ResourceNotFoundException("PromoCode not found: $id")

        request.campaignKey?.let { promoCode.campaignKey = it }
        request.maxRedemptions?.let { promoCode.maxRedemptions = it }
        request.startsAt?.let { promoCode.startsAt = it }
        request.expiresAt?.let { promoCode.expiresAt = it }
        request.isActive?.let { promoCode.isActive = it }

        promoCodeMapper.updateById(promoCode)

        val redemptionCount = promoCodeRedemptionMapper.selectCount(
            QueryWrapper().eq("promo_code_id", promoCode.id)
        ).toInt()

        return AdminPromoCodeDto(
            id = promoCode.id ?: 0L,
            code = promoCode.code,
            campaignKey = promoCode.campaignKey,
            rewardType = promoCode.rewardType,
            rewardValue = promoCode.rewardValue,
            maxRedemptions = promoCode.maxRedemptions,
            currentRedemptions = redemptionCount,
            startsAt = promoCode.startsAt,
            expiresAt = promoCode.expiresAt,
            isActive = promoCode.isActive,
            createdAt = promoCode.createdAt ?: java.time.LocalDateTime.now(),
        )
    }

    @Transactional
    override fun deletePromoCode(id: Long) {
        val promoCode = promoCodeMapper.selectById(id)
            ?: throw ResourceNotFoundException("PromoCode not found: $id")

        promoCode.isDeleted = true
        promoCodeMapper.updateById(promoCode)
    }

    override fun getPromoCodeStats(id: Long): PromoCodeStatsDto {
        val promoCode = promoCodeMapper.selectById(id)
            ?: throw ResourceNotFoundException("PromoCode not found: $id")

        val redemptions = promoCodeRedemptionMapper.selectList(
            QueryWrapper().eq("promo_code_id", id)
        )

        val uniqueUsers = redemptions.mapNotNull { it.userId }.distinct().count()
        val totalCredits = redemptions.sumOf { it.rewardValue }

        return PromoCodeStatsDto(
            totalRedemptions = redemptions.size,
            uniqueUsers = uniqueUsers,
            totalCreditsAwarded = totalCredits,
        )
    }
}
