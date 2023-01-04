package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.FieldInfo;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ContextValueFilterTest {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.id = 10;

        AtomicReference<BeanContext> contextReference = new AtomicReference<>();

        ContextValueFilter filter = (BeanContext context, Object object, String name, Object value) -> {
            contextReference.set(context);
            return ((Integer) value).intValue() + 1;
        };

        assertEquals("{\"userId\":11}", JSON.toJSONString(bean, filter));

        BeanContext context = contextReference.get();
        assertEquals(Bean.class, context.getBeanClass());
        assertEquals(int.class, context.getFieldClass());
        assertEquals(int.class, context.getFieldType());
        assertEquals(Bean.class.getField("id"), context.getField());
        assertEquals(null, context.getMethod());
        assertEquals("userId", context.getAnnotation(JSONField.class).name());
        assertEquals(null, context.getFormat());
        assertEquals(null, context.getLabel());
        assertEquals(0, context.getFeatures());
        assertEquals("userId", context.getName());
        assertFalse(context.isJsonDirect());
    }

    public static class Bean {
        @JSONField(name = "userId")
        public int id;
    }

    @Test
    public void test1() throws Exception {
        Bean1 bean = new Bean1();
        bean.id = 10;

        AtomicReference<BeanContext> contextReference = new AtomicReference<>();

        ContextValueFilter filter = (BeanContext context, Object object, String name, Object value) -> {
            contextReference.set(context);
            return ((Integer) value).intValue() + 1;
        };

        assertEquals("{\"userId\":11}", JSON.toJSONString(bean, filter));

        BeanContext context = contextReference.get();
        assertEquals(Bean1.class, context.getBeanClass());
        assertEquals(int.class, context.getFieldClass());
        assertEquals(int.class, context.getFieldType());
        assertEquals(Bean1.class.getMethod("getId"), context.getMethod());
        assertEquals(null, context.getField());
        assertEquals("userId", context.getAnnotation(JSONField.class).name());
        assertEquals(null, context.getFormat());
        assertEquals(null, context.getLabel());
        assertEquals(FieldInfo.FIELD_MASK, context.getFeatures());
        assertEquals("userId", context.getName());
        assertFalse(context.isJsonDirect());
    }

    public static class Bean1 {
        private int id;

        @JSONField(name = "userId")
        public int getId() {
            return id;
        }
    }
}
