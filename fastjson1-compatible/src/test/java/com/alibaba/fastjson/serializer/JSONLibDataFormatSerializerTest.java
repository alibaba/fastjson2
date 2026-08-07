package com.alibaba.fastjson.serializer;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONLibDataFormatSerializerTest {
    @Test
    public void test() throws Exception {
        JSONLibDataFormatSerializer serializer = new JSONLibDataFormatSerializer();
        {
            JSONSerializer jsonSerializer = new JSONSerializer();
            serializer.write(jsonSerializer, null, null, null, 0);
            assertEquals("null", jsonSerializer.toString());
        }

        JSONSerializer jsonSerializer = new JSONSerializer();
        Date date = new Date();
        serializer.write(jsonSerializer, date, null, null, 0);
    }
}
