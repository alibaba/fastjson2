package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONArrayTest_readObject {
    @Test
    public void test_serialVersionUID() {
        ObjectStreamClass desc = ObjectStreamClass.lookup(JSONArray.class);
        assertEquals(1L, desc.getSerialVersionUID());
    }

    @Test
    public void test_0() throws Exception {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(123);
        jsonArray.add("hello");
        jsonArray.add(new JSONObject());

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonArray);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONArray.class, obj.getClass());
        assertEquals(jsonArray, obj);
    }

    @Test
    public void test_1() throws Exception {
        JSONArray jsonArray = JSON.parseArray("[1,2,3,{\"id\":123}]");

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonArray);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONArray.class, obj.getClass());
        assertEquals(jsonArray, obj);
    }
}
