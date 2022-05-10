package com.alibaba.fastjson2

/**
 * <pre>{@code
 *    val text = "..."
 *    val ref = reference<User>()
 *    val user = ref.parseObject(text)
 * }</pre>
 * @since 2.0.3
 */
inline fun <reified T : Any> reference() = object : TypeReference<T>() {}
