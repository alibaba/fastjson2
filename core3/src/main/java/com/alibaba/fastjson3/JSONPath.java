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

        return new CompiledPath(segments, definite);
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
        Object root = JSON.parse(json);
        return eval(root);
    }

    /**
     * Parse JSON string and evaluate this path with type conversion.
     */
    public <T> T extract(String json, Class<T> type) {
        Object root = JSON.parse(json);
        return eval(root, type);
    }

    /**
     * Parse UTF-8 JSON bytes and evaluate this path with type conversion.
     */
    public <T> T extract(byte[] jsonBytes, Class<T> type) {
        Object root = JSON.parse(new String(jsonBytes, java.nio.charset.StandardCharsets.UTF_8));
        return eval(root, type);
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

        SingleNamePath(String name) {
            super(true);
            this.name = name;
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
    }

    // ==================== Specialized: $.a.b ====================

    private static final class TwoNamePath extends JSONPath {
        private final String name1;
        private final String name2;

        TwoNamePath(String name1, String name2) {
            super(true);
            this.name1 = name1;
            this.name2 = name2;
        }

        @Override
        public Object eval(Object root) {
            Object v1;
            if (root instanceof JSONObject obj) {
                v1 = obj.get(name1);
            } else if (root instanceof Map<?, ?> map) {
                v1 = map.get(name1);
            } else {
                return null;
            }
            if (v1 instanceof JSONObject obj2) {
                return obj2.get(name2);
            }
            if (v1 instanceof Map<?, ?> map2) {
                return map2.get(name2);
            }
            return null;
        }
    }

    // ==================== General compiled path ====================

    private static final class CompiledPath extends JSONPath {
        private final JSONPathSegment[] segments;

        CompiledPath(JSONPathSegment[] segments, boolean definite) {
            super(definite);
            this.segments = segments;
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
    }
}
