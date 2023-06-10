package com.alibaba.fastjson2;

import java.util.List;

final class JSONPathMulti
        extends JSONPath {
    final List<JSONPathSegment> segments;
    final boolean ref;
    final boolean extractSupport;

    JSONPathMulti(String path, List<JSONPathSegment> segments) {
        super(path);
        this.segments = segments;

        boolean extractSupport = true;
        boolean ref = true;
        for (int i = 0, l = segments.size(); i < l; i++) {
            JSONPathSegment segment = segments.get(i);
            if (segment instanceof JSONPathSegmentIndex) {
                if (((JSONPathSegmentIndex) segment).index < 0) {
                    extractSupport = false;
                }
                continue;
            }
            if (segment instanceof JSONPathSegmentName) {
                continue;
            }
            ref = false;
            break;
        }
        this.extractSupport = extractSupport;
        this.ref = ref;
    }

    @Override
    public boolean isRef() {
        return ref;
    }

    @Override
    public Object eval(Object root) {
        Context context = null;

        int size = segments.size();
        if (size == 0) {
            return root;
        }

        for (int i = 0; i < size; i++) {
            JSONPathSegment segment = segments.get(i);
            JSONPathSegment nextSegment = null;
            int nextIndex = i + 1;
            if (nextIndex < size) {
                nextSegment = segments.get(nextIndex);
            }
            context = new Context(this, context, segment, nextSegment, 0);
            if (i == 0) {
                context.root = root;
            }

            segment.eval(context);
        }

        return context.value;
    }
}
