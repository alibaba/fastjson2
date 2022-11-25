package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;

class JSONPathTypedMultiNames
        extends JSONPathTypedMulti {
    final JSONPath prefix;
    final JSONPath[] namePaths;
    final String[] names;
    final long[] hashCodes;
    final short[] mapping;
    final FieldReader[] fieldReaders;

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

        long[] hashCodes = new long[paths.length];
        fieldReaders = new FieldReader[paths.length];
        for (int i = 0; i < paths.length; i++) {
            JSONPathSingleName jsonPathSingleName = (JSONPathSingleName) namePaths[i];
            String fieldName = jsonPathSingleName.name;
            names[i] = fieldName;
            hashCodes[i] = jsonPathSingleName.nameHashCode;
            String format = formats != null ? formats[i] : null;

            Type fieldType = types[i];
            Class fieldClass = TypeUtils.getClass(fieldType);

            long fieldFeatures = 0;
            if (isIgnoreError(i)) {
                fieldFeatures |= JSONReader.Feature.NullOnError.mask;
            }
            fieldReaders[i] = ObjectReaderCreator.INSTANCE.createFieldReader(
                    null,
                    null,
                    fieldName,
                    fieldType,
                    fieldClass,
                    i,
                    fieldFeatures,
                    format,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
        Arrays.sort(this.hashCodes);

        mapping = new short[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = (short) i;
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

        if (object == null) {
            return new Object[paths.length];
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
            Class objectClass = object.getClass();
            ObjectWriter objectReader = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(objectClass);

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

        Object[] values = new Object[paths.length];
        while (!jsonReader.nextIfObjectEnd()) {
            long nameHashCode = jsonReader.readFieldNameHashCode();

            int m = Arrays.binarySearch(hashCodes, nameHashCode);
            if (m < 0) {
                jsonReader.skipValue();
                continue;
            }

            int index = this.mapping[m];
            FieldReader fieldReader = fieldReaders[index];
            Object fieldValue;
            try {
                fieldValue = fieldReader.readFieldValue(jsonReader);
            } catch (Exception e) {
                long features = 0;
                if (index < this.pathFeatures.length) {
                    features = this.pathFeatures[index];
                }
                if ((features & Feature.NullOnError.mask) == 0) {
                    throw e;
                }
                fieldValue = null;
            }
            values[index] = fieldValue;
        }

        return values;
    }
}
