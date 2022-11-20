package com.alibaba.fastjson2;

import java.util.function.BiFunction;

class JSONPathSingle
        extends JSONPath {
    final JSONPathSegment segment;
    final boolean ref;

    JSONPathSingle(JSONPathSegment segment, String path, Feature... features) {
        super(path, features);
        this.segment = segment;
        this.ref = segment instanceof JSONPathSegmentIndex || segment instanceof JSONPathSegmentName;
    }

    JSONPathSingle(JSONPathSegment segment, String path, long features) {
        super(path, features);
        this.segment = segment;
        this.ref = segment instanceof JSONPathSegmentIndex || segment instanceof JSONPathSegmentName;
    }

    @Override
    public boolean remove(Object root) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        return segment.remove(context);
    }

    @Override
    public boolean contains(Object root) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        return segment.contains(context);
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

    @Override
    public void set(Object root, Object value) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        segment.set(context, value);
    }

    @Override
    public void set(Object root, Object value, JSONReader.Feature... readerFeatures) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        segment.set(context, value);
    }

    @Override
    public void setCallback(Object root, BiFunction callback) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        segment.setCallback(context, callback);
    }

    @Override
    public void setInt(Object root, int value) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        segment.setInt(context, value);
    }

    @Override
    public void setLong(Object root, long value) {
        Context context = new Context(this, null, segment, null, 0);
        context.root = root;
        segment.setLong(context, value);
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        Context context = new Context(this, null, segment, null, 0);
        if (segment instanceof JSONPathSegment.EvalSegment) {
            context.root = jsonReader.readAny();
            segment.eval(context);
        } else {
            segment.accept(jsonReader, context);
        }
        return context.value;
    }

    @Override
    public String extractScalar(JSONReader jsonReader) {
        Context context = new Context(this, null, segment, null, 0);
        segment.accept(jsonReader, context);
        return JSON.toJSONString(context.value);
    }
}
