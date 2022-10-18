package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.toJSONString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * @author kraity
 */
class Issue848 {

    @Test
    fun test() {
        val sms = SmsEvent(
            1, "", listOf()
        )
        assertEquals(
            """{"m":"","source":1,"t":[]}""", sms.toJSONString()
        )
    }

    class SmsEvent(
        source: Any,
        val m: String,
        val t: List<String>
    ) : AbsEvent(source)

    abstract class Event(
        val source: Any
    )

    abstract class AbsEvent(
        source: Any
    ) : Event(source)
}
