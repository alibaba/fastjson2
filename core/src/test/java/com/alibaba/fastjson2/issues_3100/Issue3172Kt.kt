package com.alibaba.fastjson2.issues_3100

import com.alibaba.fastjson2.JSON
import com.fasterxml.jackson.annotation.JsonValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

enum class UserStatus(@JsonValue val value: Int, val label: String) {
    NORMAL(1, "正常"),
    LOCKED(2, "锁定"),
}

class EnumTest {
    @Test
    fun testEnum() {
        val value = "\"2\""
        val status = JSON.parseObject(value, UserStatus::class.java)
        println("Enum Item: $status") // null, version=2.0.53
        Assertions.assertEquals(UserStatus.LOCKED, status)
    }
}
