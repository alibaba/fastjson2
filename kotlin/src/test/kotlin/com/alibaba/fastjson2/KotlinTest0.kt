package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinTest0 {
    @Test
    fun test() {
        assertEquals(
            "{\"name\":\"Endgame\",\"rating\":9.2,\"studio\":\"Marvel\"}", JSON.toJSONString(
                Movie("Endgame", "Marvel", 9.2f)
            )
        )
    }

    data class Movie(
        var name: String,
        var studio: String,
        var rating: Float? = 1f
    )

    @Test
    fun test1() {
        assertEquals(
            "{1:\"one\",2:\"two\"}", JSON.toJSONString(
                mapOf(1 to "one", 2 to "two")
            )
        )
        assertEquals(
            "{\"1\":\"one\",\"2\":\"two\"}", JSON.toJSONString(
                mapOf(1 to "one", 2 to "two"), JSONWriter.Feature.WriteNonStringKeyAsString
            )
        )
    }
}
