package me.hker.module.template

import me.hker.common.R
import me.hker.module.template.dto.TemplateListResponse
import me.hker.module.template.service.TemplateService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class TemplateController(
    private val templateService: TemplateService,
) {
    @GetMapping("/templates")
    fun listTemplates(): R<TemplateListResponse> {
        val templates = templateService.listActiveTemplates()
        return R.ok(TemplateListResponse(templates = templates))
    }
}
