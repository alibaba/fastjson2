package com.alibaba.fastjson2

import com.alibaba.fastjson2.filter.Filter

/**
 * Parse JSON {@link String} into Object
 *
 * <pre>{@code
 *    val text = "..."
 *    val data = text.to<User>()
 * }</pre>
 *
 * @return T?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String.to() =
    JSON.parseObject(this, T::class.java)

/**
 * Parse JSON {@link ByteArray} into Object
 *
 * <pre>{@code
 *    val text = "..."
 *    val data = text.to<User>()
 * }</pre>
 *
 * @return T?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> ByteArray.to() =
    JSON.parseObject(this, T::class.java)

/**
 * Verify the {@link String} is JSON Object
 *
 * <pre>{@code
 *    val text = ...
 *    val bool = text.isJSONObject()
 * }</pre>
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.isJSONObject() = JSON.isValidObject(this)

/**
 * Verify the {@link ByteArray} is JSON Object
 *
 * <pre>{@code
 *    val text = ...
 *    val bool = text.isJSONObject()
 * }</pre>
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun ByteArray?.isJSONObject() = JSON.isValidObject(this)

/**
 * Verify the {@link String} is JSON Array
 *
 * <pre>{@code
 *    val text = ...
 *    val bool = text.isJSONArray()
 * }</pre>
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.isJSONArray() = JSON.isValidArray(this)

/**
 * Verify the {@link ByteArray} is JSON Array
 *
 * <pre>{@code
 *    val text = ...
 *    val bool = text.isJSONArray()
 * }</pre>
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun ByteArray?.isJSONArray() = JSON.isValidArray(this)

/**
 * Parse JSON {@link String} into {@link JSONArray} or {@link JSONObject}
 *
 * @return {@link JSONArray} or {@link JSONObject}
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parse() =
    JSON.parse(this)

/**
 * Parse JSON {@link String} into {@link JSONObject}
 *
 * <pre>{@code
 *    val text = "..."
 *    val data = text.parseObject()
 * }</pre>
 *
 * @return T?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parseObject() =
    JSON.parseObject(this)

/**
 * Parse JSON {@link String} into Object
 *
 * <pre>{@code
 *    val text = "..."
 *    val data = text.parseObject<User>()
 * }</pre>
 *
 * @return T?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseObject() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parse JSON {@link String} into Object
 *
 * @param features features to be enabled in parsing
 * @return T?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, *features
)

/**
 * Parse JSON {@link String} into Object
 *
 * @param features features to be enabled in parsing
 * @return T?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseObject(
    filter: JSONReader.Filter,
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, filter, *features
)

/**
 * Parse JSON {@link String} into {@link JSONArray}
 *
 * <pre>{@code
 *    val text = "..."
 *    val data = text.parseArray()
 * }</pre>
 *
 * @return T?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parseArray() =
    JSON.parseArray(this)

/**
 * Parse JSON {@link String} into {@link List}
 *
 * <pre>{@code
 *    val text = "..."
 *    val list = text.parseArray<User>()
 * }</pre>
 *
 * @return T?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseArray() =
    JSON.parseArray<T>(
        this, T::class.java
    )

/**
 * Parse JSON {@link String} into {@link List}
 *
 * @param features features to be enabled in parsing
 * @return T?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseArray(
    vararg features: JSONReader.Feature
) = JSON.parseArray<T>(
    this, T::class.java, *features
)

/**
 * Serialize Any to JSON {@link String}
 *
 * <pre>{@code
 *    val obj = ...
 *    val text = obj.toJSONString()
 * }</pre>
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONString() = JSON.toJSONString(this)

/**
 * Serialize Any to JSON {@link String}
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONString(
    filter: Filter,
    vararg features: JSONWriter.Feature
) = JSON.toJSONString(
    this, filter, *features
)

/**
 * Serialize Any to JSON {@link String}
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONString(
    filter: Array<out Filter>
) = JSON.toJSONString(
    this, filter
)

/**
 * Serialize Any to JSON {@link String}
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONString(
    filter: Array<out Filter>,
    vararg features: JSONWriter.Feature
) = JSON.toJSONString(
    this, filter, *features
)

/**
 * Serialize Any to JSON {@link String}
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONString(
    vararg features: JSONWriter.Feature
) = JSON.toJSONString(
    this, *features
)

/**
 * Serialize Any to JSON {@link ByteArray}
 *
 * <pre>{@code
 *    val obj = ...
 *    val text = obj.toJSONByteArray()
 * }</pre>
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONByteArray() = JSON.toJSONBytes(this)

/**
 * Serialize Any to JSON {@link ByteArray}
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONByteArray(
    filter: Array<out Filter>
) = JSON.toJSONBytes(
    this, filter
)

/**
 * Serialize Any to JSON {@link ByteArray}
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONByteArray(
    filter: Array<out Filter>,
    vararg features: JSONWriter.Feature
) = JSON.toJSONBytes(
    this, filter, *features
)
