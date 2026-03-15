package com.alibaba.fastjson3;

import com.alibaba.fastjson3.jsonpath.JSONPathCompiler;
import com.alibaba.fastjson3.jsonpath.JSONPathContext;
import com.alibaba.fastjson3.jsonpath.JSONPathSegment;
import com.alibaba.fastjson3.reader.FieldNameMatcher;
import com.alibaba.fastjson3.reader.FieldReader;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Compile multiple JSONPath expressions with target types for typed multi-path extraction.
     * Returns a JSONPath that evaluates all paths in a single pass and returns {@code Object[]}.
     *
     * <p>The factory analyzes path structures and selects an optimized extraction strategy:
     * <ul>
     *   <li>All single names ({@code $.f0, $.f1}): single-pass object field scan</li>
     *   <li>All single indexes ({@code $[0], $[1]}): single-pass array scan</li>
     *   <li>Same-prefix names ({@code $.data.f0, $.data.f1}): navigate to prefix, then single-pass scan</li>
     *   <li>Same-prefix index+names ({@code $[0].f0, $[0].f1}): skip to index, then single-pass scan</li>
     *   <li>Other patterns: evaluate each path independently</li>
     * </ul>
     *
     * <pre>
     * JSONPath path = JSONPath.of(
     *     new String[]{"$.id", "$.name"},
     *     new Type[]{Long.class, String.class}
     * );
     * Object[] result = (Object[]) path.extract("{\"id\":1,\"name\":\"test\"}");
     * // result = [1L, "test"]
     * </pre>
     *
     * @param paths JSONPath expressions
     * @param types target types for each path (must be same length as paths)
     * @return a compiled multi-path JSONPath
     */
    public static JSONPath of(String[] paths, Type[] types) {
        if (paths == null || types == null) {
            throw new JSONException("paths and types must not be null");
        }
        if (paths.length == 0) {
            throw new JSONException("paths must not be empty");
        }
        if (paths.length != types.length) {
            throw new JSONException("paths.length (" + paths.length + ") != types.length (" + types.length + ")");
        }

        JSONPath[] compiled = new JSONPath[paths.length];
        for (int i = 0; i < paths.length; i++) {
            compiled[i] = of(paths[i]);
        }

        return TypedMultiPath.create(compiled, types);
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

    /**
     * One-shot: set a value at the specified path on a root object.
     *
     * @param root the root object to modify (JSONObject, Map, etc.)
     * @param path the JSONPath expression (must be definite)
     * @param value the value to set
     */
    public static void set(Object root, String path, Object value) {
        of(path).set(root, value);
    }

    /**
     * One-shot: remove the value at the specified path from a root object.
     *
     * @param root the root object to modify (JSONObject, Map, etc.)
     * @param path the JSONPath expression (must be definite)
     * @return true if a value was removed
     */
    public static boolean remove(Object root, String path) {
        return of(path).remove(root);
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

    // ==================== Typed multi-path: extract multiple values ====================

    /**
     * Extracts multiple typed values from JSON in a single pass.
     * Immutable and thread-safe after construction.
     */
    static final class TypedMultiPath extends JSONPath {
        enum Strategy {
            ALL_SINGLE_NAME,
            ALL_SINGLE_INDEX,
            PREFIX_NAME_THEN_NAMES,
            PREFIX_INDEX_THEN_NAMES,
            PREFIX_NAME2_THEN_NAMES,
            GENERIC
        }

        private final JSONPath[] paths;
        private final Type[] types;
        private final Strategy strategy;

        // Strategy-specific fields (only populated for matching strategy)
        private final String[] names;
        private final Map<String, int[]> nameToIndices; // name -> result indices (handles duplicates, tree mode)
        private final FieldNameMatcher fieldNameMatcher; // hash-based field matching (zero String alloc)
        // fieldReaders[ordinal] -> int[] of result indices for that field
        private final int[][] ordinalToIndices;
        // Primary type per ordinal for type-specific reads (avoids readAny + convertType)
        private final Type[] ordinalTypes;
        private final int[] indexes;
        private final int[][] indexToPositions; // indexToPositions[elementIndex] = result positions, null if not target
        private final int maxIndex;
        private final String prefixName;
        private final String prefixName2;
        private final int prefixIndex;

        private TypedMultiPath(
                JSONPath[] paths,
                Type[] types,
                Strategy strategy,
                String[] names,
                int[] indexes,
                int maxIndex,
                String prefixName,
                String prefixName2,
                int prefixIndex
        ) {
            super(true); // TypedMultiPath always returns a fixed-size Object[]
            this.paths = paths;
            this.types = types;
            this.strategy = strategy;
            this.names = names;
            this.indexes = indexes;
            this.maxIndex = maxIndex;
            this.prefixName = prefixName;
            this.prefixName2 = prefixName2;
            this.prefixIndex = prefixIndex;

            // Build name->indices lookup for name-based strategies
            if (names != null) {
                Map<String, int[]> map = new HashMap<>();
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    int[] existing = map.get(name);
                    if (existing == null) {
                        map.put(name, new int[]{i});
                    } else {
                        int[] expanded = new int[existing.length + 1];
                        System.arraycopy(existing, 0, expanded, 0, existing.length);
                        expanded[existing.length] = i;
                        map.put(name, expanded);
                    }
                }
                this.nameToIndices = map;

                // Build FieldNameMatcher for zero-alloc hash-based field matching in stream mode
                int uniqueCount = map.size();
                FieldReader[] readers = new FieldReader[uniqueCount];
                int[][] ordToIdx = new int[uniqueCount][];
                int ri = 0;
                for (Map.Entry<String, int[]> entry : map.entrySet()) {
                    readers[ri] = new FieldReader(
                            entry.getKey(), null, Object.class, Object.class,
                            ri, null, false, null, null
                    );
                    ordToIdx[ri] = entry.getValue();
                    ri++;
                }
                this.fieldNameMatcher = FieldNameMatcher.build(readers);
                this.ordinalToIndices = ordToIdx;
                // Precompute primary type per ordinal for type-specific reads
                Type[] oTypes = new Type[uniqueCount];
                for (int i = 0; i < uniqueCount; i++) {
                    // Use the type of the first index for this ordinal
                    oTypes[i] = types[ordToIdx[i][0]];
                }
                this.ordinalTypes = oTypes;
            } else {
                this.nameToIndices = null;
                this.fieldNameMatcher = null;
                this.ordinalToIndices = null;
                this.ordinalTypes = null;
            }

            // Build index->positions lookup for index-based strategies (no autoboxing)
            if (indexes != null && maxIndex >= 0) {
                int[][] imap = new int[maxIndex + 1][];
                for (int i = 0; i < indexes.length; i++) {
                    int key = indexes[i];
                    int[] existing = imap[key];
                    if (existing == null) {
                        imap[key] = new int[]{i};
                    } else {
                        int[] expanded = new int[existing.length + 1];
                        System.arraycopy(existing, 0, expanded, 0, existing.length);
                        expanded[existing.length] = i;
                        imap[key] = expanded;
                    }
                }
                this.indexToPositions = imap;
            } else {
                this.indexToPositions = null;
            }
        }

        /**
         * Factory: analyze compiled paths and select optimal strategy.
         */
        static TypedMultiPath create(JSONPath[] compiled, Type[] types) {
            int len = compiled.length;

            // Check if all paths are SingleNamePath
            boolean allSingleName = true;
            for (JSONPath p : compiled) {
                if (!(p instanceof SingleNamePath)) {
                    allSingleName = false;
                    break;
                }
            }
            if (allSingleName) {
                String[] names = new String[len];
                for (int i = 0; i < len; i++) {
                    names[i] = ((SingleNamePath) compiled[i]).name;
                }
                return new TypedMultiPath(compiled, types, Strategy.ALL_SINGLE_NAME,
                        names, null, -1, null, null, -1);
            }

            // Check if all paths are single positive index (CompiledPath with 1 IndexSegment)
            boolean allSingleIndex = true;
            int[] idxArr = new int[len];
            int maxIdx = -1;
            for (int i = 0; i < len; i++) {
                int idx = getSinglePositiveIndex(compiled[i]);
                if (idx < 0) {
                    allSingleIndex = false;
                    break;
                }
                idxArr[i] = idx;
                if (idx > maxIdx) {
                    maxIdx = idx;
                }
            }
            if (allSingleIndex) {
                return new TypedMultiPath(compiled, types, Strategy.ALL_SINGLE_INDEX,
                        null, idxArr, maxIdx, null, null, -1);
            }

            // Check if all paths are TwoNamePath with same first segment
            boolean allTwoNameSamePrefix = true;
            String firstPrefix = null;
            for (int i = 0; i < len; i++) {
                if (!(compiled[i] instanceof TwoNamePath tnp)) {
                    allTwoNameSamePrefix = false;
                    break;
                }
                if (i == 0) {
                    firstPrefix = tnp.name1;
                } else if (!firstPrefix.equals(tnp.name1)) {
                    allTwoNameSamePrefix = false;
                    break;
                }
            }
            if (allTwoNameSamePrefix && firstPrefix != null) {
                String[] names = new String[len];
                for (int i = 0; i < len; i++) {
                    names[i] = ((TwoNamePath) compiled[i]).name2;
                }
                return new TypedMultiPath(compiled, types, Strategy.PREFIX_NAME_THEN_NAMES,
                        names, null, -1, firstPrefix, null, -1);
            }

            // Check if all paths are CompiledPath with 2 segments: IndexSegment(same) + NameSegment
            boolean allPrefixIndexThenName = true;
            int commonIdx = -1;
            for (int i = 0; i < len; i++) {
                JSONPathSegment[] segs = getSegments(compiled[i]);
                if (segs == null || segs.length != 2
                        || !(segs[0] instanceof JSONPathSegment.IndexSegment is)
                        || is.index() < 0
                        || !(segs[1] instanceof JSONPathSegment.NameSegment)) {
                    allPrefixIndexThenName = false;
                    break;
                }
                if (i == 0) {
                    commonIdx = is.index();
                } else if (is.index() != commonIdx) {
                    allPrefixIndexThenName = false;
                    break;
                }
            }
            if (allPrefixIndexThenName && commonIdx >= 0) {
                String[] names = new String[len];
                for (int i = 0; i < len; i++) {
                    JSONPathSegment[] segs = getSegments(compiled[i]);
                    names[i] = ((JSONPathSegment.NameSegment) segs[1]).name();
                }
                return new TypedMultiPath(compiled, types, Strategy.PREFIX_INDEX_THEN_NAMES,
                        names, null, -1, null, null, commonIdx);
            }

            // Check if all paths are CompiledPath with 3 segments: NameSegment(same) + NameSegment(same) + NameSegment
            boolean allPrefixName2ThenName = true;
            String prefix0 = null, prefix1 = null;
            for (int i = 0; i < len; i++) {
                JSONPathSegment[] segs = getSegments(compiled[i]);
                if (segs == null || segs.length != 3
                        || !(segs[0] instanceof JSONPathSegment.NameSegment ns0)
                        || !(segs[1] instanceof JSONPathSegment.NameSegment ns1)
                        || !(segs[2] instanceof JSONPathSegment.NameSegment)) {
                    allPrefixName2ThenName = false;
                    break;
                }
                if (i == 0) {
                    prefix0 = ns0.name();
                    prefix1 = ns1.name();
                } else if (!prefix0.equals(ns0.name()) || !prefix1.equals(ns1.name())) {
                    allPrefixName2ThenName = false;
                    break;
                }
            }
            if (allPrefixName2ThenName && prefix0 != null) {
                String[] names = new String[len];
                for (int i = 0; i < len; i++) {
                    JSONPathSegment[] segs = getSegments(compiled[i]);
                    names[i] = ((JSONPathSegment.NameSegment) segs[2]).name();
                }
                return new TypedMultiPath(compiled, types, Strategy.PREFIX_NAME2_THEN_NAMES,
                        names, null, -1, prefix0, prefix1, -1);
            }

            // Fallback: generic strategy
            return new TypedMultiPath(compiled, types, Strategy.GENERIC,
                    null, null, -1, null, null, -1);
        }

        /**
         * Get the single positive index from a path, or -1 if not applicable.
         */
        private static int getSinglePositiveIndex(JSONPath path) {
            JSONPathSegment[] segs = getSegments(path);
            if (segs != null && segs.length == 1
                    && segs[0] instanceof JSONPathSegment.IndexSegment is
                    && is.index() >= 0) {
                return is.index();
            }
            return -1;
        }

        /**
         * Get segments from a CompiledPath, or null if not a CompiledPath.
         */
        private static JSONPathSegment[] getSegments(JSONPath path) {
            if (path instanceof CompiledPath cp) {
                return cp.segments;
            }
            return null;
        }

        // ==================== Tree Mode ====================

        @Override
        public Object eval(Object root) {
            if (root == null) {
                return new Object[paths.length];
            }
            return switch (strategy) {
                case ALL_SINGLE_NAME -> evalMultiNames(root);
                case ALL_SINGLE_INDEX -> evalMultiIndexes(root);
                case PREFIX_NAME_THEN_NAMES -> {
                    Object child = getProperty(root, prefixName);
                    yield child != null ? evalMultiNames(child) : new Object[paths.length];
                }
                case PREFIX_INDEX_THEN_NAMES -> {
                    Object child = getListElement(root, prefixIndex);
                    yield child != null ? evalMultiNames(child) : new Object[paths.length];
                }
                case PREFIX_NAME2_THEN_NAMES -> {
                    Object child = getProperty(root, prefixName);
                    child = child != null ? getProperty(child, prefixName2) : null;
                    yield child != null ? evalMultiNames(child) : new Object[paths.length];
                }
                case GENERIC -> evalGeneric(root);
            };
        }

        private Object[] evalMultiNames(Object obj) {
            Object[] result = new Object[names.length];
            if (obj instanceof Map<?, ?> map) {
                for (Map.Entry<String, int[]> entry : nameToIndices.entrySet()) {
                    Object value = map.get(entry.getKey());
                    for (int idx : entry.getValue()) {
                        result[idx] = convertType(value, types[idx]);
                    }
                }
            }
            return result;
        }

        private Object[] evalMultiIndexes(Object obj) {
            Object[] result = new Object[indexes.length];
            if (obj instanceof List<?> list) {
                int size = list.size();
                for (int i = 0; i < indexes.length; i++) {
                    int idx = indexes[i];
                    if (idx < size) {
                        result[i] = convertType(list.get(idx), types[i]);
                    }
                }
            }
            return result;
        }

        private Object[] evalGeneric(Object root) {
            Object[] result = new Object[paths.length];
            for (int i = 0; i < paths.length; i++) {
                Object value = paths[i].eval(root);
                result[i] = convertType(value, types[i]);
            }
            return result;
        }

        // ==================== Stream Mode ====================

        @Override
        public Object extract(JSONParser parser) {
            return switch (strategy) {
                case ALL_SINGLE_NAME -> extractMultiNames(parser);
                case ALL_SINGLE_INDEX -> extractMultiIndexes(parser);
                case PREFIX_NAME_THEN_NAMES -> extractPrefixNameThenNames(parser);
                case PREFIX_INDEX_THEN_NAMES -> extractPrefixIndexThenNames(parser);
                case PREFIX_NAME2_THEN_NAMES -> extractPrefixName2ThenNames(parser);
                case GENERIC -> {
                    Object root = parser.readAny();
                    yield evalGeneric(root);
                }
            };
        }

        /**
         * Stream-mode: scan object fields, match against target names, read typed values.
         * Uses FieldNameMatcher for zero-allocation hash-based field matching.
         * Uses type-specific reads (readInt/readLong/readString) to avoid readAny + convertType.
         * Single-pass with early termination when all names are found.
         */
        private Object[] extractMultiNames(JSONParser parser) {
            Object[] result = new Object[names.length];
            parser.skipWS();
            int off = parser.getOffset();
            if (off >= parser.getEnd() || parser.charAt(off) != '{') {
                return result;
            }
            parser.advance(1);

            int uniqueCount = ordinalToIndices.length;
            int remaining = uniqueCount;
            long foundBits = 0;
            parser.skipWS();
            while (remaining > 0 && parser.getOffset() < parser.getEnd()
                    && parser.charAt(parser.getOffset()) != '}') {
                long hash = parser.readFieldNameHash(fieldNameMatcher);
                FieldReader fr = fieldNameMatcher.matchFlat(hash);
                if (fr != null) {
                    int ordinal = fr.ordinal;
                    int[] indices = ordinalToIndices[ordinal];
                    // Type-specific read based on primary target type
                    Object value = readTyped(parser, ordinalTypes[ordinal]);
                    if (indices.length == 1) {
                        // Fast path: single index, no conversion needed (readTyped returns correct type)
                        result[indices[0]] = value;
                    } else {
                        // Duplicate paths with potentially different types
                        for (int idx : indices) {
                            result[idx] = convertType(value, types[idx]);
                        }
                    }
                    // Early termination tracking
                    if (uniqueCount <= 64) {
                        long bit = 1L << ordinal;
                        if ((foundBits & bit) == 0) {
                            foundBits |= bit;
                            remaining--;
                        }
                    }
                } else {
                    parser.skipValue();
                }
                parser.skipWS();
                if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ',') {
                    parser.advance(1);
                    parser.skipWS();
                }
            }
            return result;
        }

        /**
         * Read a JSON value with type-specific parsing to avoid readAny + convertType overhead.
         * Only uses type-specific reads when the JSON token matches the target type.
         * Falls back to readAny + convertType for mismatched or complex types.
         */
        private static Object readTyped(JSONParser parser, Type type) {
            if (type instanceof Class<?> clazz) {
                parser.skipWS();
                int off = parser.getOffset();
                if (off < parser.getEnd()) {
                    int c = parser.charAt(off);
                    // Null check
                    if (c == 'n') {
                        if (parser.readNull()) {
                            return null;
                        }
                    }
                    // Type-specific reads only when JSON token matches expected type
                    if (c == '"') {
                        // JSON string value
                        if (clazz == String.class) {
                            return parser.readString();
                        }
                    } else if (c == '-' || (c >= '0' && c <= '9')) {
                        // JSON number value
                        if (clazz == int.class || clazz == Integer.class) {
                            return parser.readInt();
                        }
                        if (clazz == long.class || clazz == Long.class) {
                            return parser.readLong();
                        }
                        if (clazz == double.class || clazz == Double.class) {
                            return parser.readDouble();
                        }
                    } else if (c == 't' || c == 'f') {
                        // JSON boolean value
                        if (clazz == boolean.class || clazz == Boolean.class) {
                            return parser.readBoolean();
                        }
                    }
                }
            }
            // Fallback: generic read + type conversion
            Object value = parser.readAny();
            return convertType(value, type);
        }

        /**
         * Stream-mode: scan array elements, read typed values at target indexes.
         */
        private Object[] extractMultiIndexes(JSONParser parser) {
            Object[] result = new Object[indexes.length];
            parser.skipWS();
            int off = parser.getOffset();
            if (off >= parser.getEnd() || parser.charAt(off) != '[') {
                return result;
            }
            parser.advance(1);

            for (int i = 0; i <= maxIndex; i++) {
                parser.skipWS();
                if (parser.getOffset() >= parser.getEnd() || parser.charAt(parser.getOffset()) == ']') {
                    break;
                }
                int[] positions = indexToPositions[i];
                if (positions != null) {
                    Object value = parser.readAny();
                    for (int pos : positions) {
                        result[pos] = convertType(value, types[pos]);
                    }
                } else {
                    parser.skipValue();
                }
                parser.skipWS();
                if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ',') {
                    parser.advance(1);
                }
            }
            return result;
        }

        /**
         * Stream-mode: navigate to prefix name field, then extract multi names.
         */
        private Object[] extractPrefixNameThenNames(JSONParser parser) {
            if (!navigateToField(parser, prefixName)) {
                return new Object[names.length];
            }
            return extractMultiNames(parser);
        }

        /**
         * Stream-mode: navigate to array index, then extract multi names.
         */
        private Object[] extractPrefixIndexThenNames(JSONParser parser) {
            if (!navigateToIndex(parser, prefixIndex)) {
                return new Object[names.length];
            }
            return extractMultiNames(parser);
        }

        /**
         * Stream-mode: navigate through two prefix name fields, then extract multi names.
         */
        private Object[] extractPrefixName2ThenNames(JSONParser parser) {
            if (!navigateToField(parser, prefixName)) {
                return new Object[names.length];
            }
            if (!navigateToField(parser, prefixName2)) {
                return new Object[names.length];
            }
            return extractMultiNames(parser);
        }

        /**
         * Navigate parser to the value of a named field within the current object.
         * Returns true if the field was found and parser is positioned at its value.
         */
        private static boolean navigateToField(JSONParser parser, String targetName) {
            parser.skipWS();
            int off = parser.getOffset();
            if (off >= parser.getEnd() || parser.charAt(off) != '{') {
                return false;
            }
            parser.advance(1);
            parser.skipWS();

            while (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) != '}') {
                String fieldName = parser.readFieldName();
                if (targetName.equals(fieldName)) {
                    return true; // parser is now positioned at the field's value
                }
                parser.skipValue();
                parser.skipWS();
                if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ',') {
                    parser.advance(1);
                    parser.skipWS();
                }
            }
            return false; // field not found
        }

        /**
         * Navigate parser to the element at a given positive index in the current array.
         * Returns true if the element was found and parser is positioned at its value.
         */
        private static boolean navigateToIndex(JSONParser parser, int targetIndex) {
            parser.skipWS();
            int off = parser.getOffset();
            if (off >= parser.getEnd() || parser.charAt(off) != '[') {
                return false;
            }
            parser.advance(1);

            for (int i = 0; i <= targetIndex; i++) {
                parser.skipWS();
                if (parser.getOffset() >= parser.getEnd() || parser.charAt(parser.getOffset()) == ']') {
                    return false; // array too short
                }
                if (i == targetIndex) {
                    return true; // parser positioned at target element
                }
                parser.skipValue();
                parser.skipWS();
                if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ',') {
                    parser.advance(1);
                }
            }
            return false;
        }

        // ==================== Type conversion ====================

        private static Object convertType(Object value, Type type) {
            if (value == null || type == null || type == Object.class) {
                return value;
            }
            if (type instanceof Class<?> clazz) {
                if (clazz.isInstance(value)) {
                    return value;
                }
                if (value instanceof Number n) {
                    if (clazz == int.class || clazz == Integer.class) {
                        return n.intValue();
                    }
                    if (clazz == long.class || clazz == Long.class) {
                        return n.longValue();
                    }
                    if (clazz == double.class || clazz == Double.class) {
                        return n.doubleValue();
                    }
                    if (clazz == float.class || clazz == Float.class) {
                        return n.floatValue();
                    }
                    if (clazz == short.class || clazz == Short.class) {
                        return n.shortValue();
                    }
                    if (clazz == byte.class || clazz == Byte.class) {
                        return n.byteValue();
                    }
                    if (clazz == BigDecimal.class) {
                        if (n instanceof BigDecimal) {
                            return n;
                        }
                        if (n instanceof BigInteger bi) {
                            return new BigDecimal(bi);
                        }
                        if (n instanceof Long || n instanceof Integer) {
                            return BigDecimal.valueOf(n.longValue());
                        }
                        return new BigDecimal(n.toString());
                    }
                    if (clazz == BigInteger.class) {
                        if (n instanceof BigInteger) {
                            return n;
                        }
                        if (n instanceof BigDecimal bd) {
                            return bd.toBigInteger();
                        }
                        return BigInteger.valueOf(n.longValue());
                    }
                    if (clazz == String.class) {
                        return n.toString();
                    }
                }
                if (clazz == String.class) {
                    return value.toString();
                }
                if (value instanceof String s) {
                    if (clazz == int.class || clazz == Integer.class) {
                        return Integer.parseInt(s);
                    }
                    if (clazz == long.class || clazz == Long.class) {
                        return Long.parseLong(s);
                    }
                    if (clazz == double.class || clazz == Double.class) {
                        return Double.parseDouble(s);
                    }
                    if (clazz == float.class || clazz == Float.class) {
                        return Float.parseFloat(s);
                    }
                    if (clazz == short.class || clazz == Short.class) {
                        return Short.parseShort(s);
                    }
                    if (clazz == byte.class || clazz == Byte.class) {
                        return Byte.parseByte(s);
                    }
                    if (clazz == BigDecimal.class) {
                        return new BigDecimal(s);
                    }
                    if (clazz == BigInteger.class) {
                        return new BigInteger(s);
                    }
                    if (clazz == boolean.class || clazz == Boolean.class) {
                        return Boolean.parseBoolean(s);
                    }
                }
            }
            return value;
        }

        // ==================== Helpers ====================

        private static Object getProperty(Object obj, String name) {
            if (obj instanceof JSONObject o) {
                return o.get(name);
            }
            if (obj instanceof Map<?, ?> m) {
                return m.get(name);
            }
            return null;
        }

        private static Object getListElement(Object obj, int index) {
            if (obj instanceof List<?> list) {
                return index < list.size() ? list.get(index) : null;
            }
            return null;
        }
    }
}
