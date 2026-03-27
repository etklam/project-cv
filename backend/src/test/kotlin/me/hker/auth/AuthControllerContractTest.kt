package me.hker.auth

import me.hker.module.auth.AuthController
import me.hker.module.auth.JwtUtil
import me.hker.module.auth.dto.AuthUserDto
import me.hker.module.auth.dto.ChangeLocaleRequest
import me.hker.module.auth.dto.LoginRequest
import me.hker.module.auth.dto.RegisterRequest
import me.hker.module.auth.service.AuthService
import me.hker.common.CurrentUserResolver
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerContractTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var authService: AuthService

    @MockBean
    private lateinit var jwtUtil: JwtUtil

    @MockBean
    private lateinit var currentUserResolver: CurrentUserResolver

    @Test
    fun `me endpoint returns typed user payload`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(authService.me(1L)).thenReturn(
            AuthUserDto(
                id = 1L,
                email = "alice@example.com",
                displayName = "Alice",
                locale = "zh-TW",
                creditBalance = 50,
                inviteCode = "INV-ALICE1",
            ),
        )

        mockMvc.get("/api/v1/auth/me")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value(0) }
                jsonPath("$.data.user.email") { value("alice@example.com") }
                jsonPath("$.data.user.creditBalance") { value(50) }
            }
    }

    @Test
    fun `change locale endpoint uses resolver and returns updated locale`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(authService.changeLocale(1L, ChangeLocaleRequest("en"))).thenReturn(
            AuthUserDto(
                id = 1L,
                email = "alice@example.com",
                displayName = "Alice",
                locale = "en",
                creditBalance = 50,
                inviteCode = "INV-ALICE1",
            ),
        )

        mockMvc.put("/api/v1/auth/change-locale") {
            header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            content = """{"locale":"en"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.user.locale") { value("en") }
        }
    }
}
