package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JSONPathTest {
    @Test
    fun test_eval() {
        val user = User(
            1, "kraity"
        )
        val name = user.eval("$.name")

        assertTrue(name is String)
        assertEquals("kraity", name)
    }

    @Test
    fun test_contains() {
        val user = User(
            1, "kraity"
        )

        assertTrue(user.contains("$.id"))
        assertTrue(user.contains("$.name"))
        assertFalse(user.contains("$.blocked"))
    }

    data class User(
        val id: Int,
        val name: String
    )
}
