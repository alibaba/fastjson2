package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteClassNameWithFilterTest {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 101;
        ValueFilter filter = (Object object, String name, Object value) -> value;

        String str = JSON.toJSONString(bean, filter, JSONWriter.Feature.WriteClassName);
        assertEquals("{\"@type\":\"com.alibaba.fastjson2.features.WriteClassNameWithFilterTest$Bean\",\"id\":101}", str);
    }

    public static class Bean {
        public Integer id;
    }
}
