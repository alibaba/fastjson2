package com.alibaba.fastjson2.issues

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

import com.alibaba.fastjson2.*
import com.alibaba.fastjson2.annotation.JSONField
import com.alibaba.fastjson2.toJSONString

class Issue276 {

    @Test
    fun test() {
        val json = """{"access_token":"MTUZNGNKNMITZTVMMC0ZYTY0LWFIZJCTMZJLMDIYMMY4OGUW","scope":""}"""
        val accessTokenResponse = json.to<AccessTokenResponse>()

        assertEquals(
            """{"access_token":"MTUZNGNKNMITZTVMMC0ZYTY0LWFIZJCTMZJLMDIYMMY4OGUW","scope":""}""",
            accessTokenResponse.toJSONString()
        )
        assertEquals(
            """AccessTokenResponse(accessToken=MTUZNGNKNMITZTVMMC0ZYTY0LWFIZJCTMZJLMDIYMMY4OGUW, expiresIn=null, scope=, tokenType=null)""",
            accessTokenResponse.toString()
        )
    }

    @Test
    fun test2() {
        val text = """{"access_token":"MTUZNGNKNMITZTVMMC0ZYTY0LWFIZJCTMZJLMDIYMMY4OGUW","scope":""}"""
        val response = text.to<Response>()

        assertEquals(
            """{"access_token":"MTUZNGNKNMITZTVMMC0ZYTY0LWFIZJCTMZJLMDIYMMY4OGUW","scope":""}""",
            response.toJSONString()
        )
    }

    class Response {
        @JSONField(name = "access_token")
        var accessToken: String? = null

        @JSONField(name = "expires_in")
        var expiresIn: Int? = null

        @JSONField(name = "scope")
        var scope: String? = null

        @JSONField(name = "token_type")
        var tokenType: String? = null
    }

    data class AccessTokenResponse(
        @field:JSONField(name = "access_token")
        var accessToken: String? = null,

        @field:JSONField(name = "expires_in")
        var expiresIn: Int? = null,

        @field:JSONField(name = "scope")
        var scope: String? = null,

        @field:JSONField(name = "token_type")
        var tokenType: String? = null
    )
}
