package com.alibaba.fastjson2.issues_1500

import com.alibaba.fastjson2.JSON
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf

class Issue1540 {
    @Test
    fun test_map() {
        val map = emptyMap<Any?, Any?>()
        assertEquals("{}", JSON.toJSONString(map))
        assertInstanceOf(map::class.java, JSON.parseObject("{}", map::class.java))
    }

    @Test
    fun test_set() {
        val set = emptySet<Any?>()
        assertEquals("[]", JSON.toJSONString(set))
        assertInstanceOf(set::class.java, JSON.parseObject("[]", set::class.java))
    }

    @Test
    fun test_list() {
        val list = emptyList<Any?>()
        assertEquals("[]", JSON.toJSONString(list))
        assertInstanceOf(list::class.java, JSON.parseObject("[]", list::class.java))
    }
}
