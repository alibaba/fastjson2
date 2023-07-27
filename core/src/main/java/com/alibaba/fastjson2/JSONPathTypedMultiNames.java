package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Map;

class JSONPathTypedMultiNames
        extends JSONPathTypedMulti {
    final JSONPath prefix;
    final JSONPath[] namePaths;
    final String[] names;
    final FieldReader[] fieldReaders;
    final ObjectReaderAdapter<Object[]> objectReader;

    JSONPathTypedMultiNames(
            JSONPath[] paths,
            JSONPath prefix,
            JSONPath[] namePaths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            long features
    ) {
        super(paths, types, formats, pathFeatures, zoneId, features);
        this.prefix = prefix;
        this.namePaths = namePaths;
        this.names = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            JSONPathSingleName jsonPathSingleName = (JSONPathSingleName) namePaths[i];
            String fieldName = jsonPathSingleName.name;
            names[i] = fieldName;
        }
        long[] fieldReaderFeatures = new long[names.length];
        if (pathFeatures != null) {
            for (int i = 0; i < pathFeatures.length; i++) {
                if ((pathFeatures[i] & Feature.NullOnError.mask) != 0) {
                    fieldReaderFeatures[i] |= JSONReader.Feature.NullOnError.mask;
                }
            }
        }

        Type[] fieldTypes = types.clone();
        for (int i = 0; i < fieldTypes.length; i++) {
            Type fieldType = fieldTypes[i];
            if (fieldType == boolean.class) {
                fieldTypes[i] = Boolean.class;
            } else if (fieldType == char.class) {
                fieldTypes[i] = Character.class;
            } else if (fieldType == byte.class) {
                fieldTypes[i] = Byte.class;
            } else if (fieldType == short.class) {
                fieldTypes[i] = Short.class;
            } else if (fieldType == int.class) {
                fieldTypes[i] = Integer.class;
            } else if (fieldType == long.class) {
                fieldTypes[i] = Long.class;
            } else if (fieldType == float.class) {
                fieldTypes[i] = Float.class;
            } else if (fieldType == double.class) {
                fieldTypes[i] = Double.class;
            }
        }

        final int length = names.length;
        objectReader = (ObjectReaderAdapter<Object[]>) JSONFactory.getDefaultObjectReaderProvider()
                .createObjectReader(
                        names,
                        fieldTypes,
                        fieldReaderFeatures,
                        () -> new Object[length],
                        (o, i, v) -> o[i] = v
                );
        this.fieldReaders = objectReader.getFieldReaders();
    }

    @Override
    public boolean isRef() {
        return true;
    }

    @Override
    public Object eval(Object root) {
        Object[] array = new Object[paths.length];

        Object object = root;
        if (prefix != null) {
            object = prefix.eval(root);
        }

        if (object == null) {
            return new Object[paths.length];
        }

        if (object instanceof Map) {
            return objectReader.createInstance((Map) object, 0);
        } else {
            ObjectWriter objectReader = JSONFactory.defaultObjectWriterProvider
                    .getObjectWriter(
                            object.getClass()
                    );

            for (int i = 0; i < names.length; i++) {
                FieldWriter fieldWriter = objectReader.getFieldWriter(names[i]);
                if (fieldWriter == null) {
                    continue;
                }

                Object result = fieldWriter.getFieldValue(object);

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
        }

        return array;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (prefix != null) {
            Object object = jsonReader.readAny();
            return eval(object);
        }

        if (jsonReader.nextIfNull()) {
            return new Object[paths.length];
        }

        if (!jsonReader.nextIfObjectStart()) {
            throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
        }

        return objectReader.readObject(jsonReader, null, null, 0);
    }
}
