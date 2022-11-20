package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.util.ObjectHolder;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.BiConsumer;

final class JSONPathTypedMultiNames
        extends JSONPathTypedMulti {
    final JSONPath prefix;
    final JSONPath[] namePaths;
    final String[] names;
    final long[] nameHashCodes;
    final ObjectReader objectReader;
    final ObjectReader prefixObjectReader;

    JSONPathTypedMultiNames(
            JSONPath[] paths,
            JSONPath prefix,
            JSONPath[] namePaths,
            Type[] types,
            String[] formats,
            ZoneId zoneId,
            long features
    ) {
        super(paths, types, formats, zoneId, features);
        this.prefix = prefix;
        this.namePaths = namePaths;
        this.names = new String[paths.length];
        this.nameHashCodes = new long[paths.length];

        FieldReader[] fieldReaders = new FieldReader[paths.length];
        for (int i = 0; i < paths.length; i++) {
            JSONPathSingleName jsonPathSingleName = (JSONPathSingleName) namePaths[i];
            String fieldName = jsonPathSingleName.name;
            names[i] = fieldName;
            nameHashCodes[i] = jsonPathSingleName.nameHashCode;
            String format = formats != null ? formats[i] : null;

            Type fieldType = types[i];
            Class fieldClass = TypeUtils.getClass(fieldType);
            BiConsumer function = new FieldValueCallback(i);

            fieldReaders[i] = ObjectReaderCreator.INSTANCE.createFieldReader(
                    null,
                    null,
                    fieldName,
                    fieldType,
                    fieldClass,
                    i,
                    0,
                    format,
                    null,
                    null,
                    null,
                    function,
                    null
            );
        }

        objectReader = ObjectReaderCreator.INSTANCE.createObjectReader(
                Object[].class,
                null,
                features,
                null,
                () -> new Object[paths.length],
                null,
                fieldReaders
        );

        ObjectReader prefixObjectReader = null;
        if (prefix instanceof JSONPathSingleName) {
            JSONPathSingleName prefixSingleName = (JSONPathSingleName) prefix;
            FieldReader prefixFieldReader = ObjectReaderCreator.INSTANCE.createFieldReader(
                    ObjectHolder.class,
                    ObjectHolder.class,
                    prefixSingleName.name,
                    Object.class,
                    Object.class,
                    0,
                    0,
                    null,
                    null,
                    null,
                    null,
                    ObjectHolder::setObject,
                    objectReader
            );
            prefixObjectReader = ObjectReaderCreator.INSTANCE.createObjectReader(
                    ObjectHolder.class,
                    null,
                    0L,
                    null,
                    () -> new ObjectHolder(),
                    null,
                    prefixFieldReader
            );
        } else if (prefix instanceof JSONPathTwoSegment) {
            JSONPathTwoSegment two = (JSONPathTwoSegment) prefix;
            if (two.first instanceof JSONPathSegmentName && two.second instanceof JSONPathSegmentName) {
                JSONPathSegmentName firstName = (JSONPathSegmentName) two.first;
                JSONPathSegmentName secondName = (JSONPathSegmentName) two.second;

                FieldReader secondFieldReader = ObjectReaderCreator.INSTANCE.createFieldReader(
                        ObjectHolder.class,
                        ObjectHolder.class,
                        secondName.name,
                        Object.class,
                        Object.class,
                        0,
                        0,
                        null,
                        null,
                        null,
                        null,
                        ObjectHolder::setObject,
                        objectReader
                );

                prefixObjectReader = ObjectReaderCreator.INSTANCE.createObjectReader(
                        ObjectHolder.class,
                        null,
                        0L,
                        null,
                        () -> new ObjectHolder(),
                        null,
                        secondFieldReader
                );

                FieldReader firstFieldReader = ObjectReaderCreator.INSTANCE.createFieldReader(
                        ObjectHolder.class,
                        ObjectHolder.class,
                        firstName.name,
                        Object.class,
                        Object.class,
                        0,
                        0,
                        null,
                        null,
                        null,
                        null,
                        ObjectHolder::setObject,
                        prefixObjectReader
                );

                prefixObjectReader = ObjectReaderCreator.INSTANCE.createObjectReader(
                        ObjectHolder.class,
                        null,
                        0L,
                        null,
                        () -> new ObjectHolder(),
                        null,
                        firstFieldReader
                );
            }
        }
        this.prefixObjectReader = prefixObjectReader;
    }

    static class FieldValueCallback
            implements BiConsumer {
        final int index;

        FieldValueCallback(int index) {
            this.index = index;
        }

        @Override
        public void accept(Object object, Object value) {
            Object[] array = (Object[]) object;
            array[index] = value;
        }
    }

    @Override
    public boolean isRef() {
        return true;
    }

    @Override
    public boolean contains(Object object) {
        for (JSONPath jsonPath : paths) {
            if (jsonPath.contains(object)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object eval(Object root) {
        Object[] array = new Object[paths.length];

        Object object = root;
        if (prefix != null) {
            object = prefix.eval(root);
        }

        if (object instanceof Map) {
            Map map = (Map) object;
            for (int i = 0; i < names.length; i++) {
                Object result = map.get(names[i]);
                Type type = types[i];
                if (result != null && result.getClass() != type) {
                    if (type == Long.class) {
                        result = TypeUtils.toLong(result);
                    } else if (type == BigDecimal.class) {
                        result = TypeUtils.toBigDecimal(result);
                    } else if (type == String[].class) {
                        result = TypeUtils.toStringArray(result);
                    } else {
                        result = TypeUtils.cast(result, type);
                    }
                }
                array[i] = result;
            }
        } else {
            for (int i = 0; i < paths.length; i++) {
                JSONPath jsonPath = namePaths[i];
                Type type = types[i];
                Object result = jsonPath.eval(object);
                if (result != null && result.getClass() != type) {
                    if (type == Long.class) {
                        result = TypeUtils.toLong(result);
                    } else if (type == BigDecimal.class) {
                        result = TypeUtils.toBigDecimal(result);
                    } else if (type == String[].class) {
                        result = TypeUtils.toStringArray(result);
                    } else {
                        result = TypeUtils.cast(result, type);
                    }
                }
                array[i] = result;
            }
        }

        return array;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (prefix == null) {
            return objectReader.readObject(jsonReader, null, null, features);
        }

        if (jsonReader.nextIfNull()) {
            return null;
        }

        if (prefixObjectReader != null) {
            ObjectHolder holder = (ObjectHolder) prefixObjectReader.readObject(jsonReader, null, null, features);
            Object object = holder.object;
            if (object instanceof ObjectHolder) {
                object = ((ObjectHolder) object).object;
            }
            return object;
        }

        if (prefix instanceof JSONPathSingleIndex) {
            int index = ((JSONPathSingleIndex) prefix).index;
            if (index >= 0) {
                int max = jsonReader.startArray();
                for (int i = 0; i < index && i < max; i++) {
                    jsonReader.skipValue();
                }

                if (jsonReader.nextIfNull()) {
                    return null;
                }

                return objectReader.readObject(jsonReader, null, null, features);
            }
        }

        Object object = jsonReader.readAny();
        return eval(object);
    }
}
