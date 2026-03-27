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
        val raw = payload(userId, email)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
    }

    fun getUserId(token: String): Long {
        val parts = decodeParts(token)
        require(parts.size == 3) { "invalid token payload" }
        require(parts[2] == secret) { "invalid token signature" }
        return parts[0].toLong()
    }

    fun validateToken(token: String): Boolean = runCatching { getUserId(token) }.isSuccess

    private fun payload(userId: Long, email: String): String = "$userId:$email:$secret"

    private fun decodeParts(token: String): List<String> {
        val decoded = String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8)
        return decoded.split(":")
    }
}
