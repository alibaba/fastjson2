package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class Issue3620 {
    @Test
    public void testConfigFieldBased() {
        try {
            JSON.config(JSONWriter.Feature.FieldBased);
            JSON.register(LocalDate.class, (jsonWriter, object, fieldName, fieldType, features) -> {
                if (object == null) {
                    jsonWriter.writeNull();
                    return;
                }
                LocalDate date = (LocalDate) object;
                //获取月份
                jsonWriter.writeString(date.getMonthValue());
            });
            LocalDate localDate = LocalDate.of(2025, 7, 1);
            Object json = JSON.toJSON(localDate);
            Assertions.assertEquals("7", json);
        } finally {
            JSONFactory.getDefaultObjectWriterProvider().unregister(LocalDate.class, true);
            JSON.config(JSONWriter.Feature.FieldBased, false);
        }
    }

    @Test
    public void testNoConfigFieldBased() {
        try {
            JSON.register(LocalDate.class, (jsonWriter, object, fieldName, fieldType, features) -> {
                if (object == null) {
                    jsonWriter.writeNull();
                    return;
                }
                LocalDate date = (LocalDate) object;
                //获取月份
                jsonWriter.writeString(date.getMonthValue());
            });
            LocalDate localDate = LocalDate.of(2025, 7, 1);
            Object json = JSON.toJSON(localDate);
            Assertions.assertEquals("7", json);
        } finally {
            JSONFactory.getDefaultObjectWriterProvider().unregister(LocalDate.class);
        }
    }
}
