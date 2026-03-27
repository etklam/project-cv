package me.hker.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CurrentUserResolverTest {
    private val resolver: CurrentUserResolver = DefaultCurrentUserResolver()

    @Test
    fun `returns header value when provided`() {
        assertEquals(42L, resolver.resolve(42L))
    }

    @Test
    fun `falls back to default when header is null`() {
        assertEquals(1L, resolver.resolve(null))
    }
}
