package com.alibaba.fastjson2.v1issues.issue_1800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1893 {
    @Test
    public void test() throws Exception {
        assertEquals(
                "{\"value\":\"0\"}",
                JSON.toJSONString(new Bean(),
                        JSONWriter.Feature.WriteNullNumberAsZero,
                        JSONWriter.Feature.WriteLongAsString)
        );
    }

    public static class Bean {
        public Long value;
    }
}
