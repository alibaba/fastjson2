package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

final class ObjectReaderImplSimpleDateFormat
        implements ObjectReader<SimpleDateFormat> {
    static final ObjectReaderImplSimpleDateFormat INSTANCE = new ObjectReaderImplSimpleDateFormat();

    @Override
    public SimpleDateFormat readJSONBObject(
            JSONReader jsonReader,
            Type fieldType,
            Object fieldName,
            long features
    ) {
        return readObject(jsonReader, fieldType, fieldName, features);
    }

    @Override
    public SimpleDateFormat readObject(
            JSONReader jsonReader,
            Type fieldType,
            Object fieldName,
            long features
    ) {
        String pattern = jsonReader.readString();
        if (pattern == null) {
            return null;
        }

        Locale locale = jsonReader.getLocale();
        SimpleDateFormat format = locale == null
                ? new SimpleDateFormat(pattern)
                : new SimpleDateFormat(pattern, locale);
        format.setTimeZone(TimeZone.getTimeZone(jsonReader.getContext().getZoneId()));
        return format;
    }
}
