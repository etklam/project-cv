package me.hker.module.admin.controller

import me.hker.common.AdminUserResolver
import me.hker.common.R
import me.hker.module.admin.dto.*
import me.hker.module.admin.service.AdminUserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserController(
    private val adminUserService: AdminUserService,
    private val adminUserResolver: AdminUserResolver,
) {
    @GetMapping
    fun listUsers(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) search: String?,
    ): R<PaginatedUserResponse> {
        adminUserResolver.resolveAdmin() // Verify admin access
        return R.ok(adminUserService.listUsers(page, size, search))
    }

    @GetMapping("/{userId}")
    fun getUserDetail(@PathVariable userId: Long): R<AdminUserDetailDto> {
        adminUserResolver.resolveAdmin() // Verify admin access
        return R.ok(adminUserService.getUserDetail(userId))
    }

    @PutMapping("/{userId}/role")
    fun updateUserRole(
        @PathVariable userId: Long,
        @RequestBody request: UpdateUserRoleRequest,
    ): R<AdminUserDetailDto> {
        adminUserResolver.resolveAdmin() // Verify admin access
        return R.ok(adminUserService.updateUserRole(userId, request))
    }

    @PostMapping("/{userId}/credits/adjust")
    fun adjustUserCredits(
        @PathVariable userId: Long,
        @RequestBody request: AdjustCreditsRequest,
    ): R<AdminUserDetailDto> {
        adminUserResolver.resolveAdmin() // Verify admin access
        return R.ok(adminUserService.adjustUserCredits(userId, request))
    }
}
