package com.alibaba.fastjson2.v1issues.issue_3000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3082 {
    @Test
    public void test_for_issue_entry() {
        String str = "{\"k\":{\"k\":\"v\"}}";
        Map.Entry<String, Map.Entry<String, String>> entry = JSON.parseObject(str,
                new TypeReference<Map.Entry<String, Map.Entry<String, String>>>() {
                });
        assertEquals("v", entry.getValue().getValue());
    }

    public void test_for_issue() throws Exception {
        HashSet<Map.Entry<String, Map.Entry<String, String>>> nestedSet = new HashSet<Map.Entry<String, Map.Entry<String, String>>>();
        nestedSet.add(new AbstractMap.SimpleEntry<String, Map.Entry<String, String>>("a", new AbstractMap.SimpleEntry<String, String>("b", "c")));
        nestedSet.add(new AbstractMap.SimpleEntry<String, Map.Entry<String, String>>("d", new AbstractMap.SimpleEntry<String, String>("e", "f")));

        String content = JSON.toJSONString(nestedSet);

        HashSet<Map.Entry<String, Map.Entry<String, String>>> deserializedNestedSet;
        Type type = new TypeReference<HashSet<Map.Entry<String, Map.Entry<String, String>>>>() {
        }.getType();
        deserializedNestedSet = JSON.parseObject(content, type);
        assertEquals(nestedSet, deserializedNestedSet);
    }
}
