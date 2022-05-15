package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JSONObjectTest {

    @Test
    fun test_getObject() {
        // JSONObject
        val data = """{"key":{"id":1,"name":"kraity"}}""".parseObject()

        val user = data.getObject<User>("key")
        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

    @Test
    fun test_toObject() {
        // JSONObject
        val data = JSONObject().apply {
            put("id", 1)
            put("name", "kraity")
        }

        val user = data.to<User>()
        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

    data class User(
        val id: Int,
        val name: String
    )
}
