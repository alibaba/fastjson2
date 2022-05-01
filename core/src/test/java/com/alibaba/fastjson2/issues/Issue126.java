package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue126 {
    @Test
    public void test() {
        Bean bean = new Bean();
        assertEquals("{\"b\":false,\"a\":false}", JSON.toJSONString(bean));
    }

    public static class Bean {
        @JSONField(ordinal = 2)
        public boolean a;

        @JSONField(ordinal = 1)
        public boolean b;
    }

    @Test
    public void test_people() {
        People people = new People();
        people.setAge(20);
        people.setAdult(true);
        people.setWeight(145.5);
        people.setName("MASON");
        people.setHeight(185.5f);
        String peopleStr = JSON.toJSONString(people);
        assertEquals("{\"height\":185.5,\"name\":\"MASON\",\"age\":20,\"adult\":true,\"weight\":145.5}", peopleStr);
    }

    @Data
    public static class People {

        @JSONField(ordinal = 5)
        public double weight;

        @JSONField(ordinal = 4)
        public boolean adult;

        @JSONField(ordinal = 3)
        public int age;

        @JSONField(ordinal = 2)
        public String name;

        @JSONField(ordinal = 1)
        public float height;
    }
}
