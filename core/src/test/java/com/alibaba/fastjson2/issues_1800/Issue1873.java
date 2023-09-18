package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1873 {
    @Test
    public void test() {
        String json = "[{\"array\":[\"test1\",\"test2\",\"test3\"]}]";
        List<ListTest> values = JSONObject.parseObject(json, TypeReference.collectionType(List.class, ListTest.class));
        assertEquals(json, JSONObject.toJSONString(values));
        json = "{\"array\":[\"test1\",\"test2\",\"test3\"]}";
        ListTest listTest = JSONObject.parseObject(json, ListTest.class);
        assertEquals(json, JSONObject.toJSONString(listTest));
    }

    static class ListTest {
        public final List<String> array;

        public ListTest(List<String> array) {
            this.array = array;
        }
    }
}
