package com.alibaba.fastjson2.v1issues.issue_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1513 {
    @Test
    public void test_for_issue() throws Exception {
        {
            Model<Object> model = JSON.parseObject("{\"values\":[{\"id\":123}]}", new TypeReference<Model<Object>>() {
            }.getType());
            assertNotNull(model.values);
            assertEquals(1, model.values.length);
            JSONObject object = (JSONObject) model.values[0];
            assertEquals(123, object.getIntValue("id"));
        }
        {
            Model<A> model = JSON.parseObject("{\"values\":[{\"id\":123}]}", new TypeReference<Model<A>>() {
            }.getType());
            assertNotNull(model.values);
            assertEquals(1, model.values.length);
            A a = model.values[0];
            assertEquals(123, a.id);
        }
        {
            Model<B> model = JSON.parseObject("{\"values\":[{\"value\":123}]}", new TypeReference<Model<B>>() {
            }.getType());
            assertNotNull(model.values);
            assertEquals(1, model.values.length);
            B b = model.values[0];
            assertEquals(123, b.value);
        }
        {
            Model<C> model = JSON.parseObject("{\"values\":[{\"age\":123}]}", new TypeReference<Model<C>>() {
            }.getType());
            assertNotNull(model.values);
            assertEquals(1, model.values.length);
            C c = model.values[0];
            assertEquals(123, c.age);
        }
    }

    public static class Model<T> {
        public T[] values;
    }

    public static class A {
        public int id;
    }

    public static class B {
        public int value;
    }

    public static class C {
        public int age;
    }
}
