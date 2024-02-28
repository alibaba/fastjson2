package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.Assert;
import org.junit.Test;

public class CompareUtilsTest {
    @org.junit.Test
    public void diffToArray() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONArray result = CompareUtils.diffToArray(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "[\n" +
                "\t\n" +
                "]";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void diffToArray3() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONArray result = CompareUtils.diffToArray(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "[\n" +
                "\t{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":\"abc\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"ADD\",\n" +
                "\t\t\"value2\":1\n" +
                "\t}\n" +
                "]";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testDiff() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testDiff1() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testDiff2() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\"number\":{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":1\n" +
                "\t},\n" +
                "\t\"string\":{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"object.number\":{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t\"object.string\":{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"array[0].number\":{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t\"array[0].string\":{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testDiff3() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\"string\":{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t\"object.number\":{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":\"abc\"\n" +
                "\t},\n" +
                "\t\"object.string\":{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t\"array[0].number\":{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t\"array[0].string\":{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"number\":{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"ADD\",\n" +
                "\t\t\"value2\":1\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testCompare() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\"number\":{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":true\n" +
                "\t},\n" +
                "\t\"string\":{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":true\n" +
                "\t},\n" +
                "\t\"object.number\":{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":true\n" +
                "\t},\n" +
                "\t\"object.string\":{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":true\n" +
                "\t},\n" +
                "\t\"array[0].number\":{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":true\n" +
                "\t},\n" +
                "\t\"array[0].string\":{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":true\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testCompare1() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':11," +
                "'string':'abcd'," +
                "'object': {'number':11,'string':'abcd',}," +
                "'array': [{'number':11,'string':'abcd',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\"number\":{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":true,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":11,\n" +
                "\t\t\"value2\":1\n" +
                "\t},\n" +
                "\t\"string\":{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":true,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abcd\",\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"object.number\":{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":true,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":11,\n" +
                "\t\t\"value2\":1\n" +
                "\t},\n" +
                "\t\"object.string\":{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":true,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abcd\",\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"array[0].number\":{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":true,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":11,\n" +
                "\t\t\"value2\":1\n" +
                "\t},\n" +
                "\t\"array[0].string\":{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":true,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abcd\",\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testCompare2() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\"number\":{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":1\n" +
                "\t},\n" +
                "\t\"string\":{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"object.number\":{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t\"object.string\":{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"array[0].number\":{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t\"array[0].string\":{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testCompare3() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\n" +
                "\t\"string\":{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t\"object.number\":{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":\"abc\"\n" +
                "\t},\n" +
                "\t\"object.string\":{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t\"array[0].number\":{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t\"array[0].string\":{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t\"number\":{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"ADD\",\n" +
                "\t\t\"value2\":1\n" +
                "\t}\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testCompareToArray3() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONArray result = CompareUtils.compareToArray(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "[\n" +
                "\t{\n" +
                "\t\t\"path\":\"string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"object.number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":\"abc\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"object.string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"REMOVE\",\n" +
                "\t\t\"value1\":123\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"array[0].number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":\"abc\",\n" +
                "\t\t\"value2\":123\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"array[0].string\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"typeEqual\":false,\n" +
                "\t\t\"diffType\":\"MODIFY\",\n" +
                "\t\t\"value1\":123,\n" +
                "\t\t\"value2\":\"abc\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"path\":\"number\",\n" +
                "\t\t\"valueEqual\":false,\n" +
                "\t\t\"diffType\":\"ADD\",\n" +
                "\t\t\"value2\":1\n" +
                "\t}\n" +
                "]";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }

    @org.junit.Test
    public void testEquals() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        boolean result = CompareUtils.equals(json1, json2);
        System.out.println(result);
        Object expected = true;
        Assert.assertEquals(expected, result);
    }

    @org.junit.Test
    public void testEquals3() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        boolean result = CompareUtils.equals(json1, json2);
        System.out.println(result);
        Object expected = false;
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testSum() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONArray list = CompareUtils.diffToArray(json1, json2);
        JSONObject result = CompareUtils.sum(list);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        Object expected = "{\n" +
                "\t\"equal\":false,\n" +
                "\t\"total\":6,\n" +
                "\t\"valueEqualCount\":0,\n" +
                "\t\"typeEqualCount\":2,\n" +
                "\t\"diffCount\":6,\n" +
                "\t\"addCount\":1,\n" +
                "\t\"removeCount\":3,\n" +
                "\t\"modifyCount\":2\n" +
                "}";
        Assert.assertEquals(expected, result.toString(JSONWriter.Feature.PrettyFormat));
    }
}
