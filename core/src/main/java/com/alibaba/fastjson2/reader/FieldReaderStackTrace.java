package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.BiConsumer;

public class FieldReaderStackTrace
        extends FieldReaderObject {
    public FieldReaderStackTrace(
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
            BiConsumer function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function);
    }

    public void accept(Object object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        if (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }
        if (value instanceof Collection) {
            Collection collection = (Collection) value;

            int nullCount = 0;
            for (Iterator it = collection.iterator(); it.hasNext(); ) {
                Object item = it.next();
                if (item == null) {
                    nullCount++;
                }
            }

            if (nullCount == collection.size()) {
                value = new StackTraceElement[0];
            } else {
                StackTraceElement[] array = new StackTraceElement[collection.size()];
                collection.toArray(array);
                value = array;
            }
        }

        function.accept(object, value);
    }
}
