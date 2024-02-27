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
        String expected = "[]";
        Assert.assertEquals(expected, result.toString());
    }

    @org.junit.Test
    public void diffToArray3() {
        JSONObject json1 = JSONObject.parseObject("{" +
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONArray result = CompareUtils.diffToArray(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "[{\"path\":\"string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},{\"path\":\"object.number\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":\"abc\"},{\"path\":\"object.string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},{\"path\":\"array[0].number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},{\"path\":\"array[0].string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},{\"path\":\"number\",\"valueEqual\":false,\"diffType\":\"ADD\",\"value2\":1}]";
        Assert.assertEquals(expected, result.toString());
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
        String expected = "{}";
        Assert.assertEquals(expected, result.toString());
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
        String expected = "{}";
        Assert.assertEquals(expected, result.toString());
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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":1},\"string\":{\"path\":\"string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"}}";
        Assert.assertEquals(expected, result.toString());
    }

    @org.junit.Test
    public void testDiff3() {
        JSONObject json1 = JSONObject.parseObject("{" +
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"string\":{\"path\":\"string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":\"abc\"},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},\"number\":{\"path\":\"number\",\"valueEqual\":false,\"diffType\":\"ADD\",\"value2\":1}}";
        Assert.assertEquals(expected, result.toString());
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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":true},\"string\":{\"path\":\"string\",\"valueEqual\":true},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":true},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":true},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":true},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":true}}";
        Assert.assertEquals(expected, result.toString());
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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":true,\"value1\":11,\"value2\":1},\"string\":{\"path\":\"string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":true,\"value1\":\"abcd\",\"value2\":\"abc\"},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":true,\"value1\":11,\"value2\":1},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":true,\"value1\":\"abcd\",\"value2\":\"abc\"},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":true,\"value1\":11,\"value2\":1},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":true,\"value1\":\"abcd\",\"value2\":\"abc\"}}";
        Assert.assertEquals(expected, result.toString());
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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":1},\"string\":{\"path\":\"string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"}}";
        Assert.assertEquals(expected, result.toString());
    }

    @org.junit.Test
    public void testCompare3() {
        JSONObject json1 = JSONObject.parseObject("{" +
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"string\":{\"path\":\"string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":\"abc\"},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},\"number\":{\"path\":\"number\",\"valueEqual\":false,\"diffType\":\"ADD\",\"value2\":1}}";
        Assert.assertEquals(expected, result.toString());
    }

    @org.junit.Test
    public void testCompareToArray3() {
        JSONObject json1 = JSONObject.parseObject("{" +
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONArray result = CompareUtils.compareToArray(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "[{\"path\":\"string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},{\"path\":\"object.number\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":\"abc\"},{\"path\":\"object.string\",\"valueEqual\":false,\"diffType\":\"REMOVE\",\"value1\":123},{\"path\":\"array[0].number\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123},{\"path\":\"array[0].string\",\"valueEqual\":false,\"diffType\":\"MODIFY\",\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\"},{\"path\":\"number\",\"valueEqual\":false,\"diffType\":\"ADD\",\"value2\":1}]";
        Assert.assertEquals(expected, result.toString());
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
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
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
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
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
