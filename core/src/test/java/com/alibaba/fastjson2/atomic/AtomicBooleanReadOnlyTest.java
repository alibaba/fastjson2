package com.alibaba.fastjson2.atomic;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicBooleanReadOnlyTest {
    @Test
    public void test_readOnly_method() {
        V0 v = new V0(true);

        String text = JSON.toJSONString(v);
        assertEquals("{\"value\":true}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue().get(), v.getValue().get());
    }

    @Test
    public void test_readOnly_field() {
        V1 v = new V1();
        v.value.set(true);

        String text = JSON.toJSONString(v);
        assertEquals("{\"value\":true}", text);

        V1 v1 = JSON.parseObject(text, V1.class);

        assertEquals(v1.value.get(), v.value.get());
    }

    public static class V0 {
        private final AtomicBoolean value;

        public V0() {
            this(false);
        }

        public V0(boolean value) {
            this.value = new AtomicBoolean(value);
        }

        public AtomicBoolean getValue() {
            return value;
        }
    }

    public static class V1 {
        public final AtomicBoolean value = new AtomicBoolean();
    }
}
