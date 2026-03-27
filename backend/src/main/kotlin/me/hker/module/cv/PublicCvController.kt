package me.hker.module.cv

import me.hker.common.R
import me.hker.module.cv.dto.PublicCvDetailResponse
import me.hker.module.cv.dto.PublicProfileResponse
import me.hker.module.cv.service.PublicCvService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/public")
class PublicCvController(
    private val publicCvService: PublicCvService,
) {
    @GetMapping("/{username}")
    fun publicProfile(
        @PathVariable username: String,
    ): R<PublicProfileResponse> = R.ok(publicCvService.getPublicProfile(username))

    @GetMapping("/{username}/{slug}")
    fun publicCv(
        @PathVariable username: String,
        @PathVariable slug: String,
    ): R<PublicCvDetailResponse> = R.ok(publicCvService.getPublicCv(username, slug))
}
