package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class FieldReaderMapField<T>
        extends FieldReaderObjectField<T>
        implements FieldReaderMap<T> {
    protected final String arrayToMapKey;
    protected final Type valueType;
    protected final BiConsumer arrayToMapDuplicateHandler;
    FieldReaderMapField(
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
            String arrayToMapKey,
            BiConsumer arrayToMapDuplicateHandler
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, field);
        this.valueType = TypeUtils.getMapValueType(fieldType);
        this.arrayToMapKey = arrayToMapKey;
        this.arrayToMapDuplicateHandler = arrayToMapDuplicateHandler;
    }

    protected void acceptAny(T object, Object fieldValue, long features) {
        if (arrayToMapKey != null && fieldValue instanceof Collection) {
            ObjectReader reader = this.getObjectReader(JSONFactory.createReadContext());
            Map map = (Map) reader.createInstance(features);
            arrayToMap(map,
                    (Collection) fieldValue,
                    arrayToMapKey,
                    JSONFactory.getObjectReader(valueType, this.features | features),
                    arrayToMapDuplicateHandler);
            accept(object, map);
            return;
        }

        super.acceptAny(object, fieldValue, features);
    }

    @Override
    public final String getArrayToMapKey() {
        return arrayToMapKey;
    }
}
