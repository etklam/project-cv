package me.hker.common

import me.hker.module.auth.AuthenticatedUserPrincipal
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

interface CurrentUserResolver {
    fun resolve(requestUserId: Long?): Long
}

@Component
class DefaultCurrentUserResolver : CurrentUserResolver {
    override fun resolve(requestUserId: Long?): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken
        ) {
            throw InsufficientAuthenticationException("authentication is required")
        }

        val principal = authentication.principal
        return when (principal) {
            is AuthenticatedUserPrincipal -> principal.userId
            is Number -> principal.toLong()
            is String -> principal.toLongOrNull()
            else -> null
        } ?: throw InsufficientAuthenticationException("authenticated user is invalid")
    }
}
