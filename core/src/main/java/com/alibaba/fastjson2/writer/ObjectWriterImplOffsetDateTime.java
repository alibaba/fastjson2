package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;

final class ObjectWriterImplOffsetDateTime
        extends DateTimeCodec implements ObjectWriter {
    static final ObjectWriterImplOffsetDateTime INSTANCE = new ObjectWriterImplOffsetDateTime(null, null);

    public ObjectWriterImplOffsetDateTime(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context ctx = jsonWriter.getContext();

        OffsetDateTime dateTime = (OffsetDateTime) object;

        if (formatUnixTime || (format == null && ctx.isDateFormatUnixTime())) {
            long millis = dateTime
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        if (formatMillis || (format == null && ctx.isDateFormatMillis())) {
            long millis = dateTime
                    .toInstant()
                    .toEpochMilli();
            jsonWriter.writeInt64(millis);
            return;
        }

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = ctx.getDateFormatter();
        }

        if (formatter == null) {
            int year = dateTime.get(ChronoField.YEAR);
            int month = dateTime.get(ChronoField.MONTH_OF_YEAR);
            int dayOfMonth = dateTime.get(ChronoField.DAY_OF_MONTH);
            int hour = dateTime.get(ChronoField.HOUR_OF_DAY);
            int minute = dateTime.get(ChronoField.MINUTE_OF_HOUR);
            int second = dateTime.get(ChronoField.SECOND_OF_MINUTE);
            jsonWriter.writeDateTime19(year, month, dayOfMonth, hour, minute, second);
        } else {
            String str = formatter.format(dateTime);
            jsonWriter.writeString(str);
        }
    }
}
