package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue823 {
    @Test
    public void test() {
        TestItem source = new TestItem(1, "1");
        source.setName("测试实例1");
        source.setDescription("兼容性测试！");

        TestItem child = new TestItem(2, "2");
        child.setName("测试子实例2");
        child.setDescription("兼容性测试！");
        source.setChild(child);

        String sourceContent = com.alibaba.fastjson2.JSON.toJSONString(source);
        com.alibaba.fastjson2.JSONObject targetV2Json = com.alibaba.fastjson2.JSONObject.parseObject(sourceContent);
        assertEquals(sourceContent, targetV2Json.toString());

        TestItem targetV2 = com.alibaba.fastjson2.JSON.to(TestItem.class, targetV2Json);
        assertEquals(sourceContent, com.alibaba.fastjson2.JSON.toJSONString(targetV2));

        assertEquals(
                JSON.toJSONString(source.child),
                JSON.toJSONString(targetV2Json.getObject("child", TestItem.class))
        );
    }

    public static class TestItem {
        private Integer id;
        private String code;
        private String name;
        private String description;
        private TestItem child;

        public TestItem(Integer id, String code) {
            this.id = id;
            this.code = code;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public TestItem getChild() {
            return child;
        }

        public void setChild(TestItem child) {
            this.child = child;
        }
    }
}
