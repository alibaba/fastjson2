package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WideSetterReturnTest {
    @Test
    public void longReturn() {
        assertEquals(123, JSON.parseObject("{\"value\":123}", LongSetter.class).getValue());
        assertEquals(123, JSONB.parseObject(JSONB.toBytes(JSONObject.of("value", 123)), LongSetter.class).getValue());
    }

    @Test
    public void doubleReturn() {
        assertEquals(123, JSON.parseObject("{\"value\":123}", DoubleSetter.class).getValue());
        assertEquals(123, JSONB.parseObject(JSONB.toBytes(JSONObject.of("value", 123)), DoubleSetter.class).getValue());
    }

    public static class LongSetter {
        private int value;

        public int getValue() {
            return value;
        }

        public long setValue(int value) {
            this.value = value;
            return value;
        }
    }

    public static class DoubleSetter {
        private int value;

        public int getValue() {
            return value;
        }

        public double setValue(int value) {
            this.value = value;
            return value;
        }
    }
}
