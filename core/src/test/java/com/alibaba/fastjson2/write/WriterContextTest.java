package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriterContextTest {
    @Test
    public void test() {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setDateFormat("yyyy-MM-dd");

        LocalDateTime localDateTime = LocalDateTime.of(2014, 1, 2, 12, 13, 14);
        assertEquals("\"2014-01-02\"", JSON.toJSONString(localDateTime, context));
    }
}
