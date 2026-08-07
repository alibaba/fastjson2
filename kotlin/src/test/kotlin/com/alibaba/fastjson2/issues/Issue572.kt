package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.parseArray
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author kraity
 */
class Issue572 {

    @Test
    fun test0() {
        val str = "[\n" +
                " {\n" +
                " \"id\": \"82646a0f-3556-4d04-899d-c5e1283575d4\",\n" +
                " \"cagetory\": \"\",\n" +
                " \"name\": \"Mars\",\n" +
                " \"subName\": \"\",\n" +
                " \"uuid\": \"\"\n" +
                " },\n" +
                " {\n" +
                " \"id\": \"a6ad62bb-55bd-44e8-a29e-cd6535e3b5af\",\n" +
                " \"cagetory\": \"\",\n" +
                " \"name\": \"DT02\",\n" +
                " \"subName\": \"\",\n" +
                " \"uuid\": \"\"\n" +
                " },\n" +
                " {\n" +
                " \"id\": \"67c227a6-a1f3-4e3d-8f06-ba408d160a0f\",\n" +
                " \"cagetory\": \"\",\n" +
                " \"name\": \"RMA\",\n" +
                " \"subName\": \"\",\n" +
                " \"uuid\": \"\"\n" +
                " }\n" +
                " ]"

        val list = str.parseArray<F1CheckItem>()
        assertEquals(3, list.size)

        val f0 = list[0]
        assertEquals("82646a0f-3556-4d04-899d-c5e1283575d4", f0.id)

        val f2 = list[2]
        assertEquals("RMA", f2.name)
    }

    class F1CheckItem(
        var id: String = "",
        var cagetory: String = "",
        var name: String = "",
        var subName: String = "",
        var uuid: String = "",
    )
}
