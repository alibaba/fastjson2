package com.alibaba.fastjson2.issues_3100

import com.alibaba.fastjson2.JSON
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class Issue3152 {

    data class Foo(val int: Int = 1, val bar: Bar)
    data class Foo2(val int: Int, val bar: Bar)
    data class Bar(val text: String)

    @Test
    fun test() {
        Assertions.assertEquals(
            Foo(bar = Bar("hello")),
            JSON.parseObject(
                """
                    {
                        "bar": {
                            "text" : "hello"
                        }
                    }
                """.trimIndent()
            ).toJavaObject(Foo::class.java)
        )

        Assertions.assertEquals(
            Foo2(1, Bar("hello")),
            JSON.parseObject(
                """
                    {
                        "int": 1,
                        "bar": {
                            "text" : "hello"
                        }
                    }
                """.trimIndent()
            ).to(Foo2::class.java)
        )
    }
}
