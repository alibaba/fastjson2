package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpperTest {
    @Test
    public void test0() {
        String value = "abc";
        String valueUpper = value.toUpperCase();

        JSONObject jsonObject = JSONObject.of("value", value);
        String json = jsonObject.toJSONString();
        assertEquals(
                valueUpper,
                jsonObject.toJavaObject(Bean0.class).value);
        assertEquals(
                valueUpper,
                JSON.parseObject(json, Bean0.class).value);
        assertEquals(
                valueUpper,
                ObjectReaderCreator.INSTANCE
                        .createObjectReader(Bean0.class)
                        .readObject(json).value);
    }

    public static class Bean0 {
        @JSONField(format = "upper")
        public String value;
    }

    @Test
    public void test1() {
        String value = "abc";
        String valueUpper = value.toUpperCase();

        JSONObject jsonObject = JSONObject.of("value", value);
        String json = jsonObject.toJSONString();
        assertEquals(
                valueUpper,
                jsonObject.toJavaObject(Bean1.class).value);
        assertEquals(
                valueUpper,
                JSON.parseObject(json, Bean1.class).value);
        assertEquals(
                valueUpper,
                ObjectReaderCreator.INSTANCE
                        .createObjectReader(Bean1.class)
                        .readObject(json).value);
    }

    public static class Bean1 {
        @JSONField(format = "upper")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
