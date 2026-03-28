package me.hker.module.admin.service

import me.hker.module.admin.dto.AdminUserDetailDto
import me.hker.module.admin.dto.AdjustCreditsRequest
import me.hker.module.admin.dto.PaginatedUserResponse
import me.hker.module.admin.dto.UpdateUserRoleRequest

interface AdminUserService {
    fun listUsers(page: Int, size: Int, search: String?): PaginatedUserResponse
    fun getUserDetail(userId: Long): AdminUserDetailDto
    fun updateUserRole(userId: Long, request: UpdateUserRoleRequest): AdminUserDetailDto
    fun adjustUserCredits(userId: Long, request: AdjustCreditsRequest): AdminUserDetailDto
}
