package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue235 {
    @Test
    public void test() {
        String json = "[{\r\n"
                + "  \"namespace\":\"unit07\", \r\n"
                + "  \"items\":[\"COUNTER13_14.AV\",\r\n"
                + "  \"COUNTER13_15.AV\"]\r\n"
                + "}\r\n"
                + "}]";

        assertThrows(JSONException.class, () -> JSON.parseArray(json));
        assertThrows(JSONException.class, () -> JSON.parseArray(json, TModal.class));
    }

    public static class TModal {
        private String namespace;
        private List<String> items;
    }
}
