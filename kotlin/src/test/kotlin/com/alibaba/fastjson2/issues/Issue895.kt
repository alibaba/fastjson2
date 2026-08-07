package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * @author kraity
 */
class Issue895Kt {
    @Test
    fun test() {
        val text = """{"a":1,"b":"asd","c":{"a":9},"d":{"a":10},"e":{"a":11}}"""
        for (bean in arrayOf(
            text.into<Bean<DD>>(),
            JSON.parseObject(text, object : TypeReference<Bean<DD>>() {})
        )) {
            assertEquals(text, bean.toJSONString())
            assertEquals(DD::class.java, bean.c?.javaClass)
            assertEquals(DD::class.java, bean.d?.javaClass)
            assertEquals(DD::class.java, bean.getE()?.javaClass)
        }
    }

    class DD {
        var a: Int? = null
    }

    class Bean<T> {
        var a: Int? = null
        var b: String? = null
        var c: T? = null
            private set

        fun setC(c: T) {
            this.c = c
        }

        var d: T? = null

        private var e: T? = null

        fun getE() = e
        fun setE(e: T) {
            this.e = e
        }
    }
}
