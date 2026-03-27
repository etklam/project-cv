package me.hker.module.user.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.module.user.entity.User
import me.hker.module.user.mapper.UserMapper
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userMapper: UserMapper,
) : UserService {
    override fun findById(userId: Long): User? = userMapper.selectById(userId)

    override fun findByEmail(email: String): User? =
        userMapper.selectOne(
            QueryWrapper<User>()
                .eq("email", email)
                .last("LIMIT 1"),
        )

    override fun findByUsername(username: String): User? =
        userMapper.selectOne(
            QueryWrapper<User>()
                .eq("username", username)
                .eq("is_deleted", false)
                .last("LIMIT 1"),
        )

    override fun findByInviteCode(inviteCode: String): User? =
        userMapper.selectOne(
            QueryWrapper<User>()
                .eq("invite_code", inviteCode)
                .last("LIMIT 1"),
        )
}
