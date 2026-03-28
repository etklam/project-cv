package me.hker.module.auth.dto

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
    val displayName: String,
    val locale: String,
    val creditBalance: Int,
    val inviteCode: String,
    val role: String = "USER",
)

data class AuthUserResponse(
    val user: AuthUserDto,
)
