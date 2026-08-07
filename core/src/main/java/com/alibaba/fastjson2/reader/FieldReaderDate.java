package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderDate<T>
        extends FieldReaderDateTimeCodec<T> {
    public FieldReaderDate(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Field field,
            Method method,
            BiConsumer<T, Date> function
    ) {
        super(
                fieldName,
                fieldType,
                fieldClass,
                ordinal,
                features,
                format,
                locale,
                defaultValue,
                schema,
                method,
                field,
                function,
                ObjectReaderImplDate.of(format, locale)
        );
    }

    @Override
    protected void acceptNull(T object) {
        accept(object, (Date) null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Date date;
        try {
            date = (Date) dateReader.readObject(jsonReader, fieldType, fieldName, features);
        } catch (Exception e) {
            if ((features & JSONReader.Feature.NullOnError.mask) == 0) {
                throw e;
            }
            date = null;
        }
        accept(object, date);
    }

    @Override
    protected void accept(T object, Date value) {
        propertyAccessor.setObject(object, value);
    }

    @Override
    protected void accept(T object, Instant instant) {
        Date date = Date.from(instant);
        accept(object, date);
    }

    @Override
    public void accept(T object, long value) {
        accept(object, new Date(value));
    }

    @Override
    protected void accept(T object, ZonedDateTime zdt) {
        long epochMilli = zdt.toInstant().toEpochMilli();
        Date value = new Date(epochMilli);
        accept(object, value);
    }

    @Override
    protected Object apply(LocalDateTime ldt) {
        ZoneOffset offset = DateUtils.DEFAULT_ZONE_ID.getRules().getOffset(ldt);
        Instant instant = ldt.toInstant(offset);
        return Date.from(instant);
    }

    @Override
    protected void accept(T object, LocalDateTime ldt) {
        ZoneOffset offset = DateUtils.DEFAULT_ZONE_ID.getRules().getOffset(ldt);
        Instant instant = ldt.toInstant(offset);
        Date value = Date.from(instant);
        accept(object, value);
    }

    @Override
    protected Object apply(Date value) {
        return value;
    }

    @Override
    protected Object apply(Instant instant) {
        return Date.from(instant);
    }

    @Override
    protected Object apply(ZonedDateTime zdt) {
        Instant instant = zdt.toInstant();
        return Date.from(instant);
    }

    @Override
    protected Object apply(long millis) {
        return new Date(millis);
    }
}
