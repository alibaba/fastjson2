package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.DateTimeCodec;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ObjectWriterImplInstant
        extends DateTimeCodec
        implements ObjectWriter {
    static final ObjectWriterImplInstant INSTANCE = new ObjectWriterImplInstant(null, null);

    public ObjectWriterImplInstant(String format, Locale locale) {
        super(format, locale);
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeInstant((Instant) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        JSONWriter.Context context = jsonWriter.getContext();

        Instant instant = (Instant) object;
        if (format == null && context.getDateFormat() == null) {
            jsonWriter.writeInstant(instant);
            return;
        }

        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, context.getZoneId());

        if (formatUnixTime || (format == null && context.isDateFormatUnixTime())) {
            long millis = zdt.toInstant().toEpochMilli();
            jsonWriter.writeInt64(millis / 1000);
            return;
        }

        if (formatMillis || (format == null && context.isDateFormatMillis())) {
            jsonWriter.writeInt64(zdt
                    .toInstant()
                    .toEpochMilli());
            return;
        }

        if (formatISO8601 || (format == null && context.isDateFormatISO8601())) {
            jsonWriter.writeDateTimeISO8601(
                    zdt.getYear(),
                    zdt.getMonthValue(),
                    zdt.getDayOfMonth(),
                    zdt.getHour(),
                    zdt.getMinute(),
                    zdt.getSecond(),
                    zdt.getNano() / 1000_000,
                    zdt.getOffset().getTotalSeconds()
            );
            return;
        }

        DateTimeFormatter formatter = this.getDateFormatter();
        if (formatter == null) {
            formatter = context.getDateFormatter();
        }

        if (formatter == null) {
            jsonWriter.writeZonedDateTime(zdt);
        } else {
            String str = formatter.format(zdt);
            jsonWriter.writeString(str);
        }
    }
}
