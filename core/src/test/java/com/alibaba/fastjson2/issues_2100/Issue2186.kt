package com.alibaba.fastjson2.issues_2100

import com.alibaba.fastjson2.JSON
import org.junit.jupiter.api.Test

class Issue2186 {
    @Test
    fun test() {
        var str = "{\"status\":200,\"message\":\"OK\",\"info\":{\"token\":\"XXXXXXX\",\"name\":\"admin\"}}\n"
        JSON.parseObject(str, Message::class.java)
    }

    data class Message(
        var status: Int,
        var message: String,
        var msgInfo: String?,
        var info: Info
    )

    data class Info(
        var token: String,
        var name: String
    )
}
