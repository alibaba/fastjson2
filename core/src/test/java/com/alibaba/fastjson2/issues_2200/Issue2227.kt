package com.alibaba.fastjson2.issues_2200

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Issue2227 {
    @Test
    fun testAssertJSONObjectWithVersion2() {
        val jsonObject = JSON.toJSON(OuterClass()) as JSONObject
        // 以下断言失败
        Assertions.assertTrue(jsonObject["nestedClass"] is JSONObject)
    }

    @Test
    fun testAssertJSONObjectWithVersion1() {
        val jsonObject = com.alibaba.fastjson.JSON.toJSON(OuterClass()) as com.alibaba.fastjson.JSONObject
        // 以下断言成功
        Assertions.assertTrue(jsonObject["nestedClass"] is com.alibaba.fastjson.JSONObject)
    }

    data class OuterClass(val id: Int = 1, val nestedClass: NestedClass = NestedClass())

    data class NestedClass(val id: Int = 2)
}
