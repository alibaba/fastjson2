package com.alibaba.fastjson2.issues_2200

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.annotation.JSONField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue2264 {
    @Test
    fun test() {
        assertEquals("{\"isOk\":true}", (JSON.toJSONString(Bean(true))))
    }

    data class Bean(@JSONField(name = "isOk") val isOk: Boolean)

    @Test
    fun test1() {
        assertEquals("{\"xx\":true}", (JSON.toJSONString(Bean1(true))))
    }

    data class Bean1(@JSONField(name = "xx") val isOk: Boolean)

    @Test
    fun test2() {
        assertEquals("{\"k\":true}", (JSON.toJSONString(Bean2(true))))
    }

    data class Bean2(val k: Boolean)
}
