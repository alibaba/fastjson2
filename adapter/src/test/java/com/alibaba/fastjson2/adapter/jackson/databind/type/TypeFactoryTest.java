package com.alibaba.fastjson2.adapter.jackson.databind.type;

import com.alibaba.fastjson2.adapter.jackson.core.type.TypeReference;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TypeFactoryTest {
    @Test
    public void test() {
        JavaType javaType = TypeFactory
                .defaultInstance()
                .constructType(new TypeReference<SerializedValue<Object>>() {
                });
        assertNotNull(javaType);
    }

    @Test
    public void test1() {
        CollectionType collectionType = TypeFactory
                .defaultInstance()
                .constructCollectionType(ArrayList.class, Bean.class);

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Bean> values = mapper.readValue("[{\"id\":123,\"name\":\"XX\"}]", collectionType);
        assertEquals(1, values.size());
        assertEquals(123, values.get(0).id);
        assertEquals("XX", values.get(0).name);
    }

    @Test
    public void test2() {
        TypeFactory typeFactory = TypeFactory
                .defaultInstance();
        JavaType collectionType = typeFactory
                .constructParametricType(ArrayList.class, Bean.class);

        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Bean> values = mapper.readValue("[{\"id\":123,\"name\":\"XX\"}]", collectionType);
        assertEquals(1, values.size());
        assertEquals(123, values.get(0).id);
        assertEquals("XX", values.get(0).name);
        typeFactory.clearCache();
        typeFactory.withClassLoader(null);
    }

    @Test
    public void test3() {
        JavaType collectionType = TypeFactory
                .defaultInstance()
                .constructMapType(ConcurrentHashMap.class, Key.class, Bean.class);

        ObjectMapper mapper = new ObjectMapper();
        ConcurrentHashMap<Key, Bean> map = mapper.readValue("{\"value\":{\"id\":123,\"name\":\"XXX\"}}", collectionType);
        assertEquals(1, map.size());
        Bean bean = map.get(new Key("value"));
        assertEquals(123, bean.id);
        assertEquals("XXX", bean.name);
    }

    public static class Key {
        private final String value;

        @JSONCreator
        public Key(@JSONField(value = true) String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(value, key.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static class Bean {
        public int id;
        public String name;
    }

    public static class SerializedValue<T> {
        private final T value;

        public SerializedValue(T value) throws IOException {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }
}
