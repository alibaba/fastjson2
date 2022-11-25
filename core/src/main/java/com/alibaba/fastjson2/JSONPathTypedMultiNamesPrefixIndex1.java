package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.util.Arrays;

public class JSONPathTypedMultiNamesPrefixIndex1
        extends JSONPathTypedMultiNames {
    final int index;

    JSONPathTypedMultiNamesPrefixIndex1(
            JSONPath[] paths,
            JSONPathSingleIndex prefix,
            JSONPath[] namePaths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            long features) {
        super(paths, prefix, namePaths, types, formats, pathFeatures, zoneId, features);
        index = prefix.index;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader.nextIfNull()) {
            return new Object[paths.length];
        }

        if (!jsonReader.nextIfMatch('[')) {
            throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
        }

        for (int i = 0; i < index; ++i) {
            if (jsonReader.nextIfMatch(']')) {
                return new Object[paths.length];
            }

            if (jsonReader.isEnd()) {
                throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
            }

            jsonReader.skipValue();
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
