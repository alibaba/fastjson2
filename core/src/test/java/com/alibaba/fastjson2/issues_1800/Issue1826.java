package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1826 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.values = new ArrayList<>();
        bean.values.add("a");
        bean.values.add(null);
        bean.values.add("b");

        byte[] bytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(bytes, Bean.class);
        assertEquals(bean.values, bean1.values);
    }

    public static class Bean {
        public List<String> values;
    }
}
