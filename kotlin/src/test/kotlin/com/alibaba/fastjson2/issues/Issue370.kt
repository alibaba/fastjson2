package com.alibaba.fastjson2.issues

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

import com.alibaba.fastjson2.*

class Issue370 {

    @Test
    fun test0() {
        val data = "{\"test\":\"999\"}".to<TestBean>()
        assertEquals("999", data.test)
        assertEquals("2222", data.test2)
    }

    @Test
    fun test1() {
        val data = "{\"test2\":\"999\"}".to<TestBean>()
        assertEquals("111", data.test)
        assertEquals("999", data.test2)
    }

    @Test
    fun test2() {
        val bean = TestBean()
        val text = bean.toJSONString()
        val data = text.to<TestBean>()
        assertEquals("TestBean(test=111, test2=2222)", data.toString())
    }

    data class TestBean(
        var test: String = "111",
        val test2: String = "2222"
    )
}
