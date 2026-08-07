package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONWriter
import com.alibaba.fastjson2.annotation.JSONField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue928 {
    @Test
    fun test() {
        assertEquals(
            "\"Accepted\"",
            JSON.toJSONString(RegistrationStatusEnum.ACCEPT, JSONWriter.Feature.WriteEnumUsingToString)
        )
    }

    enum class RegistrationStatusEnum {
        @JSONField(name = "Accepted")
        ACCEPT {
            override fun toString(): String {
                return "Accepted"
            }
        }
    }
}
