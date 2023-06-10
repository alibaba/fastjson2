package com.alibaba.fastjson2;

import java.util.*;

final class JSONPathSingleIndex
        extends JSONPathSingle {
    final JSONPathSegmentIndex segment;
    final int index;

    public JSONPathSingleIndex(String path, JSONPathSegmentIndex segment) {
        super(segment, path);
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
}
