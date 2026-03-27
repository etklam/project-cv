package me.hker.contract

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ServiceInterfaceStructureTest {
    private fun assertServicePair(serviceClassName: String, implClassName: String) {
        val service = Class.forName(serviceClassName)
        val impl = Class.forName(implClassName)

        assertTrue(service.isInterface)
        assertTrue(service.isAssignableFrom(impl))
    }

    @Test
    fun `services should use interface plus impl structure`() {
        listOf(
            "me.hker.module.auth.service.AuthService" to "me.hker.module.auth.service.impl.AuthServiceImpl",
            "me.hker.module.credit.service.CreditService" to "me.hker.module.credit.service.impl.CreditServiceImpl",
            "me.hker.module.user.service.UserService" to "me.hker.module.user.service.impl.UserServiceImpl",
            "me.hker.module.onboarding.service.OnboardingService" to "me.hker.module.onboarding.service.impl.OnboardingServiceImpl",
            "me.hker.module.reward.service.RewardService" to "me.hker.module.reward.service.impl.RewardServiceImpl",
            "me.hker.module.reward.service.RewardSummaryService" to "me.hker.module.reward.service.impl.RewardSummaryServiceImpl",
            "me.hker.module.reward.service.RewardCodeRedemptionService" to "me.hker.module.reward.service.impl.RewardCodeRedemptionServiceImpl",
            "me.hker.module.reward.service.PromoCodeRewardRedemptionService" to "me.hker.module.reward.service.impl.PromoCodeRewardRedemptionServiceImpl",
            "me.hker.module.reward.service.InviteCodeRewardRedemptionService" to "me.hker.module.reward.service.impl.InviteCodeRewardRedemptionServiceImpl",
            "me.hker.module.template.service.TemplateService" to "me.hker.module.template.service.impl.TemplateServiceImpl",
            "me.hker.module.cv.service.CvService" to "me.hker.module.cv.service.impl.CvServiceImpl",
            "me.hker.module.cv.service.PublicCvService" to "me.hker.module.cv.service.impl.PublicCvServiceImpl",
        ).forEach { (serviceClassName, implClassName) ->
            assertServicePair(serviceClassName, implClassName)
        }
    }
}
