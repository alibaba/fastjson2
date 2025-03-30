package com.alibaba.fastjson2.issues_3400

import com.alibaba.fastjson2.JSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.validation.constraints.Size

class Issue3433 {
    @Test
    fun test() {
        var data = Data()
        var str = JSON.toJSONString(data)
        assertEquals(str, "{\"cityPairs\":[],\"dates\":[]}")
    }

    data class Data(
        @Size(max = 6)
        val cityPairs: List<List<Int>> = emptyList(),
        @Size(max = 6)
        val dates: List<String> = emptyList(),
    )
}
