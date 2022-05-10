package com.alibaba.fastjson2

/**
 * <pre>{@code
 *    val data = "...".parseArray()
 *    val user = data.toList<User>()
 * }</pre>
 *
 * @receiver JSONArray
 * @param features features to be enabled in parsing
 * @return T?
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONArray.toList(
    vararg features: JSONReader.Feature
) = toJavaList(T::class.java, *features)

/**
 * <pre>{@code
 *   // JSONArray
 *   val data = "...".parseArray()
 *   val user = data.getObject<User>(0)
 * }</pre>
 *
 * @receiver JSONArray
 * @param index index of the element to return
 * @param features features to be enabled in parsing
 * @return T?
 */
@Suppress("HasPlatformType")
inline fun <reified T> JSONArray.getObject(
    index: Int,
    vararg features: JSONReader.Feature
) = getObject(index, T::class.java, *features)
