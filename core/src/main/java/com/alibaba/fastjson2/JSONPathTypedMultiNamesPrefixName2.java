package com.alibaba.fastjson2;

import java.lang.reflect.Type;
import java.time.ZoneId;

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
            throw error(jsonReader);
        }

        while (true) {
            if (jsonReader.nextIfObjectEnd()) {
                return new Object[paths.length];
            }

            if (jsonReader.isEnd()) {
                throw error(jsonReader);
            }

            boolean match = jsonReader.readFieldNameHashCode() == prefixNameHash0;
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
            throw error(jsonReader);
        }

        while (true) {
            if (jsonReader.nextIfObjectEnd()) {
                return new Object[paths.length];
            }

            if (jsonReader.isEnd()) {
                throw error(jsonReader);
            }

            boolean match = jsonReader.readFieldNameHashCode() == prefixNameHash1;
            if (!match) {
                jsonReader.skipValue();
                continue;
            }

            break;
        }

        if (jsonReader.nextIfNull()) {
            return new Object[paths.length];
        }

        return objectReader.readObject(jsonReader);
    }

    private static JSONException error(JSONReader jsonReader) {
        return new JSONException(jsonReader.info("illegal input, expect '[', but " + jsonReader.current()));
    }
}
