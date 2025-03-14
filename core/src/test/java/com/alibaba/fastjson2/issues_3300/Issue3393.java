package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3393 {
    @Test
    public void test() {
        String str = "{'date':'۲۰۲۵-۰۳-۱۰ ۰۱:۴۴:۵۵'}";
        assertEquals("2025-03-10T01:44:55", JSON.parseObject(str, Bean.class).date.toString());
        assertEquals("2025-03-10T01:44:55", JSON.parseObject(str.toCharArray(), Bean.class).date.toString());
        assertEquals("2025-03-10T01:44:55", JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class).date.toString());
    }

    public static class Bean {
        @JSONField(deserializeUsing = DateDeser.class)
        public LocalDateTime date;
    }

    public static class DateDeser
            implements ObjectReader {
        static final DecimalStyle persianStyle = DecimalStyle.STANDARD.withZeroDigit('۰');
        static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .toFormatter()
                .withDecimalStyle(persianStyle);
        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            String str = jsonReader.readString();
            return str == null ? null : LocalDateTime.parse(str, formatter);
        }
    }
}
