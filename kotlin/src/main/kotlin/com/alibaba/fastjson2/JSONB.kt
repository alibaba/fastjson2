@file:Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)

package com.alibaba.fastjson2

/**
 * Serializes this boolean value to jsonb byte array
 *
 * @return [ByteArray]
 * @since 2.0.7
 */
inline fun Boolean.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serializes this integer value to jsonb byte array
 *
 * @return [ByteArray]
 * @since 2.0.7
 */
inline fun Int.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serializes this long value to jsonb byte array
 *
 * @return [ByteArray]
 * @since 2.0.7
 */
inline fun Long.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serializes this object to jsonb byte array
 *
 * E.g.
 * ```
 *   val obj = ...
 *   val text = obj.toJSONB()
 * ```
 *
 * @return [ByteArray]
 * @since 2.0.7
 */
inline fun Any?.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serializes this object to jsonb byte array with the specified symbolTable
 *
 * @return [ByteArray]
 * @since 2.0.7
 */
inline fun Any?.toJSONB(
    symbolTable: SymbolTable
) = JSONB.toBytes(
    this, symbolTable
)

/**
 * Serializes this object to jsonb byte array with the specified features and symbolTable
 *
 * @return [ByteArray]
 * @since 2.0.7
 */
inline fun Any?.toJSONB(
    symbolTable: SymbolTable,
    vararg features: JSONWriter.Feature
) = JSONB.toBytes(
    this, symbolTable, *features
)
