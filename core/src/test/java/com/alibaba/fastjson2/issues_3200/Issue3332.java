package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2_vo.Int1;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3332 {
    @Test
    public void test() {
        int value = 123;
        JSONArray array = JSONArray.of(
                new Int1(value)
        );
        byte[] bytes = JSONB.toBytes(array);
        try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
            Collection<Int1> list = new ArrayList<>(10);
            reader.readArray(list, Int1.class);
            assertEquals(1, list.size());
            assertEquals(value, list.iterator().next().getV0000());
        }
        try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
            List<Int1> list = new ArrayList<>(10);
            reader.readArray(list, Int1.class);
            assertEquals(1, list.size());
            assertEquals(value, list.get(0).getV0000());
        }
    }
}
