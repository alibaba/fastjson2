package com.alibaba.fastjson2

/**
 * E.g.
 * ```
 *   val data = "...".parseObject()
 *   val user = data.to<User>()
 * ```
 *
 * @receiver JSONObject
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONObject.to(
    vararg features: JSONReader.Feature
) = to(
    T::class.java, *features
)

/**
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val data = "...".parseObject()
 *   val user = data.into<User>()
 * ```
 *
 * @receiver JSONObject
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.4
 */
@Suppress("HasPlatformType")
inline fun <reified T : Any> JSONObject.into(
    vararg features: JSONReader.Feature
) = to<T>(
    reference<T>().getType(), *features
)

/**
 * E.g.
 * ```
 *  // JSONObject
 *   val data = "...".parseObject()
 *   val user = data.to<User>("key")
 * ```
 *
 * @receiver JSONObject
 * @param key the key whose associated value is to be returned
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.4
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONObject.to(
    key: String,
    vararg features: JSONReader.Feature
) = getObject(
    key, T::class.java, *features
)

/**
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val data = "...".parseObject()
 *   val users = data.into<List<User>>("key")
 * ```
 *
 * @return [T]?
 * @since 2.0.4
 */
@Suppress("HasPlatformType")
inline fun <reified T : Any> JSONObject.into(
    key: String,
    vararg features: JSONReader.Feature
) = getObject<T>(
    key, reference<T>().getType(), *features
)
