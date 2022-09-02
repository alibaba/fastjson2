package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSONB
import com.alibaba.fastjson2.JSONReader
import com.alibaba.fastjson2.annotation.JSONField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Issue715K {
    @Test
    fun test() {
        val student = Student("bin", 12, "湖北")
        var jsonbBytes = JSONB.toBytes(student);
        var student2 = JSONB.parseObject(jsonbBytes, Student::class.java, JSONReader.Feature.FieldBased);
        assertEquals(student.name, student2.name);
        assertEquals(student.sge, student2.sge);
        assertEquals(student.address, student2.address);
    }

    @Test
    fun test1() {
        var o = JNote("1001", "BigBook", "a1001", 1, 2, 3);
        val jsonbBytes = JSONB.toBytes(o);
        var o1 = JSONB.parseObject(jsonbBytes, JNote::class.java, JSONReader.Feature.FieldBased);
        assertEquals(o.nodeId, o1.nodeId)
        assertEquals(o.name, o1.name)
        assertEquals(o.firstPageId, o1.firstPageId)
        assertEquals(o.type, o1.type)
        assertEquals(o.maxPageNumber, o1.maxPageNumber)
        assertEquals(o.deleteFlag, o1.deleteFlag)
    }
}

class Student {
    var name: String = ""
    var sge: Int = 0
    var address: String = ""

    constructor(
        name: String,
        sge: Int,
        address: String
    ) {
        this.name = name
        this.sge = sge
        this.address = address
    }

    constructor() {

    }
}

data class JNote (
    var nodeId: String,
    var name: String? = null,
    var firstPageId: String? = null,
    var type: Int = 0,
    var maxPageNumber: Int = 0,
    var deleteFlag:Int = 0
)
