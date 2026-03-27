package me.hker.module.auth.service

import jakarta.servlet.http.HttpServletResponse
import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.auth.dto.ChangeLocaleRequest
import me.hker.module.auth.dto.LoginRequest
import me.hker.module.auth.dto.RegisterRequest

interface AuthService {
    fun register(request: RegisterRequest): AuthUserDto
    fun login(request: LoginRequest): AuthUserDto
    fun me(userId: Long?): AuthUserDto
    fun changeLocale(userId: Long?, request: ChangeLocaleRequest): AuthUserDto
    fun writeAuthCookie(response: HttpServletResponse, token: String)
    fun clearAuthCookie(response: HttpServletResponse)
}
