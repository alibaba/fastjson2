package com.alibaba.fastjson2

/**
 * E.g.
 * ```
 *    val refer = reference<User>()
 * ```
 * @since 2.0.3
 */
inline fun <reified T : Any> reference() =
    object : TypeReference<T>() {}
