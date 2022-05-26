package com.alibaba.fastjson2

import com.alibaba.fastjson2.filter.Filter

import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset
import kotlin.jvm.Throws

/**
 * Parse JSON [String] into [T]
 *
 * E.g.
 * ```
 *   val text = "..."
 *   val data = text.to<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.to() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parse JSON [String] into [T]
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val text = "..."
 *   val data = text.into<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T : Any> String?.into() =
    JSON.parseObject<T>(
        this, reference<T>().getType()
    )

/**
 * Parse JSON [ByteArray] into [T]
 *
 * E.g.
 * ```
 *   val text = ...
 *   val data = text.to<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> ByteArray?.to() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parse JSON [ByteArray] into [T]
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val text = "..."
 *   val data = text.into<Map<String, User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T : Any> ByteArray.into() =
    JSON.parseObject<T>(
        this, reference<T>().getType()
    )

/**
 * Parse JSON [URL] into [T]
 *
 * E.g.
 * ```
 *   val url = ...
 *   val data = url.to<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.4
 * @throws JSONException If an I/O error occurs
 */
@Suppress("HasPlatformType")
@Throws(JSONException::class)
inline fun <reified T> URL?.to(
    vararg features: JSONReader.Feature
) = JSON.parseObject<T>(
    this, T::class.java, *features
)

/**
 * Parse JSON [URL] into [T]
 *
 * E.g.
 * ```
 *   val url = ...
 *   val data = in.into<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.4
 * @throws JSONException If an I/O error occurs
 */
@Suppress("HasPlatformType")
@Throws(JSONException::class)
inline fun <reified T : Any> URL?.into(
    vararg features: JSONReader.Feature
) = JSON.parseObject<T>(
    this, reference<T>().getType(), *features
)

/**
 * Parse JSON [InputStream] into [T]
 *
 * E.g.
 * ```
 *   val in = ...
 *   val data = in.to<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.4
 */
@Suppress("HasPlatformType")
inline fun <reified T> InputStream?.to(
    vararg features: JSONReader.Feature
) = JSON.parseObject<T>(
    this, T::class.java, *features
)

/**
 * Parse JSON [InputStream] into [T]
 *
 * E.g.
 * ```
 *   val in = ...
 *   val data = in.into<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.4
 */
@Suppress("HasPlatformType")
inline fun <reified T : Any> InputStream?.into(
    vararg features: JSONReader.Feature
) = JSON.parseObject<T>(
    this, reference<T>().getType(), *features
)

