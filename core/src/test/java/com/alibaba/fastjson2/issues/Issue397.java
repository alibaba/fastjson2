package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue397 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"items\":{}}", Bean.class);
        assertNull(bean.getItems());
    }

    public static class Bean {
        public Map<String, Object> getItems() {
            return null;
        }
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"items\":{}}", Bean1.class);
        assertNull(bean.items);
    }

    public static class Bean1 {
        public final Map<String, Object> items = null;
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{\"items\":[]}", Bean2.class);
        assertNull(bean.getItems());
    }

    public static class Bean2 {
        public List<String> getItems() {
            return null;
        }
    }

    @Test
    public void test3() {
        Bean3 bean = JSON.parseObject("{\"items\":[]}", Bean3.class);
        assertNull(bean.items);
    }

    public static class Bean3 {
        public final List<String> items = null;
    }
}
