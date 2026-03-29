package me.hker.module.admin.controller

import me.hker.common.R
import me.hker.module.admin.dto.*
import me.hker.module.admin.service.AdminTemplateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/templates")
class AdminTemplateController(
    private val adminTemplateService: AdminTemplateService,
) {
    @GetMapping
    fun listAllTemplates(): R<List<AdminTemplateDto>> {
        return R.ok(adminTemplateService.listAllTemplates())
    }

    @PostMapping
    fun createTemplate(@RequestBody request: CreateTemplateRequest): R<AdminTemplateDto> {
        return R.ok(adminTemplateService.createTemplate(request))
    }

    @PutMapping("/{id}")
    fun updateTemplate(
        @PathVariable id: Long,
        @RequestBody request: UpdateTemplateRequest,
    ): R<AdminTemplateDto> {
        return R.ok(adminTemplateService.updateTemplate(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteTemplate(@PathVariable id: Long): R<Nothing> {
        adminTemplateService.deleteTemplate(id)
        return R.ok()
    }

    @PutMapping("/{id}/status")
    fun toggleTemplateStatus(
        @PathVariable id: Long,
        @RequestBody request: ToggleTemplateStatusRequest,
    ): R<AdminTemplateDto> {
        return R.ok(adminTemplateService.toggleTemplateStatus(id, request))
    }
}
