package me.hker.module.user.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.module.user.entity.User
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface UserMapper : BaseMapper<User> {
    @Select(
        """
        UPDATE users
        SET credit_balance = credit_balance + #{delta},
            updated_at = NOW()
        WHERE id = #{userId}
          AND is_deleted = false
          AND credit_balance + #{delta} >= 0
        RETURNING credit_balance
        """,
    )
    fun adjustCreditBalanceReturning(
        @Param("userId") userId: Long,
        @Param("delta") delta: Int,
    ): Int?

    @Select(
        """
        SELECT credit_balance
        FROM users
        WHERE id = #{userId}
          AND is_deleted = false
        LIMIT 1
        """,
    )
    fun selectCreditBalance(@Param("userId") userId: Long): Int?

    @Select(
        """
        SELECT *
        FROM users
        WHERE email = #{email}
          AND is_deleted = false
        LIMIT 1
        """,
    )
    fun selectByEmail(@Param("email") email: String): User?

    @Select(
        """
        SELECT *
        FROM users
        WHERE invite_code = #{inviteCode}
          AND is_deleted = false
        LIMIT 1
        FOR UPDATE
        """,
    )
    fun selectForUpdate(@Param("inviteCode") inviteCode: String): User?

    @Select(
        """
        SELECT COUNT(*) FROM users WHERE is_deleted = false
        """,
    )
    fun countTotal(): Long

    @Select(
        """
        SELECT COUNT(*) FROM users WHERE is_deleted = false AND role = 'ADMIN'
        """,
    )
    fun countAdmins(): Long
}
