/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson;

import com.alibaba.fastjson2.JSONException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JSONObjectTest extends TestCase {

    public void test_toJSONObject() throws Exception {
        {
            Assert.assertNull(JSONObject.parse(null));
        }
    }
//
//    public void test_writeJSONString() throws Exception {
//        {
//            StringWriter out = new StringWriter();
//            new JSONObject().writeJSONString(out);
//            Assert.assertEquals("{}", out.toString());
//        }
//    }

    public void test_getLong() throws Exception {
        JSONObject json = new JSONObject(true);
        json.put("A", 55L);
        json.put("B", 55);
        json.put("K", true);
        Assert.assertEquals(json.getLong("A").longValue(), 55L);
        Assert.assertEquals(json.getLong("B").longValue(), 55L);
        Assert.assertEquals(json.getLong("C"), null);
        Assert.assertEquals(json.getBooleanValue("K"), true);
        Assert.assertEquals(json.getBoolean("K"), Boolean.TRUE);
    }

    public void test_getLong_1() throws Exception {
        JSONObject json = new JSONObject(false);
        json.put("A", 55L);
        json.put("B", 55);
        Assert.assertEquals(json.getLong("A").longValue(), 55L);
        Assert.assertEquals(json.getLong("B").longValue(), 55L);
        Assert.assertEquals(json.getLong("C"), null);
    }

    public void test_getDate() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        JSONObject json = new JSONObject();
        json.put("A", new Date(currentTimeMillis));
        json.put("B", currentTimeMillis);
        Assert.assertEquals(json.getDate("A").getTime(), currentTimeMillis);
        Assert.assertEquals(json.getDate("B").getTime(), currentTimeMillis);
        Assert.assertEquals(json.getLong("C"), null);
    }

    public void test_getBoolean() throws Exception {
        JSONObject json = new JSONObject();
        json.put("A", true);
        Assert.assertEquals(json.getBoolean("A").booleanValue(), true);
        Assert.assertEquals(json.getLong("C"), null);
    }

    public void test_getInt() throws Exception {
        JSONObject json = new JSONObject();
        json.put("A", 55L);
        json.put("B", 55);
        Assert.assertEquals(json.getInteger("A").intValue(), 55);
        Assert.assertEquals(json.getInteger("B").intValue(), 55);
        Assert.assertEquals(json.getInteger("C"), null);
    }

    public void test_order() throws Exception {
        JSONObject json = new JSONObject(true);
        json.put("C", 55L);
        json.put("B", 55);
        json.put("A", 55);
        Assert.assertEquals("C", json.keySet().toArray()[0]);
        Assert.assertEquals("B", json.keySet().toArray()[1]);
        Assert.assertEquals("A", json.keySet().toArray()[2]);

        Assert.assertEquals(0, json.getIntValue("D"));
        Assert.assertEquals(0L, json.getLongValue("D"));
        Assert.assertEquals(false, json.getBooleanValue("D"));
    }

    public void test_all() throws Exception {
        JSONObject json = new JSONObject();
        Assert.assertEquals(true, json.isEmpty());
        json.put("C", 51L);
        json.put("B", 52);
        json.put("A", 53);
        Assert.assertEquals(false, json.isEmpty());
        Assert.assertEquals(true, json.containsKey("C"));
        Assert.assertEquals(false, json.containsKey("D"));
        Assert.assertEquals(true, json.containsValue(52));
        Assert.assertEquals(false, json.containsValue(33));
        Assert.assertEquals(null, json.remove("D"));
        Assert.assertEquals(51L, json.remove("C"));
        Assert.assertEquals(2, json.keySet().size());
        Assert.assertEquals(2, json.values().size());
        Assert.assertEquals(new BigDecimal("53"), json.getBigDecimal("A"));

        json.putAll(Collections.singletonMap("E", 99));
        Assert.assertEquals(3, json.values().size());
        json.clear();
        Assert.assertEquals(0, json.values().size());
        json.putAll(Collections.singletonMap("E", 99));
        Assert.assertEquals(99L, json.getLongValue("E"));
        Assert.assertEquals(99, json.getIntValue("E"));
        Assert.assertEquals("99", json.getString("E"));
        Assert.assertEquals(null, json.getString("F"));
        Assert.assertEquals(null, json.getDate("F"));
        Assert.assertEquals(null, json.getBoolean("F"));
    }

    public void test_all_2() throws Exception {
        JSONObject array = new JSONObject();
        array.put("0", 123);
        array.put("1", "222");
        array.put("2", 3);
        array.put("3", true);
        array.put("4", "true");
        array.put("5", "2.0");

        Assert.assertEquals(123, array.getIntValue("0"));
        Assert.assertEquals(123, array.getLongValue("0"));
        Assert.assertEquals(new BigDecimal("123"), array.getBigDecimal("0"));

        Assert.assertEquals(222, array.getIntValue("1"));
        Assert.assertEquals(3, array.getByte("2").byteValue());
        Assert.assertEquals(3, array.getByteValue("2"));
        Assert.assertEquals(3, array.getShort("2").shortValue());
        Assert.assertEquals(3, array.getShortValue("2"));
        Assert.assertEquals(new Integer(222), array.getInteger("1"));
        Assert.assertEquals(new Long(222), array.getLong("1"));
        Assert.assertEquals(new BigDecimal("222"), array.getBigDecimal("1"));

        Assert.assertEquals(true, array.getBooleanValue("4"));
        Assert.assertTrue(2.0F == array.getFloat("5").floatValue());
        Assert.assertTrue(2.0F == array.getFloatValue("5"));
        Assert.assertTrue(2.0D == array.getDouble("5").doubleValue());
        Assert.assertTrue(2.0D == array.getDoubleValue("5"));
    }

    public void test_getObject_null() throws Exception {
        JSONObject json = new JSONObject();
        json.put("obj", null);

        Assert.assertTrue(json.getJSONObject("obj") == null);
    }

    public void test_bytes () throws Exception {
        JSONObject object = new JSONObject();
        Assert.assertNull(object.getBytes("bytes"));
    }

    public void test_getObject() throws Exception {
        JSONObject json = new JSONObject();
        json.put("obj", new JSONObject());

        Assert.assertEquals(0, json.getJSONObject("obj").size());
    }

    public void test_getObject_map() throws Exception {
        JSONObject json = new JSONObject();
        json.put("obj", new HashMap());

        Assert.assertEquals(0, json.getJSONObject("obj").size());
    }

    public void test_getObjectOrDefault() {
        JSONObject json = new JSONObject();
        json.put("testKey", "testVal");
        json.put("testKey2", null);

        Assert.assertEquals("default", json.getOrDefault("testNonKet", "default"));
        Assert.assertEquals("default", json.getOrDefault("testKey2", "default"));
    }

    @Test
    public void test_null() {
        JSONObject object = new JSONObject();
        object.put("val", null);
        assertEquals(0L, object.getLongValue("val"));
        assertEquals(null, object.getLong("val"));

        assertEquals(0, object.getIntValue("val"));
        assertEquals(null, object.getInteger("val"));

        assertEquals(null, object.getString("val"));
        assertEquals(null, object.getJSONArray("val"));
        assertEquals(null, object.getJSONObject("val"));
        assertEquals(null, object.getBigInteger("val"));
        assertEquals(null, object.getBigDecimal("val"));
        assertEquals(null, object.getDouble("val"));
        assertEquals(0D, object.getDoubleValue("val"));
        assertEquals(null, object.getFloat("val"));
        assertEquals(0F, object.getFloatValue("val"));
        assertEquals(false, object.getBooleanValue("val"));
        assertEquals(null, object.getBoolean("val"));
        assertEquals((short) 0, object.getShortValue("val"));
        assertEquals(null, object.getShort("val"));
        assertEquals((byte) 0, object.getByteValue("val"));
        assertEquals(null, object.getByte("val"));
    }

    @Test
    public void test_null_str() {
        JSONObject object = new JSONObject();
        object.put("val", "null");
        assertEquals(0L, object.getLongValue("val"));
        assertEquals(null, object.getLong("val"));

        assertEquals(0, object.getIntValue("val"));
        assertEquals(null, object.getInteger("val"));

        assertEquals(null, object.getJSONArray("val"));
        assertEquals(null, object.getJSONObject("val"));
        assertEquals(null, object.getBigInteger("val"));
        assertEquals(null, object.getBigDecimal("val"));
        assertEquals(null, object.getFloat("val"));
        assertEquals(null, object.getDouble("val"));
        assertEquals(null, object.getBoolean("val"));
        assertEquals(null, object.getByte("val"));
        assertEquals(null, object.getShort("val"));
        assertEquals(0, object.getByteValue("val"));
        assertEquals(0, object.getShortValue("val"));
    }

    @Test
    public void test_null_str_empty() {
        JSONObject object = new JSONObject();
        object.put("val", "");
        assertEquals(0L, object.getLongValue("val"));
        assertEquals(null, object.getLong("val"));

        assertEquals(0, object.getIntValue("val"));
        assertEquals(null, object.getInteger("val"));

        assertEquals(null, object.getJSONArray("val"));
        assertEquals(null, object.getJSONObject("val"));
        assertEquals(null, object.getBigInteger("val"));
        assertEquals(null, object.getBigDecimal("val"));
        assertEquals(null, object.getBoolean("val"));
        assertEquals(null, object.getFloat("val"));
        assertEquals(null, object.getDouble("val"));
        assertEquals(null, object.getByte("val"));
        assertEquals(null, object.getShort("val"));
        assertEquals(0, object.getByteValue("val"));
        assertEquals(0, object.getShortValue("val"));
    }

    @Test
    public void test_error() {
        JSONObject jsonObject = new JSONObject().fluentPut("val", new Object());
        {
            Exception error = null;
            try {
                jsonObject.getLong("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getLongValue("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getInteger("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getIntValue("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShort("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShortValue("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByte("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByteValue("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDouble("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDoubleValue("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloat("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloatValue("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigInteger("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigDecimal("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBoolean("val");
            } catch (com.alibaba.fastjson2.JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBooleanValue("val");
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
    }

    @Test
    public void test_getBoolean2() {
        assertEquals(
                Boolean.TRUE
                , new JSONObject()
                        .fluentPut("val", 1)
                        .getBoolean("val"));
        assertEquals(
                Boolean.TRUE
                , new JSONObject()
                        .fluentPut("val", "true")
                        .getBoolean("val"));
        assertEquals(
                Boolean.FALSE
                , new JSONObject()
                        .fluentPut("val", Boolean.FALSE)
                        .getBoolean("val"));
        assertEquals(
                Boolean.FALSE
                , new JSONObject()
                        .fluentPut("val", "FALSE")
                        .getBoolean("val"));
    }

    @Test
    public void test_getBigInt() {
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (byte) 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", (short) 12)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12L)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12F)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", 12D)
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigDecimal("12"))
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", new BigInteger("12"))
                        .getBigInteger("val"));
        assertEquals(
                BigInteger.valueOf(12)
                , new JSONObject()
                        .fluentPut("val", "12")
                        .getBigInteger("val"));
    }
}
