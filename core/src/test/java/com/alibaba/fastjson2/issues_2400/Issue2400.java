package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.BeanContext;
import com.alibaba.fastjson2.filter.ContextValueFilter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2400 {
    @Test
    public void test() {
        final AtomicReference<String> formatRef = new AtomicReference<>();
        ContextValueFilter contextValueFilter = new ContextValueFilter() {
            @Override
            public Object process(BeanContext context, Object object, String name, Object value) {
                formatRef.set(context.getFormat());
                return value;
            }
        };

        Bean bean = new Bean();
        bean.id = LocalDate.of(2012, 3, 4);
        JSON.toJSONString(bean, contextValueFilter);
        assertEquals("yyyyMMdd", formatRef.get());
    }

    public static class Bean {
        @JSONField(format = "yyyyMMdd")
        public LocalDate id;
    }
}
