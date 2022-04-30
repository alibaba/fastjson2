package com.alibaba.fastjson2.v1issues.issue_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2387 {
    @Test
    public void test_for_issue() throws Exception {
        String jsonStr = "{\"id\":\"ss\",'ddd':\"sdfsd\",'name':\"hh\"}";
        TestEntity news = JSON.parseObject(jsonStr, TestEntity.class, JSONReader.Feature.InitStringFieldAsEmpty);
        assertEquals("{\"ddd\":\"sdfsd\",\"id\":\"ss\",\"name\":\"hh\"}", JSON.toJSONString(news));
    }

    public static class TestEntity {
        private String id;
        private String ddd;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDdd() {
            return ddd;
        }

        public void setDdd(String ddd) {
            this.ddd = ddd;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
