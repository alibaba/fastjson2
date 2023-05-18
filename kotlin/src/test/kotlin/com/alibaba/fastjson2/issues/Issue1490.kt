package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.parseObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author kraity
 */
class Issue1490 {

    @Test
    fun test_field36() {
        val json = """
            {
                "field1":"1",
                "field31":"31"
                "field35":"35"
            }
        """.trimIndent()
        val entity1 = json.parseObject<Entity1>()
        assertNotNull(entity1)
        assertEquals("1", entity1.field1)
        assertEquals("31", entity1.field31)
        assertEquals("field32", entity1.field32)
        assertEquals("35", entity1.field35)
        assertEquals("field36", entity1.field36)
    }

    @Test
    fun test_field72() {
        val json = """
            {
                "field1":"1",
                "field31":"31",
                "field63":"63"
                "field71":"71"
            }
        """.trimIndent()
        val entity2 = json.parseObject<Entity2>()
        assertNotNull(entity2)
        assertEquals("1", entity2.field1)
        assertEquals("31", entity2.field31)
        assertEquals("field32", entity2.field32)
        assertEquals("63", entity2.field63)
        assertEquals("field64", entity2.field64)
        assertEquals("71", entity2.field71)
        assertEquals("field72", entity2.field72)
    }

    data class Entity1(
        val field1: String = "",
        val field2: String = "",
        val field3: String = "",
        val field4: String = "",
        val field5: String = "",
        val field6: String = "",
        val field7: String = "",
        val field8: String = "",
        val field9: String = "",
        val field10: String = "",
        val field11: String = "",
        val field12: String = "",
        val field13: String = "",
        val field14: String = "",
        val field15: String = "",
        val field16: String = "",
        val field17: String = "",
        val field18: String = "",
        val field19: String = "",
        val field20: String = "",
        val field21: String = "",
        val field22: String = "",
        val field23: String = "",
        val field24: String = "",
        val field25: String = "",
        val field26: String = "",
        val field27: String = "",
        val field28: String = "",
        val field29: String = "",
        val field30: String = "",
        val field31: String = "field31",
        val field32: String = "field32",
        val field33: String = "",
        val field34: String = "",
        val field35: String = "field35",
        val field36: String = "field36"
    )

    data class Entity2(
        val field1: String = "",
        val field2: String = "",
        val field3: String = "",
        val field4: String = "",
        val field5: String = "",
        val field6: String = "",
        val field7: String = "",
        val field8: String = "",
        val field9: String = "",
        val field10: String = "",
        val field11: String = "",
        val field12: String = "",
        val field13: String = "",
        val field14: String = "",
        val field15: String = "",
        val field16: String = "",
        val field17: String = "",
        val field18: String = "",
        val field19: String = "",
        val field20: String = "",
        val field21: String = "",
        val field22: String = "",
        val field23: String = "",
        val field24: String = "",
        val field25: String = "",
        val field26: String = "",
        val field27: String = "",
        val field28: String = "",
        val field29: String = "",
        val field30: String = "",
        val field31: String = "field31",
        val field32: String = "field32",
        val field33: String = "",
        val field34: String = "",
        val field35: String = "",
        val field36: String = "",
        val field37: String = "",
        val field38: String = "",
        val field39: String = "",
        val field40: String = "",
        val field41: String = "",
        val field42: String = "",
        val field43: String = "",
        val field44: String = "",
        val field45: String = "",
        val field46: String = "",
        val field47: String = "",
        val field48: String = "",
        val field49: String = "",
        val field50: String = "",
        val field51: String = "",
        val field52: String = "",
        val field53: String = "",
        val field54: String = "",
        val field55: String = "",
        val field56: String = "",
        val field57: String = "",
        val field58: String = "",
        val field59: String = "",
        val field60: String = "",
        val field61: String = "",
        val field62: String = "",
        val field63: String = "field63",
        val field64: String = "field64",
        val field65: String = "",
        val field66: String = "",
        val field67: String = "",
        val field68: String = "",
        val field69: String = "",
        val field70: String = "",
        val field71: String = "field71",
        val field72: String = "field72"
    )
}
