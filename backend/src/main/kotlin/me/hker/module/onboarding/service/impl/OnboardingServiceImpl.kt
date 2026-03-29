package me.hker.module.onboarding.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.hker.common.ResourceNotFoundException
import me.hker.common.toDto
import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.onboarding.dto.OnboardingStatusResponse
import me.hker.module.onboarding.dto.OnboardingStep1Request
import me.hker.module.onboarding.dto.OnboardingStep2Request
import me.hker.module.onboarding.dto.OnboardingStep3Request
import me.hker.module.onboarding.service.OnboardingService
import me.hker.module.template.service.TemplateService
import me.hker.module.user.entity.User
import me.hker.module.user.mapper.UserMapper
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingServiceImpl(
    private val userService: UserService,
    private val userMapper: UserMapper,
    private val templateService: TemplateService,
    private val objectMapper: ObjectMapper,
) : OnboardingService {
    override fun status(userId: Long): OnboardingStatusResponse =
        OnboardingStatusResponse(requireUser(userId).onboardingStatus)

    @Transactional
    override fun submitStep1(userId: Long, request: OnboardingStep1Request): AuthUserDto {
        val user = requireUser(userId)
        val normalizedDisplayName = request.displayName.trim()
        require(normalizedDisplayName.isNotBlank()) { "display name is required" }

        val normalizedUsername = normalizeUsername(request.username)
        require(isUsernameAvailable(normalizedUsername, userId)) { "username is already taken" }

        user.displayName = normalizedDisplayName
        user.username = normalizedUsername
        user.onboardingStatus = "STEP_2"
        user.onboardingDraft = mergeDraft(user.onboardingDraft, mapOf("step1" to request))
        userMapper.updateById(user)
        return user.toDto()
    }

    @Transactional
    override fun submitStep2(userId: Long, request: OnboardingStep2Request): AuthUserDto {
        val user = requireUser(userId)
        user.onboardingStatus = "STEP_3"
        user.onboardingDraft = mergeDraft(user.onboardingDraft, mapOf("step2" to request))
        userMapper.updateById(user)
        return user.toDto()
    }

    @Transactional
    override fun skipStep2(userId: Long): AuthUserDto {
        val user = requireUser(userId)
        user.onboardingStatus = "STEP_3"
        user.onboardingDraft = mergeDraft(user.onboardingDraft, mapOf("step2Skipped" to true))
        userMapper.updateById(user)
        return user.toDto()
    }

    @Transactional
    override fun submitStep3(userId: Long, request: OnboardingStep3Request): AuthUserDto {
        val user = requireUser(userId)
        val templateKey = request.templateKey.trim().lowercase()
        require(templateKey.isNotBlank()) { "template key is required" }
        require(templateService.existsActiveTemplate(templateKey)) { "template not found" }

        user.onboardingStatus = "DONE"
        user.onboardingDraft = mergeDraft(user.onboardingDraft, mapOf("selectedTemplateKey" to templateKey))
        userMapper.updateById(user)
        return user.toDto()
    }

    override fun isUsernameAvailable(username: String, currentUserId: Long?): Boolean {
        val normalizedUsername = normalizeUsername(username)
        val existing = userService.findByUsername(normalizedUsername) ?: return true
        return existing.id == currentUserId
    }

    private fun requireUser(userId: Long): User =
        userService.findById(userId) ?: throw ResourceNotFoundException("user not found")

    private fun normalizeUsername(raw: String): String {
        val normalized = raw.trim().lowercase()
        require(normalized.matches(USERNAME_PATTERN)) { "username is invalid" }
        return normalized
    }

    private fun mergeDraft(existingJson: String?, patch: Map<String, Any?>): String {
        val current: MutableMap<String, Any?> = if (existingJson.isNullOrBlank()) {
            mutableMapOf()
        } else {
            objectMapper.readValue(existingJson)
        }
        current.putAll(patch)
        return objectMapper.writeValueAsString(current)
    }

    companion object {
        private val USERNAME_PATTERN = Regex("^[a-z0-9][a-z0-9-]{2,49}$")
    }
}
