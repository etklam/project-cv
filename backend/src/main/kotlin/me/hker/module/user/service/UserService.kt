package me.hker.module.user.service

import me.hker.module.user.entity.User

interface UserService {
    fun findById(userId: Long): User?
    fun findByEmail(email: String): User?
    fun findByUsername(username: String): User?
    fun findByInviteCode(inviteCode: String): User?
}
