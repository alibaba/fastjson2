package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest_readObject {
    @Test
    public void test_0() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 123);
        jsonObject.put("obj", new JSONObject());

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonObject);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONObject.class, obj.getClass());
        assertEquals(jsonObject, obj);
    }

    @Test
    public void test_2() throws Exception {
        JSONObject jsonObject = JSON.parseObject("{123:345}");

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonObject);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONObject.class, obj.getClass());
        assertEquals(jsonObject, obj);
    }

    @Test
    public void test_3() throws Exception {
        JSONObject jsonObject = JSON.parseObject("{123:345,\"items\":[1,2,3,4]}");

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonObject);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONObject.class, obj.getClass());
        assertEquals(jsonObject, obj);
    }

    @Test
    public void test_4() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("val", new Byte[]{});

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonObject);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONObject.class, obj.getClass());
        assertEquals(jsonObject.toJSONString(), JSON.toJSONString(obj));
    }

    @Test
    public void test_5() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("val", new byte[]{});

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonObject);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONObject.class, obj.getClass());
//        assertEquals(jsonObject.toJSONString(), JSON.toJSONString(obj));
    }

    @Test
    public void test_6() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("val", new Character[]{});
        jsonObject.put("cls", Number.class);
        jsonObject.put("nums", new Number[]{});

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonObject);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();

        assertEquals(JSONObject.class, obj.getClass());
        assertEquals(jsonObject.toJSONString(), JSON.toJSONString(obj));
    }

    @Test
    public void test_7() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("m", new java.util.HashMap());

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
        objOut.writeObject(jsonObject);
        objOut.flush();

        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        ObjectInputStream objIn = new ObjectInputStream(bytesIn);

        Object obj = objIn.readObject();
    }
}
