package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionConversionReviewTest {
    @Test
    public void parameterizedArrayElement() {
        Box<Integer> original = new Box<>();
        original.value = 42;
        JSONArray array = JSONArray.of(original);
        Box<String> converted = array.getObject(0, new TypeReference<Box<String>>() { }.getType());
        assertEquals("42", converted.value);
        assertNotSame(original, converted);
        assertSame(original, array.getObject(0, (java.lang.reflect.Type) Box.class));
    }

    @Test
    public void duplicateInitialNullKey() {
        assertThrows(JSONException.class, () -> JSONObject.of(
                "a", null, "b", 2, "c", 3, "d", 4, "e", 5, "a", 6));
        assertThrows(JSONException.class, () -> JSONObject.of(
                "a", 1, "b", 2, "c", 3, "d", 4, "e", 5, "f", null, "f", 6));
        JSONObject object = JSONObject.of("a", null, "b", 2, "c", 3, "d", 4, "e", 5, "f", 6);
        assertTrue(object.containsKey("a"));
        assertEquals(6, object.size());
    }

    public static class Box<T> {
        public T value;
    }
}
