package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2560 {
    @Test
    public void mutatedTest() {
        List<TestJson> list = new ArrayList<>();
        list.add(new TestJson(true, Boolean.TRUE));
        list.add(new TestJson(false, Boolean.FALSE));
        String json = JSON.toJSONString(list, JSONWriter.Feature.WriteNonStringValueAsString);
        List<TestJson> list2 = JSON.parseArray(json, TestJson.class);
        assertEquals(list, list2);
        String expected = "[{\"b\":\"true\",\"b2\":\"true\"},{\"b\":\"false\",\"b2\":\"false\"}]";
        assertEquals(expected, json);
        assertEquals(expected, new String(JSON.toJSONBytes(list, JSONWriter.Feature.WriteNonStringValueAsString)));
    }

    @Data
    @AllArgsConstructor
    public static class TestJson {
        private boolean b;
        private Boolean b2;
    }

    @Test
    public void mutatedTest1() {
        List<TestJson1> list = new ArrayList<>();
        list.add(new TestJson1(1, 1));
        list.add(new TestJson1(2, 2));
        String json = JSON.toJSONString(list, JSONWriter.Feature.WriteNonStringValueAsString);
        System.out.println(json);
        List<TestJson1> list2 = JSON.parseArray(json, TestJson1.class);
        assertEquals(list, list2);
        assertEquals("[{\"a\":\"1\",\"a2\":\"1\"},{\"a\":\"2\",\"a2\":\"2\"}]", json);
    }

    @Data
    @AllArgsConstructor
    public static class TestJson1 {
        private int a;
        private Integer a2;
    }
}
