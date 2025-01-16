package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3163 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"id\":1234}", Bean.class);
        assertEquals(0, bean.id);
    }

    public static class Bean {
        @JSONField(deserialize = false)
        public int id;
    }
}
