package me.hker.module.admin.dto

import java.time.LocalDateTime

// User Management DTOs
data class AdminUserDetailDto(
    val id: Long,
    val email: String,
    val displayName: String,
    val username: String?,
    val role: String,
    val creditBalance: Int,
    val inviteCode: String,
    val onboardingStatus: String,
    val locale: String,
    val createdAt: LocalDateTime,
    val isActive: Boolean,
)

data class PaginatedUserResponse(
    val users: List<AdminUserDetailDto>,
    val total: Long,
    val page: Int,
    val size: Int,
)

data class UpdateUserRoleRequest(val role: String)

data class AdjustCreditsRequest(val delta: Int, val reason: String?, val idempotencyKey: String? = null)

// PromoCode Management DTOs
data class AdminPromoCodeDto(
    val id: Long,
    val code: String,
    val campaignKey: String?,
    val rewardType: String,
    val rewardValue: Int,
    val maxRedemptions: Int?,
    val currentRedemptions: Int,
    val startsAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
)

data class PaginatedPromoCodeResponse(
    val promoCodes: List<AdminPromoCodeDto>,
    val total: Long,
    val page: Int,
    val size: Int,
)

data class CreatePromoCodeRequest(
    val code: String,
    val campaignKey: String?,
    val rewardType: String = "CREDIT_FIXED",
    val rewardValue: Int,
    val maxRedemptions: Int?,
    val startsAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
)

data class UpdatePromoCodeRequest(
    val campaignKey: String?,
    val maxRedemptions: Int?,
    val startsAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
    val isActive: Boolean?,
)

data class PromoCodeStatsDto(
    val totalRedemptions: Int,
    val uniqueUsers: Int,
    val totalCreditsAwarded: Int,
)

// Template Management DTOs
data class AdminTemplateDto(
    val id: Long,
    val componentKey: String,
    val displayNameI18n: String,
    val descriptionI18n: String?,
    val previewImage: String?,
    val creditCost: Int,
    val isActive: Boolean,
    val sortOrder: Int,
    val createdAt: LocalDateTime,
)

data class CreateTemplateRequest(
    val componentKey: String,
    val displayNameI18n: String,
    val descriptionI18n: String?,
    val previewImage: String?,
    val creditCost: Int,
    val sortOrder: Int,
)

data class UpdateTemplateRequest(
    val displayNameI18n: String?,
    val descriptionI18n: String?,
    val previewImage: String?,
    val creditCost: Int?,
    val sortOrder: Int?,
)

data class ToggleTemplateStatusRequest(
    val active: Boolean,
)

// Credit Transaction DTOs
data class AdminCreditTransactionDto(
    val id: Long,
    val userId: Long,
    val userEmail: String?,
    val userName: String?,
    val amount: Int,
    val balanceAfter: Int,
    val type: String,
    val referenceType: String?,
    val referenceId: Long?,
    val description: String?,
    val createdAt: LocalDateTime,
)

data class PaginatedTransactionResponse(
    val transactions: List<AdminCreditTransactionDto>,
    val total: Long,
    val page: Int,
    val size: Int,
)
