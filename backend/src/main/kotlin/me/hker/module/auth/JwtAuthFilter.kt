package me.hker.module.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtUtil: JwtUtil,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.cookies
            ?.firstOrNull { it.name == TOKEN_COOKIE_NAME }
            ?.value

        if (
            SecurityContextHolder.getContext().authentication == null &&
            !token.isNullOrBlank() &&
            jwtUtil.validateToken(token)
        ) {
            val authentication = UsernamePasswordAuthenticationToken.authenticated(
                AuthenticatedUserPrincipal(jwtUtil.getUserId(token)),
                null,
                emptyList(),
            )
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private const val TOKEN_COOKIE_NAME = "token"
    }
}
