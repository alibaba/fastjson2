package com.alibaba.fastjson2.issues_3100

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONB
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class Issue3139 {
    data class Item(val value: String = "默认值")
    data class Result(val low: Item = Item(), val mid: Item = Item(), val high: Item = Item() )

    @Test
    fun test() {
        var r = Result(Item("L"), Item("M"), Item("H"))
        var jsonb = JSONB.toBytes(r)
        println(JSONB.toJSONString(jsonb))
        var r1 = JSONB.parseObject(jsonb, Result::class.java)
        assertEquals("{\"high\":{\"value\":\"H\"},\"low\":{\"value\":\"L\"},\"mid\":{\"value\":\"M\"}}", JSON.toJSONString(r1))
    }
}
