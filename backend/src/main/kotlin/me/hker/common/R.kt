package me.hker.common

data class R<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> ok(data: T? = null, message: String = "OK"): R<T> = R(0, message, data)

        fun fail(code: Int, message: String): R<Nothing> = R(code, message, null)
    }
}
