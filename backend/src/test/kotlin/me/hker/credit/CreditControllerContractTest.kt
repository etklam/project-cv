package me.hker.credit

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.common.CurrentUserResolver
import me.hker.module.auth.JwtAuthFilter
import me.hker.module.credit.CreditController
import me.hker.module.credit.entity.CreditTransaction
import me.hker.module.credit.service.CreditService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(CreditController::class)
@AutoConfigureMockMvc(addFilters = false)
class CreditControllerContractTest(@Autowired private val mockMvc: MockMvc) {

    @MockBean
    private lateinit var creditService: CreditService

    @MockBean
    private lateinit var currentUserResolver: CurrentUserResolver

    @MockBean
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @Test
    fun `balance endpoint returns typed DTO`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        whenever(creditService.getBalance(1L)).thenReturn(42)

        mockMvc.get("/api/v1/me/credits/balance")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.balance") { value(42) }
            }
    }

    @Test
    fun `transactions endpoint returns DTO with metadata`() {
        whenever(currentUserResolver.resolve(null)).thenReturn(1L)
        val page = Page<CreditTransaction>(1, 10).apply {
            this.records = mutableListOf()
            this.total = 0
        }
        whenever(creditService.getTransactions(1L, 1, 10)).thenReturn(page)

        mockMvc.get("/api/v1/me/credits/transactions?page=1&size=10")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.page") { value(1) }
                jsonPath("$.data.size") { value(10) }
                jsonPath("$.data.total") { value(0) }
            }
    }
}
