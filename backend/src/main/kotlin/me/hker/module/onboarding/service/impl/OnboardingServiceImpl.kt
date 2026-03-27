package me.hker.module.onboarding.service.impl

import me.hker.module.onboarding.service.OnboardingService
import org.springframework.stereotype.Service

@Service
class OnboardingServiceImpl : OnboardingService {
    override fun status(userId: Long): Map<String, Any?> {
        return mapOf("status" to "STEP_1", "nextStep" to "STEP_1")
    }
}
