package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeWriter implements ObjectWriter<ZonedDateTime> {
    public static final ZonedDateTimeWriter INSTANCE = new ZonedDateTimeWriter();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        // 对齐 vertx 默认行为，抹除地区 ID（如 Asia/Shanghai）
        jsonWriter.writeString(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((ZonedDateTime) object));
    }
}
