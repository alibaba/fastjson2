package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3755 {
    @Test
    public void test() {
        MyRecord obj = new MyRecord("zValue", "aValue");
        assertEquals("{\"z\":\"zValue\",\"a\":\"aValue\"}", JSON.toJSONString(obj));

        MyRecord2 obj2 = new MyRecord2("zValue", "aValue");
        assertEquals("{\"z\":\"zValue\",\"a\":\"aValue\"}", JSON.toJSONString(obj2));
    }

    @JSONType(alphabetic = false)
    record MyRecord(String z, String a) {}

    @JSONType(alphabetic = false)
    public record MyRecord2(String z, String a) {}
}
