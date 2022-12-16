package com.alibaba.fastjson2;

final class JSONPathSingleNameLong
        extends JSONPathTyped {
    final long nameHashCode;
    final String name;

    public JSONPathSingleNameLong(JSONPathSingleName jsonPath) {
        super(jsonPath, Long.class);
        this.nameHashCode = jsonPath.nameHashCode;
        this.name = jsonPath.name;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader.isJSONB()) {
            if (jsonReader.isObject()) {
                jsonReader.nextIfObjectStart();
                while (!jsonReader.nextIfObjectEnd()) {
                    long nameHashCode = jsonReader.readFieldNameHashCode();
                    if (nameHashCode == 0) {
                        continue;
                    }

                    boolean match = nameHashCode == this.nameHashCode;
                    if (!match && (!jsonReader.isObject()) && !jsonReader.isArray()) {
                        jsonReader.skipValue();
                        continue;
                    }

                    return jsonReader.readInt64();
                }
            }
        } else if (jsonReader.nextIfObjectStart()) {
            while (!jsonReader.nextIfObjectEnd()) {
                long nameHashCode = jsonReader.readFieldNameHashCode();
                boolean match = nameHashCode == this.nameHashCode;

                if (!match) {
                    jsonReader.skipValue();
                    continue;
                }

                return jsonReader.readInt64();
            }
        }

        return null;
    }
}
