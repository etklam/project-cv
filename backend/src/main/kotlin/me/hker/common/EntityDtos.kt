package me.hker.common

import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.user.entity.User

fun User.toDto(): AuthUserDto {
    return AuthUserDto(
        id = id,
        email = email,
        username = username,
        displayName = displayName,
        locale = locale,
        creditBalance = creditBalance,
        inviteCode = inviteCode,
        onboardingStatus = onboardingStatus,
        role = role,
    )
}