/**
 * Verify the [String] is JSON `Object`
 *
 * E.g.
 * ```
 *   val text = ...
 *   val bool = text.isJSONObject()
 * ```
 *
 * @receiver [Boolean]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.isJSONObject() =
    JSON.isValidObject(this)

/**
 * Verify the [ByteArray] is JSON `Object`
 *
 * E.g.
 * ```
 *   val text = ...
 *   val bool = text.isJSONObject()
 * ```
 *
 * @receiver [Boolean]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun ByteArray?.isJSONObject() =
    JSON.isValidObject(this)

/**
 * Verify the [String] is JSON `Array`
 *
 * E.g.
 * ```
 *   val text = ...
 *   val bool = text.isJSONArray()
 * ```
 *
 * @receiver [Boolean]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.isJSONArray() =
    JSON.isValidArray(this)

/**
 * Verify the [ByteArray] is JSON `Array`
 *
 * E.g.
 * ```
 *   val text = ...
 *   val bool = text.isJSONArray()
 * ```
 *
 * @receiver Any?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun ByteArray?.isJSONArray() =
    JSON.isValidArray(this)

/**
 * Parse JSON [String] into [JSONArray] or [JSONObject]
 *
 * @return [JSONArray] or [JSONObject]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parse() =
    JSON.parse(this)

/**
 * Parse JSON [String] into [JSONArray] or [JSONObject]
 *
 * @param features features to be enabled in parsing
 * @return [JSONArray] or [JSONObject]
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parse(
    vararg features: JSONReader.Feature
) = JSON.parse(
    this, *features
)

/**
 * Parse JSON [String] into [JSONObject]
 *
 * E.g.
 * ```
 *   val text = "..."
 *   val data = text.parseObject()
 * ```
 *
 * @return [JSONObject]?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parseObject() =
    JSON.parseObject(this)

/**
 * Parse JSON [ByteArray] into [JSONObject]
 *
 * E.g.
 * ```
 *   val text = ...
 *   val data = text.parseObject()
 * ```
 *
 * @return [JSONObject]?
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun ByteArray?.parseObject() =
    JSON.parseObject(this)

/**
 * Parse JSON [ByteArray] into [JSONObject]
 *
 *
 * @return [JSONObject]?
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun ByteArray?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, *features
)

/**
 * Parse JSON [String] into [JSONObject]
 *
 * @param features features to be enabled in parsing
 * @return [JSONObject]?
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, *features
)

/**
 * Parse JSON [InputStream] into [JSONObject]
 *
 * @param features features to be enabled in parsing
 * @return [JSONObject]?
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun InputStream?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, *features
)

/**
 * Parse JSON [String] into [T]
 *
 * E.g.
 * ```
 *   val text = "..."
 *   val data = text.parseObject<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseObject() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parse JSON [String] into [T]
 *
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, *features
)

/**
 * Parse JSON [String] into [T]
 *
 * @param features features to be enabled in parsing
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseObject(
    filter: Filter,
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, filter, *features
)

/**
 * Parse JSON [ByteArray] into [T]
 *
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> ByteArray?.parseObject(
) = JSON.parseObject(
    this, T::class.java
)

/**
 * Parse JSON [ByteArray] into [T]
 *
 * @return [T]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> ByteArray.parseObject(
    offset: Int,
    length: Int = size,
    charset: Charset = Charsets.UTF_8
) = JSON.parseObject<T>(
    this, offset, length, charset, T::class.java
)

/**
 * Parse JSON [Reader] into [T]
 *
 * E.g.
 * ```
 *   val reader = ...
 *   reader.parseObject<User>() {
 *       val id = it.id
 *   }
 * ```
 *
 * @param delimiter specify the delimiter
 * @param consumer Function1<T, Unit>
 * @since 2.0.3
 */
inline fun <reified T> Reader.parseObject(
    delimiter: Char = '\n',
    noinline consumer: (T) -> Unit
) = JSON.parseObject(
    this, delimiter, T::class.java, consumer
)

/**
 * Parse JSON [InputStream] into [T]
 *
 * E.g.
 * ```
 *   val input = ...
 *   input.parseObject<User> {
 *       val id = it.id
 *   }
 * ```
 *
 * @param features features to be enabled in parsing
 * @param consumer Function1<T, Unit>
 * @since 2.0.3
 */
inline fun <reified T> InputStream.parseObject(
    vararg features: JSONReader.Feature,
    noinline consumer: (T) -> Unit
) = JSON.parseObject(
    this, T::class.java, consumer, *features
)

/**
 * Parse JSON [InputStream] into [T]
 *
 * E.g.
 * ```
 *   val input = ...
 *   input.parseObject<User>(Charsets.UTF_8) {
 *       val id = it.id
 *   }
 * ```
 *
 * @param charset   specify [Charset] to parse
 * @param delimiter specify the delimiter
 * @param features features to be enabled in parsing
 * @param consumer Function1<T, Unit>
 * @since 2.0.3
 */
inline fun <reified T> InputStream.parseObject(
    charset: Charset,
    delimiter: Char = '\n',
    vararg features: JSONReader.Feature,
    noinline consumer: (T) -> Unit
) = JSON.parseObject(
    this, charset, delimiter, T::class.java, consumer, *features
)

