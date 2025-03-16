package com.alibaba.fastjson2.issues_3300

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.annotation.JSONField
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class Issue3287 {
    @Test
    fun test() {
        val bean = Bean(1, 2);
        Assertions.assertEquals("{\"a\":1,\"b\":2}", JSON.toJSONString(bean))
    }

    data class Bean(
        @JSONField(serialize = true, deserialize = true)
        private val a: Int,
        val b: Int
    )
}
