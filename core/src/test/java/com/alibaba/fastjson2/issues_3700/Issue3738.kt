package com.alibaba.fastjson2.issues_3700

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue3738 {
    @Test
    fun test() {
        val a = JSON.parseObject("{\"isReplaced\":true}", Bean::class.java, JSONReader.Feature.SupportSmartMatch)
        assertEquals(true, a.isReplaced)
    }

    class Bean {
        var isReplaced: Boolean? = false
    }
}
