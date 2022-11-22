package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue945 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.id = 10;
        bean.id2 = 102;

        assertEquals("{\"id\":10}", JSON.toJSONString(bean));
    }

    public static class Bean {
        public int id;
        public transient int id2;
    }
}
