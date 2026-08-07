package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONArrayTest2 {
    @Test
    public void test_0() throws Exception {
        long time = System.currentTimeMillis();
        JSONArray array = new JSONArray();
        array.add(null);
        array.add(1);
        array.add(time);
        assertEquals(0, array.getByteValue(0));
        assertEquals(0, array.getShortValue(0));
        assertTrue(0F == array.getFloatValue(0));
        assertTrue(0D == array.getDoubleValue(0));
        assertEquals(new BigInteger("1"), array.getBigInteger(1));
        assertEquals("1", array.getString(1));
        assertEquals(new java.util.Date(time), array.getDate(2));
//        assertEquals(new java.sql.Date(time), array.getSqlDate(2));
//        assertEquals(new java.sql.Timestamp(time), array.getTimestamp(2));

        JSONArray array2 = (JSONArray) array.clone();
        assertEquals(0, array2.getByteValue(0));
        assertEquals(0, array2.getShortValue(0));
        assertTrue(0F == array2.getFloatValue(0));
        assertTrue(0D == array2.getDoubleValue(0));
        assertEquals(new BigInteger("1"), array2.getBigInteger(1));
        assertEquals("1", array2.getString(1));
        assertEquals(new java.util.Date(time), array2.getDate(2));
//        assertEquals(new java.sql.Date(time), array2.getSqlDate(2));
//        assertEquals(new java.sql.Timestamp(time), array2.getTimestamp(2));
        assertEquals(array2.size(), array2.size());
    }
}
