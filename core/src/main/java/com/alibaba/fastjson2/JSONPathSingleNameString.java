package com.alibaba.fastjson2;

final class JSONPathSingleNameString
        extends JSONPathTyped {
    final long nameHashCode;
    final String name;

    public JSONPathSingleNameString(JSONPathSingleName jsonPath) {
        super(jsonPath, String.class);
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

                    return jsonReader.readString();
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

                return jsonReader.readString();
            }
        }

        return null;
    }
}
