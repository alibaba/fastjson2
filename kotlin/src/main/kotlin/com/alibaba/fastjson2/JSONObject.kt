package com.alibaba.fastjson2

/**
 * E.g.
 * ```
 *   val data = "...".parseObject()
 *   val user = data.toObject<User>()
 * ```
 *
 * @receiver JSONObject
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONObject.toObject(
    vararg features: JSONReader.Feature
) = toJavaObject(
    T::class.java, *features
)

/**
 * E.g.
 * ```
 *  // JSONObject
 *   val data = "...".parseObject()
 *   val user = data.getObject<User>("key")
 * ```
 *
 * @receiver JSONObject
 * @param key the key whose associated value is to be returned
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONObject.getObject(
    key: String,
    vararg features: JSONReader.Feature
) = getObject(
    key, T::class.java, *features
)
