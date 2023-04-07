package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1331 {
    @Test
    public void test() {
        List list = new ArrayList();
        list.add(101);
        list.add(102L);

        String str = JSON.toJSONString(list, JSONWriter.Feature.WriteNonStringValueAsString);
        assertEquals("[\"101\",\"102\"]", str);

        Bean bean = new Bean();
        bean.values = list;

        assertEquals(
                "{\"values\":[\"101\",\"102\"]}",
                JSON.toJSONString(bean, JSONWriter.Feature.WriteNonStringValueAsString)
        );
    }

    public static class Bean {
        public List<Integer> values;
    }
}
