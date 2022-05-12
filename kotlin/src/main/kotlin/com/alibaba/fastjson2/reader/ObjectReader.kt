package com.alibaba.fastjson2.reader

import com.alibaba.fastjson2.JSON

/**
 * E.g.
 * ```
 *   val reader = ...
 *   val status = reader.register<User>()
 * ```
 * @receiver ObjectReader<*>
 * @return [Boolean]
 * @since 2.0.3
 */
inline fun <reified T> ObjectReader<*>.register() =
    JSON.register(T::class.java, this)
