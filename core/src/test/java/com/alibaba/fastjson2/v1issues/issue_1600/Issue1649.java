package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1649 {
    @Test
    public void test_for_issue() throws Exception {
        Apple apple = new Apple();
        String json = JSON.toJSONString(apple);
        assertEquals("{\"color\":\"\",\"productCity\":\"\",\"size\":0}", json);
    }

    @JSONType(serializeFeatures = Feature.NullAsDefaultValue)
    public static class Apple {
        private String color;
        private String productCity;
        private int size;

        public String getColor() {
            return color;
        }

        public Apple setColor(String color) {
            this.color = color;
            return this;
        }

        public int getSize() {
            return size;
        }

        public Apple setSize(int size) {
            this.size = size;
            return this;
        }

        public String getProductCity() {
            return productCity;
        }

        public Apple setProductCity(String productCity) {
            this.productCity = productCity;
            return this;
        }
    }
}
