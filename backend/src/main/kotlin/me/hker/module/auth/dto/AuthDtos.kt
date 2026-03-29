package me.hker.module.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val locale: String? = null,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class ChangeLocaleRequest(
    val locale: String,
)

data class AuthUserDto(
    val id: Long?,
    val email: String,
    val username: String?,
    val displayName: String,
    val locale: String,
    val creditBalance: Int,
    val inviteCode: String,
    @JsonProperty("onboarding_status")
    val onboardingStatus: String,
    val role: String = "USER",
)

data class AuthUserResponse(
    val user: AuthUserDto,
)
