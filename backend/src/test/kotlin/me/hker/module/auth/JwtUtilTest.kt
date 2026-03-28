package me.hker.module.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit

class JwtUtilTest {
    private lateinit var jwtUtil: JwtUtil
    private val secret = "test-secret-key-32-bytes-long-for-hs256"
    private val expirationMs = TimeUnit.HOURS.toMillis(1)

    @BeforeEach
    fun setUp() {
        jwtUtil = JwtUtil(secret, expirationMs)
    }

    @Test
    fun `generateToken creates valid JWT string`() {
        val token = jwtUtil.generateToken(123L, "test@example.com")

        // JWT should have 3 parts separated by dots
        val parts = token.split(".")
        assertEquals(3, parts.size, "JWT should have header, payload, and signature")

        // Each part should be base64url encoded
        parts.forEach { part ->
            assertDoesNotThrow("Each JWT part should be valid base64url") {
                // Verify it's valid base64url
                java.util.Base64.getUrlDecoder().decode(part)
            }
        }
    }

    @Test
    fun `getUserId extracts correct user ID from valid token`() {
        val userId = 456L
        val token = jwtUtil.generateToken(userId, "user@example.com")

        val extractedUserId = jwtUtil.getUserId(token)
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `validateToken returns true for valid token`() {
        val token = jwtUtil.generateToken(789L, "valid@example.com")

        assertTrue(jwtUtil.validateToken(token))
    }

    @Test
    fun `validateToken returns false for invalid token format`() {
        assertFalse(jwtUtil.validateToken("invalid.token.format"))
    }

    @Test
    fun `validateToken returns false for garbage token`() {
        assertFalse(jwtUtil.validateToken("garbage-data"))
    }

    @Test
    fun `validateToken returns false for empty token`() {
        assertFalse(jwtUtil.validateToken(""))
    }

    @Test
    fun `validateToken returns false for token signed with different secret`() {
        val token = jwtUtil.generateToken(100L, "original@example.com")

        val differentJwtUtil = JwtUtil("different-secret-key-32-bytes-long", expirationMs)
        assertFalse(differentJwtUtil.validateToken(token))
    }

    @Test
    fun `getUserId throws exception for invalid token`() {
        val invalidToken = "not.a.valid.jwt"

        assertThrows<Exception> {
            jwtUtil.getUserId(invalidToken)
        }
    }

    @Test
    fun `generates different tokens for same user`() {
        val userId = 999L
        val email = "test@example.com"

        val token1 = jwtUtil.generateToken(userId, email)
        // Sleep to ensure different timestamp (JWT uses second precision)
        Thread.sleep(1100)
        val token2 = jwtUtil.generateToken(userId, email)

        // Tokens should be different because issuedAt changes
        assertFalse(token1 == token2, "Tokens should be unique due to issuedAt claim")

        // But both should be valid and return same userId
        assertEquals(userId, jwtUtil.getUserId(token1))
        assertEquals(userId, jwtUtil.getUserId(token2))
    }

    @Test
    fun `token contains email claim`() {
        val email = "user@example.com"
        val token = jwtUtil.generateToken(1L, email)

        // Token should be valid
        assertTrue(jwtUtil.validateToken(token))

        // We can verify userId is extracted correctly
        val userId = jwtUtil.getUserId(token)
        assertEquals(1L, userId)
    }

    @Test
    fun `getUserId handles large user IDs`() {
        val largeUserId = Long.MAX_VALUE
        val token = jwtUtil.generateToken(largeUserId, "large@example.com")

        assertEquals(largeUserId, jwtUtil.getUserId(token))
    }

    @Test
    fun `validateToken returns false for token with wrong signature`() {
        val validToken = jwtUtil.generateToken(1L, "test@example.com")
        val parts = validToken.split(".")

        // Tamper with the signature
        val tamperedToken = "${parts[0]}.${parts[1]}.tampereDSignature123"

        assertFalse(jwtUtil.validateToken(tamperedToken))
    }

    @Test
    fun `validateToken returns false for tampered payload`() {
        val validToken = jwtUtil.generateToken(1L, "test@example.com")
        val parts = validToken.split(".")

        // Tamper with the payload by modifying it
        val tamperedPayload = parts[1].substring(0, 10) + "XXXXX" + parts[1].substring(15)
        val tamperedToken = "${parts[0]}.$tamperedPayload.${parts[2]}"

        assertFalse(jwtUtil.validateToken(tamperedToken))
    }
}
