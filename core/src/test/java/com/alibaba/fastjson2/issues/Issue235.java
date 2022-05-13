package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.junit.jupiter.api.Test;

import java.util.List;

public class Issue235 {
    @Test
    public void test() {
        String json = "[{\r\n"
                + "  \"namespace\":\"unit07\", \r\n"
                + "  \"items\":[\"COUNTER13_14.AV\",\r\n"
                + "  \"COUNTER13_15.AV\"]\r\n"
                + "}\r\n"
                + "]";
        JSONArray arrays = JSON.parseArray(json);
        System.out.println(arrays.size());
        List<TModal> list = JSON.parseArray(json, TModal.class);
        System.out.println(list.size());
    }

    public static class TModal{
        private String namespace;
        private List<String> items;
    }
}
