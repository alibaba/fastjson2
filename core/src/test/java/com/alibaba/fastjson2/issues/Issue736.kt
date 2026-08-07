package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class Issue736 {
    @Test
    fun test() {
        var bean = Bean(
            "","","",1,2,3
        )
        var json = JSON.toJSONString(bean)
        var bean1 = JSON.parseObject(json, Bean::class.java)
        assertEquals(bean.a, bean1.a)
    }
}

data class Bean (
    var a: String,
    var b: String,
    var c: String,
    var d: Int = 1,
    var e: Int = 9999,
    var f: Int = 0,
    var g: Calendar = Calendar.getInstance(),
    var h: Calendar = Calendar.getInstance(),
    var i: Calendar = Calendar.getInstance(),
    var j: String = "",
    var k: String = "",
    var l: String = "",
    var m: Int = 0,
    var n: String = "",
    var o: Int = 0,
    var p: Float = 0f,
    var q: Float = 0f,
    var r: Boolean = false,
    var s: Boolean = false,
    var t: Boolean = false,
    var u: String = "",
    var v: Int = 0,
    var w: Int = 0,
    var x: Int = 0,
    var y: String = "",
    var z: String = "",
    var a1: Int = 1,
    var a2: Calendar = Calendar.getInstance(),
    var a3: Int = 0
) {
    var a4: String = ""
    var a5: String = ""
    var a6: Int = 0
}
