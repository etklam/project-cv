package me.hker.module.admin.controller

import me.hker.common.AdminUserResolver
import me.hker.common.R
import me.hker.module.admin.dto.*
import me.hker.module.admin.service.AdminPromoCodeService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/promocodes")
class AdminPromoCodeController(
    private val adminPromoCodeService: AdminPromoCodeService,
    private val adminUserResolver: AdminUserResolver,
) {
    @GetMapping
    fun listPromoCodes(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): R<PaginatedPromoCodeResponse> {
        adminUserResolver.resolveAdmin()
        return R.ok(adminPromoCodeService.listPromoCodes(page, size))
    }

    @PostMapping
    fun createPromoCode(@RequestBody request: CreatePromoCodeRequest): R<AdminPromoCodeDto> {
        adminUserResolver.resolveAdmin()
        return R.ok(adminPromoCodeService.createPromoCode(request))
    }

    @PutMapping("/{id}")
    fun updatePromoCode(
        @PathVariable id: Long,
        @RequestBody request: UpdatePromoCodeRequest,
    ): R<AdminPromoCodeDto> {
        adminUserResolver.resolveAdmin()
        return R.ok(adminPromoCodeService.updatePromoCode(id, request))
    }

    @DeleteMapping("/{id}")
    fun deletePromoCode(@PathVariable id: Long): R<Nothing> {
        adminUserResolver.resolveAdmin()
        adminPromoCodeService.deletePromoCode(id)
        return R.ok()
    }

    @GetMapping("/{id}/stats")
    fun getPromoCodeStats(@PathVariable id: Long): R<PromoCodeStatsDto> {
        adminUserResolver.resolveAdmin()
        return R.ok(adminPromoCodeService.getPromoCodeStats(id))
    }
}
