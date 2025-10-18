package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3828 {
    @Test
    public void test() {
        Data data = new Data();
        data.list = Collections.emptyList();

        byte[] bytes = JSONB.toBytes(data, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased);

        System.out.println(JSONB.toJSONString(bytes));
        Data data1 = JSONB.parseObject(bytes, Data.class, JSONReader.Feature.FieldBased);
        assertEquals(0, data1.list.size());
    }

    public static class Data {
        private Collection<String> list;
    }
}
