/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.annotation.JSONType;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.Assert.*;

/**
 * vertx 官方内置测试类改写（替换为 Fastjson2Codec）
 */
public class JsonPOJOMapperTest {
    private final Fastjson2Codec codec = new Fastjson2Codec();

    @JSONType(orders = {"a", "b", "c", "d", "e"})
    public static class MyType {
        public int a;
        public String b;
        public HashMap<String, Object> c = new LinkedHashMap<>();
        public List<MyType> d = new ArrayList<>();
        public List<Integer> e = new ArrayList<>();
    }

    @Test
    public void testSerialization() {
        MyType myObj0 = new MyType();
        myObj0.a = -1;
        myObj0.b = "obj0";
        myObj0.c.put("z", Arrays.asList(7, 8));
        myObj0.e.add(9);

        MyType myObj1 = new MyType();
        myObj1.a = 5;
        myObj1.b = "obj1";
        myObj1.c.put("x", "1");
        myObj1.c.put("y", 2);
        myObj1.d.add(myObj0);
        myObj1.e.add(3);

        JsonObject jsonObject1 = codec.fromValue(myObj1, JsonObject.class);
        String jsonStr1 = codec.toString(jsonObject1, false);
        assertEquals("{\"a\":5,\"b\":\"obj1\",\"c\":{\"x\":\"1\",\"y\":2},\"d\":["
                + "{\"a\":-1,\"b\":\"obj0\",\"c\":{\"z\":[7,8]},\"d\":[],\"e\":[9]}"
                + "],\"e\":[3]}", jsonStr1);

        MyType myObj1Roundtrip = codec.fromValue(jsonObject1, MyType.class);
        assertEquals(myObj1Roundtrip.a, 5);
        assertEquals(myObj1Roundtrip.b, "obj1");
        assertEquals(myObj1Roundtrip.c.get("x"), "1");
        assertEquals(myObj1Roundtrip.c.get("y"), 2);
        assertEquals(myObj1Roundtrip.e, Arrays.asList(3));
        MyType myObj0Roundtrip = myObj1Roundtrip.d.get(0);
        assertEquals(myObj0Roundtrip.a, -1);
        assertEquals(myObj0Roundtrip.b, "obj0");
        assertEquals(myObj0Roundtrip.c.get("z"), Arrays.asList(7, 8));
        assertEquals(myObj0Roundtrip.e, Arrays.asList(9));

        boolean caughtCycle = false;
        try {
            myObj0.d.add(myObj0);
            codec.fromValue(myObj0, JsonObject.class);
        } catch (IllegalArgumentException e) {
            caughtCycle = true;
        }
        if (!caughtCycle) {
            fail();
        }
    }

    public static class MyType2 {
        public Instant isodate = Instant.now();
        public byte[] base64 = "Hello World!".getBytes();
    }

    public static class MyType3 {
        public Instant isodate = Instant.now();
        public Buffer base64 = Buffer.buffer("Hello World!");
    }

    @Test
    public void testInstantFromPOJO() {
        JsonObject json = codec.fromValue(new MyType2(), JsonObject.class);
        assertNotNull(json.getInstant("isodate"));
    }

    @Test
    public void testInstantToPOJO() {
        MyType2 obj = codec.fromValue(new JsonObject().put("isodate", Instant.EPOCH), MyType2.class);
        assertEquals(Instant.EPOCH, obj.isodate);
    }

    @Test
    public void testInvalidInstantToPOJO() {
        testInvalidValueToPOJO("isodate");
    }

    @Test
    public void testBase64FromPOJO() {
        JsonObject json = codec.fromValue(new MyType2(), JsonObject.class);
        assertNotNull(json.getBinary("base64"));
        assertNotNull(json.getBuffer("base64"));
    }

    @Test
    public void testBase64ToPOJO() {
        MyType2 obj = codec.fromValue(new JsonObject().put("base64", "Hello World!".getBytes()), MyType2.class);
        assertArrayEquals("Hello World!".getBytes(), obj.base64);

        MyType3 obj2 = codec.fromValue(new JsonObject().put("base64", Buffer.buffer("Hello World!")), MyType3.class);
        assertEquals(Buffer.buffer("Hello World!"), obj2.base64);
    }

    @Test
    public void testInvalidBase64ToPOJO() {
        testInvalidValueToPOJO("base64");
    }

    private void testInvalidValueToPOJO(String key) {
        try {
            codec.fromValue(new JsonObject().put(key, "1"), MyType2.class);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testNullPOJO() {
        assertNull(codec.fromValue(null, JsonObject.class));
    }
}
