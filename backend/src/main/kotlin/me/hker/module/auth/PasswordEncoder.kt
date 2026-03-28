package me.hker.module.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {
    /**
     * Hash a raw password using BCrypt with cost factor 12
     */
    fun encode(rawPassword: String): String {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray())
    }

    /**
     * Verify a raw password against a hash
     */
    fun verify(rawPassword: String, hash: String): Boolean {
        return try {
            BCrypt.verifyer().verify(rawPassword.toCharArray(), hash).verified
        } catch (e: Exception) {
            false
        }
    }
}
