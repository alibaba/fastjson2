package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonValueTest {
    @Test
    public void test() {
        assertEquals("\"ok\"", JSON.toJSONString(Type.OK));
    }

    public enum Type {
        OK("ok"),
        LOW("low"),
        HIGH("high");

        private final String level;

        Type(String level) {
            this.level = level;
        }

        @JsonValue
        @Override
        public String toString() {
            return level;
        }
    }
}
