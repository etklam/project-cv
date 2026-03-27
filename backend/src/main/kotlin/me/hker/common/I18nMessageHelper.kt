package me.hker.common

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class I18nMessageHelper(
    private val messageSource: MessageSource,
) {
    fun get(key: String, vararg args: Any?): String {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale()) ?: key
    }
}
