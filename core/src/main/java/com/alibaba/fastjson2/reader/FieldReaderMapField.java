package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class FieldReaderMapField<T>
        extends FieldReaderObjectField<T> {
    protected final String arrayToMapKey;
    protected final PropertyNamingStrategy namingStrategy;
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
        this.namingStrategy = PropertyNamingStrategy.of(format);
        this.arrayToMapDuplicateHandler = arrayToMapDuplicateHandler;
    }

    protected void acceptAny(T object, Object fieldValue, long features) {
        if (arrayToMapKey != null && fieldValue instanceof Collection) {
            ObjectReader reader = this.getObjectReader(JSONFactory.createReadContext());
            Map map = (Map) reader.createInstance(features);
            arrayToMap(map,
                    (Collection) fieldValue,
                    arrayToMapKey,
                    namingStrategy,
                    JSONFactory.getObjectReader(valueType, this.features | features),
                    arrayToMapDuplicateHandler);
            accept(object, map);
            return;
        }

        super.acceptAny(object, fieldValue, features);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (arrayToMapKey != null && jsonReader.isArray()) {
            ObjectReader reader = this.getObjectReader(jsonReader);
            Map map = (Map) reader.createInstance(features);
            List array = jsonReader.readArray(valueType);
            arrayToMap(map,
                    array,
                    arrayToMapKey,
                    namingStrategy,
                    JSONFactory.getObjectReader(valueType, this.features | features),
                    arrayToMapDuplicateHandler);
            accept(object, map);
            return;
        }
        super.readFieldValue(jsonReader, object);
    }
}
