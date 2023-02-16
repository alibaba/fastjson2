package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NameFilterTest {
    @Test
    public void test() throws Exception {
        NameFilter filter0 = (Object object, String name, Object value) -> name + "_1";
        NameFilter filter1 = (Object object, String name, Object value) -> name + "_2";

        Bean bean = new Bean();
        bean.id = 1001;
        String str = JSON.toJSONString(bean, new Filter[]{filter0, filter1});
        assertEquals("{\"id_1_2\":1001}", str);
    }

    public static class Bean {
        public int id;
    }
}
