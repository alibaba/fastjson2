package com.alibaba.fastjson.atomic;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicIntegerReadOnlyTest {
    @Test
    public void test_codec_null() {
        V0 v = new V0(123);

        String text = JSON.toJSONString(v);
        assertEquals("{\"value\":123}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue().intValue(), v.getValue().intValue());
    }

    public static class V0 {
        private final AtomicInteger value;

        public V0() {
            this(0);
        }

        public V0(int value) {
            this.value = new AtomicInteger(value);
        }

        public AtomicInteger getValue() {
            return value;
        }
    }
}
