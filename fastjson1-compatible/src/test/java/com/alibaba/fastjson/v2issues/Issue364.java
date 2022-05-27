package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue364 {
    @Test
    public void test() {
        TestBean bean = new TestBean();
        assertEquals("{\"bean\":null,\"msg\":\"\"}", JSON.toJSONString(bean, SerializerFeature.WriteNullStringAsEmpty));
    }

    @Data
    public class TestBean {
        private String msg;
        private TestBean bean;
    }
}
