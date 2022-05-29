package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JSONObjectKtTest {
    @Test
    fun test_getObject() {
        // JSONObject
        val data = JSON.parseObject(
            """{"key":{"id":1,"name":"kraity"}}"""
        )

        val user = data.getObject(
            "key", User::class.java
        )

        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

    data class User(
        val id: Int,
        val name: String
    )
}
