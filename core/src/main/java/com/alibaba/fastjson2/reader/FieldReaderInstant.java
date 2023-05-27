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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;

public final class FieldReaderInstant<T>
        extends FieldReaderDateTimeCodec<T> {
    final BiConsumer<T, Instant> function;

    FieldReaderInstant(
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
            BiConsumer<T, Instant> function
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
                ObjectReaderImplInstant.of(format, locale)
        );
        this.function = function;
    }

    @Override
    public final void readFieldValue(JSONReader jsonReader, T object) {
        Instant date = (Instant) dateReader.readObject(jsonReader, fieldType, fieldName, features);
        accept(object, date);
    }

    @Override
    public final void readFieldValueJSONB(JSONReader jsonReader, T object) {
        Instant instant = jsonReader.readInstant();
        accept(object, instant);
    }

    @Override
    protected void accept(T object, LocalDateTime ldt) {
        ZoneOffset offset = DateUtils.DEFAULT_ZONE_ID.getRules().getOffset(ldt);
        Instant instant = ldt.toInstant(offset);
        accept(object, instant);
    }

    @Override
    protected void accept(T object, Date value) {
        accept(object, value.toInstant());
    }

    @Override
    protected void accept(T object, ZonedDateTime zdt) {
        accept(object, zdt.toInstant());
    }

    @Override
    protected Object apply(Date value) {
        return value.toInstant();
    }

    @Override
    protected Object apply(Instant value) {
        return value;
    }

    @Override
    protected Object apply(ZonedDateTime zdt) {
        return zdt.toInstant();
    }

    @Override
    protected Object apply(LocalDateTime ldt) {
        ZoneOffset offset = DateUtils.DEFAULT_ZONE_ID.getRules().getOffset(ldt);
        return ldt.toInstant(offset);
    }

    @Override
    protected Object apply(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    @Override
    protected void acceptNull(T object) {
        accept(object, (Instant) null);
    }

    @Override
    public void accept(T object, long milli) {
        accept(object, Instant.ofEpochMilli(milli));
    }

    @Override
    protected void accept(T object, Instant instant) {
        if (schema != null) {
            schema.assertValidate(instant);
        }

        if (object == null) {
            throw new JSONException("set " + fieldName + " error, object is null");
        }

        if (instant == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        if (function != null) {
            function.accept(object, instant);
            return;
        }

        if (method != null) {
            try {
                method.invoke(object, instant);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
            return;
        }

        if (fieldOffset != -1) {
            UnsafeUtils.putObject(object, fieldOffset, instant);
            return;
        }

        try {
            field.set(object, instant);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
