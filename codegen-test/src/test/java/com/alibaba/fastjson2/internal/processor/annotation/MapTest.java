package com.alibaba.fastjson2.internal.processor.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.values = new HashMap<>();
        bean.values.put("a", "101");
        bean.values.put("b", "201");

        String str = JSON.toJSONString(bean);
        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertEquals(bean.values.size(), bean1.values.size());
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean{
        public Map<String, String> values;
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.values = new HashMap<>();
        bean.values.put("a", "101");
        bean.values.put("b", "201");

        String str = JSON.toJSONString(bean);
        Bean1 bean1 = JSON.parseObject(str, Bean1.class);
        assertEquals(bean.values.size(), bean1.values.size());
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean1{
        public Map values;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.values = new HashMap<>();
        bean.values.put("a", "101");
        bean.values.put("b", "201");

        String str = JSON.toJSONString(bean);
        Bean2 bean1 = JSON.parseObject(str, Bean2.class);
        assertEquals(bean.values.size(), bean1.values.size());
        String str1 = JSON.toJSONString(bean1);
        assertEquals(str, str1);
    }

    @JSONCompiled
    public static class Bean2{
        private Map<String, String> values;

        public Map<String, String> getValues() {
            return values;
        }

        public void setValues(Map<String, String> values) {
            this.values = values;
        }
    }
}
