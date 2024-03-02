package com.alibaba.fastjson2.issues_2200

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import com.alibaba.fastjson2.annotation.JSONField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue2276 {
    @Test
    fun test() {
        var str = JSON.toJSONString(Type.ACCEPTED, JSONWriter.Feature.WriteEnumsUsingName)
        assertEquals("\"Accepted\"", str)
        assertEquals(Type.ACCEPTED, JSON.parseObject(str, Type::class.java))
    }

    enum class Type {
        @JSONField(name = "Accepted")
        ACCEPTED,
        @JSONField(name = "Pending")
        PENDING,
        @JSONField(name = "Rejected")
        REJECTED
    }
}
