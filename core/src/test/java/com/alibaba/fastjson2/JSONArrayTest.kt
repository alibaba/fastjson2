package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JSONArrayKtTest {
    @Test
    fun test_getObject() {
        // JSONObject
        val data = JSON.parseArray(
            """[{"id":1,"name":"kraity"}]"""
        )

        val user = data.getObject(
            0, User::class.java
        )

        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

    data class User(
        val id: Int,
        val name: String
    )
}
