package com.alibaba.fastjson2;

class JSONPathSingle
        extends JSONPath {
    final JSONPathSegment segment;
    final boolean ref;
    final boolean extractSupport;

    JSONPathSingle(JSONPathSegment segment, String path) {
        super(path);
        this.segment = segment;
        this.ref = segment instanceof JSONPathSegmentIndex || segment instanceof JSONPathSegmentName;

        boolean extractSupport = !(segment instanceof JSONPathSegmentIndex) || ((JSONPathSegmentIndex) segment).index >= 0;
        this.extractSupport = extractSupport;
    }

    @Override
    public boolean isRef() {
        return ref;
    }

    @Override
    public Object eval(Object root) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        segment.eval(context);
        return context.value;
    }
}
