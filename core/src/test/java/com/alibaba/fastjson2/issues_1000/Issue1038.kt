package com.alibaba.fastjson2.issues_1000

import com.alibaba.fastjson2.JSONObject
import org.junit.jupiter.api.Test
import java.io.Serializable

class Issue1038 {
    @Test
    fun test() {
        val json = "{\"id\":1,\"username\":\"user\",\"password\":\"123\",\"nickname\":\"用户\",\"gender\":1,\"mobile\":\"13000000000\",\"email\":\"abc@qq.com\",\"status\":1,\"deleted\":false,\"create_time\":\"2020-11-30 22:12:58.0\",\"update_time\":\"2020-11-30 22:13:01.0\",\"roles\":[\"ADMIN\"]}"
        val user = JSONObject.parseObject(json).to(AuthUser::class.java);
    }
}

class AuthUser : Serializable {
    var username: String? = null
    var password: String? = null
    var avatar: String? = null
    var nickname: String? = null
    var gender: Int? = null
    var mobile: String? = null
    var email: String? = null
    var status: Int? = null
    var deleted: Boolean = false
    var roles: Set<String>? = null
    var auths: Set<String>? = null
    var setting: JSONObject? = null
}
