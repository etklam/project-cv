package me.hker.module.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class JwtUtil(
    @Value("\${jwt.secret:dev-secret}") private val secret: String,
) {
    fun generateToken(userId: Long, email: String): String {
        val raw = "$userId:$email:$secret"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
    }

    fun getUserId(token: String): Long {
        val decoded = String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8)
        return decoded.substringBefore(":").toLong()
    }

    fun validateToken(token: String): Boolean {
        return runCatching { getUserId(token) }.isSuccess
    }
}
