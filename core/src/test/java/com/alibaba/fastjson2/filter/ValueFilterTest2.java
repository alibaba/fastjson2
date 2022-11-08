package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueFilterTest2 {
    @Test
    public void valueFilter() {
        Bean bean = new Bean();
        bean.id = 100;
        bean.gender = 1;

        ValueFilter filter = (Object object, String name, Object value) -> {
            if ("gender".equals(name) && value instanceof Integer) {
                switch (((Integer) value).intValue()) {
                    case 1:
                        return "男";
                    case 2:
                        return "女";
                    default:
                        break;
                }
            }
            return value;
        };

        JSON.register(Bean.class, filter);
        String str = JSON.toJSONString(bean);
        assertEquals("{\"gender\":\"男\",\"id\":100}", str);
    }

    public static class Bean {
        public int id;
        public int gender;
    }
}
