package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterUTF8VectorTest {
    @Test
    public void test() {
        JSONWriter jsonWriter = (JSONWriter) new JSONWriterUTF8Vector.Factory().apply(JSONFactory.createWriteContext());
        jsonWriter.writeString("01234567890012345678900123456789001234567890");
        assertEquals("\"01234567890012345678900123456789001234567890\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16() {
        JSONWriter jsonWriter = (JSONWriter) new JSONWriterUTF16Vector.Factory().apply(JSONFactory.createWriteContext());
        jsonWriter.writeString("01234567890012345678900123456789001234567890中国");
        assertEquals("\"01234567890012345678900123456789001234567890中国\"", jsonWriter.toString());
    }
}
