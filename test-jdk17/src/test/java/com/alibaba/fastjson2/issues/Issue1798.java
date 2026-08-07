package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class Issue1798 {
    @Test
    public void test() {
        record A(String id, Date data) {
        }

        A a = new A("a", new Date());
        String s = JSON.toJSONString(a, JSONWriter.Feature.FieldBased, JSONWriter.Feature.WriteClassName);
        a = JSON.parseObject(s, A.class, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportAutoType);
    }
}
