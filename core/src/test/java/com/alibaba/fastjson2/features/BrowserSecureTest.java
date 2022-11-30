package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrowserSecureTest {
    @Test
    public void testUTF8_2_0() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("<>");
        assertEquals("\"\\u003c\\u003e\"", jsonWriter.toString());
    }

    @Test
    public void testUTF8_2_1() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("()");
        assertEquals("\"\\u0028\\u0029\"", jsonWriter.toString());
    }

    @Test
    public void testUTF8_2_2() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("A)");
        assertEquals("\"A\\u0029\"", jsonWriter.toString());
    }

    @Test
    public void testUTF8_2_3() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString(")A");
        assertEquals("\"\\u0029A\"", jsonWriter.toString());
    }

    @Test
    public void testUTF8_4() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("<>()");
        assertEquals("\"\\u003c\\u003e\\u0028\\u0029\"", jsonWriter.toString());
    }

    @Test
    public void testUTF8_8() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("abcd<>()");
        assertEquals("\"abcd\\u003c\\u003e\\u0028\\u0029\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16_2_0() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("<>");
        assertEquals("\"\\u003c\\u003e\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16_2_1() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("()");
        assertEquals("\"\\u0028\\u0029\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16_2_2() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("A)");
        assertEquals("\"A\\u0029\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16_2_3() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString(")A");
        assertEquals("\"\\u0029A\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16_4() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("<>()");
        assertEquals("\"\\u003c\\u003e\\u0028\\u0029\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16_8() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.BrowserSecure);
        jsonWriter.writeString("abcd<>()");
        assertEquals("\"abcd\\u003c\\u003e\\u0028\\u0029\"", jsonWriter.toString());
    }
}
