package me.hker.module.onboarding.service

interface OnboardingService {
    fun status(userId: Long): Map<String, Any?>
}
