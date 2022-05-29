package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1649_private {
    @Test
    public void test_for_issue() throws Exception {
        Apple apple = new Apple();
        String json = JSON.toJSONString(apple);
        assertEquals("{\"color\":\"\",\"productCity\":\"\",\"size\":0}", json);
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.NullAsDefaultValue)
    private static class Apple {
        // @JSONField(serialzeFeatures = {SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue})
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

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }

        public static void main(String[] args) {
            System.out.println(JSON.toJSONString(new Apple()));
        }
    }
}
