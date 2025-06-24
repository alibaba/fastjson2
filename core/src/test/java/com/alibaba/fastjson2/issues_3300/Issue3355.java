package com.alibaba.fastjson2.issues_3300;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3355 {
    @Test
    public void test() {
        com.alibaba.fastjson.JSONObject jsonObject1 = new com.alibaba.fastjson.JSONObject();
        Bean bean1 = jsonObject1.toJavaObject(Bean.class);

        com.alibaba.fastjson2.JSONObject jsonObject = new com.alibaba.fastjson2.JSONObject();
        Bean bean2 = jsonObject.toJavaObject(Bean.class);

        assertEquals(bean2.value, bean1.value);
    }

    public static class Bean {
        public Integer value = 2;
    }
}
