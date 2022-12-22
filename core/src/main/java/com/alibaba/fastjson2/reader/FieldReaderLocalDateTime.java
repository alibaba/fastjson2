package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;

public class FieldReaderLocalDateTime<T>
        extends FieldReaderDateTimeCodec<T> {
    final BiConsumer<T, ZonedDateTime> function;

    FieldReaderLocalDateTime(
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
            BiConsumer<T, ZonedDateTime> function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field);
        this.function = function;
    }

    public boolean supportAcceptType(Class valueClass) {
        return fieldClass == Instant.class || fieldClass == Long.class;
    }

    @Override
    public void accept(Object object, long value) {
        Instant instant = Instant.ofEpochMilli(value);
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        LocalDateTime ldt = zdt.toLocalDateTime();
        accept(object, ldt);
    }

    @Override
    protected void accept(Object object, Date value) {
        Instant instant = value.toInstant();
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        LocalDateTime ldt = zdt.toLocalDateTime();
        accept(object, ldt);
    }

    @Override
    protected void acceptNull(Object object) {
        accept(object, (LocalDateTime) null);
    }

    @Override
    protected void accept(Object object, Instant instant) {
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        LocalDateTime ldt = zdt.toLocalDateTime();
        accept(object, ldt);
    }

    @Override
    protected void accept(Object object, ZonedDateTime zdt) {
        LocalDateTime ldt = zdt.toLocalDateTime();
        accept(object, ldt);
    }

    @Override
    protected Object apply(Date value) {
        Instant instant = value.toInstant();
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        return zdt.toLocalDateTime();
    }

    @Override
    protected Object apply(Instant instant) {
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        return zdt.toLocalDateTime();
    }

    @Override
    protected Object apply(ZonedDateTime zdt) {
        return zdt.toLocalDateTime();
    }

    @Override
    protected Object apply(LocalDateTime ldt) {
        return ldt;
    }

    @Override
    protected Object apply(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        ZonedDateTime zdt = instant.atZone(DateUtils.DEFAULT_ZONE_ID);
        return zdt.toLocalDateTime();
    }

    public void accept(Object object, LocalDateTime value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (object == null) {
            throw new JSONException("set " + fieldName + " error, object is null");
        }

        if (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        if (fieldOffset != -1) {
            UnsafeUtils.putObject(object, fieldOffset, value);
            return;
        }

        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderImplLocalDateTime.INSTANCE
                    : new ObjectReaderImplLocalDateTime(format, locale);
        }
        return dateReader;
    }

    public ObjectReader getObjectReader(JSONReader.Context context) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderImplLocalDateTime.INSTANCE
                    : new ObjectReaderImplLocalDateTime(format, locale);
        }
        return dateReader;
    }
}
