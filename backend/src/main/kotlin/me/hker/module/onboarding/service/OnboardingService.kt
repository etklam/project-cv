package me.hker.module.onboarding.service

import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.onboarding.dto.OnboardingStatusResponse
import me.hker.module.onboarding.dto.OnboardingStep1Request
import me.hker.module.onboarding.dto.OnboardingStep2Request
import me.hker.module.onboarding.dto.OnboardingStep3Request

interface OnboardingService {
    fun status(userId: Long): OnboardingStatusResponse
    fun submitStep1(userId: Long, request: OnboardingStep1Request): AuthUserDto
    fun submitStep2(userId: Long, request: OnboardingStep2Request): AuthUserDto
    fun skipStep2(userId: Long): AuthUserDto
    fun submitStep3(userId: Long, request: OnboardingStep3Request): AuthUserDto
    fun isUsernameAvailable(username: String, currentUserId: Long? = null): Boolean
}
