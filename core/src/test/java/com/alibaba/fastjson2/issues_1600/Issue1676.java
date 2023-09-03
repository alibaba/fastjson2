package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1676 {
    @Test
    public void test1() {
        ParameterizedType parameterizedType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {Bean.class};
            }

            @Override
            public Type getRawType() {
                return PageImpl.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        String s = "{\"current\":1,\"hitCount\":false,\"optimizeCountSql\":true,\"orders\":[],\"pages\":1,\"records\":[{\"name\": \"test\"}],\"searchCount\":true,\"size\":10,\"total\":1}";
        PageImpl fastJson2PageObject = JSON.parseObject(s, parameterizedType);
        assertEquals(Bean.class, fastJson2PageObject.getRecords().get(0).getClass());
    }

    @Test
    public void test2() {
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
        assertEquals(Bean.class, fastJson2PageObject.getRecords().get(0).getClass());
    }

    public static class Bean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public interface IPage<T>
            extends Serializable {
        List<T> getRecords();

        // 问题出现在这里，如果这里返回结果是IPage<T>，那么fastjson2就会解析有问题，但fastjson1不会有问题；如果返回结果是void也不会有问题
        IPage<T> setRecords(List<T> records);
    }

    public static class PageImpl<T>
            implements IPage<T> {
        protected List<T> records;

        @Override
        public List<T> getRecords() {
            return this.records;
        }

        @Override
        public PageImpl<T> setRecords(List<T> records) {
            this.records = records;
            return this;
        }
    }

    public static class Page<T> {
        private List<T> records;

        public List<T> getRecords() {
            return records;
        }

        public Page<T> setRecords(List<T> records) {
            this.records = records;
            return this;
        }
    }
}
