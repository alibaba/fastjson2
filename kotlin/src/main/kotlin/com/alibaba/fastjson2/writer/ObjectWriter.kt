package com.alibaba.fastjson2.writer

import com.alibaba.fastjson2.JSON

/**
 * E.g.
 * ```
 *   val writer = ...
 *   val status = writer.register<User>()
 * ```
 * @receiver ObjectWriter<*>
 * @return [Boolean]
 * @since 2.0.3
 */
inline fun <reified T> ObjectWriter<*>.register() =
    JSON.register(T::class.java, this)
