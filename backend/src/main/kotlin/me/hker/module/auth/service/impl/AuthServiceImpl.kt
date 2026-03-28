package me.hker.module.auth.service.impl

import jakarta.servlet.http.HttpServletResponse
import me.hker.common.toDto
import me.hker.module.auth.AuthenticationException
import me.hker.module.auth.InvalidCredentialsException
import me.hker.module.auth.PasswordEncoder
import me.hker.module.auth.UserAlreadyExistsException
import me.hker.module.auth.UserNotFoundException
import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.auth.dto.ChangeLocaleRequest
import me.hker.module.auth.dto.LoginRequest
import me.hker.module.auth.dto.RegisterRequest
import me.hker.module.auth.service.AuthService
import me.hker.module.user.entity.User
import me.hker.module.user.mapper.UserMapper
import me.hker.config.AppBusinessProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
class AuthServiceImpl(
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val appBusinessProperties: AppBusinessProperties,
) : AuthService {

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

    @Transactional
    override fun register(request: RegisterRequest): AuthUserDto {
        // Check if user already exists
        val existingUser = userMapper.selectByEmail(request.email)
        if (existingUser != null) {
            throw UserAlreadyExistsException(request.email)
        }

        // Hash the password
        val passwordHash = passwordEncoder.encode(request.password)

        // Generate invite code
        val inviteCode = generateInviteCode()

        // Create new user
        val user = User()
        user.email = request.email
        user.passwordHash = passwordHash
        user.displayName = request.displayName
        user.locale = request.locale ?: "zh-TW"
        user.creditBalance = appBusinessProperties.credit.signUpBonus
        user.inviteCode = inviteCode
        user.onboardingStatus = "STEP_1"

        userMapper.insert(user)

        return user.toDto()
    }

    override fun login(request: LoginRequest): AuthUserDto {
        // Find user by email
        val user = userMapper.selectByEmail(request.email)
            ?: throw UserNotFoundException(request.email)

        // Verify password
        if (!passwordEncoder.verify(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return user.toDto()
    }

    override fun me(userId: Long?): AuthUserDto {
        if (userId == null) {
            throw AuthenticationException("User ID is required")
        }

        val user = userMapper.selectById(userId)
            ?: throw UserNotFoundException("ID: $userId")

        return user.toDto()
    }

    override fun changeLocale(userId: Long?, request: ChangeLocaleRequest): AuthUserDto {
        if (userId == null) {
            throw AuthenticationException("User ID is required")
        }

        val user = userMapper.selectById(userId)
            ?: throw UserNotFoundException("ID: $userId")

        user.locale = request.locale
        userMapper.updateById(user)

        return user.toDto()
    }

    private fun generateInviteCode(): String {
        val suffix = UUID.randomUUID().toString().take(8).uppercase()
        return "INV-$suffix"
    }
}
