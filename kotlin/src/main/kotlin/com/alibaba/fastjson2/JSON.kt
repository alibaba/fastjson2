@file:Suppress(
    "HasPlatformType", "NOTHING_TO_INLINE"
)

package com.alibaba.fastjson2

import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset

import com.alibaba.fastjson2.filter.Filter

/**
 * Parses the json string as [T]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val data = json.to<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> String?.to() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parses the json string as [T]
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val data = json.into<Map<String, User>>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T : Any> String?.into() =
    JSON.parseObject<T>(
        this, reference<T>().getType()
    )

/**
 * Parses the json byte array as [T]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val data = json.to<User>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> ByteArray?.to() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parses the json byte array as [T]
 * Implemented using [TypeReference]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val data = json.into<Map<String, User>>()
 * ```
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T : Any> ByteArray.into() =
    JSON.parseObject<T>(
        this, reference<T>().getType()
    )

/**
 * Parses the json stream as [T] with the specified features
 *
 * E.g.
 * ```
 *   val url = ...
 *   val data = url.to<User>()
 * ```
 *
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.30
 * @throws JSONException If an I/O error occurs or other
 */
inline fun <reified T> URL?.to(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, *features
)

/**
 * Parses the json stream as [T] with the specified features
 *
 * E.g.
 * ```
 *   val url = ...
 *   val data = url.into<Map<String, User>>()
 * ```
 *
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.4
 * @throws JSONException If an I/O error occurs or other
 */
inline fun <reified T : Any> URL?.into(
    vararg features: JSONReader.Feature
) = JSON.parseObject<T>(
    this, reference<T>().getType(), *features
)

/**
 * Parses the json stream as [T] with the specified features
 *
 * E.g.
 * ```
 *   val in = ...
 *   val data = in.to<User>()
 * ```
 *
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.4
 */
inline fun <reified T> InputStream?.to(
    vararg features: JSONReader.Feature
) = JSON.parseObject<T>(
    this, T::class.java, *features
)

/**
 * Parses the json stream as [T] with the specified features
 *
 * E.g.
 * ```
 *   val in = ...
 *   val data = in.into<Map<String, User>>()
 * ```
 *
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.4
 */
inline fun <reified T : Any> InputStream?.into(
    vararg features: JSONReader.Feature
) = JSON.parseObject<T>(
    this, reference<T>().getType(), *features
)

/**
 * Verify that the json string is a JsonObject
 *
 * E.g.
 * ```
 *   val json = ...
 *   val bool = json.isJSONObject()
 * ```
 *
 * @receiver [Boolean]
 * @since 2.0.3
 */
inline fun String?.isJSONObject() =
    JSON.isValidObject(this)

/**
 * Verify that the json byte array is a JsonObject
 *
 * E.g.
 * ```
 *   val json = ...
 *   val bool = json.isJSONObject()
 * ```
 *
 * @receiver [Boolean]
 * @since 2.0.3
 */
inline fun ByteArray?.isJSONObject() =
    JSON.isValidObject(this)

/**
 * Verify that the json string is a JsonArray
 *
 * E.g.
 * ```
 *   val json = ...
 *   val bool = json.isJSONArray()
 * ```
 *
 * @receiver [Boolean]
 * @since 2.0.3
 */
inline fun String?.isJSONArray() =
    JSON.isValidArray(this)

/**
 * Verify that the json byte array is a JsonArray
 *
 * E.g.
 * ```
 *   val json = ...
 *   val bool = json.isJSONArray()
 * ```
 *
 * @receiver Any?
 * @since 2.0.3
 */
inline fun ByteArray?.isJSONArray() =
    JSON.isValidArray(this)

/**
 * Parses the json string as [JSONObject]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val data = json.parseObject()
 * ```
 *
 * @return [JSONObject]?
 * @since 2.0.3
 */
inline fun String?.parseObject() =
    JSON.parseObject(this)

/**
 * Parses the json byte array as [JSONObject]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val data = json.parseObject()
 * ```
 *
 * @return [JSONObject]?
 * @since 2.0.4
 */
inline fun ByteArray?.parseObject() =
    JSON.parseObject(this)

/**
 * Parses the json string as [JSONObject] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [JSONObject]?
 * @since 2.0.4
 */
inline fun String?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, *features
)

/**
 * Parses the json byte array as [JSONObject] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [JSONObject]?
 * @since 2.0.4
 */
inline fun ByteArray?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, *features
)

/**
 * Parses the json reader as [JSONObject] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [JSONObject]?
 * @since 2.0.30
 */
inline fun Reader?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, *features
)

/**
 * Parses the json stream as [JSONObject] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [JSONObject]?
 * @since 2.0.4
 */
inline fun InputStream?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, *features
)

/**
 * Parses the json string as [T]
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> String?.parseObject() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parses the json string as [T] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> String?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, *features
)

/**
 * Parses the json string as [T] with the specified filter and features
 *
 * @param filter the specified filter is applied to parsing
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> String?.parseObject(
    filter: Filter,
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, filter, *features
)

/**
 * Parses the json byte array as [T]
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> ByteArray?.parseObject() =
    JSON.parseObject(
        this, T::class.java
    )

/**
 * Parses the json byte array as [T] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.30
 */
inline fun <reified T> ByteArray?.parseObject(
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, *features
)

/**
 * Parses the json byte array as [T] with the specified filter and features
 *
 * @param filter the specified filter is applied to parsing
 * @param features the specified features is applied to parsing
 * @return [T]?
 * @since 2.0.30
 */
inline fun <reified T> ByteArray?.parseObject(
    filter: Filter,
    vararg features: JSONReader.Feature
) = JSON.parseObject(
    this, T::class.java, filter, *features
)

