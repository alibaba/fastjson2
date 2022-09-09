package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue732 {
    @Test
    public void test() {
        SimpleDto bean = JSON.parseObject("{\"testProperty\":null}", SimpleDto.class);
        assertNull(bean.testProperty);
    }

    public static class SimpleDto {
        private List<String> testProperty;

        public List<String> getTestProperty() {
            return testProperty;
        }

        public void setTestProperty(List<String> testProperty) {
            this.testProperty = testProperty;
        }

        public List<String> getTestMethod() {
            return null;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{\"testProperty\":null}", Bean2.class);
        assertNull(bean.testProperty);
    }

    public static class Bean2 {
        private List testProperty;

        public List getTestProperty() {
            return testProperty;
        }

        public void setTestProperty(List testProperty) {
            this.testProperty = testProperty;
        }

        public List getTestMethod() {
            return null;
        }
    }
}
