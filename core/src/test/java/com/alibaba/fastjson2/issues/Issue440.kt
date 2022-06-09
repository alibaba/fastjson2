package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSONB
import org.junit.jupiter.api.Test

class Issue440 {
    class User { val map = mapOf(1 to 2, "a" to "a", 3f to 3f) }

    @Test
    fun test() {
        val user = User()
        val bytes = JSONB.toBytes(user)
    }
}
