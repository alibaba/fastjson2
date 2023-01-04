package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.FieldReader;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.util.Arrays;

public class JSONPathTypedMultiNamesPrefixName2
        extends JSONPathTypedMultiNames {
    final String prefixName0;
    final long prefixNameHash0;

    final String prefixName1;
    final long prefixNameHash1;

    JSONPathTypedMultiNamesPrefixName2(
            JSONPath[] paths,
            JSONPath prefix,
            JSONPath[] namePaths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            long features) {
        super(paths, prefix, namePaths, types, formats, pathFeatures, zoneId, features);

        JSONPathTwoSegment prefixTwo = (JSONPathTwoSegment) prefix;
        prefixName0 = ((JSONPathSegmentName) prefixTwo.first).name;
        prefixNameHash0 = ((JSONPathSegmentName) prefixTwo.first).nameHashCode;

        prefixName1 = ((JSONPathSegmentName) prefixTwo.second).name;
        prefixNameHash1 = ((JSONPathSegmentName) prefixTwo.second).nameHashCode;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader.nextIfNull()) {
            return new Object[paths.length];
        }

        if (!jsonReader.nextIfObjectStart()) {
            throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
        }

        while (true) {
            if (jsonReader.nextIfObjectEnd()) {
                return new Object[paths.length];
            }

            if (jsonReader.isEnd()) {
                throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
            }

            long nameHashCode = jsonReader.readFieldNameHashCode();
            boolean match = nameHashCode == prefixNameHash0;
            if (!match) {
                jsonReader.skipValue();
                continue;
            }

            break;
        }

        if (jsonReader.nextIfNull()) {
            return new Object[paths.length];
        }

        if (!jsonReader.nextIfObjectStart()) {
            throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
        }

        while (true) {
            if (jsonReader.nextIfObjectEnd()) {
                return new Object[paths.length];
            }

            if (jsonReader.isEnd()) {
                throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
            }

            long nameHashCode = jsonReader.readFieldNameHashCode();
            boolean match = nameHashCode == prefixNameHash1;
            if (!match) {
                jsonReader.skipValue();
                continue;
            }

            break;
        }

        if (jsonReader.nextIfNull()) {
            return new Object[paths.length];
        }

        if (!jsonReader.nextIfObjectStart()) {
            throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
        }

        Object[] values = new Object[paths.length];
        while (true) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            if (jsonReader.isEnd()) {
                throw new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
            }

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
