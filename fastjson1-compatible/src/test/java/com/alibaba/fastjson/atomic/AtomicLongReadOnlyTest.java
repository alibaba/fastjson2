package com.alibaba.fastjson.atomic;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicLongReadOnlyTest {
    @Test
    public void test_codec_null() {
        V0 v = new V0(123);

        String text = JSON.toJSONString(v);
        assertEquals("{\"value\":123}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue().intValue(), v.getValue().intValue());
    }

    public static class V0 {
        private final AtomicLong value;

        public V0() {
            this(0);
        }

        public V0(int value) {
            this.value = new AtomicLong(value);
        }

        public AtomicLong getValue() {
            return value;
        }
    }
}
