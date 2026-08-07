package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TypeReferenceTest {
    @Test
    fun test_reference1() {
        val referUser = reference<User>()

        referUser.parseObject(
            """{"id":1,"name":"kraity"}"""
        ).apply {
            assertEquals(1, id)
            assertEquals("kraity", name)
            assertEquals("""{"id":1,"name":"kraity"}""", toJSONString())
        }

        val list = referUser.parseArray(
            """[{"id":1,"name":"kraity"}]"""
        )
        assertEquals(1, list.size)
        list[0].apply {
            assertEquals(1, id)
            assertEquals("kraity", name)
            assertEquals("""{"id":1,"name":"kraity"}""", toJSONString())
        }
    }

    @Test
    fun test_reference2() {
        val referUserList = reference<List<User>>()

        val list = referUserList.parseObject(
            """[{"id":1,"name":"kraity"}]"""
        )

        assertEquals(1, list.size)
        list[0].apply {
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