/**
 * Parse JSON [String] into [JSONArray]
 *
 * E.g.
 * ```
 *   val text = "..."
 *   val data = text.parseArray()
 * ```
 *
 * @return [JSONArray]?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String?.parseArray() =
    JSON.parseArray(this)

/**
 * Parse JSON [String] into [List]
 *
 * E.g.
 * ```
 *   val text = "..."
 *   val list = text.parseArray<User>()
 * ```
 *
 * @return [List]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseArray() =
    JSON.parseArray<T>(
        this, T::class.java
    )

/**
 * Parse JSON [String] into [List]
 *
 * @param features features to be enabled in parsing
 * @return [List]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> String?.parseArray(
    vararg features: JSONReader.Feature
) = JSON.parseArray<T>(
    this, T::class.java, *features
)

/**
 * Parse JSON [ByteArray] into [List]
 *
 * @param features features to be enabled in parsing
 * @return [List]?
 * @since 2.0.3
 */
@Suppress("HasPlatformType")
inline fun <reified T> ByteArray?.parseArray(
    vararg features: JSONReader.Feature
) = JSON.parseArray<T>(
    this, T::class.java, *features
)

/**
 * Serialize [Any]? to JSON [String]
 *
 * E.g.
 * ```
 *   val obj = ...
 *   val text = obj.toJSONString()
 * ```
 *
 * @receiver [Any]?
 * @return [String]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONString() =
    JSON.toJSONString(this)

/**
 * Serialize [Any]? to JSON [String]
 *
 * @receiver [Any]?
 * @return [String]
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
 * Serialize [Any]? to JSON [String]
 *
 * @receiver [Any]?
 * @return [String]
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
 * Serialize [Any]? to JSON [String]
 *
 * @receiver [Any]?
 * @return [String]
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
 * Serialize [Any]? to JSON [String]
 *
 * @receiver [Any]?
 * @return [String]
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
 * Serialize [Any]? to JSON [ByteArray]
 *
 * E.g.
 * ```
 *   val obj = ...
 *   val text = obj.toJSONByteArray()
 * ```
 *
 * @receiver [Any]?
 * @return [ByteArray]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONByteArray() =
    JSON.toJSONBytes(this)

/**
 * Serialize [Any]? to JSON [ByteArray]
 *
 * @receiver [Any]?
 * @return [ByteArray]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONByteArray(
    filters: Array<out Filter>
) = JSON.toJSONBytes(
    this, filters
)

/**
 * Serialize [Any]? to JSON [ByteArray]
 *
 * @receiver [Any]?
 * @return [ByteArray]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONByteArray(
    vararg features: JSONWriter.Feature
) = JSON.toJSONBytes(
    this, *features
)

/**
 * Serialize [Any]? to JSON [ByteArray]
 *
 * @receiver [Any]?
 * @return [ByteArray]
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONByteArray(
    filters: Array<out Filter>,
    vararg features: JSONWriter.Feature
) = JSON.toJSONBytes(
    this, filters, *features
)

/**
 * Serialize [Any]? to JSON [ByteArray] and write to [OutputStream]
 *
 * E.g.
 * ```
 *   val out = ...
 *   val data = ...
 *   data.writeTo(out)
 * ```
 *
 * @receiver [Any]?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.writeTo(
    out: OutputStream,
    vararg features: JSONWriter.Feature
) = JSON.writeTo(
    out, this, *features
)

/**
 * Serialize [Any]? to JSON [ByteArray] and write to [OutputStream]
 *
 * E.g.
 * ```
 *   val out = ...
 *   val data = ...
 *   val filters = ...
 *   data.writeTo(out, filters)
 * ```
 *
 * @receiver [Any]?
 * @since 2.0.3
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.writeTo(
    out: OutputStream,
    filters: Array<out Filter>,
    vararg features: JSONWriter.Feature
) = JSON.writeTo(
    out, this, filters, *features
)
