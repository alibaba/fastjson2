package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("features")
public class AnySetterTest {
    public static class Bag {
        public int known;
        final Map<String, Object> extras = new LinkedHashMap<>();

        @JSONField(unwrapped = true)
        public void collect(String name, Object value) {
            extras.put(name, value);
        }
    }

    @Test
    public void collectUnknownFields() {
        Bag bag = JSON.parseObject("{\"known\":1,\"e1\":\"x\",\"e2\":true,\"e3\":{\"a\":1},\"e4\":[1,2]}",
                Bag.class);
        assertEquals(1, bag.known);
        assertEquals(4, bag.extras.size());
        assertEquals("x", bag.extras.get("e1"));
        assertEquals(true, bag.extras.get("e2"));
        assertEquals(1, ((Map) bag.extras.get("e3")).get("a"));
        assertEquals(2, ((java.util.List) bag.extras.get("e4")).size());
    }

    @Test
    public void withoutUnknownFields() {
        Bag bag = JSON.parseObject("{\"known\":7}", Bag.class);
        assertEquals(7, bag.known);
        assertTrue(bag.extras.isEmpty());
    }

    public static class StrictBag {
        public int known;

        @JSONField(unwrapped = true)
        public void reject(String name, Object value) {
            throw new IllegalArgumentException("Unknown field: " + name);
        }
    }

    @Test
    public void rejectUnknownFieldsViaAnySetter() {
        // the jackson @JsonAnySetter-as-unknown-field-rejection pattern, expressed natively;
        // method exceptions surface wrapped in JSONException
        JSONException e = assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"known\":1,\"bogus\":2}", StrictBag.class));
        assertTrue(e.getCause() instanceof IllegalArgumentException
                || e.getMessage().contains("any set error"));
    }

    @Test
    public void rejectUnknownFieldsViaFeature() {
        // the idiomatic fastjson2 way, no any-setter method required
        assertThrows(JSONException.class,
                () -> JSON.parseObject("{\"known\":1,\"bogus\":2}", NoSetter.class,
                        JSONReader.Feature.ErrorOnUnknownProperties));
        NoSetter bean = JSON.parseObject("{\"known\":1}", NoSetter.class,
                JSONReader.Feature.ErrorOnUnknownProperties);
        assertEquals(1, bean.known);
    }

    public static class NoSetter {
        public int known;
    }

    @Test
    public void typedValue() {
        TypedBag bag = JSON.parseObject("{\"known\":1,\"n\":42}", TypedBag.class);
        assertEquals(Integer.valueOf(42), bag.value);
        assertEquals("n", bag.name);
    }

    public static class TypedBag {
        public int known;
        String name;
        Object value;

        @JSONField(unwrapped = true)
        public void collect(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

    @Test
    public void unknownFieldsIgnoredByDefault() {
        NoSetter bean = JSON.parseObject("{\"known\":1,\"bogus\":2}", NoSetter.class);
        assertEquals(1, bean.known);
    }

    @Test
    public void jsonNullCollection() {
        Bag bag = JSON.parseObject("{\"known\":1,\"e1\":null}", Bag.class);
        assertEquals(1, bag.extras.size());
        assertNull(bag.extras.get("e1"));
    }
}
