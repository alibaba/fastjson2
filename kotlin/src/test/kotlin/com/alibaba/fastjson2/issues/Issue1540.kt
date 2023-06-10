package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.toJSONString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf

class Issue1540 {

    @Test
    fun test_map() {
        val map = emptyMap<Any?, Any?>()
        assertEquals("{}", map.toJSONString())
        assertInstanceOf(map::class.java, JSON.parseObject("{}", map::class.java))
    }

    @Test
    fun test_set() {
        val set = emptySet<Any?>()
        assertEquals("[]", set.toJSONString())
        assertInstanceOf(set::class.java, JSON.parseObject("[]", set::class.java))
    }

    @Test
    fun test_list() {
        val list = emptyList<Any?>()
        assertEquals("[]", list.toJSONString())
        assertInstanceOf(list::class.java, JSON.parseObject("[]", list::class.java))
    }
}
