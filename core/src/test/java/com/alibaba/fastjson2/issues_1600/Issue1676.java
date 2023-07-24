package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue1676 {
    @Test
    public void test() {
        ParameterizedType parameterizedType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {Bean.class};
            }

            @Override
            public Type getRawType() {
                return Page.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        String s = "{\"current\":1,\"hitCount\":false,\"optimizeCountSql\":true,\"orders\":[],\"pages\":1,\"records\":[{\"name\": \"test\"}],\"searchCount\":true,\"size\":10,\"total\":1}";
        Page fastJson2PageObject = JSON.parseObject(s, parameterizedType);
        assertSame(Bean.class, fastJson2PageObject.getRecords().get(0).getClass());
    }

    public static class Bean {
    }

    public static class Page<T> {
        private List<T> records;

        public List<T> getRecords() {
            return records;
        }

        public void setRecords(List<T> records) {
            this.records = records;
        }
    }
}
