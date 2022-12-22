package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
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

import static com.alibaba.fastjson2.util.DateUtils.DEFAULT_ZONE_ID;

public class FieldReaderZonedDateTime<T>
        extends FieldReaderDateTimeCodec<T> {
    final BiConsumer<T, ZonedDateTime> function;

    FieldReaderZonedDateTime(
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

    @Override
    protected void accept(T object, Date value) {
        Instant instant = value.toInstant();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
        accept(object, zdt);
    }

    @Override
    protected void accept(T object, Instant instant) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
        accept(object, zdt);
    }

    @Override
    protected void accept(T object, LocalDateTime ldt) {
        ZonedDateTime zdt = ZonedDateTime.of(ldt, DEFAULT_ZONE_ID);
        accept(object, zdt);
    }

    @Override
    protected Object apply(Date value) {
        Instant instant = value.toInstant();
        return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }

    @Override
    protected Object apply(Instant value) {
        return ZonedDateTime.ofInstant(value, DEFAULT_ZONE_ID);
    }

    @Override
    protected Object apply(ZonedDateTime zdt) {
        return zdt;
    }

    @Override
    protected Object apply(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
    }

    @Override
    protected Object apply(LocalDateTime ldt) {
        return ldt.atZone(DEFAULT_ZONE_ID);
    }

    @Override
    protected void acceptNull(T object) {
        accept(object, (ZonedDateTime) null);
    }

    @Override
    public void accept(T object, long milli) {
        Instant instant = Instant.ofEpochMilli(milli);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, DEFAULT_ZONE_ID);
        accept(object, zdt);
    }

    @Override
    protected void accept(T object, ZonedDateTime zdt) {
        if (schema != null) {
            schema.assertValidate(zdt);
        }

        if (zdt == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        if (object == null) {
            throw new JSONException("set " + fieldName + " error, object is null");
        }

        if (function != null) {
            function.accept(object, zdt);
            return;
        }

        if (method != null) {
            try {
                method.invoke(object, zdt);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
            return;
        }

        if (fieldOffset != -1) {
            UnsafeUtils.putObject(object, fieldOffset, zdt);
            return;
        }

        try {
            field.set(object, zdt);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderImplZonedDateTime.INSTANCE
                    : new ObjectReaderImplZonedDateTime(format, locale);
        }
        return dateReader;
    }

    public ObjectReader getObjectReader(JSONReader.Context context) {
        if (dateReader == null) {
            dateReader = format == null
                    ? ObjectReaderImplZonedDateTime.INSTANCE
                    : new ObjectReaderImplZonedDateTime(format, locale);
        }
        return dateReader;
    }
}
