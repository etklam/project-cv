package me.hker.auth

import me.hker.common.CurrentUserResolver
import me.hker.module.auth.AuthController
import me.hker.module.auth.JwtAuthFilter
import me.hker.module.auth.JwtUtil
import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.auth.dto.AuthUserResponse
import me.hker.module.auth.service.AuthService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthBootstrapContractTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var authService: AuthService

    @MockBean
    private lateinit var jwtUtil: JwtUtil

    @MockBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @MockBean
    private lateinit var currentUserResolver: CurrentUserResolver

    @Test
    fun `login should set auth cookie`() {
        whenever(authService.login(me.hker.module.auth.dto.LoginRequest("demo@example.com", "secret123")))
            .thenReturn(
                AuthUserDto(
                    id = 1L,
                    email = "demo@example.com",
                    username = "demo",
                    displayName = "Demo User",
                    locale = "zh-TW",
                    creditBalance = 50,
                    inviteCode = "INV-DEMO01",
                    onboardingStatus = "STEP_1",
                ),
            )
        whenever(jwtUtil.generateToken(1L, "demo@example.com", "USER")).thenReturn("signed-auth-token")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"demo@example.com","password":"secret123"}"""
        }.andExpect {
            status { isOk() }
        }

        verify(authService).writeAuthCookie(any(), eq("signed-auth-token"))
    }

    @Test
    fun `bootstrap me should return authenticated user payload`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(authService.me(1L)).thenReturn(
            AuthUserDto(
                id = 1L,
                email = "demo@example.com",
                username = "demo",
                displayName = "Demo User",
                locale = "zh-TW",
                creditBalance = 50,
                inviteCode = "INV-DEMO01",
                onboardingStatus = "STEP_1",
            ),
        )

        mockMvc.get("/api/v1/auth/me")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.user.email") { value("demo@example.com") }
                jsonPath("$.data.user.onboarding_status") { value("STEP_1") }
            }
    }
}
