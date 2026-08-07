@file:Suppress(
    "HasPlatformType"
)

package com.alibaba.fastjson2

/**
 * E.g.
 * ```
 *   val data = "...".parseArray()
 *   val users = data.to<Users>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> JSONArray.to() =
    to(T::class.java)

/**
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val data = "...".parseArray()
 *   val users = data.into<List<User>>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T : Any> JSONArray.into() =
    to<T>(reference<T>().getType())

/**
 * E.g.
 * ```
 *   val data = "...".parseArray()
 *   val users = data.to<User>(6)
 * ```
 *
 * @return [T]?
 * @since 2.0.4
 */
inline fun <reified T> JSONArray.to(
    index: Int,
    vararg features: JSONReader.Feature
) = getObject(
    index, T::class.java, *features
)

/**
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val data = "...".parseArray()
 *   val users = data.into<Map<String, User>>(6)
 * ```
 *
 * @return [T]?
 * @since 2.0.4
 */
inline fun <reified T : Any> JSONArray.into(
    index: Int,
    vararg features: JSONReader.Feature
) = getObject<T>(
    index, reference<T>().getType(), *features
)

/**
 * E.g.
 * ```
 *   val data = "...".parseArray()
 *   val user = data.toList<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> JSONArray.toList(
    vararg features: JSONReader.Feature
) = toList(
    T::class.java, *features
)
