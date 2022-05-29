package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBTableTest7 {
    @Test
    public void test_0() {
        List list = new ArrayList<>();
        list.add(new com.alibaba.fastjson.JSONObject().fluentPut("id", 101).fluentPut("name", "DataWorks"));
        list.add(new com.alibaba.fastjson.JSONObject().fluentPut("id", 102));

        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);
        System.out.println(JSON.toJSONString(JSONB.parse(bytes, JSONReader.Feature.SupportAutoType)));

        List list2 = (List) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertEquals(list2.size(), list.size());
    }
}
