package me.hker.auth

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class AuthBootstrapContractTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `me endpoint should return unauthorized without auth cookie`() {
        mockMvc.get("/api/v1/auth/me")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.code") { value(401) }
            }
    }

    @Test
    fun `login should set httpOnly auth cookie and allow bootstrap me call`() {
        val login = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"demo@example.com","password":"secret"}"""
        }.andReturn()

        val authCookie = requireNotNull(login.response.getCookie("token"))

        mockMvc.get("/api/v1/auth/me") {
            cookie(authCookie)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value(0) }
                jsonPath("$.data.user.id") { value(1) }
            }
    }
}
