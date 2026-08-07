package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.toJSONString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue3288 {
    @JvmInline
    value class TestBean(val a: Int)

    data class Test2(
        val b: TestBean
    )

    @Test
    fun test() {
        val s1 = "{\"a\":1}"
        assertEquals(s1, TestBean(1).toJSONString())
        assertEquals(s1, JSON.parseObject(s1, TestBean::class.java).toJSONString())
        assertEquals(s1, TestBean(1).toJSONString())
        val s2 = "{\"b\":2}"
        assertEquals(s2, JSON.parseObject(s2, Test2::class.java)
            .toJSONString())
        assertEquals(s2, JSON.parseObject(s2, Test2::class.java).toJSONString())
    }
}
