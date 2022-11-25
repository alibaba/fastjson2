package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.util.Arrays;

public class JSONPathTypedMultiNamesPrefixSingleName
        extends JSONPathTypedMultiNames {
    JSONPathTypedMultiNamesPrefixSingleName(
            JSONPath[] paths,
            JSONPath prefix,
            JSONPath[] namePaths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            long features) {
        super(paths, prefix, namePaths, types, formats, pathFeatures, zoneId, features);
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        JSONPathSingleName prefixName = (JSONPathSingleName) prefix;
        long prefixNameHash = prefixName.nameHashCode;
        if (!jsonReader.nextIfObjectStart()) {
            throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
        }

        while (!jsonReader.nextIfObjectEnd()) {
            long nameHashCode = jsonReader.readFieldNameHashCode();
            boolean match = nameHashCode == prefixNameHash;
            if (!match) {
                jsonReader.skipValue();
                continue;
            }

            break;
        }

        if (jsonReader.nextIfNull()) {
            return null;
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
