package com.alibaba.fastjson2;

import java.util.ArrayList;
import java.util.List;

public class JSONPath {
    static final JSONPath ROOT = new JSONPath("$", new ArrayList<>(), true, false);
    static final JSONPath PREVIOUS = new JSONPath("#-1", new ArrayList<>(), false, true);
    static final JSONReader.Context PARSE_CONTEXT = JSONFactory.createReadContext();

    JSONReader.Context readerContext;
    JSONWriter.Context writerContext;
    final String path;
    final List<JSONPathSegment> segments;
    final boolean root;
    public final boolean previous;

    protected JSONPath(String path, List<JSONPathSegment> segments, boolean root, boolean previous) {
        this.path = path;
        this.segments = segments;
        this.root = root;
        this.previous = previous;
    }

    @Override
    public final String toString() {
        return path;
    }

    public Object eval(Object root) {
        if (this.root) {
            return root;
        }

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

    public static JSONPath of(String path) {
        if ("#-1".equals(path)) {
            return JSONPath.PREVIOUS;
        }

        return new JSONPathParser(path)
                .parse();
    }

    static final class Context {
        final JSONPath path;
        final Context parent;
        final JSONPathSegment current;
        final JSONPathSegment next;
        final long readerFeatures;
        Object root;
        Object value;

        boolean eval;

        Context(JSONPath path, Context parent, JSONPathSegment current, JSONPathSegment next, long readerFeatures) {
            this.path = path;
            this.current = current;
            this.next = next;
            this.parent = parent;
            this.readerFeatures = readerFeatures;
        }
    }
}
