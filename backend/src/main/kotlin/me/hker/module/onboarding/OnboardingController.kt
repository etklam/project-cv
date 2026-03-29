package me.hker.module.onboarding

import me.hker.common.CurrentUserResolver
import me.hker.common.R
import me.hker.module.auth.dto.AuthUserResponse
import me.hker.module.onboarding.dto.OnboardingStatusResponse
import me.hker.module.onboarding.dto.OnboardingStep1Request
import me.hker.module.onboarding.dto.OnboardingStep2Request
import me.hker.module.onboarding.dto.OnboardingStep3Request
import me.hker.module.onboarding.service.OnboardingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/onboarding")
class OnboardingController(
    private val onboardingService: OnboardingService,
    private val currentUserResolver: CurrentUserResolver,
) {
    @GetMapping("/status")
    fun status(): R<OnboardingStatusResponse> =
        R.ok(onboardingService.status(currentUserResolver.resolve(null)))

    @PutMapping("/step1")
    fun step1(@RequestBody request: OnboardingStep1Request): R<AuthUserResponse> =
        R.ok(AuthUserResponse(onboardingService.submitStep1(currentUserResolver.resolve(null), request)))

    @PutMapping("/step2")
    fun step2(@RequestBody request: OnboardingStep2Request): R<AuthUserResponse> =
        R.ok(AuthUserResponse(onboardingService.submitStep2(currentUserResolver.resolve(null), request)))

    @PostMapping("/skip-step2")
    fun skipStep2(): R<AuthUserResponse> =
        R.ok(AuthUserResponse(onboardingService.skipStep2(currentUserResolver.resolve(null))))

    @PutMapping("/step3")
    fun step3(@RequestBody request: OnboardingStep3Request): R<AuthUserResponse> =
        R.ok(AuthUserResponse(onboardingService.submitStep3(currentUserResolver.resolve(null), request)))
}
