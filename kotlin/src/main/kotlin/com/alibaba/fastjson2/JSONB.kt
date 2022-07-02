package com.alibaba.fastjson2

/**
 * Serialize Boolean to JSONB [ByteArray]
 *
 * @receiver Boolean
 * @return [ByteArray]
 * @since 2.0.7
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Boolean.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serialize Int to JSONB [ByteArray]
 *
 * @receiver Int
 * @return [ByteArray]
 * @since 2.0.7
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Int.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serialize Long to JSONB [ByteArray]
 *
 * @receiver Long
 * @return [ByteArray]
 * @since 2.0.7
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Long.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serialize [Any]? to JSONB [ByteArray]
 *
 * E.g.
 * ```
 *   val obj = ...
 *   val text = obj.toJSONB()
 * ```
 *
 * @receiver [Any]?
 * @return [ByteArray]
 * @since 2.0.7
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONB() =
    JSONB.toBytes(this)

/**
 * Serialize [Any]? to JSONB [ByteArray]
 *
 * @receiver [Any]?
 * @return [ByteArray]
 * @since 2.0.7
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONB(
    symbolTable: SymbolTable
) = JSONB.toBytes(
    this, symbolTable
)

/**
 * Serialize [Any]? to JSONB [ByteArray]
 *
 * @param features features to be enabled in parsing
 * @receiver [Any]?
 * @return [ByteArray]
 * @since 2.0.7
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any?.toJSONB(
    symbolTable: SymbolTable,
    vararg features: JSONWriter.Feature
) = JSONB.toBytes(
    this, symbolTable, *features
)
