package com.alibaba.fastjson2.v1issues.issue_3200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @Author ：Nanqi
 * @Date ：Created in 20:38 2020/6/27
 */
public class Issue3227 {
    @Test
    public void test_for_issue() {
        String json = "{\"code\":\"123\"}";
        Child child = JSON.parseObject(json, Child.class);
        assertNotNull(child);
    }

    static class Parent<T> {
        protected String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        protected T code;

        public T getCode() {
            return code;
        }

        public void setCode(T code) {
            this.code = code;
        }
    }

    static class Child
            extends Parent<Integer> {
        @Override
        public Integer getCode() {
            return code;
        }

        @Override
        public void setCode(Integer code) {
            this.code = code;
        }
    }
}
