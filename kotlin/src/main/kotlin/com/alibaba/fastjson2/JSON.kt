package com.alibaba.fastjson2

/**
 * Serialize Java Object to JSON {@link String}
 *
 * <pre>{@code
 *    val obj = ...
 *    val text = obj.toJSONString()
 * }</pre>
 *
 * @receiver Any?
 */
@Suppress("NOTHING_TO_INLINE", "HasPlatformType")
inline fun Any?.toJSONString() = JSON.toJSONString(this)

/**
 * Parse JSON {@link String} into Object
 *
 * <pre>{@code
 *    val text = "..."
 *    val data = parseObject<User>(text)
 * }</pre>
 *
 * @param text the JSON {@link String} to be parsed
 * @return T?
 */
@Suppress("HasPlatformType")
inline fun <reified T> parseObject(
    text: String
) = JSON.parseObject(
    text, T::class.java
)

/**
 * Parse JSON {@link String} into Object
 *
 * @param text the JSON {@link String} to be parsed
 * @param features features to be enabled in parsing
 * @return T?
 */
@Suppress("HasPlatformType")
inline fun <reified T> parseObject(
    text: String,
    filter: JSONReader.Filter,
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    text, T::class.java, filter, *features
)

/**
 * Parse JSON {@link String} into Object
 *
 * @param text the JSON {@link String} to be parsed
 * @param features features to be enabled in parsing
 * @return T?
 */
@Suppress("HasPlatformType")
inline fun <reified T> parseObject(
    text: String,
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    text, T::class.java, *features
)
