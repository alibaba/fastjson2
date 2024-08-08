package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2851 {
    @Test
    public void test() {
        List<String> list = Arrays.asList("").subList(0, 1);
        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.WriteClassName);
        JSONReader.AutoTypeBeforeHandler autoTypeFilter = JSONReader.autoTypeFilter("java.util.AbstractList");
        JSONReader.Context context = JSONFactory.createReadContext();
        context.config(autoTypeFilter);
        List<String> parsed = (List<String>) JSONB.parse(bytes, context);
        assertEquals(list.get(0), parsed.get(0));
    }
}
