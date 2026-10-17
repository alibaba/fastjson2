package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
public class Issue7842 {
    @Test
    public void testTrueSpellings() {
        for (String s : new String[]{"true", "TRUE", "True", "T", "t", "Y", "y"}) {
            assertTrue(TypeUtils.cast(s, Boolean.class), s);
        }
    }

    @Test
    public void testFalseSpellings() {
        for (String s : new String[]{"false", "FALSE", "False", "0", "F", "f", "N", "n"}) {
            assertFalse(TypeUtils.cast(s, Boolean.class), s);
        }
    }

    @Test
    public void testLenientFallback() {
        assertFalse(TypeUtils.cast("abc", Boolean.class));
        assertFalse(TypeUtils.cast("2", Boolean.class));
        assertFalse(TypeUtils.cast("YES", Boolean.class));
        // "1" stays false: pinned by JSONObjectTest.test_invoke, do not change it here
        assertFalse(TypeUtils.cast("1", Boolean.class));
        assertNull(TypeUtils.cast("", Boolean.class));
        assertNull(TypeUtils.cast("null", Boolean.class));
        assertNull(TypeUtils.cast(null, Boolean.class));
    }

    @Test
    public void testPrimitive() {
        assertEquals(Boolean.TRUE, TypeUtils.cast("TRUE", boolean.class));
        assertEquals(Boolean.FALSE, TypeUtils.cast("1", boolean.class));
        assertEquals(Boolean.FALSE, TypeUtils.cast("FALSE", boolean.class));
        assertEquals(Boolean.FALSE, TypeUtils.cast("abc", boolean.class));
        assertEquals(Boolean.FALSE, TypeUtils.cast("", boolean.class));
    }

    @Test
    public void testAccessorAgreement() {
        // getBoolean and getObject(Boolean.class) must agree on every spelling ...
        for (String s : new String[]{
                "true", "TRUE", "True", "T", "t", "Y", "y",
                "false", "FALSE", "False", "0", "F", "f", "N", "n",
                "YES", "abc", "2", "", "null"}) {
            JSONObject jo = new JSONObject();
            jo.put("f", s);
            assertEquals(jo.getObject("f", Boolean.class), jo.getBoolean("f"), s);
        }
        // ... except "1", which is a deliberate per-path contract:
        // getBoolean("1") is true, cast("1") stays false (pinned by JSONObjectTest.test_invoke)
        JSONObject jo = new JSONObject();
        jo.put("f", "1");
        assertTrue(jo.getBoolean("f"));
        assertFalse(jo.getObject("f", Boolean.class));
        // ... and except mixed-case null spellings, also a deliberate per-path contract:
        // getBoolean uses an equalsIgnoreCase null guard (null), while getObject routes
        // through StringToAny's case-sensitive "null" guard and falls back to FALSE
        for (String s : new String[]{"NULL", "Null"}) {
            JSONObject joNull = new JSONObject();
            joNull.put("f", s);
            assertNull(joNull.getBoolean("f"), s);
            assertFalse(joNull.getObject("f", Boolean.class), s);
        }
    }

    @Test
    public void testJSONArrayAgreement() {
        for (String s : new String[]{
                "true", "TRUE", "True", "T", "t", "Y", "y",
                "false", "FALSE", "False", "0", "F", "f", "N", "n",
                "YES", "abc", "2", "", "null"}) {
            JSONArray ja = new JSONArray();
            ja.add(s);
            assertEquals(ja.getObject(0, Boolean.class), ja.getBoolean(0), s);
        }
        // "1" keeps the same deliberate per-path contract as the JSONObject twin
        JSONArray ja = new JSONArray();
        ja.add("1");
        assertTrue(ja.getBoolean(0));
        assertFalse(ja.getObject(0, Boolean.class));
    }

    public static class FlagBean {
        public Boolean f;
    }

    @Test
    public void testBeanParseBoundary() {
        // deliberate boundary contract: tree/cast paths stay lenient ...
        JSONObject jo = JSON.parseObject("{\"f\":\"t\"}");
        assertTrue(jo.getBoolean("f"));
        assertTrue(jo.getObject("f", Boolean.class));
        assertTrue(TypeUtils.cast("t", Boolean.class));
        // ... while bean-binding stays strict and rejects "t" as malformed
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"f\":\"t\"}", FlagBean.class));
    }
}
