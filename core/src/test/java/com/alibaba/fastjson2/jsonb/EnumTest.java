package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnumTest {
    @Test
    public void test() {
        TimeUnit value = TimeUnit.DAYS;

        byte[] bytes = JSONB.toBytes(value, JSONWriter.Feature.WriteClassName);

        assertEquals(value, JSONB.parse(bytes, JSONReader.Feature.SupportAutoType));
        assertEquals(value, JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType));
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.value = PropertyNamingStrategy.KebabCase;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

        assertEquals(bean.value, ((Bean) JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)).value);
        assertEquals(bean.value, ((Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType)).value);
    }

    public static class Bean {
        public Enum value;
    }

    @Test
    public void test2() {
        Bean2 bean = new Bean2();
        bean.value = PropertyNamingStrategy.KebabCase;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName);

        assertEquals(bean.value, ((Bean2) JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)).value);
        assertEquals(bean.value, ((Bean2) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType)).value);
    }

    public static class Bean2 {
        public Object value;
    }

    @Test
    public void test3() {
        Bean2 bean = new Bean2();
        bean.value = PropertyNamingStrategy.KebabCase;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);

        assertEquals(bean.value, JSONB.parseObject(bytes, Bean3.class, JSONReader.Feature.SupportAutoType).value);
    }

    public static class Bean3 {
        public PropertyNamingStrategy value;
    }

    @Test
    public void test4() {
        Bean2 bean = new Bean2();
        bean.value = Type.Big;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.NotWriteRootClassName);

        assertEquals(bean.value, JSONB.parseObject(bytes, Bean4.class, JSONReader.Feature.SupportAutoType).value);
        assertThrows(
                JSONException.class,
                () -> JSONB.parseObject(bytes, Bean3.class, JSONReader.Feature.SupportAutoType)
        );
    }

    public static class Bean4 {
        public Type value;
    }

    public enum Type {
        Big, Small
    }
}
