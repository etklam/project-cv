package me.hker.common

import org.springframework.stereotype.Component

interface CurrentUserResolver {
    fun resolve(requestUserId: Long?): Long
}

@Component
class DefaultCurrentUserResolver : CurrentUserResolver {
    override fun resolve(requestUserId: Long?): Long = requestUserId ?: DEV_USER_ID

    companion object {
        private const val DEV_USER_ID = 1L
    }
}
