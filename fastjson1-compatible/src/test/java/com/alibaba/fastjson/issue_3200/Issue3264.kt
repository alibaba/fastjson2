package com.alibaba.fastjson.issue_3200

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.annotation.JSONField
import org.junit.jupiter.api.Test
import kotlin.reflect.full.findAnnotation

class TestIssue3264 {
    @Test
    fun test() {
//        val str1 = JSON.toJSONString(also)
//        println(str1);

        val str2 = JSON.toJSONString(MyData());
        println(str2);

        println(MyData::isTest.findAnnotation<JSONField>());
    }

    class MyData(
        @JSONField(name = "is_test")
        var isTest: Boolean = false
    )

    val also = MyData().also {
        it.isTest = true
    }
}
