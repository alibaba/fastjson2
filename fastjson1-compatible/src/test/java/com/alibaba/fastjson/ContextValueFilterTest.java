package com.alibaba.fastjson;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextValueFilterTest {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.id = 10;

        AtomicReference<BeanContext> contextReference = new AtomicReference<>();

        ContextValueFilter filter = (BeanContext context, Object object, String name, Object value) -> {
            contextReference.set(context);
            return value;
        };

        JSON.toJSONString(bean, filter);

        BeanContext context = contextReference.get();
        assertEquals(Bean.class, context.getBeanClass());
        assertEquals(int.class, context.getFieldClass());
        assertEquals(int.class, context.getFieldType());
        assertEquals(Bean.class.getField("id"), context.getField());
        assertEquals("userId", context.getAnnotation(JSONField.class).name());
        assertEquals(null, context.getFormat());
        assertEquals(null, context.getLabel());
        assertEquals(0, context.getFeatures());
    }

    public static class Bean {
        @JSONField(name = "userId")
        public int id;
    }
}
