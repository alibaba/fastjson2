package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JdbcSupport;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
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
                    return JdbcSupport.createTimeReader(format);
                case "java.sql.Timestamp":
                    return JdbcSupport.createTimestampReader(format);
                case "java.sql.Date":
                    return JdbcSupport.createDateReader(format);
                case "byte[]":
                case "[B":
                    return new ObjectReaderBaseModule.Inte8ArrayImpl(format);
                default:
                    if (Calendar.class.isAssignableFrom(fieldClass)) {
                        if (format == null) {
                            return ObjectReaderBaseModule.CalendarImpl.INSTANCE;
                        }

                        switch (format) {
                            case "unixtime":
                                return ObjectReaderBaseModule.CalendarImpl.INSTANCE_UNIXTIME;
                            default:
                                return new ObjectReaderBaseModule.CalendarImpl(format);
                        }
                    }

                    if (fieldClass == ZonedDateTime.class) {
                        if (format == null) {
                            return ObjectReaderBaseModule.ZonedDateTimeImpl.INSTANCE;
                        }

                        switch (format) {
                            case "unixtime":
                                return ObjectReaderBaseModule.ZonedDateTimeImpl.INSTANCE_UNIXTIME;
                            default:
                                return new ObjectReaderBaseModule.ZonedDateTimeImpl(format);
                        }
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
                        if (format == null) {
                            return ObjectReaderImplInstant.INSTANCE;
                        }

                        return new ObjectReaderImplInstant(format, locale);
                    }

                    if (fieldClass == Optional.class) {
                        if (fieldType instanceof ParameterizedType) {
                            Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
                            if (actualTypeArguments.length == 1) {
                                Type paramType = actualTypeArguments[0];
                                Class<?> paramClass = TypeUtils.getClass(paramType);
                                return createFormattedObjectReader(paramType, paramClass, format, locale);
                            }
                        }
                        return ObjectReaderBaseModule.OptionalImpl.INSTANCE;
                    }
                    break;
            }
        }
        return null;
    }
}
