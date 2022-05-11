package com.alibaba.fastjson2

/**
 * E.g.
 * ```
 *   val data = "...".parseArray()
 *   val user = data.toList<User>()
 * ```
 *
 * @receiver JSONArray
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONArray.toList(
    vararg features: JSONReader.Feature
) = toJavaList(
    T::class.java, *features
)

/**
 * E.g.
 * ```
 *   // JSONArray
 *   val data = "...".parseArray()
 *   val user = data.getObject<User>(0)
 * ```
 *
 * @receiver JSONArray
 * @param index index of the element to return
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONArray.getObject(
    index: Int,
    vararg features: JSONReader.Feature
) = getObject(
    index, T::class.java, *features
)
