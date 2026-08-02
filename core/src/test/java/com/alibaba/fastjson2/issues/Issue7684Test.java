package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
public class Issue7684Test {
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

    @Test
    public void customListGenericPreserved() {
        String json = "[{\"id\":1},{\"id\":2}]";
        CustomList<Bean> result = JSON.parseObject(json, new TypeReference<CustomList<Bean>>() {});

        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof Bean, "element type must be Bean, was: " + result.get(0).getClass());
        assertEquals(1, result.get(0).id);
        assertEquals(2, result.get(1).id);
    }
}
