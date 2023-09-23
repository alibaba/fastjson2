package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriter.Feature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Issue1894 {
    @Test
    public void test() {
        JSONWriter.Context context = new JSONWriter.Context(Feature.WriteNullNumberAsZero, Feature.WriteLongAsString);

        Demo demo = new Demo();
        String json = JSON.toJSONString(demo, context);
        assertEquals("{\"a\":\"0\",\"b\":\"0\",\"c\":\"1\"}", json);
    }

    public static class Demo {
        public Long a;
        public Long b;
        public Long c = 1L;
    }
}
