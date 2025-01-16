package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
        HashSet<Map.Entry<String, Map.Entry<String, String>>> nestedSet = new HashSet<>();
        nestedSet.add(new AbstractMap.SimpleEntry<>("a", new AbstractMap.SimpleEntry<>("b", "c")));
        nestedSet.add(new AbstractMap.SimpleEntry<>("d", new AbstractMap.SimpleEntry<>("e", "f")));

        String content = JSON.toJSONString(nestedSet);

        HashSet<Map.Entry<String, Map.Entry<String, String>>> deserializedNestedSet;
        Type type = new TypeReference<HashSet<Map.Entry<String, Map.Entry<String, String>>>>() {
        }.getType();
        deserializedNestedSet = JSON.parseObject(content, type);
        assertEquals(nestedSet, deserializedNestedSet);
    }
}
