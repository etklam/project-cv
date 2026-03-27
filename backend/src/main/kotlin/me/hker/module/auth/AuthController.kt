package me.hker.module.auth

import jakarta.servlet.http.HttpServletResponse
import me.hker.common.CurrentUserResolver
import me.hker.common.R
import me.hker.module.auth.dto.ChangeLocaleRequest
import me.hker.module.auth.dto.AuthUserResponse
import me.hker.module.auth.dto.LoginRequest
import me.hker.module.auth.dto.RegisterRequest
import me.hker.module.auth.service.AuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtUtil: JwtUtil,
    private val currentUserResolver: CurrentUserResolver,
) {
    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest,
        response: HttpServletResponse,
    ): R<AuthUserResponse> {
        val user = authService.register(request)
        authService.writeAuthCookie(response, jwtUtil.generateToken(user.id ?: 1L, user.email))
        return R.ok(AuthUserResponse(user = user))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): R<AuthUserResponse> {
        val user = authService.login(request)
        authService.writeAuthCookie(response, jwtUtil.generateToken(user.id ?: 1L, user.email))
        return R.ok(AuthUserResponse(user = user))
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): R<Nothing> {
        authService.clearAuthCookie(response)
        return R.ok()
    }

    @GetMapping("/me")
    fun me(): R<AuthUserResponse> {
        val user = authService.me(currentUserResolver.resolve(null))
        return R.ok(AuthUserResponse(user = user))
    }

    @PutMapping("/change-locale")
    fun changeLocale(
        @RequestBody request: ChangeLocaleRequest,
    ): R<AuthUserResponse> {
        val user = authService.changeLocale(currentUserResolver.resolve(null), request)
        return R.ok(AuthUserResponse(user = user))
    }
}
