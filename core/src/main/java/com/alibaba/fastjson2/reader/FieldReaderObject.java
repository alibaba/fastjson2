package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JdbcSupport;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

public interface FieldReaderObject<T, V> extends FieldReader<T> {
    ObjectReader<V> getFieldObjectReader(JSONReader.Context context);

    @Override
    default void readFieldValue(JSONReader jsonReader, T object) {
        accept(object,
                getFieldObjectReader(jsonReader.getContext())
                        .readObject(jsonReader, 0));
    }

    static ObjectReader createFormattedObjectReader(Type fieldType, Class fieldClass, String format, Locale locale) {
        if (format != null && !format.isEmpty()) {
            String typeName = fieldType.getTypeName();
            switch (typeName) {
                case "java.sql.Time":
                    return JdbcSupport.createTimeReader(format, locale);
                case "java.sql.Timestamp":
                    return JdbcSupport.createTimestampReader(format, locale);
                case "java.sql.Date":
                    return JdbcSupport.createDateReader(format, locale);
                case "byte[]":
                case "[B":
                    return new ObjectReaderImplInt8Array(format);
                default:
                    if (Calendar.class.isAssignableFrom(fieldClass)) {
                        return ObjectReaderImplCalendar.of(format, locale);
                    }

                    if (fieldClass == ZonedDateTime.class) {
                        return ObjectReaderImplZonedDateTime.of(format, locale);
                    }

                    if (fieldClass == LocalDateTime.class) {
                        if (format == null) {
                            return ObjectReaderBaseModule.LocalDateTimeImpl.INSTANCE;
                        }

                        switch (format) {
                            case "unixtime":
                                return ObjectReaderBaseModule.LocalDateTimeImpl.INSTANCE_UNIXTIME;
                            default:
                                return new ObjectReaderBaseModule.LocalDateTimeImpl(format);
                        }
                    }

                    if (fieldClass == Instant.class) {
                        return ObjectReaderImplInstant.of(format, locale);
                    }

                    if (fieldClass == Optional.class) {
                        return ObjectReaderImplOptional.of(fieldType, format, locale);
                    }

                    if (fieldClass == Date.class) {
                        return ObjectReaderImplDate.of(format, locale);
                    }

                    break;
            }
        }
        return null;
    }
}
