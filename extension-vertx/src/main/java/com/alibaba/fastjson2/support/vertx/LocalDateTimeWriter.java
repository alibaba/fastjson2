package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeWriter implements ObjectWriter<LocalDateTime> {
    public static final LocalDateTimeWriter INSTANCE = new LocalDateTimeWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        // 对齐 vertx 默认行为，使用带 'T' 的 ISO 标准格式
        jsonWriter.writeString(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((LocalDateTime) object));
    }
}
