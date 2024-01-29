package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1835 {
    @Test
    public void test() {
        List<Person> sourceList = new ArrayList<>();
        Set<Person> sourceSet = new HashSet<>();
        Map<Integer, Person> sourceMap = new HashMap<>();
        Map<String, Object> source = new HashMap<>();
        source.put("data", sourceList);
        source.put("data2", sourceSet);
        source.put("data3", sourceMap);

        JSONObject target = JSON.parseObject(JSON.toJSONString(source));
        List<Person> targetList = target.getObject("data", getType("list"));
        Set<Person> targetSet = target.getObject("data2", getType("set"));
        Map<Integer, Person> targetMap = target.getObject("data3", getType("map"));

        Person lisi = new Person("lisi");
        targetList.add(lisi);
        assertEquals("[{\"name\":\"lisi\"}]", JSON.toJSONString(targetList));
        targetSet.add(lisi);
        assertEquals("[{\"name\":\"lisi\"}]", JSON.toJSONString(targetSet));
        targetMap.put(0, lisi);
        assertEquals("{0:{\"name\":\"lisi\"}}", JSON.toJSONString(targetMap));
    }

    public static Type getType(String type) {
        try {
            Issue1835 app = new Issue1835();
            Method method = app.getClass().getMethod(type);
            return method.getGenericReturnType();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Person> list() {
        return new ArrayList<>();
    }

    public Set<Person> set() {
        return new HashSet<>();
    }

    public Map<Integer, Person> map() {
        return new HashMap<>();
    }

    public static class Person {
        private String name;

        public Person() {
        }

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void test1() {
        Page<Person> page = new Page<>();
        List<Person> data = new ArrayList<>();
        data.add(null);
        data.add(new Person("abc"));
        data.add(null);
        page.data = Collections.unmodifiableList(data);

        byte[] bytes = JSONB.toBytes(page, JSONWriter.Feature.WriteClassName);
        Page page1 = JSONB.parseObject(bytes, Page.class);
        assertNotNull(page1);
    }

    public static class Page<T> {
        public List<T> data = new ArrayList<>();

        public void setData(List<T> items) {
            if (items == null || items.isEmpty()) {
                return;
            }
            this.data = items.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
    }
}
