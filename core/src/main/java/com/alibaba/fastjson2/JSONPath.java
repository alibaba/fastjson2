package com.alibaba.fastjson2;

public abstract class JSONPath {
    static final JSONReader.Context PARSE_CONTEXT = JSONFactory.createReadContext();

    JSONReader.Context readerContext;
    JSONWriter.Context writerContext;
    final String path;

    protected JSONPath(String path) {
        this.path = path;
    }

    public boolean isPrevious() {
        return false;
    }

    @Override
    public final String toString() {
        return path;
    }

    public abstract boolean isRef();

    public abstract Object eval(Object object);

    public static JSONPath of(String path) {
        if ("#-1".equals(path)) {
            return PreviousPath.INSTANCE;
        }

        return new JSONPathParser(path)
                .parse();
    }

    static final class PreviousPath
            extends JSONPath {
        static final PreviousPath INSTANCE = new PreviousPath("#-1");

        PreviousPath(String path) {
            super(path);
        }

        @Override
        public boolean isRef() {
            throw new JSONException("unsupported operation");
        }

        @Override
        public boolean isPrevious() {
            return true;
        }

        @Override
        public Object eval(Object rootObject) {
            throw new JSONException("unsupported operation");
        }
    }

    static final class RootPath
            extends JSONPath {
        static final RootPath INSTANCE = new RootPath();

        protected RootPath() {
            super("$");
        }

        @Override
        public boolean isRef() {
            return true;
        }

        @Override
        public Object eval(Object object) {
            return object;
        }
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
