package me.hker.module.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
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
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("token_type", TOKEN_TYPE_AUTH)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(key)
            .compact()
    }

    fun generateExportToken(userId: Long, cvId: Long): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("cv_id", cvId)
            .claim("token_type", TOKEN_TYPE_EXPORT)
            .issuedAt(now)
            .expiration(Date(now.time + EXPORT_TOKEN_EXPIRATION_MS))
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

    fun isExportToken(token: String, userId: Long, cvId: Long): Boolean = runCatching {
        val claims = parseToken(token)
        claims.subject.toLong() == userId &&
            claims.get("cv_id", Number::class.java)?.toLong() == cvId &&
            claims.get("token_type", String::class.java) == TOKEN_TYPE_EXPORT
    }.getOrDefault(false)

    fun validateToken(token: String): Boolean = runCatching { parseToken(token) }.isSuccess

    private fun parseToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    companion object {
        private const val TOKEN_TYPE_AUTH = "auth"
        private const val TOKEN_TYPE_EXPORT = "export"
        private const val EXPORT_TOKEN_EXPIRATION_MS = 5 * 60 * 1000L
    }
}
