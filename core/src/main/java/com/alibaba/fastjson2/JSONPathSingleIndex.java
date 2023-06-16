package com.alibaba.fastjson2;

import java.util.*;

final class JSONPathSingleIndex
        extends JSONPathSingle {
    final JSONPathSegmentIndex segment;
    final int index;

    public JSONPathSingleIndex(String path, JSONPathSegmentIndex segment, Feature... features) {
        super(segment, path, features);
        this.segment = segment;
        this.index = segment.index;
    }

    @Override
    public Object eval(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof java.util.List) {
            Object value = null;
            List list = (List) object;
            if (index < list.size()) {
                value = list.get(index);
            }
            return value;
        }

        Context context = new Context(this, null, segment, null, 0);
        context.root = object;
        segment.eval(context);
        return context.value;
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        int max = jsonReader.startArray();
        if (jsonReader.isJSONB() && index >= max) {
            return null;
        }

        if ((!jsonReader.isJSONB()) && jsonReader.nextIfArrayEnd()) {
            return null;
        }

        for (int i = 0; i < index && i < max; i++) {
            jsonReader.skipValue();
            if ((!jsonReader.isJSONB()) && jsonReader.nextIfArrayEnd()) {
                return null;
            }
        }
        return jsonReader.readAny();
    }
}
