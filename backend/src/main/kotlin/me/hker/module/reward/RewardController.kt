package me.hker.module.reward

import me.hker.common.CurrentUserResolver
import me.hker.common.R
import me.hker.module.reward.dto.RedeemCodeRequest
import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.dto.RewardSummaryDto
import me.hker.module.reward.service.RewardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me/rewards")
class RewardController(
    private val rewardService: RewardService,
    private val currentUserResolver: CurrentUserResolver,
) {
    @GetMapping("/summary")
    fun summary(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
    ): R<RewardSummaryDto> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(rewardService.summary(resolvedUserId))
    }

    @PostMapping("/redeem")
    fun redeem(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @RequestBody request: RedeemCodeRequest,
    ): R<RewardRedeemResultDto> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(rewardService.redeem(resolvedUserId, request.code))
    }
}
