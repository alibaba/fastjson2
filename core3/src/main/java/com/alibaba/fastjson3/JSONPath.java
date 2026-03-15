package com.alibaba.fastjson3;

import com.alibaba.fastjson3.jsonpath.JSONPathCompiler;
import com.alibaba.fastjson3.jsonpath.JSONPathContext;
import com.alibaba.fastjson3.jsonpath.JSONPathSegment;

import java.util.Map;

/**
 * JSONPath query engine for fastjson3 (RFC 9535 compatible).
 *
 * <p>Compiles path expressions into reusable, thread-safe query objects.
 * Supports tree-mode evaluation on JSONObject/JSONArray and streaming extraction
 * from JSON strings/bytes.</p>
 *
 * <h3>Basic usage:</h3>
 * <pre>
 * // Compile once, reuse many times
 * JSONPath path = JSONPath.of("$.store.book[0].title");
 *
 * // Evaluate on a parsed object
 * String title = path.eval(jsonObject, String.class);
 *
 * // Extract directly from JSON string (parse + eval)
 * String title = path.extract("{...}", String.class);
 *
 * // Static convenience
 * String title = JSONPath.eval("{...}", "$.store.book[0].title", String.class);
 * </pre>
 *
 * <h3>Supported syntax (RFC 9535):</h3>
 * <pre>
 * $                     root
 * $.name  or $['name']  child property
 * $[0], $[-1]           array index
 * $[*]                  wildcard
 * $[1:3], $[::2]        array slice
 * $..name               recursive descent
 * $[?&#64;.price &lt; 10]    filter expression
 * </pre>
 */
public abstract sealed class JSONPath {
    final boolean definite;

    JSONPath(boolean definite) {
        this.definite = definite;
    }

    // ==================== Factory ====================

    /**
     * Compile a JSONPath expression. The returned object is immutable and thread-safe.
     * Users should cache compiled paths for repeated use.
     *
     * @param path the JSONPath expression (e.g., "$.store.book[*].author")
     * @return a compiled JSONPath
     */
    public static JSONPath of(String path) {
        if ("$".equals(path)) {
            return RootPath.INSTANCE;
        }

        JSONPathCompiler.CompileResult result = JSONPathCompiler.compile(path);
        JSONPathSegment[] segments = result.segments();
        boolean definite = result.definite();

        // Specialization for common patterns
        if (segments.length == 1 && segments[0] instanceof JSONPathSegment.NameSegment ns) {
            return new SingleNamePath(ns.name());
        }
        if (segments.length == 2
                && segments[0] instanceof JSONPathSegment.NameSegment ns1
                && segments[1] instanceof JSONPathSegment.NameSegment ns2) {
            return new TwoNamePath(ns1.name(), ns2.name());
        }

        return new CompiledPath(segments, definite, result.streamable());
    }

    // ==================== Evaluation ====================

    /**
     * Evaluate this path on a root object (JSONObject, JSONArray, or Map/List).
     * For definite paths: returns a single value or null.
     * For indefinite paths: returns a List of matched values.
     */
    public abstract Object eval(Object root);

    /**
     * Evaluate with type conversion.
     */
    @SuppressWarnings("unchecked")
    public <T> T eval(Object root, Class<T> type) {
        Object result = eval(root);
        if (result == null) {
            return null;
        }
        if (type.isInstance(result)) {
            return type.cast(result);
        }
        // Numeric conversions
        if (result instanceof Number n) {
            if (type == int.class || type == Integer.class) {
                return (T) Integer.valueOf(n.intValue());
            }
            if (type == long.class || type == Long.class) {
                return (T) Long.valueOf(n.longValue());
            }
            if (type == double.class || type == Double.class) {
                return (T) Double.valueOf(n.doubleValue());
            }
            if (type == String.class) {
                return (T) n.toString();
            }
        }
        if (type == String.class) {
            return (T) result.toString();
        }
        return (T) result;
    }

    /**
     * Whether this path is definite (always returns a single value).
     * Definite paths: $.name, $[0], $.a.b
     * Indefinite paths: $[*], $..name, $[?...], $[0,1]
     */
    public boolean isDefinite() {
        return definite;
    }

