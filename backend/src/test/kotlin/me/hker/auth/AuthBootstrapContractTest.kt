package me.hker.auth

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class AuthBootstrapContractTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `me endpoint should follow unified response contract`() {
        mockMvc.get("/api/v1/auth/me")
            .andExpect {
                status { isOk() }
                jsonPath("$.code") { value(0) }
                jsonPath("$.message") { value("OK") }
                jsonPath("$.data.user") { exists() }
            }
    }

    @Disabled("Pending: real JWT cookie bootstrap flow is not implemented in skeleton")
    @Test
    fun `register should set httpOnly auth cookie`() {
        mockMvc.get("/api/v1/auth/me")
            .andExpect {
                status { isOk() }
            }
    }
}
