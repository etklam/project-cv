package me.hker.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import me.hker.module.auth.AuthenticatedUserPrincipal
import me.hker.module.auth.JwtAuthFilter
import me.hker.module.auth.JwtUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthFilterTest {
    // HS256 requires at least 256 bits (32 bytes)
    private val jwtUtil = JwtUtil("test-secret-key-32-bytes-long-for-hs256")
    private val filter = JwtAuthFilter(jwtUtil)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sets security context when token cookie is valid`() {
        val request = MockHttpServletRequest().apply {
            setCookies(Cookie("token", jwtUtil.generateToken(7L, "alice@example.com")))
        }

        filter.doFilter(request, MockHttpServletResponse(), mock<FilterChain>())

        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication.principal as AuthenticatedUserPrincipal
        assertEquals(7L, principal.userId)
    }

    @Test
    fun `does not authenticate when token cookie is invalid`() {
        val request = MockHttpServletRequest().apply {
            setCookies(Cookie("token", "bad-token"))
        }

        filter.doFilter(request, MockHttpServletResponse(), mock<FilterChain>())

        assertNull(SecurityContextHolder.getContext().authentication)
    }
}
