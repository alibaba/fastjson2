package com.alibaba.fastjson2.issues

import com.alibaba.fastjson2.JSONB
import com.alibaba.fastjson2.JSONReader
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
