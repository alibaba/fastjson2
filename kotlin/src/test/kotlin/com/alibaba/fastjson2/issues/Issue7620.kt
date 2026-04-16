package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.util.KotlinUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class Issue7620 {

    data class Bean(val id: Int, val name: String)

    @Test
    fun testSerializeWhenKotlinReflectionFails() {
        val field = KotlinUtils::class.java.getDeclaredField("kotlin_error")
        field.isAccessible = true
        val original = field.getBoolean(null)
        try {
            field.setBoolean(null, true)
            val bean = Bean(1, "test")
            val json = JSON.toJSONString(bean)
            assertNotNull(json)
            val parsed = JSON.parseObject(json, Bean::class.java)
            assertEquals(bean.id, parsed.id)
            assertEquals(bean.name, parsed.name)
        } finally {
            field.setBoolean(null, original)
        }
    }
}
