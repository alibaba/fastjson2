package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueFilterTest3 {
    @Test
    public void valueFilter() {
        Bean bean = new Bean();
        bean.id = 100;
        bean.gender = 1;

        JSON.register(Bean.class, ValueFilter.of(
                "gender",
                (value) -> {
                    switch (((Integer) value).intValue()) {
                        case 1:
                            return "男";
                        case 2:
                            return "女";
                        default:
                            return value;
                    }
                })
        );
        String str = JSON.toJSONString(bean);
        assertEquals("{\"gender\":\"男\",\"id\":100}", str);
    }

    public static class Bean {
        public int id;
        public int gender;
    }
}
