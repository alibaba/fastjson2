package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.BeanContext;
import com.alibaba.fastjson2.filter.ContextValueFilter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1090 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 1001;

        AtomicReference<Field> fieldRef = new AtomicReference<>();

        ContextValueFilter filter = new ContextValueFilter() {
            @Override
            public Object process(BeanContext context, Object object, String name, Object value) {
                fieldRef.set(context.getField());
                return value;
            }
        };

        JSON.toJSONString(bean, filter);

        assertNotNull(fieldRef.get());
    }

    public static class Bean {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
