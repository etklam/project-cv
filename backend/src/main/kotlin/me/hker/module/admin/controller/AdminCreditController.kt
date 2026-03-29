package me.hker.module.admin.controller

import me.hker.common.R
import me.hker.module.admin.dto.PaginatedTransactionResponse
import me.hker.module.admin.service.AdminCreditService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/credits")
class AdminCreditController(
    private val adminCreditService: AdminCreditService,
) {
    @GetMapping("/transactions")
    fun listTransactions(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) userId: Long?,
    ): R<PaginatedTransactionResponse> {
        return R.ok(adminCreditService.listTransactions(page, size, userId))
    }
}
