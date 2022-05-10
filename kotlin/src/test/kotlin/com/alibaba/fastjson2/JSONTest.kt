package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JSONTest {

    @Test
    fun test_parseObject1() {
        val text = """{"id":1,"name":"kraity"}"""
        val data = text.to<User>()

        assertEquals(1, data.id)
        assertEquals("kraity", data.name)
        assertEquals("""{"id":1,"name":"kraity"}""", data.toJSONString())
    }

    @Test
    fun test_parseObject2() {
        parseObject<User>(
            """{"id":1,"name":"kraity"}"""
        ).apply {
            assertEquals(1, id)
            assertEquals("kraity", name)
            assertEquals("""{"id":1,"name":"kraity"}""", toJSONString())
        }
    }

    data class User(
        val id: Int,
        val name: String
    )
}
