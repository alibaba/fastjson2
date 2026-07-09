package com.alibaba.fastjson2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class KotlinReflectWarningTest {
    data class Order(val id: Int, val title: String)

    @Test
    fun no_warning_when_kotlin_reflect_resolves_parameter_names() {
        val logger = Logger.getLogger("com.alibaba.fastjson2.util.KotlinUtils")
        val records = mutableListOf<LogRecord>()
        val handler = object : Handler() {
            override fun publish(record: LogRecord) {
                records.add(record)
            }

            override fun flush() {
            }

            override fun close() {
            }
        }
        logger.addHandler(handler)
        try {
            val json = JSON.toJSONString(Order(1, "kraity"))
            val order = json.to<Order>()
            assertEquals(1, order.id)
            assertEquals("kraity", order.title)
        } finally {
            logger.removeHandler(handler)
        }
        assertTrue(records.isEmpty()) {
            "expected no kotlin-reflect warning, got: " + records.joinToString { it.message }
        }
    }
}
