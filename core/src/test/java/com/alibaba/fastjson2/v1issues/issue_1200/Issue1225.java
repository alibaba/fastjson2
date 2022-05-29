package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 30/05/2017.
 */
public class Issue1225 {
    @Test
    public void test_parseObject_0() {
        BaseGenericType<List<String>> o = JSON.parseObject("{\"data\":[\"1\",\"2\",\"3\"]}",
                new TypeReference<BaseGenericType<List<String>>>() {
                }.getType());
        assertEquals("2", o.data.get(1));
    }

    @Test
    public void test_parseObject_1() {
        Type type = new TypeReference<ExtendGenericType<String>>() {
        }.getType();
        ExtendGenericType<String> o = JSON.parseObject("{\"data\":[\"1\",\"2\",\"3\"]}", type);
        assertEquals("2", o.data.get(1));
    }

    @Test
    public void test_parseObject_2() {
        SimpleGenericObject object = JSON.parseObject("{\"data\":[\"1\",\"2\",\"3\"],\"a\":\"a\"}",
                SimpleGenericObject.class);

        assertEquals("2", object.data.get(1));
    }

    static class BaseGenericType<T> {
        public T data;
    }

    static class ExtendGenericType<T>
            extends BaseGenericType<List<T>> {
    }

    static class SimpleGenericObject
            extends ExtendGenericType<String> {
    }
}
