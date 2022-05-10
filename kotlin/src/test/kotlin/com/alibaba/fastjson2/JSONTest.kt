package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JSONTest {

    @Test
    fun test_parseObject1() {
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
