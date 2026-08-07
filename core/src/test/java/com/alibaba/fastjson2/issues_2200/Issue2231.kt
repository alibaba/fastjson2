package com.alibaba.fastjson2.issues_2200

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONB
import com.alibaba.fastjson2.JSONReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue2231 {
    @Test
    fun test() {
        var data = JSON.parseObject("""{}""", Hello::class.java)
        assertEquals("hello", data.value)

        var jsonb = JSONB.toBytes(data)

        assertEquals(
            """{
	"value":"hello"
}""",
            JSONB.toJSONString(jsonb)
        )
        val data1 = JSONB.parseObject(jsonb, Hello::class.java)
        assertEquals(data.value, data1.value)

        val data2 = JSONB.parseObject(jsonb, Hello::class.java, JSONReader.Feature.FieldBased)
        assertEquals(data.value, data2.value)
    }
}

data class Hello(
    val value: String = "hello"
)
