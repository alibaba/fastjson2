package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializeWriterTest {
    @Test
    public void test_0() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.append('A');
        writer.writeInt(156);
        assertEquals("A156", writer.toString());
        writer.writeLong(345);
        assertEquals("A156345", writer.toString());
    }

    @Test
    public void test_1() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.writeInt(-1);
        assertEquals("-1", writer.toString());
    }

    @Test
    public void test_4() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.writeInt(-1);
        writer.write(',');
        assertEquals("-1,", writer.toString());
    }

    @Test
    public void test_5() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.writeLong(-1L);
        assertEquals("-1", writer.toString());
    }

    @Test
    public void test_6() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.writeLong(-1L);
        writer.write(',');
        assertEquals("-1,", writer.toString());
    }

    @Test
    public void test_7() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.writeInt(-1);
        writer.write((int) ',');
        assertEquals("-1,", writer.toString());
    }

    @Test
    public void test_8() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.writeNull(SerializerFeature.BeanToArray);
        assertEquals("null", writer.toString());
    }
}
