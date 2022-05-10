package com.alibaba.fastjson2

/**
 * <pre>{@code
 *    val data = "...".parseObject()
 *    val user = data.toObject<User>()
 * }</pre>
 *
 * @receiver JSONObject
 * @param features features to be enabled in parsing
 * @return T?
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONObject.toObject(
    vararg features: JSONReader.Feature
) = toJavaObject(T::class.java, *features)

/**
 * <pre>{@code
 *   // JSONObject
 *   val data = "...".parseObject()
 *   val user = data.getObject<User>("key")
 * }</pre>
 *
 * @receiver JSONObject
 * @param key the key whose associated value is to be returned
 * @param features features to be enabled in parsing
 * @return T?
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONObject.getObject(
    key: String,
    vararg features: JSONReader.Feature
) = getObject(key, T::class.java, *features)
