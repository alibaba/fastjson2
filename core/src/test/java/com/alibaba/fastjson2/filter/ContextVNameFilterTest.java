package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.BeanUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextVNameFilterTest {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.id = 10;

        AtomicReference<BeanContext> contextReference = new AtomicReference<>();

        ContextNameFilter filter = (BeanContext context, Object object, String name, Object value) -> {
            contextReference.set(context);
            return BeanUtils.fieldName(name, PropertyNamingStrategy.KebabCase.name());
        };

        assertEquals("{\"user-id\":10}", JSON.toJSONString(bean, filter));

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
