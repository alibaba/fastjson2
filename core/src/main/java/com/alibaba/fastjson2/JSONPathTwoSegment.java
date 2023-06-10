package com.alibaba.fastjson2;

final class JSONPathTwoSegment
        extends JSONPath {
    final JSONPathSegment first;
    final JSONPathSegment second;
    final boolean ref;

    JSONPathTwoSegment(String path, JSONPathSegment first, JSONPathSegment second) {
        super(path);
        this.first = first;
        this.second = second;
        this.ref = (first instanceof JSONPathSegmentIndex || first instanceof JSONPathSegmentName)
                && (second instanceof JSONPathSegmentIndex || second instanceof JSONPathSegmentName);
    }

    @Override
    public boolean isRef() {
        return ref;
    }

    @Override
    public Object eval(Object root) {
        Context context0 = new Context(this, null, first, second, 0);
        context0.root = root;
        first.eval(context0);
        if (context0.value == null) {
            return null;
        }

        Context context1 = new Context(this, context0, second, null, 0);
        second.eval(context1);
        return context1.value;
    }
}
