package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class Issue7678 {
    public static class Person {
        public Set<Bean> testSet;
    }

    public static class Bean {
        public String xxx1;
        public String xxx2;
        public String xxx3;
    }

    public static class Root {
        public List<Object> data;
        public Person person;
    }

    @Test
    public void testHashSetRefDetectJSON() {
        Bean bean = new Bean();
        bean.xxx1 = "123";
        bean.xxx2 = "123";
        bean.xxx3 = "123";

        Person person = new Person();
        person.testSet = new HashSet<>();
        person.testSet.add(bean);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("Person", person);

        Root root = new Root();
        root.data = new ArrayList<>();
        root.data.add(item);
        root.person = person;

        String json = JSON.toJSONString(root, JSONWriter.Feature.ReferenceDetection);
        assertTrue(json.contains("$ref"), "Expected $ref in output: " + json);

        Object parsed = JSON.parse(json);
        assertNotNull(parsed);
    }

    @Test
    public void testHashSetRefDetectJSONB() {
        Bean bean = new Bean();
        bean.xxx1 = "123";
        bean.xxx2 = "123";
        bean.xxx3 = "123";

        Person person = new Person();
        person.testSet = new HashSet<>();
        person.testSet.add(bean);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("Person", person);

        Root root = new Root();
        root.data = new ArrayList<>();
        root.data.add(item);
        root.person = person;

        byte[] bytes = JSONB.toBytes(root, JSONWriter.Feature.ReferenceDetection);
        Root parsed = JSONB.parseObject(bytes, Root.class);
        assertNotNull(parsed);
        assertNotNull(parsed.person);
        assertEquals(1, parsed.person.testSet.size());
        Bean parsedBean = parsed.person.testSet.iterator().next();
        assertEquals("123", parsedBean.xxx1);
    }

    @Test
    public void testHashSetMultiElementRefDetect() {
        Bean bean1 = new Bean();
        bean1.xxx1 = "a";

        Bean bean2 = new Bean();
        bean2.xxx1 = "b";

        Set<Bean> set = new HashSet<>();
        set.add(bean1);
        set.add(bean2);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("set1", set);
        map.put("set2", set);

        String json = JSON.toJSONString(map, JSONWriter.Feature.ReferenceDetection);
        assertTrue(json.contains("$ref"), "Expected $ref in output: " + json);
    }

    @Test
    public void testLinkedHashSetRefDetect() {
        Bean bean = new Bean();
        bean.xxx1 = "123";

        Person person = new Person();
        person.testSet = new LinkedHashSet<>();
        person.testSet.add(bean);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("p1", person);
        map.put("p2", person);

        String json = JSON.toJSONString(map, JSONWriter.Feature.ReferenceDetection);
        assertTrue(json.contains("$ref"), "Expected $ref in output: " + json);
    }

    @Test
    public void testTreeSetRefDetect() {
        Set<String> set = new TreeSet<>();
        set.add("a");
        set.add("b");

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("s1", set);
        map.put("s2", set);

        String json = JSON.toJSONString(map, JSONWriter.Feature.ReferenceDetection);
        assertTrue(json.contains("$ref"), "Expected $ref in output: " + json);
    }
}
