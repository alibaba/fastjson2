package com.alibaba.fastjson2.issues_2000

import com.alibaba.fastjson2.JSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue2069 {
    data class User(
        val id: Long?,
        val username: String?,
        val isTrue: String?,
        val isMain: Int?,
        val isBoolean: Boolean?
    ) {
        var isNormal: String? = null

        override fun toString(): String {
            return "User(id=$id, username=$username, isTrue=$isTrue, isMain=$isMain, isBoolean=$isBoolean, isNormal=$isNormal)"
        }
    }

    @Test
    fun test() {
        val user = User(1, "lili", "是", 1, true)
        user.isNormal = "否"
        var jsonStr = JSON.toJSONString(user)
        println(jsonStr)
        jsonStr = "{\"id\":1,\"username\":\"lili\",\"isTrue\":\"是\",\"isMain\":1,\"isBoolean\": true,\"isNormal\":\"否\"}"
        val parseObject = JSON.parseObject(jsonStr, User::class.java)
        assertEquals(parseObject.id, parseObject.id);
        assertEquals(parseObject.username, parseObject.username);
        assertEquals(parseObject.isMain, parseObject.isMain);
        assertEquals(parseObject.isBoolean, parseObject.isBoolean);
        assertEquals(parseObject.isTrue, parseObject.isTrue);
        assertEquals(parseObject.isNormal, parseObject.isNormal);
    }
}
