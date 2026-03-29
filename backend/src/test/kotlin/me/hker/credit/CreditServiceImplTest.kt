package me.hker.credit

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.common.InsufficientCreditsException
import me.hker.module.credit.entity.CreditTransaction
import me.hker.module.credit.mapper.CreditTransactionMapper
import me.hker.module.credit.service.impl.CreditServiceImpl
import me.hker.module.user.mapper.UserMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CreditServiceImplTest {
    private val creditTransactionMapper = mock<CreditTransactionMapper>()
    private val userMapper = mock<UserMapper>()
    private val service = CreditServiceImpl(
        creditTransactionMapper = creditTransactionMapper,
        userMapper = userMapper,
    )

    @Test
    fun `credit uses returned balance from atomic update and writes ledger with resulting balance`() {
        whenever(userMapper.adjustCreditBalanceReturning(1L, 30)).thenReturn(80)

        service.credit(
            userId = 1L,
            amount = 30,
            type = "PROMO_CODE",
            referenceType = "PROMO_CODE",
            referenceId = 9L,
            description = "reward",
        )

        val transaction = argumentCaptor<CreditTransaction>()
        verify(userMapper).adjustCreditBalanceReturning(1L, 30)
        verify(userMapper, never()).selectCreditBalance(1L)
        verify(creditTransactionMapper).insert(transaction.capture())
        assertEquals(30, transaction.firstValue.amount)
        assertEquals(80, transaction.firstValue.balanceAfter)
        assertEquals("PROMO_CODE", transaction.firstValue.type)
    }

    @Test
    fun `deduct rejects insufficient credits after failed atomic update`() {
        whenever(userMapper.adjustCreditBalanceReturning(1L, -10)).thenReturn(null)
        whenever(userMapper.selectCreditBalance(1L)).thenReturn(5)

        val error = assertThrows(InsufficientCreditsException::class.java) {
            service.deduct(
                userId = 1L,
                amount = 10,
                type = "PDF_EXPORT",
                referenceType = "CV",
                referenceId = 1L,
                description = "export",
            )
        }

        assertEquals("insufficient credits", error.message)
    }

    @Test
    fun `deduct rejects missing user after failed atomic update`() {
        whenever(userMapper.adjustCreditBalanceReturning(99L, -10)).thenReturn(null)
        whenever(userMapper.selectCreditBalance(99L)).thenReturn(null)

        val error = assertThrows(IllegalArgumentException::class.java) {
            service.deduct(
                userId = 99L,
                amount = 10,
                type = "PDF_EXPORT",
                referenceType = "CV",
                referenceId = 1L,
                description = "export",
            )
        }

        assertEquals("user not found", error.message)
    }

    @Test
    fun `transactions are paged by user`() {
        val page = Page<CreditTransaction>(1, 20)
        whenever(creditTransactionMapper.selectPage(any<Page<CreditTransaction>>(), any<QueryWrapper<CreditTransaction>>()))
            .thenReturn(page)

        val result = service.getTransactions(1L, 1, 20)

        assertEquals(page, result)
        verify(creditTransactionMapper).selectPage(any<Page<CreditTransaction>>(), any<QueryWrapper<CreditTransaction>>())
    }

    @Test
    fun `getBalance uses dedicated balance query`() {
        whenever(userMapper.selectCreditBalance(1L)).thenReturn(42)

        assertEquals(42, service.getBalance(1L))
        verify(userMapper).selectCreditBalance(eq(1L))
    }
}
