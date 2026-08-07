package com.alibaba.fastjson2;

import java.lang.reflect.Type;
import java.time.ZoneId;

public class JSONPathTypedMultiNamesPrefixName1
        extends JSONPathTypedMultiNames {
    final JSONPathSingleName prefixName;
    final long prefixNameHash;

    JSONPathTypedMultiNamesPrefixName1(
            JSONPath[] paths,
            JSONPath prefix,
            JSONPath[] namePaths,
            Type[] types,
            String[] formats,
            long[] pathFeatures,
            ZoneId zoneId,
            long features) {
        super(paths, prefix, namePaths, types, formats, pathFeatures, zoneId, features);
        prefixName = (JSONPathSingleName) prefix;
        prefixNameHash = prefixName.nameHashCode;
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
            boolean match = nameHashCode == prefixNameHash;
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

        return objectReader.readObject(jsonReader);
    }
}
