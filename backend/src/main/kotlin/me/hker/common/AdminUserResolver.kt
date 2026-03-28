package me.hker.common

import me.hker.module.auth.AuthenticatedUserPrincipal
import me.hker.module.auth.AuthenticationException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

interface AdminUserResolver {
    fun resolveAdmin(): Long
}

@Component
class DefaultAdminUserResolver : AdminUserResolver {
    override fun resolveAdmin(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        if (
            authentication == null ||
            !authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken
        ) {
            throw AuthenticationException("authentication is required")
        }

        val principal = authentication.principal
        if (principal is AuthenticatedUserPrincipal) {
            if (principal.role != "ADMIN") {
                throw AuthenticationException("admin access required")
            }
            return principal.userId
        }

        throw AuthenticationException("authenticated user is invalid")
    }
}
