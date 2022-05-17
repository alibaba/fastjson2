package com.alibaba.fastjson2

/**
 * E.g.
 * ```
 *   val text = "..."
 *   val path = text.toPath()
 *   // path.extract(...)
 * ```
 *
 * @return [JSONPath]
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun String.toPath() =
    JSONPath.of(this)

/**
 * E.g.
 * ```
 *   val root = ...
 *   val path = "..."
 *   val data = root.eval(path)
 * ```
 *
 * @return [Any]?
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any.eval(
    path: String
) = JSONPath.of(path).eval(this)

/**
 * E.g.
 * ```
 *   val root = ...
 *   val path = "..."
 *   val data = root.contains(path)
 * ```
 *
 * @return [Boolean]
 * @since 2.0.4
 */
@Suppress(
    "HasPlatformType",
    "NOTHING_TO_INLINE"
)
inline fun Any.contains(
    path: String
) = JSONPath.of(path).contains(this)
