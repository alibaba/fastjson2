package com.alibaba.fastjson.serializer.filters;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ListSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListSerializerTest {
    @Test
    public void write() throws Exception {
        JSONArray jsonArray = new JSONArray();

        JSONSerializer serializer = new JSONSerializer();
        ListSerializer.instance.write(serializer, jsonArray, null, null, 0);
        assertEquals("[]", serializer.out.toString());
    }

    @Test
    public void write1() throws Exception {
        JSONArray jsonArray = new JSONArray();

        JSONSerializer serializer = new JSONSerializer();
        ListSerializer.instance.write(serializer, jsonArray, null, null);
        assertEquals("[]", serializer.out.toString());
    }
}
