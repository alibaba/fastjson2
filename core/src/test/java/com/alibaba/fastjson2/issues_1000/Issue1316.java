package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1316 {
    @Test
    public void test() {
        Person person = new Person();
        person.setWoman(new AtomicBoolean(true));
        person.setName("小红");
        String str = JSON.toJSONString(person, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"@type\":\"com.alibaba.fastjson2.issues_1000.Issue1316$Person\",\"name\":\"小红\",\"woman\":true}", str);

        Person bean = (Person) JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(person.name, bean.name);
        assertEquals(person.woman.get(), bean.woman.get());
    }

    @Data
    public static class Person {
        private AtomicBoolean woman;
        private String name;
    }
}
