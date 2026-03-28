package me.hker.module.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.secret:dev-secret}") private val secret: String,
    @Value("\${jwt.expiration-ms:86400000}") private val expirationMs: Long = 86400000L, // 24 hours
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: Long, email: String, role: String = "USER"): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun getUserId(token: String): Long {
        val claims: Claims = parseToken(token)
        return claims.subject.toLong()
    }

    fun getRole(token: String): String {
        val claims: Claims = parseToken(token)
        return claims.get("role", String::class.java) ?: "USER"
    }

    fun validateToken(token: String): Boolean = runCatching { parseToken(token) }.isSuccess

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
