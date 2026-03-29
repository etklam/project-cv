package me.hker.onboarding

import me.hker.module.onboarding.dto.OnboardingStep1Request
import me.hker.module.onboarding.service.impl.OnboardingServiceImpl
import me.hker.module.template.service.TemplateService
import me.hker.module.user.entity.User
import me.hker.module.user.mapper.UserMapper
import me.hker.module.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class OnboardingServiceTest {
    private val userService = mock<UserService>()
    private val userMapper = mock<UserMapper>()
    private val templateService = mock<TemplateService>()
    private val service = OnboardingServiceImpl(
        userService = userService,
        userMapper = userMapper,
        templateService = templateService,
    )

    @Test
    fun `submitStep1 tolerates malformed existing draft and persists normalized identity`() {
        val user = User().apply {
            id = 7L
            email = "user@example.com"
            displayName = "Draft User"
            onboardingStatus = "STEP_1"
            onboardingDraft = "\"legacy-string-draft\""
        }
        whenever(userService.findById(7L)).thenReturn(user)
        whenever(userService.findByUsername("fresh-name")).thenReturn(null)

        service.submitStep1(
            7L,
            OnboardingStep1Request(
                displayName = " Fresh Name ",
            ),
        )

        val savedUser = argumentCaptor<User>()
        verify(userMapper).updateById(savedUser.capture())
        assertEquals("Fresh Name", savedUser.firstValue.displayName)
        assertEquals(null, savedUser.firstValue.username)
        assertEquals("STEP_2", savedUser.firstValue.onboardingStatus)
        assertEquals(null, savedUser.firstValue.onboardingDraft)
    }

    @Test
    fun `submitStep1 resets existing draft to a fresh step1 payload`() {
        val user = User().apply {
            id = 9L
            email = "user@example.com"
            displayName = "Draft User"
            onboardingStatus = "STEP_1"
            onboardingDraft = """{"step2Skipped":true}"""
        }
        whenever(userService.findById(9L)).thenReturn(user)
        service.submitStep1(
            9L,
            OnboardingStep1Request(
                displayName = "Usable Name",
            ),
        )

        val savedUser = argumentCaptor<User>()
        verify(userMapper).updateById(savedUser.capture())
        assertEquals(null, savedUser.firstValue.onboardingDraft)
    }
}
