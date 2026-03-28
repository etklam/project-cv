package me.hker.common

import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.user.entity.User

fun User.toDto(): AuthUserDto {
    return AuthUserDto(
        id = id,
        email = email,
        displayName = displayName,
        locale = locale,
        creditBalance = creditBalance,
        inviteCode = inviteCode,
        role = role,
    )
}
