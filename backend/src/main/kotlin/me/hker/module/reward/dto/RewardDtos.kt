package me.hker.module.reward.dto

data class RewardSummaryDto(
    val balance: Int,
    val inviteCode: String,
    val inviteStats: InviteStatsDto,
    val promoRedemptions: Int,
    val inviteRedemption: InviteRedemptionDto?,
)

data class InviteStatsDto(
    val inviterRedemptions: Int,
    val totalInviterCredits: Int,
)

data class InviteRedemptionDto(
    val inviteCode: String,
    val inviterUserId: Long,
    val inviterDisplayName: String?,
    val inviteeReward: Int,
)

data class RedeemCodeRequest(
    val code: String,
)

data class RewardRedeemResultDto(
    val type: String,
    val code: String,
    val creditedAmount: Int,
    val balanceAfter: Int,
    val inviterReward: Int?,
    val inviteeReward: Int?,
    val message: String,
)
