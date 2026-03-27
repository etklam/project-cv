package me.hker.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import me.hker.module.auth.AuthenticatedUserPrincipal

class CurrentUserResolverTest {
    private val resolver: CurrentUserResolver = DefaultCurrentUserResolver()

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `returns authenticated principal user id`() {
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken.authenticated(
            AuthenticatedUserPrincipal(42L),
            null,
            emptyList(),
        )

        assertEquals(42L, resolver.resolve(null))
    }

    @Test
    fun `throws when authentication is missing`() {
        SecurityContextHolder.clearContext()

        assertThrows(InsufficientAuthenticationException::class.java) {
            resolver.resolve(null)
        }
    }
}
