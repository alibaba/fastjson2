package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3351 {
    record AnimalRecord(@JSONField(name = "age", alternateNames = "age2") Long age) {
    }

    static class AnimalClass {
        @JSONField(name = "age", alternateNames = "age2")
        public Long age;
    }

    @Test
    public void test() {
        String s1 = "{\"age\": 20}";
        AnimalRecord a1 = JSON.parseObject(s1, AnimalRecord.class);
        assertEquals(20, a1.age);

        String s2 = "{\"age2\": 20}";
        AnimalRecord a2 = JSON.parseObject(s2, AnimalRecord.class);
        assertEquals(20, a2.age);

        String s3 = "{\"age\": 20}";
        AnimalClass a3 = JSON.parseObject(s3, AnimalClass.class);
        assertEquals(20, a3.age);

        String s4 = "{\"age2\": 20}";
        AnimalClass a4 = JSON.parseObject(s4, AnimalClass.class);
        assertEquals(20, a4.age);
    }
}
