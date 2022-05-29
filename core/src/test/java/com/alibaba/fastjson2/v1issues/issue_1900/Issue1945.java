package com.alibaba.fastjson2.v1issues.issue_1900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteClassName;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1945 {
    @Test
    public void test_0() throws Exception {
        B b = new B();
        b.clazz = new Class[]{String.class};
        b.aInstance = new HashMap();
        b.aInstance.put("test", "test");
        String s = JSON.toJSONString(b, WriteClassName);
        System.out.println(s);
        B a1 = JSON.parseObject(s, B.class, JSONReader.Feature.SupportClassForName);
        assertNotNull(a1);
    }

    static class B
            implements Serializable {
        public Class[] clazz;
        public Map aInstance;
    }
}
