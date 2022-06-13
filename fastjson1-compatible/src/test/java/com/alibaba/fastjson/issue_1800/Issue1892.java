package com.alibaba.fastjson.issue_1800;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1892 {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals("\"2018-10-10T00:00:00+08:00\"",
                JSON.toJSONString(
                        LocalDateTime.of(2018, 10, 10, 0, 0), SerializerFeature.UseISO8601DateFormat
                )
        );
    }

    @Test
    public void test_for_issue_1() throws Exception {
        String json = JSON.toJSONString(
                LocalDateTime.of(2018, 10, 10, 0, 0, 40, 788000000), SerializerFeature.UseISO8601DateFormat
        );
        assertEquals("\"2018-10-10T00:00:40.788+08:00\"", json);
    }
}