    // ==================== Convenience: parse + eval ====================

    /**
     * Parse JSON string and evaluate this path.
     */
    public Object extract(String json) {
        try (JSONParser parser = JSONParser.of(json)) {
            return extract(parser);
        }
    }

    /**
     * Parse JSON string and evaluate this path with type conversion.
     */
    public <T> T extract(String json, Class<T> type) {
        Object result = extract(json);
        return convertResult(result, type);
    }

    /**
     * Parse UTF-8 JSON bytes and evaluate this path with type conversion.
     */
    public <T> T extract(byte[] jsonBytes, Class<T> type) {
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            Object result = extract(parser);
            return convertResult(result, type);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertResult(Object result, Class<T> type) {
        if (result == null) {
            return null;
        }
        if (type.isInstance(result)) {
            return type.cast(result);
        }
        if (result instanceof Number n) {
            if (type == int.class || type == Integer.class) {
                return (T) Integer.valueOf(n.intValue());
            }
            if (type == long.class || type == Long.class) {
                return (T) Long.valueOf(n.longValue());
            }
            if (type == double.class || type == Double.class) {
                return (T) Double.valueOf(n.doubleValue());
            }
        }
        if (type == String.class) {
            return (T) result.toString();
        }
        return (T) result;
    }

    // ==================== Modification ====================

    /**
     * Set a value at this path location. Only works for definite paths.
     */
    public void set(Object root, Object value) {
        throw new JSONException("set not supported for this path type");
    }

    /**
     * Remove the value at this path location. Only works for definite paths.
     *
     * @return true if a value was removed
     */
    public boolean remove(Object root) {
        throw new JSONException("remove not supported for this path type");
    }

    // ==================== Stream Mode ====================

    /**
     * Stream Mode: extract directly from a JSONParser without building a full tree.
     * Only works for simple definite paths (e.g., $.a.b[0].c).
     * Falls back to Tree Mode for complex/indefinite paths.
     */
    public Object extract(JSONParser parser) {
        // Default: Tree Mode fallback
        Object root = parser.readAny();
        return eval(root);
    }

    // ==================== Static convenience ====================

    /**
     * One-shot: compile path, parse JSON, evaluate, return typed result.
     */
    public static <T> T eval(String json, String path, Class<T> type) {
        return of(path).extract(json, type);
    }

    /**
     * One-shot: compile path, evaluate on object, return typed result.
     */
    public static <T> T eval(Object root, String path, Class<T> type) {
        return of(path).eval(root, type);
    }

    // ==================== Root path: $ ====================

    private static final class RootPath extends JSONPath {
        static final RootPath INSTANCE = new RootPath();

        RootPath() {
            super(true);
        }

        @Override
        public Object eval(Object root) {
            return root;
        }
    }

    // ==================== Specialized: $.name ====================

    private static final class SingleNamePath extends JSONPath {
        private final String name;
        private final JSONPathSegment.NameSegment segment;
        private static final JSONPathSegment[] EMPTY = {};

        SingleNamePath(String name) {
            super(true);
            this.name = name;
            this.segment = new JSONPathSegment.NameSegment(name);
        }

        @Override
        public Object eval(Object root) {
            if (root instanceof JSONObject obj) {
                return obj.get(name);
            }
            if (root instanceof Map<?, ?> map) {
                return map.get(name);
            }
            return null;
        }

        @Override
        public Object extract(JSONParser parser) {
            return segment.extract(parser, EMPTY, 0);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(Object root, Object value) {
            if (root instanceof Map map) {
                map.put(name, value);
            }
        }

        @Override
        public boolean remove(Object root) {
            if (root instanceof Map<?, ?> map) {
                return map.remove(name) != null || map.containsKey(name);
            }
            return false;
        }
    }

    // ==================== Specialized: $.a.b ====================

    private static final class TwoNamePath extends JSONPath {
        private final String name1;
        private final String name2;
        private final JSONPathSegment[] segments;

        TwoNamePath(String name1, String name2) {
            super(true);
            this.name1 = name1;
            this.name2 = name2;
            this.segments = new JSONPathSegment[]{
                    new JSONPathSegment.NameSegment(name1),
                    new JSONPathSegment.NameSegment(name2)
            };
        }

        @Override
        public Object eval(Object root) {
            Object v1 = getProperty(root, name1);
            return v1 != null ? getProperty(v1, name2) : null;
        }

        @Override
        public Object extract(JSONParser parser) {
            return segments[0].extract(parser, segments, 1);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(Object root, Object value) {
            Object parent = getProperty(root, name1);
            if (parent instanceof Map map) {
                map.put(name2, value);
            }
        }

        @Override
        public boolean remove(Object root) {
            Object parent = getProperty(root, name1);
            if (parent instanceof Map<?, ?> map) {
                return map.remove(name2) != null;
            }
            return false;
        }

        private static Object getProperty(Object obj, String name) {
            if (obj instanceof JSONObject o) {
                return o.get(name);
            }
            if (obj instanceof Map<?, ?> m) {
                return m.get(name);
            }
            return null;
        }
    }

    // ==================== General compiled path ====================

    private static final class CompiledPath extends JSONPath {
        private final JSONPathSegment[] segments;
        private final boolean streamable;

        CompiledPath(JSONPathSegment[] segments, boolean definite, boolean streamable) {
            super(definite);
            this.segments = segments;
            this.streamable = streamable;
        }

        @Override
        public Object eval(Object root) {
            if (root == null) {
                return null;
            }
            JSONPathContext ctx = new JSONPathContext(root, segments, definite);
            if (segments.length > 0) {
                ctx.segmentIndex = 0;
                segments[0].eval(ctx);
            }
            return ctx.getResult();
        }

        @Override
        public Object extract(JSONParser parser) {
            if (streamable) {
                return segments[0].extract(parser, segments, 1);
            }
            // Fall back to Tree Mode
            Object root = parser.readAny();
            return eval(root);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(Object root, Object value) {
            if (!definite || segments.length == 0) {
                throw new JSONException("set only supported for definite paths");
            }
            // Navigate to parent, then set on last segment
            Object parent = navigateToParent(root);
            if (parent == null) {
                return;
            }
            JSONPathSegment last = segments[segments.length - 1];
            if (last instanceof JSONPathSegment.NameSegment ns && parent instanceof Map map) {
                map.put(ns.name(), value);
            } else if (last instanceof JSONPathSegment.IndexSegment is && parent instanceof java.util.List list) {
                int idx = is.index() >= 0 ? is.index() : list.size() + is.index();
                if (idx >= 0 && idx < list.size()) {
                    list.set(idx, value);
                }
            }
        }

        @Override
        public boolean remove(Object root) {
            if (!definite || segments.length == 0) {
                throw new JSONException("remove only supported for definite paths");
            }
            Object parent = navigateToParent(root);
            if (parent == null) {
                return false;
            }
            JSONPathSegment last = segments[segments.length - 1];
            if (last instanceof JSONPathSegment.NameSegment ns && parent instanceof Map<?, ?> map) {
                return map.remove(ns.name()) != null;
            }
            if (last instanceof JSONPathSegment.IndexSegment is && parent instanceof java.util.List<?> list) {
                int idx = is.index() >= 0 ? is.index() : list.size() + is.index();
                if (idx >= 0 && idx < list.size()) {
                    list.remove(idx);
                    return true;
                }
            }
            return false;
        }

        /**
         * Navigate to the parent of the last segment (for set/remove).
         * Evaluates segments[0..n-2] and returns the result.
         */
        private Object navigateToParent(Object root) {
            if (segments.length == 1) {
                return root;
            }
            // Create a sub-path with all but last segment
            JSONPathSegment[] parentSegments = java.util.Arrays.copyOf(segments, segments.length - 1);
            JSONPathContext ctx = new JSONPathContext(root, parentSegments, true);
            if (parentSegments.length > 0) {
                ctx.segmentIndex = 0;
                parentSegments[0].eval(ctx);
            }
            return ctx.getResult();
        }
    }
}
