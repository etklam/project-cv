package me.hker.module.user

import me.hker.common.CurrentUserResolver
import me.hker.common.R
import me.hker.module.onboarding.dto.UsernameAvailabilityResponse
import me.hker.module.onboarding.service.OnboardingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val onboardingService: OnboardingService,
    private val currentUserResolver: CurrentUserResolver,
) {
    @GetMapping("/check-username")
    fun checkUsername(@RequestParam username: String): R<UsernameAvailabilityResponse> {
        val userId = runCatching { currentUserResolver.resolve(null) }.getOrNull()
        return R.ok(UsernameAvailabilityResponse(onboardingService.isUsernameAvailable(username, userId)))
    }
}
