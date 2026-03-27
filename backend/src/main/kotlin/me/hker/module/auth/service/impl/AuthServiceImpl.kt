package me.hker.module.auth.service.impl

import jakarta.servlet.http.HttpServletResponse
import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.auth.dto.ChangeLocaleRequest
import me.hker.module.auth.dto.LoginRequest
import me.hker.module.auth.dto.RegisterRequest
import me.hker.module.auth.service.AuthService
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AuthServiceImpl : AuthService {
    override fun register(request: RegisterRequest): AuthUserDto {
        return AuthUserDto(
            id = 1L,
            email = request.email,
            displayName = request.displayName,
            locale = "zh-TW",
            creditBalance = 50,
            inviteCode = "INV-DEMO01",
        )
    }

    override fun login(request: LoginRequest): AuthUserDto {
        return AuthUserDto(
            id = 1L,
            email = request.email,
            displayName = "Demo User",
            locale = "zh-TW",
            creditBalance = 50,
            inviteCode = "INV-DEMO01",
        )
    }

    override fun me(userId: Long?): AuthUserDto {
        return AuthUserDto(
            id = userId ?: 1L,
            email = "demo@example.com",
            displayName = "Demo User",
            locale = "zh-TW",
            creditBalance = 50,
            inviteCode = "INV-DEMO01",
        )
    }

    override fun changeLocale(userId: Long?, request: ChangeLocaleRequest): AuthUserDto {
        return me(userId).copy(locale = request.locale)
    }

    override fun writeAuthCookie(response: HttpServletResponse, token: String) {
        val cookie = ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/api")
            .maxAge(Duration.ofDays(7))
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    override fun clearAuthCookie(response: HttpServletResponse) {
        val cookie = ResponseCookie.from("token", "")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/api")
            .maxAge(Duration.ZERO)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
