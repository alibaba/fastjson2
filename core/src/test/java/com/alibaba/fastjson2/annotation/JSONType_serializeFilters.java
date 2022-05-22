package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONType_serializeFilters {
    @Test
    public void test_for_jsonField() {
        Model m = new Model();
        String json = JSON.toJSONString(m);
        assertEquals("{\"id\":0}", json);
    }

    public static class MyValueFilter
            implements ValueFilter {
        @Override
        public Object apply(Object object, String name, Object value) {
            if (name.equals("id") && value == null) {
                return 123;
            }

            return null;
        }
    }

    @JSONType(serializeFilters = MyValueFilter.class)
    public static class Model {
        public int id;
    }
}
