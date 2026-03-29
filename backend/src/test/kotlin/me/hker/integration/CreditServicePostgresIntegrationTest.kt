package me.hker.integration

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import me.hker.common.InsufficientCreditsException
import me.hker.module.credit.service.CreditService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest
class CreditServicePostgresIntegrationTest : PostgresIntegrationTestSupport() {
    @Autowired
    private lateinit var creditService: CreditService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun resetAliceBalance() {
        jdbcTemplate.update("DELETE FROM credit_transactions WHERE user_id = 1")
        jdbcTemplate.update("UPDATE users SET credit_balance = 50 WHERE id = 1")
    }

    @Test
    fun `credit should persist balance and ledger on postgres`() {
        creditService.credit(
            userId = 1L,
            amount = 15,
            type = "TEST_CREDIT",
            referenceType = "TEST",
            referenceId = 1L,
            description = "integration credit",
        )

        assertEquals(65, creditService.getBalance(1L))
        val ledgerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM credit_transactions WHERE user_id = 1 AND type = 'TEST_CREDIT'",
            Int::class.java,
        )
        assertEquals(1, ledgerCount)
    }

    @Test
    fun `deduct should reject insufficient credits on postgres without going negative`() {
        val error = assertThrows(InsufficientCreditsException::class.java) {
            creditService.deduct(
                userId = 1L,
                amount = 80,
                type = "TEST_DEDUCT",
                referenceType = "TEST",
                referenceId = 1L,
                description = "integration deduct",
            )
        }

        assertEquals("insufficient credits", error.message)
        assertEquals(50, creditService.getBalance(1L))
    }

    @Test
    fun `concurrent credits should record per-transaction resulting balances`() {
        val executor = Executors.newFixedThreadPool(2)
        val startGate = CountDownLatch(1)

        val first = executor.submit<Unit> {
            startGate.await()
            creditService.credit(
                userId = 1L,
                amount = 10,
                type = "CONCURRENT_CREDIT",
                referenceType = "TEST",
                referenceId = 101L,
                description = "concurrent credit 1",
            )
        }
        val second = executor.submit<Unit> {
            startGate.await()
            creditService.credit(
                userId = 1L,
                amount = 10,
                type = "CONCURRENT_CREDIT",
                referenceType = "TEST",
                referenceId = 102L,
                description = "concurrent credit 2",
            )
        }

        startGate.countDown()
        first.get(10, TimeUnit.SECONDS)
        second.get(10, TimeUnit.SECONDS)
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        val ledgerBalances = jdbcTemplate.queryForList(
            """
            SELECT balance_after
            FROM credit_transactions
            WHERE user_id = 1
              AND type = 'CONCURRENT_CREDIT'
            ORDER BY id
            """.trimIndent(),
            Int::class.java,
        ).sorted()

        assertEquals(70, creditService.getBalance(1L))
        assertEquals(listOf(60, 70), ledgerBalances)
    }
}
