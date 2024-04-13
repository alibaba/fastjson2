package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2426 {
    @Test
    public void test() {
        String defaultWriterFormat = JSONFactory.getDefaultWriterFormat();
        try {
            JSON.configWriterDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = new Date(1712988987882L);
            JSONWriter.Context writeContext = JSONFactory.createWriteContext();
            String json = JSON.toJSONString(date, writeContext);
            assertEquals("\"2024-04-13T14:16:27\"", json);
        } finally {
            JSON.configWriterDateFormat(defaultWriterFormat);
        }
        assertEquals(defaultWriterFormat, JSONFactory.getDefaultWriterFormat());
    }
}
