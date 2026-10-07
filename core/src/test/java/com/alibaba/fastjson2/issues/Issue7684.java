package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TypeReference with a custom List implementation (not derived from ArrayList)
 * lost its generic element type — elements were decoded as JSONObject.
 */
@Tag("regression")
public class Issue7684 {
    public static class CustomList<E> extends AbstractList<E> {
        private final List<E> list = new ArrayList<>();

        @Override
        public E get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public void add(int index, E element) {
            list.add(index, element);
        }

        @Override
        public E set(int index, E element) {
            return list.set(index, element);
        }

        @Override
        public E remove(int index) {
            return list.remove(index);
        }
    }

    public static class Bean {
        public int id;
    }

    /**
     * A non-{@code ArrayList} {@code List} subclass that declares a custom deserializer.
     * Used to verify the deserializer is honored (not bypassed by the list fast-path)
     * when the type is used as a parameterized {@link TypeReference} argument.
     */
    @JSONType(deserializer = DeserializerListReader.class)
    public static class DeserializerList<E> extends AbstractList<E> {
        private final List<E> list = new ArrayList<>();

        @Override
        public E get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public void add(int index, E element) {
            list.add(index, element);
        }
    }

    public static class DeserializerListReader implements ObjectReader {
        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            jsonReader.readAny(); // consume the JSON payload
            // Return a recognizable sentinel so the test can prove this reader ran
            // (rather than the generic list reader that would bypass it).
            DeserializerList list = new DeserializerList();
            list.add("INVOKED");
            return list;
        }
    }

    @Test
    public void customListGenericPreserved() {
        String json = "[{\"id\":1},{\"id\":2}]";
        CustomList<Bean> result = JSON.parseObject(json, new TypeReference<CustomList<Bean>>() {});

        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof Bean, "element type must be Bean, was: " + result.get(0).getClass());
        assertEquals(1, result.get(0).id);
        assertEquals(2, result.get(1).id);
    }

    @Test
    public void customDeserializerOnListSubclassHonored() {
        // Regression: @JSONType(deserializer=...) on a non-ArrayList List subclass used as a
        // TypeReference argument must be honored, not silently bypassed by the list fast-path.
        DeserializerList<String> result = JSON.parseObject(
                "[\"a\",\"b\"]", new TypeReference<DeserializerList<String>>() {});

        assertEquals(1, result.size());
        assertEquals("INVOKED", result.get(0));
    }
}