/**
 * Parses the json byte array as [T] with the specified offset, length and charset
 *
 * @return [T]?
 * @since 2.0.3
 */
inline fun <reified T> ByteArray.parseObject(
    offset: Int,
    length: Int = size,
    charset: Charset = Charsets.UTF_8
) = JSON.parseObject(
    this, offset, length, charset, T::class.java
)

/**
 * Parses the json reader as [T] with the specified delimiter and consumer
 *
 * E.g.
 * ```
 *   val reader = ...
 *   reader.parseObject<User>() {
 *       val id = it.id
 *   }
 * ```
 *
 * @param delimiter the specified delimiter is used to distinguish
 * @since 2.0.3
 */
inline fun <reified T> Reader.parseObject(
    delimiter: Char = '\n',
    noinline consumer: (T) -> Unit
) = JSON.parseObject(
    this, delimiter, T::class.java, consumer
)

/**
 * Parses the json stream as [T] with the specified features and consumer
 *
 * E.g.
 * ```
 *   val input = ...
 *   input.parseObject<User> {
 *       val id = it.id
 *   }
 * ```
 *
 * @param features the specified features is applied to parsing
 * @param consumer the specified consumer is applied to parsing
 * @since 2.0.3
 */
inline fun <reified T> InputStream.parseObject(
    vararg features: JSONReader.Feature,
    noinline consumer: (T) -> Unit
) = JSON.parseObject(
    this, T::class.java, consumer, *features
)

/**
 * Parses the json stream as [T] with the specified charset and features and so on
 *
 * E.g.
 * ```
 *   val input = ...
 *   input.parseObject<User>(Charsets.UTF_8) {
 *       val id = it.id
 *   }
 * ```
 *
 * @param charset the specified charset of the stream
 * @param features the specified features is applied to parsing
 * @param consumer the specified consumer is applied to parsing
 * @param delimiter the specified delimiter is used to distinguish
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
 * Parses the json string as [JSONArray]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val data = json.parseArray()
 * ```
 *
 * @return [JSONArray]?
 * @since 2.0.3
 */
inline fun String?.parseArray() =
    JSON.parseArray(this)

/**
 * Parses the json string as a list of [T]
 *
 * E.g.
 * ```
 *   val json = ...
 *   val list = json.parseArray<User>()
 * ```
 *
 * @return [List]?
 * @since 2.0.3
 */
inline fun <reified T> String?.parseArray() =
    JSON.parseArray(
        this, T::class.java
    )

/**
 * Parses the json string as a list of [T] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [List]?
 * @since 2.0.3
 */
inline fun <reified T> String?.parseArray(
    vararg features: JSONReader.Feature
) = JSON.parseArray(
    this, T::class.java, *features
)

/**
 * Parses the json byte array as a list of [T] with the specified features
 *
 * @param features the specified features is applied to parsing
 * @return [List]?
 * @since 2.0.3
 */
inline fun <reified T> ByteArray?.parseArray(
    vararg features: JSONReader.Feature
) = JSON.parseArray(
    this, T::class.java, *features
)

/**
 * Serializes this object to the json string
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
inline fun Any?.toJSONString() =
    JSON.toJSONString(this)

/**
 * Serializes this object to the json string with the specified filters
 *
 * @param filters the specified filters is applied to serialization
 * @return [String]
 * @since 2.0.3
 */
inline fun Any?.toJSONString(
    filters: Array<out Filter>
) = JSON.toJSONString(
    this, filters
)

/**
 * Serializes this object to the json string with the specified features
 *
 * @param features the specified features is applied to serialization
 * @return [String]
 * @since 2.0.3
 */
inline fun Any?.toJSONString(
    vararg features: JSONWriter.Feature
) = JSON.toJSONString(
    this, *features
)

/**
 * Serializes this object to the json string with the specified filter and features
 *
 * @param filter the specified filter is applied to serialization
 * @param features the specified features is applied to serialization
 * @return [String]
 * @since 2.0.3
 */
inline fun Any?.toJSONString(
    filter: Filter,
    vararg features: JSONWriter.Feature
) = JSON.toJSONString(
    this, filter, *features
)

/**
 * Serializes this object to the json string with the specified filters and features
 *
 * @param filters the specified filters is applied to serialization
 * @param features the specified features is applied to serialization
 * @return [String]
 * @since 2.0.3
 */
inline fun Any?.toJSONString(
    filters: Array<out Filter>,
    vararg features: JSONWriter.Feature
) = JSON.toJSONString(
    this, filters, *features
)

/**
 * Serializes this object to the json byte array
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
inline fun Any?.toJSONByteArray() =
    JSON.toJSONBytes(this)

/**
 * Serializes this object to the json byte array with the specified filters
 *
 * @param filters the specified filters is applied to serialization
 * @return [ByteArray]
 * @since 2.0.3
 */
inline fun Any?.toJSONByteArray(
    filters: Array<out Filter>
) = JSON.toJSONBytes(
    this, filters
)

/**
 * Serializes this object to the json byte array with the specified features
 *
 * @param features the specified features is applied to serialization
 * @return [ByteArray]
 * @since 2.0.3
 */
inline fun Any?.toJSONByteArray(
    vararg features: JSONWriter.Feature
) = JSON.toJSONBytes(
    this, *features
)

/**
 * Serializes this object to the json byte array with the specified filters and features
 *
 * @param filters the specified filters is applied to serialization
 * @param features the specified features is applied to serialization
 * @return [ByteArray]
 * @since 2.0.3
 */
inline fun Any?.toJSONByteArray(
    filters: Array<out Filter>,
    vararg features: JSONWriter.Feature
) = JSON.toJSONBytes(
    this, filters, *features
)
