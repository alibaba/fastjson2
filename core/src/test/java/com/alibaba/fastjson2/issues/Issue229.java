package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue229 {
    @Test
    public void testParse() {
        String json = "{\n" +
                "\t\"name\": { //这是个注释\n" +
                "\t\t\"one\": 2020,\n" +
                "\t\t\"two\": \"red!\"\n" +
                "\t}\n" +
                "}";
        JSONObject object = JSON.parseObject(json);
        assertEquals(2020, object.getJSONObject("name").get("one"));
    }

    @Test
    public void testParseBytes() {
        String json = "{\n" +
                "\t\"name\": { //这是个注释\n" +
                "\t\t\"one\": 2020,\n" +
                "\t\t\"two\": \"red!\"\n" +
                "\t}\n" +
                "}";
        JSONObject object = JSON.parseObject(json.getBytes(StandardCharsets.UTF_8));
        assertEquals(2020, object.getJSONObject("name").get("one"));
    }

    @Test
    public void testParseStr() {
        String json = "{\n" +
                "\t\"name\": { //这是个注释\n" +
                "\t\t\"one\": 2020,\n" +
                "\t\t\"two\": \"red!\"\n" +
                "\t}\n" +
                "}";
        JSONObject object = TestUtils.createJSONReaderStr(json).read(JSONObject.class);
        assertEquals(2020, object.getJSONObject("name").get("one"));
    }

    @Test
    public void testParseStr1() {
        String json = "{\n" +
                "\t\"name\": { \n" +
                "\t\t\"one\": 2020,//这是个注释\n" +
                "\t\t\"two\": \"red!\"\n" +
                "\t}\n" +
                "}";
        JSONObject object = TestUtils.createJSONReaderStr(json).read(JSONObject.class);
        assertEquals(2020, object.getJSONObject("name").get("one"));
    }
}
