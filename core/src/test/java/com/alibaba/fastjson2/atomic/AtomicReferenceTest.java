package com.alibaba.fastjson2.atomic;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicReferenceTest {
    @Test
    public void test_method() {
        V0 v = new V0(new Value(123));

        String text = JSON.toJSONString(v);
        assertEquals("{\"value\":{\"id\":123}}", text);

        V0 v1 = JSON.parseObject(text, V0.class);

        assertEquals(v1.getValue().get().id, v.getValue().get().id);
    }

    @Test
    public void test_method_jsonb() {
        V0 v = new V0(new Value(123));

        byte[] jsonbBytes = JSONB.toBytes(v);

        V0 v1 = JSONB.parseObject(jsonbBytes, V0.class);

        assertEquals(v1.getValue().get().id, v.getValue().get().id);
    }

    @Test
    public void test_field() {
        V1 v = new V1();
        v.value = new AtomicReference<>();
        v.value.set(new Value(123));

        String text = JSON.toJSONString(v);
        assertEquals("{\"value\":{\"id\":123}}", text);

        V1 v1 = JSON.parseObject(text, V1.class);

        assertEquals(v1.value.get().id, v.value.get().id);
    }

    @Test
    public void test_field_jsonb() {
        V1 v = new V1();
        v.value = new AtomicReference<>();
        v.value.set(new Value(123));

        byte[] jsonbBytes = JSONB.toBytes(v);

        V1 v1 = JSONB.parseObject(jsonbBytes, V1.class);

        assertEquals(v1.value.get().id, v.value.get().id);
    }

    public static class V0 {
        private AtomicReference<Value> value;

        public V0() {
        }

        public V0(Value value) {
            this.value = new AtomicReference(value);
        }

        public void setValue(AtomicReference<Value> value) {
            this.value = value;
        }

        public AtomicReference<Value> getValue() {
            return value;
        }
    }

    public static class V1 {
        public AtomicReference<Value> value;
    }

    public static class Value {
        public int id;

        public Value() {
        }

        public Value(int id) {
            this.id = id;
        }
    }
}
