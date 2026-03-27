package me.hker.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ResponseContractTest {

    @Test
    fun `ok should use code 0 and wrap data`() {
        val result = R.ok(mapOf("health" to "ok"))
        assertEquals(0, result.code)
        assertEquals("OK", result.message)
        assertEquals("ok", (result.data as Map<*, *>)["health"])
    }

    @Test
    fun `fail should keep message and error code`() {
        val result = R.fail(401, "unauthorized")
        assertEquals(401, result.code)
        assertEquals("unauthorized", result.message)
        assertNull(result.data)
    }
}
