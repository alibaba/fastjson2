package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;

final class ObjectWriterImplOffsetDateTime extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplOffsetDateTime INSTANCE = new ObjectWriterImplOffsetDateTime();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.getContext();

        OffsetDateTime dateTime = (OffsetDateTime) object;

        String dateFormat = ctx.getDateFormat();
        if (dateFormat == null) {
            int year = dateTime.get(ChronoField.YEAR);
            int month = dateTime.get(ChronoField.MONTH_OF_YEAR);
            int dayOfMonth = dateTime.get(ChronoField.DAY_OF_MONTH);
            int hour = dateTime.get(ChronoField.HOUR_OF_DAY);
            int minute = dateTime.get(ChronoField.MINUTE_OF_HOUR);
            int second = dateTime.get(ChronoField.SECOND_OF_MINUTE);
            jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
        } else {
            String str = ctx.getDateFormatter().format(dateTime);
            jsonWriter.writeString(str);
        }
    }
}
