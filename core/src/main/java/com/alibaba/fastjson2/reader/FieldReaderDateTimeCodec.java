package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.*;
import java.util.Date;
import java.util.Locale;

abstract class FieldReaderDateTimeCodec<T>
        extends FieldReader<T> {
    final ObjectReader dateReader;
    final boolean formatUnixTime;
    final boolean formatMillis;

    public FieldReaderDateTimeCodec(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjectReader dateReader
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field);
        this.dateReader = dateReader;

        boolean formatUnixTime = false, formatMillis = false, hasDay = false, hasHour = false;
        if (format != null) {
            switch (format) {
                case "unixtime":
                    formatUnixTime = true;
                    break;
                case "millis":
                    formatMillis = true;
                    break;
                default:
                    break;
            }
        }
        this.formatUnixTime = formatUnixTime;
        this.formatMillis = formatMillis;
    }

    @Override
    public final Object readFieldValue(JSONReader jsonReader) {
        return dateReader.readObject(jsonReader, fieldType, fieldName, features);
    }

    @Override
    public final ObjectReader getObjectReader(JSONReader jsonReader) {
        return dateReader;
    }

    public final ObjectReader getObjectReader(JSONReader.Context context) {
        return dateReader;
    }

    protected abstract void accept(T object, Date value);

    protected abstract void acceptNull(T object);

    protected abstract void accept(T object, Instant value);

    protected abstract void accept(T object, LocalDateTime ldt);

    protected abstract void accept(T object, ZonedDateTime zdt);

    protected abstract Object apply(Date value);

    protected abstract Object apply(Instant value);

    protected abstract Object apply(ZonedDateTime zdt);

    protected abstract Object apply(LocalDateTime zdt);

    protected abstract Object apply(long millis);

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            acceptNull(object);
            return;
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                acceptNull(object);
                return;
            }

            if ((format == null || formatUnixTime || formatMillis) && IOUtils.isNumber(str)) {
                long millis = Long.parseLong(str);
                if (formatUnixTime) {
                    millis *= 1000L;
                }
                accept(object, millis);
                return;
            } else {
                value = DateUtils.parseDate(str, format, DateUtils.DEFAULT_ZONE_ID);
            }
        }

        if (value instanceof Date) {
            accept(object, (Date) value);
        } else if (value instanceof Instant) {
            accept(object, (Instant) value);
        } else if (value instanceof Long) {
            accept(object, ((Long) value).longValue());
        } else if (value instanceof LocalDateTime) {
            accept(object, (LocalDateTime) value);
        } else if (value instanceof ZonedDateTime) {
            accept(object, (ZonedDateTime) value);
        } else {
            throw new JSONException("not support value " + value.getClass());
        }
    }

    public boolean supportAcceptType(Class valueClass) {
        return valueClass == Date.class
                || valueClass == String.class;
    }
}
