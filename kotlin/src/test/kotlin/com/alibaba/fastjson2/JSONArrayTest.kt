package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JSONArrayTest {
    @Test
    fun test_getObject() {
        // JSONArray
        val data = """[{"id":1,"name":"kraity"}]""".parseArray()

        val user = data.to<User>(0)
        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

    @Test
    fun test_toObject() {
        // JSONArray
        val data = "[{\"id\": 1, \"name\": \"fastjson\"}, {\"id\": 2, \"name\": \"fastjson2\"}]".parseArray()

        val list = data.toList<User>()
        assertEquals(2, list.size)

        list[0].apply {
            assertEquals(1, id)
            assertEquals("fastjson", name)
        }

        list[1].apply {
            assertEquals(2, id)
            assertEquals("fastjson2", name)
        }
    }

    data class User(
        val id: Int,
        val name: String
    )
}
