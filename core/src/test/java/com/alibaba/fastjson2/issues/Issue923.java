package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AccessLevel;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue923 {
    @Test
    public void test() {
        Bean bean = new Bean("DataWorks", 101, 102, new String[]{"abc"});
        String str = JSON.toJSONString(bean);
        assertEquals("{\"age\":101,\"name\":\"DataWorks\",\"score\":102.0,\"tags\":[\"abc\"]}", str);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.age, bean1.age);
        assertEquals(bean.name, bean1.name);
        assertEquals(bean.score, bean1.score);
        assertArrayEquals(bean.tags, bean1.tags);
    }

    @Value
    public static class Bean {
        String name;
        @With(AccessLevel.PACKAGE)
        @NonFinal
        int age;
        double score;
        protected String[] tags;

        @ToString(includeFieldNames = true)
        @Value(staticConstructor = "of")
        public static class Exercise<T> {
            String name;
            T value;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1(101);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"ID\":101}", str);
    }

    @Value
    public static class Bean1 {
        @JSONField(name = "ID")
        int id;
    }

    @Test
    public void test2() {
        String json = "{\"id\":1, \"name\":\"Name\", \"user_name\":\"User name\"}";
        User object = JSON.parseObject(json, User.class);
        assertEquals(1, object.id);
        assertEquals("Name", object.name);
        assertEquals("User name", object.userName);

        String result = JSON.toJSONString(object, JSONWriter.Feature.WriteNulls);
        assertEquals("{\"id\":1,\"name\":\"Name\",\"user_name\":\"User name\"}", result);
    }

    @Value
    public class User {
        @JSONField(name = "id")
        int id;
        @JSONField(name = "name")
        String name;
        @JSONField(name = "user_name")
        String userName;
    }
}
