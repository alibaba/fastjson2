package com.alibaba.fastjson2;

import java.lang.reflect.Type;
import java.time.ZoneId;

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

        int size = jsonReader.startArray();
        if (jsonReader.jsonb && index >= size) {
            return new Object[paths.length];
        }

        for (int i = 0; i < index; ++i) {
            if (!jsonReader.jsonb && jsonReader.nextIfArrayEnd()) {
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

        if (!jsonReader.jsonb && jsonReader.nextIfArrayEnd()) {
            return new Object[paths.length];
        }

        return objectReader.readObject(jsonReader);
    }
}
