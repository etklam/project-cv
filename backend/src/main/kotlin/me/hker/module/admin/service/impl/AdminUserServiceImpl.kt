package me.hker.module.admin.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.common.ResourceNotFoundException
import me.hker.module.admin.dto.*
import me.hker.module.admin.service.AdminUserService
import me.hker.module.credit.mapper.CreditTransactionMapper
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import me.hker.module.user.entity.User
import me.hker.module.user.mapper.UserMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminUserServiceImpl(
    private val userMapper: UserMapper,
    private val creditService: CreditService,
    private val promoCodeRedemptionMapper: PromoCodeRedemptionMapper,
    private val creditTransactionMapper: CreditTransactionMapper,
) : AdminUserService {

    override fun listUsers(page: Int, size: Int, search: String?): PaginatedUserResponse {
        val query = QueryWrapper<User>().eq("is_deleted", false)

        // Apply search filter if provided
        if (!search.isNullOrBlank()) {
            query.and(wrapper ->
                wrapper.like("email", search)
                    .or().like("display_name", search)
                    .or().like("username", search)
            )
        }

        query.orderByDesc("created_at")

        val userPage: IPage<User> = userMapper.selectPage(Page(page.toLong(), size.toLong()), query)

        val users = userPage.records.map { user ->
            AdminUserDetailDto(
                id = user.id ?: 0L,
                email = user.email,
                displayName = user.displayName,
                username = user.username,
                role = user.role,
                creditBalance = user.creditBalance,
                inviteCode = user.inviteCode,
                onboardingStatus = user.onboardingStatus,
                locale = user.locale,
                createdAt = user.createdAt ?: java.time.LocalDateTime.now(),
                isActive = !user.isDeleted,
            )
        }

        return PaginatedUserResponse(
            users = users,
            total = userPage.total,
            page = page,
            size = size,
        )
    }

    override fun getUserDetail(userId: Long): AdminUserDetailDto {
        val user = userMapper.selectById(userId)
            ?: throw ResourceNotFoundException("User not found: $userId")

        return AdminUserDetailDto(
            id = user.id ?: 0L,
            email = user.email,
            displayName = user.displayName,
            username = user.username,
            role = user.role,
            creditBalance = user.creditBalance,
            inviteCode = user.inviteCode,
            onboardingStatus = user.onboardingStatus,
            locale = user.locale,
            createdAt = user.createdAt ?: java.time.LocalDateTime.now(),
            isActive = !user.isDeleted,
        )
    }

    @Transactional
    override fun updateUserRole(userId: Long, request: UpdateUserRoleRequest): AdminUserDetailDto {
        val user = userMapper.selectById(userId)
            ?: throw ResourceNotFoundException("User not found: $userId")

        if (request.role != "USER" && request.role != "ADMIN") {
            throw IllegalArgumentException("Invalid role: ${request.role}")
        }

        user.role = request.role
        userMapper.updateById(user)

        return getUserDetail(userId)
    }

    @Transactional
    override fun adjustUserCredits(userId: Long, request: AdjustCreditsRequest): AdminUserDetailDto {
        val user = userMapper.selectById(userId)
            ?: throw ResourceNotFoundException("User not found: $userId")

        // Check idempotency if key is provided
        if (!request.idempotencyKey.isNullOrBlank()) {
            val idempotencyDescription = "[idempotency:${request.idempotencyKey}] ${request.reason ?: "Admin credit adjustment"}"
            val existingTx = creditTransactionMapper.findByReferenceTypeAndDescription(
                "ADMIN_ADJUSTMENT",
                idempotencyDescription
            )
            if (existingTx != null) {
                // Idempotent request - return the user detail without processing again
                return getUserDetail(userId)
            }
        }

        val description = if (!request.idempotencyKey.isNullOrBlank()) {
            "[idempotency:${request.idempotencyKey}] ${request.reason ?: "Admin credit adjustment"}"
        } else {
            request.reason ?: "Admin credit adjustment"
        }

        if (request.delta > 0) {
            creditService.credit(
                userId = userId,
                amount = request.delta,
                type = "ADMIN_ADJUSTMENT",
                description = description,
            )
        } else if (request.delta < 0) {
            creditService.deduct(
                userId = userId,
                amount = -request.delta,
                type = "ADMIN_ADJUSTMENT",
                description = description,
            )
        }

        return getUserDetail(userId)
    }
}
