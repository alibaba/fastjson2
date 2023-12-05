package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2072 {
    @Test
    public void test() {
        Bean2 test21 = new Bean2();
        test21.value = "test21";

        Bean2 test22 = new Bean2();
        test22.value = "test22";

        Bean1 test1 = new Bean1();
        test1.test11 = test21;
        test1.test12 = new HashMap<>(2);
        test1.test12.put("test21", test21);
        test1.test13 = new HashMap<>(2);
        test1.test13.put("test22", test22);

        String fastJson1Str = com.alibaba.fastjson.JSON.toJSONString(test1);
        Bean1 fastJson1Parse = com.alibaba.fastjson.JSON.parseObject(fastJson1Str, Bean1.class);
        String fastjson2Str = com.alibaba.fastjson2.JSON.toJSONString(fastJson1Parse, JSONWriter.Feature.ReferenceDetection);
        assertEquals(fastJson1Str, fastjson2Str);

        Bean1 fastJson2Parse = com.alibaba.fastjson2.JSON.parseObject(fastJson1Str, Bean1.class);
        String fastjson2Str2 = com.alibaba.fastjson2.JSON.toJSONString(fastJson2Parse, JSONWriter.Feature.ReferenceDetection);
        assertEquals(fastJson1Str, fastjson2Str2);
    }

    @Data
    private static class Bean1 {
        private Bean2 test11;
        private Map<String, Bean2> test12;
        private Map<String, Bean2> test13;
    }

    @Data
    private static class Bean2 {
        private String value;
    }
}
