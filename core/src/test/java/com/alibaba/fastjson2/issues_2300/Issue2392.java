package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2392 {
    @Test
    public void emptyList() throws Exception {
        Class<?> clazz = Class.forName("kotlin.collections.EmptyList");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object object = constructor.newInstance();
        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName);
        Object parsed = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed.getClass());
    }

    @Test
    public void emptyList1() throws Exception {
        Class<?> clazz = Class.forName("kotlin.collections.EmptyList");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Bean bean = new Bean();
        bean.value = (List<String>) constructor.newInstance();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        System.out.println(JSONB.toJSONString(bytes));
        Bean parsed = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed.value.getClass());

        Bean parsed1 = JSONB.parseObject(bytes, Bean.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed1.value.getClass());
    }

    @Test
    public void emptyList2() throws Exception {
        Class<?> clazz = Class.forName("kotlin.collections.EmptyList");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Bean2 bean = new Bean2();
        bean.value = (Collection<String>) constructor.newInstance();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean2 parsed = JSONB.parseObject(bytes, Bean2.class, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed.value.getClass());

        Bean2 parsed1 = JSONB.parseObject(bytes, Bean2.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed1.value.getClass());
    }

    @Test
    public void emptySet() throws Exception {
        Class<?> clazz = Class.forName("kotlin.collections.EmptySet");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object object = constructor.newInstance();
        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName);
        Object parsed = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed.getClass());
    }

    @Test
    public void emptySet1() throws Exception {
        Class<?> clazz = Class.forName("kotlin.collections.EmptySet");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Bean1 bean = new Bean1();
        bean.value = (Set<String>) constructor.newInstance();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean1 parsed = JSONB.parseObject(bytes, Bean1.class, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed.value.getClass());

        Bean1 parsed1 = JSONB.parseObject(bytes, Bean1.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed1.value.getClass());
    }

    @Test
    public void emptySet2() throws Exception {
        Class<?> clazz = Class.forName("kotlin.collections.EmptySet");
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Bean2 bean = new Bean2();
        bean.value = (Collection<String>) constructor.newInstance();
        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);
        Bean2 parsed = JSONB.parseObject(bytes, Bean2.class, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed.value.getClass());

        Bean2 parsed1 = JSONB.parseObject(bytes, Bean2.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
        assertEquals(clazz, parsed1.value.getClass());
    }

    public static class Bean {
        public List<String> value;
    }

    public static class Bean1 {
        public Set<String> value;
    }

    public static class Bean2 {
        public Collection<String> value;
    }
}
