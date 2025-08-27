package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3755 {
    @com.alibaba.fastjson2.annotation.JSONType(alphabetic = false)
    public record MyRecordWithAlphabeticFalse(String z, String a) {
    }

    public record MyRecordWithoutAnnotation(String z, String a) {
    }

    @com.alibaba.fastjson2.annotation.JSONType(orders = {"a", "z"})
    public record MyRecordWithOrders(String z, String a) {
    }

    @Test
    public void testRecordFieldOrder() {
        MyRecordWithAlphabeticFalse record1 = new MyRecordWithAlphabeticFalse("z的值", "a的值");
        String json1 = JSON.toJSONString(record1);
        assertEquals("{\"z\":\"z的值\",\"a\":\"a的值\"}", json1);

        MyRecordWithoutAnnotation record2 = new MyRecordWithoutAnnotation("z的值", "a的值");
        String json2 = JSON.toJSONString(record2);
        assertEquals("{\"a\":\"a的值\",\"z\":\"z的值\"}", json2);

        MyRecordWithOrders record3 = new MyRecordWithOrders("z的值", "a的值");
        String json3 = JSON.toJSONString(record3);
        assertEquals("{\"a\":\"a的值\",\"z\":\"z的值\"}", json3);


    }
}
