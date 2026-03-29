package me.hker.module.onboarding.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OnboardingStatusResponse(
    @JsonProperty("onboarding_status")
    val onboardingStatus: String,
)

data class OnboardingStep1Request(
    val displayName: String,
    val username: String,
)

data class OnboardingStep2Request(
    val industry: String,
    val years: String,
    val targetTitle: String,
    val leadership: String,
)

data class OnboardingStep3Request(
    val templateKey: String,
)

data class UsernameAvailabilityResponse(
    val available: Boolean,
)
