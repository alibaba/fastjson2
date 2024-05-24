package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordTest {
    @Test
    public void test() {
        Item item = new Item(123);
        String str = JSON.toJSONString(item);
        String str1 = JSON.toJSONString(item, JSONWriter.Feature.FieldBased);
        assertEquals(str, str1);

        JSONObject object = JSON.parseObject(str);
        assertEquals(item.value, object.get("value"));
    }

    record Item(int value) { }
}
