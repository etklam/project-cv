package me.hker.module.admin.controller

import me.hker.common.AdminUserResolver
import me.hker.common.R
import me.hker.module.admin.dto.PaginatedTransactionResponse
import me.hker.module.admin.service.AdminCreditService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/credits")
class AdminCreditController(
    private val adminCreditService: AdminCreditService,
    private val adminUserResolver: AdminUserResolver,
) {
    @GetMapping("/transactions")
    fun listTransactions(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) userId: Long?,
    ): R<PaginatedTransactionResponse> {
        adminUserResolver.resolveAdmin()
        return R.ok(adminCreditService.listTransactions(page, size, userId))
    }
}
