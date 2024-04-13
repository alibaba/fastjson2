package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2426 {
    @Test
    public void test() {
        String defaultWriterFormat = JSONFactory.getDefaultWriterFormat();
        try {
            String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
            JSON.configWriterDateFormat(dateFormat);

            JSONWriter.Context writeContext = JSONFactory.createWriteContext();
            assertEquals(dateFormat, writeContext.getDateFormat());

            Date date = new Date(1712988987882L);
            String json = JSON.toJSONString(date, writeContext);
            assertEquals("\"2024-04-13T14:16:27\"", json);

            java.sql.Timestamp ts = new Timestamp(date.getTime());
            String json1 = JSON.toJSONString(date, writeContext);
            assertEquals("\"2024-04-13T14:16:27\"", json1);
        } finally {
            JSON.configWriterDateFormat(defaultWriterFormat);
        }
        assertEquals(defaultWriterFormat, JSONFactory.getDefaultWriterFormat());
    }
}
