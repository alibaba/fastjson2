package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValueFilterTest5 {
    @Test
    public void valueFilter() {
        Bean bean = new Bean();
        bean.id = 100;
        bean.gender = 1;

        Map map = new HashMap();
        map.put(1, "男");
        map.put(2, "女");

        JSON.register(Bean.class, ValueFilter.of("gender", map));
        String str = JSON.toJSONString(bean);
        assertEquals("{\"gender\":\"男\",\"id\":100}", str);

        JSON.register(
                Bean.class,
                NameFilter.of(
                        e -> "id".equals(e) ? "xid" : e
                )
        );
        assertEquals("{\"gender\":\"男\",\"xid\":100}", JSON.toJSONString(bean));
    }

    public static class Bean {
        public int id;
        public int gender;
    }
}
