package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
