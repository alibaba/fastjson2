package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class JSONTest {
    @Test
    fun test_into1() {
        val users = """[{"id":1,"name":"kraity"}]""".into<List<User>>()
        assertEquals(1, users.size)

        val user = users[0]
        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

    @Test
    fun test_into2() {
        val users = """{"user":{"id":1,"name":"kraity"}}""".into<Map<String, User>>()
        assertEquals(1, users.size)

        val user = users["user"]!!
        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

    @Test
    fun test_stream_into1() {
        val input = ByteArrayInputStream(
            "{\"id\":1,\"name\":\"kraity\"}".toByteArray()
        )

        val user = input.into<User>()
        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
    }

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
        val user = """{"id":1,"name":"kraity"}""".parseObject<User>()

        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
        assertEquals("""{"id":1,"name":"kraity"}""", user.toJSONString())
    }

    @Test
    fun test_parseObject3() {
        // JSONObject
        val user = """{"id":1,"name":"kraity"}""".parseObject()

        assertEquals(1, user.get("id"))
        assertEquals("kraity", user.get("name"))
    }

    @Test
    fun test_parseObject4() {
        val input = ByteArrayInputStream(
            "{\"id\":1,\"name\":\"fastjson\"}\n{\"id\":2,\"name\":\"fastjson2\"}\n".toByteArray()
        )

        input.parseObject<User> {
            when (it.id) {
                1 -> assertEquals("fastjson", it.name)
                2 -> assertEquals("fastjson2", it.name)
            }
        }

        input.reset()

        input.parseObject<User>(Charsets.UTF_8) {
            when (it.id) {
                1 -> assertEquals("fastjson", it.name)
                2 -> assertEquals("fastjson2", it.name)
            }
        }

        input.reset()

        input.parseObject<User>(Charsets.UTF_8, '\n') {
            when (it.id) {
                1 -> assertEquals("fastjson", it.name)
                2 -> assertEquals("fastjson2", it.name)
            }
        }
    }

    @Test
    fun test_parseArray1() {
        // JSONArray
        val data = """[0,"1",true]""".parseArray()

        assertEquals(0, data[0])
        assertEquals("1", data[1])
        assertEquals(true, data[2])
    }

    @Test
    fun test_parseArray2() {
        val list = """[{"id":1,"name":"kraity"}]""".parseArray<User>()

        assertEquals(1, list.size)
        val user = list[0]

        assertEquals(1, user.id)
        assertEquals("kraity", user.name)
        assertEquals("""{"id":1,"name":"kraity"}""", user.toJSONString())
    }

    data class User(
        val id: Int,
        val name: String
    )

    @Test
    fun test_parseDefaultMarker() {
        val m0 = """{}""".to<Meta>()
        assertEquals(1, m0.id)
        assertEquals("json", m0.tag)

        val m1 = """{"id":2}""".to<Meta>()
        assertEquals(2, m1.id)
        assertEquals("json", m1.tag)

        val m2 = """{"tag":"kraity"}""".to<Meta>()
        assertEquals(1, m2.id)
        assertEquals("kraity", m2.tag)
    }

    data class Meta(
        var id: Int = 1,
        var tag: String = "json"
    )
}
