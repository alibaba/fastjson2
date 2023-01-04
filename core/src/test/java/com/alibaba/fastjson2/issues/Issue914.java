package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue914 {
    @Test
    public void test() {
        JSONObject object = JSONObject.of("type", "java.lang.String");
        Bean bean = object.to(Bean.class, JSONReader.Feature.SupportClassForName);
        assertEquals(String.class, bean.type);
    }

    public static class Bean {
        public Class type;
    }
}
