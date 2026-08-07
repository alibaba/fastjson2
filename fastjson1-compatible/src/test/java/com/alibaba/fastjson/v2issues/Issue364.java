package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Issue364 {
    @Test
    public void test() {
        TestBean bean = new TestBean();
        Assertions.assertEquals("{\"msg\":\"\"}", JSON.toJSONString(bean, SerializerFeature.WriteNullStringAsEmpty));
    }

    @Data
    public class TestBean {
        private String msg;
        private TestBean bean;
    }
}
