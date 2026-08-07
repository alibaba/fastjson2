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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class JSONArrayTest {
//
//    @Test
//    public void test_toString() throws Exception {
//        StringWriter out = new StringWriter();
//        new JSONArray().writeJSONString(out);
//        assertEquals("[]", out.toString());
//        assertEquals("[]", new JSONArray().toString());
//    }

    @Test
    public void test_toJSONString() throws Exception {
        assertEquals("null", JSONArray.toJSONString(null));
        assertEquals("[null]", JSONArray.toJSONString(Collections.singletonList(null)));
    }

    @Test
    public void test_1() throws Exception {
        JSONArray array = new JSONArray(3);
        assertEquals(true, array.isEmpty());
        array.add(1);
        assertEquals(false, array.isEmpty());
        assertEquals(true, array.contains(1));
        assertEquals(1, array.toArray()[0]);
        {
            Object[] items = new Object[1];
            array.toArray(items);
            assertEquals(1, items[0]);
        }
        assertEquals(true, array.containsAll(Collections.singletonList(1)));
        assertEquals(true, array.remove(Integer.valueOf(1)));
        assertEquals(true, array.isEmpty());
        array.addAll(Collections.singletonList(1));
        assertEquals(1, array.size());
        array.removeAll(Collections.singletonList(1));
        assertEquals(0, array.size());
        array.addAll(0, Arrays.asList(1, 2, 3));
        assertEquals(3, array.size());
        array.clear();
        array.addAll(0, Arrays.asList(1, 2, 3));
        assertEquals(true, array.retainAll(Arrays.asList(1, 2)));
        assertEquals(2, array.size());
        assertEquals(true, array.retainAll(Arrays.asList(2, 4)));
        assertEquals(1, array.size());
        array.set(0, 4);
        assertEquals(4, array.toArray()[0]);
        array.add(0, 4);
        assertEquals(4, array.toArray()[0]);
        array.remove(0);
        array.remove(0);
        assertEquals(0, array.size());
        array.addAll(Arrays.asList(1, 2, 3, 4, 5, 4, 3));
        assertEquals(2, array.indexOf(3));
        assertEquals(6, array.lastIndexOf(3));
        {
            AtomicInteger count = new AtomicInteger();
            for (ListIterator<Object> iter = array.listIterator(); iter.hasNext(); iter.next()) {
                count.incrementAndGet();
            }
            assertEquals(7, count.get());
        }
        {
            AtomicInteger count = new AtomicInteger();
            for (ListIterator<Object> iter = array.listIterator(2); iter.hasNext(); iter.next()) {
                count.incrementAndGet();
            }
            assertEquals(5, count.get());
        }
        {
            assertEquals(2, array.subList(2, 4).size());
        }
    }

    @Test
    public void test_2() throws Exception {
        JSONArray array = new JSONArray();
        array.add(123);
        array.add("222");
        array.add(3);
        array.add(true);
        array.add("true");
        array.add(null);

        assertEquals(123, array.getByte(0).byteValue());
        assertEquals(123, array.getByteValue(0));

        assertEquals(123, array.getShort(0).shortValue());
        assertEquals(123, array.getShortValue(0));

        assertTrue(123F == array.getFloat(0).floatValue());
        assertTrue(123F == array.getFloatValue(0));

        assertTrue(123D == array.getDouble(0).doubleValue());
        assertTrue(123D == array.getDoubleValue(0));

        assertEquals(123, array.getIntValue(0));
        assertEquals(123, array.getLongValue(0));
        assertEquals(new BigDecimal("123"), array.getBigDecimal(0));

        assertEquals(222, array.getIntValue(1));
        assertEquals(new Integer(222), array.getInteger(1));
        assertEquals(new Long(222), array.getLong(1));
        assertEquals(new BigDecimal("222"), array.getBigDecimal(1));

        assertEquals(true, array.getBooleanValue(4));
        assertEquals(Boolean.TRUE, array.getBoolean(4));

        assertEquals(0, array.getIntValue(5));
        assertEquals(0, array.getLongValue(5));
        assertEquals(null, array.getInteger(5));
        assertEquals(null, array.getLong(5));
        assertEquals(null, array.getBigDecimal(5));
        assertEquals(null, array.getBoolean(5));
        assertEquals(false, array.getBooleanValue(5));
    }

    @Test
    public void test_getObject_null() throws Exception {
        JSONArray array = new JSONArray();
        array.add(null);

        assertTrue(array.getJSONObject(0) == null);
    }

    @Test
    public void test_getObject() throws Exception {
        JSONArray array = new JSONArray();
        array.add(new JSONObject());

        assertEquals(0, array.getJSONObject(0).size());
    }

    @Test
    public void test_getObject_map() throws Exception {
        JSONArray array = new JSONArray();
        array.add(new HashMap());

        assertEquals(0, array.getJSONObject(0).size());
    }

    @Test
    public void test_getArray() throws Exception {
        JSONArray array = new JSONArray();
        array.add(new ArrayList());

        assertEquals(0, array.getJSONArray(0).size());
    }

    @Test
    public void test_getArray_1() throws Exception {
        JSONArray array = new JSONArray();
        array.add(new JSONArray());

        assertEquals(0, array.getJSONArray(0).size());
    }

    @Test
    public void test_constructor() throws Exception {
        List<Object> list = new ArrayList();
        JSONArray array = new JSONArray(list);
        array.add(3);
        assertEquals(1, list.size());
        assertEquals(3, list.get(0));
    }

    @Test
    public void test_getJavaBean() throws Exception {
        JSONArray array = JSON.parseArray("[{'id':123, 'name':'aaa'}]");
        assertEquals(1, array.size());
        assertEquals(123, array.getObject(0, User.class).getId());
    }

    public static class User {
        private long id;
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void test_null() {
        JSONArray array = new JSONArray();
        array.add(null);
        assertEquals(0L, array.getLongValue(0));
        assertEquals(null, array.getLong(0));

        assertEquals(0, array.getIntValue(0));
        assertEquals(null, array.getInteger(0));

        assertEquals(null, array.getString(0));
        assertEquals(null, array.getJSONArray(0));
        assertEquals(null, array.getJSONObject(0));
    }

    @Test
    public void test_null2() {
        JSONArray array = new JSONArray();
        array.add(null);
        assertEquals(0L, array.getLongValue(0));
        assertEquals(null, array.getLong(0));

        assertEquals(0, array.getIntValue(0));
        assertEquals(null, array.getInteger(0));

        assertEquals(null, array.getString(0));
        assertEquals(null, array.getJSONArray(0));
        assertEquals(null, array.getJSONObject(0));
        assertEquals(null, array.getBigInteger(0));
        assertEquals(null, array.getBigDecimal(0));
        assertEquals(null, array.getDouble(0));
        assertEquals(0D, array.getDoubleValue(0));
        assertEquals(null, array.getFloat(0));
        assertEquals(0F, array.getFloatValue(0));
        assertEquals(false, array.getBooleanValue(0));
        assertEquals(null, array.getBoolean(0));
        assertEquals((short) 0, array.getShortValue(0));
        assertEquals(null, array.getShort(0));
        assertEquals((byte) 0, array.getByteValue(0));
        assertEquals(null, array.getByte(0));
    }

    @Test
    public void test_null_str() {
        JSONArray object = new JSONArray();
        object.add("null");
        assertEquals(0L, object.getLongValue(0));
        assertEquals(null, object.getLong(0));

        assertEquals(0, object.getIntValue(0));
        assertEquals(null, object.getInteger(0));

        assertEquals(null, object.getJSONArray(0));
        assertEquals(null, object.getJSONObject(0));
        assertEquals(null, object.getBigInteger(0));
        assertEquals(null, object.getBigDecimal(0));
        assertEquals(null, object.getFloat(0));
        assertEquals(null, object.getDouble(0));
        assertEquals(null, object.getBoolean(0));
        assertEquals(null, object.getByte(0));
        assertEquals(null, object.getShort(0));
        assertEquals(0, object.getByteValue(0));
        assertEquals(0, object.getShortValue(0));
    }

    @Test
    public void test_null_str_empty() {
        JSONArray object = new JSONArray();
        object.add("");
        assertEquals(0L, object.getLongValue(0));
        assertEquals(null, object.getLong(0));

        assertEquals(0, object.getIntValue(0));
        assertEquals(null, object.getInteger(0));

        assertEquals(null, object.getJSONArray(0));
        assertEquals(null, object.getJSONObject(0));
        assertEquals(null, object.getBigInteger(0));
        assertEquals(null, object.getBigDecimal(0));
        assertEquals(null, object.getBoolean(0));
        assertEquals(null, object.getFloat(0));
        assertEquals(null, object.getDouble(0));
        assertEquals(null, object.getByte(0));
        assertEquals(null, object.getShort(0));
        assertEquals(0, object.getByteValue(0));
        assertEquals(0, object.getShortValue(0));
    }

    @Test
    public void test_error() {
        JSONArray jsonObject = new JSONArray().fluentAdd(new Object());
        {
            Exception error = null;
            try {
                jsonObject.getLong(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getLongValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getInteger(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getIntValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShort(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getShortValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByte(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getByteValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDouble(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getDoubleValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloat(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getFloatValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigInteger(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBigDecimal(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBoolean(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
        {
            Exception error = null;
            try {
                jsonObject.getBooleanValue(0);
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
        }
    }

    @Test
    public void test_getBigInt() {
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd(12)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd(12F)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd(12D)
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getBigInteger(0));
        assertEquals(
                BigInteger.valueOf(12),
                new JSONArray()
                        .fluentAdd("12")
                        .getBigInteger(0));
    }

    @Test
    public void test_getBigDecimal() {
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONArray()
                        .fluentAdd(12)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12F),
                new JSONArray()
                        .fluentAdd(12F)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12D),
                new JSONArray()
                        .fluentAdd(12D)
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getBigDecimal(0));
        assertEquals(
                BigDecimal.valueOf(12),
                new JSONArray()
                        .fluentAdd("12")
                        .getBigDecimal(0));
    }

    @Test
    public void test_getFloatValue() {
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(12)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12F),
                new JSONArray()
                        .fluentAdd(12F)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(12D)
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getFloatValue(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd("12")
                        .getFloatValue(0));
    }

    @Test
    public void test_getFloat() {
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(12)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12F),
                new JSONArray()
                        .fluentAdd(12F)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(12D)
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getFloat(0));
        assertEquals(
                Float.valueOf(12),
                new JSONArray()
                        .fluentAdd("12")
                        .getFloat(0));
    }

    @Test
    public void test_getDoubleValue() {
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(12)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12F),
                new JSONArray()
                        .fluentAdd(12F)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(12D)
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getDoubleValue(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd("12")
                        .getDoubleValue(0));
    }

    @Test
    public void test_getDouble() {
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(12)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12F),
                new JSONArray()
                        .fluentAdd(12F)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(12D)
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getDouble(0));
        assertEquals(
                Double.valueOf(12),
                new JSONArray()
                        .fluentAdd("12")
                        .getDouble(0));
    }

    @Test
    public void test_getBoolean() {
        assertEquals(
                Boolean.TRUE,
                new JSONArray()
                        .fluentAdd(1)
                        .getBoolean(0));
        assertEquals(
                Boolean.TRUE,
                new JSONArray()
                        .fluentAdd("true")
                        .getBoolean(0));
        assertEquals(
                Boolean.FALSE,
                new JSONArray()
                        .fluentAdd(Boolean.FALSE)
                        .getBoolean(0));
        assertEquals(
                Boolean.FALSE,
                new JSONArray()
                        .fluentAdd("FALSE")
                        .getBoolean(0));
    }

    @Test
    public void test_getBooleanValue() {
        assertEquals(
                true,
                new JSONArray()
                        .fluentAdd(1)
                        .getBooleanValue(0));
        assertEquals(
                true,
                new JSONArray()
                        .fluentAdd("true")
                        .getBooleanValue(0));
        assertEquals(
                false,
                new JSONArray()
                        .fluentAdd("FALSE")
                        .getBooleanValue(0));
        assertEquals(
                false,
                new JSONArray()
                        .fluentAdd(Boolean.FALSE)
                        .getBooleanValue(0));
    }

    @Test
    public void test_getShortValue() {
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd(12)
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd(12L)
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd(12F)
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd(12D)
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getShortValue(0));
        assertEquals(
                (short) 12,
                new JSONArray()
                        .fluentAdd("12")
                        .getShortValue(0));
    }

    @Test
    public void test_getShort() {
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(12)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(12F)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(12D)
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(Short.valueOf((short) 12))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd(Byte.valueOf((byte) 12))
                        .getShort(0));
        assertEquals(
                Short.valueOf((short) 12),
                new JSONArray()
                        .fluentAdd("12")
                        .getShort(0));
    }

    @Test
    public void test_getByteValue() {
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd(12)
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd(12L)
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd(12F)
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd(12D)
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getByteValue(0));
        assertEquals(
                (byte) 12,
                new JSONArray()
                        .fluentAdd("12")
                        .getByteValue(0));
    }

    @Test
    public void test_getByte() {
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(12)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd((byte) 12)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd((short) 12)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(12L)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(12F)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(12D)
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(new BigDecimal("12"))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(new BigInteger("12"))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(Short.valueOf((short) 12))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd(Byte.valueOf((byte) 12))
                        .getByte(0));
        assertEquals(
                Byte.valueOf((byte) 12),
                new JSONArray()
                        .fluentAdd("12")
                        .getByte(0));
    }

    @Test
    public void test() {
        JSONArray array = new JSONArray();
        ArrayList arrayList = array.toJavaObject(new TypeReference<ArrayList<Integer>>(){}.getType());
        assertEquals(array.size(), arrayList.size());

        assertNull(array.getComponentType());
        array.setComponentType(Integer.class);
        assertEquals(Integer.class, array.getComponentType());

        assertNull(array.getRelatedArray());

        Object[] javaArray = new Object[0];
        array.setRelatedArray(javaArray);
        assertSame(javaArray, array.getRelatedArray());
    }
}
