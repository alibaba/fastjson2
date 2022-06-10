package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueFilterTest {
    @Test
    public void test_valuefilterCompose() {
        ValueFilter filter0 = (source, name, value) -> {
            if (name.equals("id")) {
                return ((Integer) value).intValue() + 1;
            }
            return value;
        };

        ValueFilter filter1 = (source, name, value) -> {
            if (name.equals("id")) {
                return ((Integer) value).intValue() + 10;
            }
            return value;
        };

        Bean bean = new Bean();
        bean.id = 100;
        String str = JSON.toJSONString(bean, ValueFilter.compose(filter0, filter1));
        assertEquals("{\"id\":111}", str);
    }

    public static class Bean {
        public int id;
    }
}
