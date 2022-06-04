package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue364 {
    @Test
    public void test() {
        TestBean bean = new TestBean();
        assertEquals("{\"msg\":\"\"}", JSON.toJSONString(bean, JSONWriter.Feature.NullAsDefaultValue));
    }

    @Data
    public class TestBean {
        private String msg;
        private TestBean bean;
    }
}
