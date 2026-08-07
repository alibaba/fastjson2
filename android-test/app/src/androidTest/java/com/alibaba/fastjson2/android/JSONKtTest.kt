package com.alibaba.fastjson2.android

import org.junit.Test
import org.junit.Assert.*

import com.alibaba.fastjson2.*

class JSONKtTest {

    @Test
    fun test_parseObject() {
        val user = """{"id":1,"name":"kraity"}""".to<User>()

        assertEquals(1, user.id.toLong())
        assertEquals("kraity", user.name)
    }

    class User(
        val id: Int,
        val name: String
    )
}