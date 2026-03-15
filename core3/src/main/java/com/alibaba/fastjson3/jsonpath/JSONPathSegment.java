package com.alibaba.fastjson3.jsonpath;

import com.alibaba.fastjson3.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * A segment in a compiled JSONPath expression. Each segment represents one step
 * in the path (e.g., property access, array index, wildcard).
 *
 * <p>Segments are evaluated sequentially: each reads from {@code ctx.current},
 * processes it, and passes results to the next segment via {@code evalNext}.</p>
 */
public sealed interface JSONPathSegment {
    /**
     * Evaluate this segment on the current context value (Tree Mode).
     * Results are either passed to the next segment or accumulated in ctx.results.
     */
    void eval(JSONPathContext ctx);

    /**
     * Extract a value from a JSONParser (Stream Mode).
     * Only supported for definite, forward segments (NameSegment, IndexSegment with positive index).
     * Returns null if not found. Falls back to Tree Mode for unsupported segments.
     *
     * @param parser    the JSON parser positioned at the start of a value
     * @param segments  the full segment array
     * @param nextIndex the index of the next segment to process
     * @return the extracted value, or null
     */
    default Object extract(com.alibaba.fastjson3.JSONParser parser,
                           JSONPathSegment[] segments, int nextIndex) {
        // Default: fall back to Tree Mode
        return null;
    }

    // ==================== Property name: $.name or $['name'] ====================

    record NameSegment(String name) implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            Object current = ctx.current;
            if (current instanceof JSONObject obj) {
                Object value = obj.get(name);
                if (value != null || obj.containsKey(name)) {
                    ctx.current = value;
                    ctx.evalNext();
                }
            } else if (current instanceof Map<?, ?> map) {
                Object value = map.get(name);
                if (value != null || map.containsKey(name)) {
                    ctx.current = value;
                    ctx.evalNext();
                }
            }
        }

        @Override
        public Object extract(com.alibaba.fastjson3.JSONParser parser,
                               JSONPathSegment[] segments, int nextIndex) {
            parser.skipWS();
            int off = parser.getOffset();
            if (off >= parser.getEnd() || parser.charAt(off) != '{') {
                return null;
            }
            parser.advance(1);

            // Scan fields until we find the target name
            parser.skipWS();
            while (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) != '}') {
                String fieldName = parser.readFieldName();
                if (name.equals(fieldName)) {
                    // Found — delegate to next segment or read value
                    if (nextIndex >= segments.length) {
                        return parser.readAny();
                    }
                    return segments[nextIndex].extract(parser, segments, nextIndex + 1);
                }
                // Not matched — skip this value
                parser.skipValue();
                parser.skipWS();
                if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ',') {
                    parser.advance(1);
                    parser.skipWS();
                }
            }
            return null; // field not found
        }
    }

    // ==================== Array index: $[0], $[-1] ====================

    record IndexSegment(int index) implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            Object current = ctx.current;
            if (current instanceof List<?> list) {
                int idx = index >= 0 ? index : list.size() + index;
                if (idx >= 0 && idx < list.size()) {
                    ctx.current = list.get(idx);
                    ctx.evalNext();
                }
            }
        }

        @Override
        public Object extract(com.alibaba.fastjson3.JSONParser parser,
                               JSONPathSegment[] segments, int nextIndex) {
            if (index < 0) {
                return null; // negative index requires knowing array length — fall back
            }
            parser.skipWS();
            int off = parser.getOffset();
            if (off >= parser.getEnd() || parser.charAt(off) != '[') {
                return null;
            }
            parser.advance(1);

            // Skip elements until we reach the target index
            for (int i = 0; i <= index; i++) {
                parser.skipWS();
                if (parser.getOffset() >= parser.getEnd() || parser.charAt(parser.getOffset()) == ']') {
                    return null; // array too short
                }
                if (i == index) {
                    // Target element — delegate to next segment or read value
                    if (nextIndex >= segments.length) {
                        return parser.readAny();
                    }
                    return segments[nextIndex].extract(parser, segments, nextIndex + 1);
                }
                // Skip this element
                parser.skipValue();
                parser.skipWS();
                if (parser.getOffset() < parser.getEnd() && parser.charAt(parser.getOffset()) == ',') {
                    parser.advance(1);
                }
            }
            return null;
        }
    }

    // ==================== Wildcard: $[*] or $.* ====================

    record WildcardSegment() implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            Object current = ctx.current;
            if (current instanceof List<?> list) {
                Object saved = ctx.current;
                for (Object item : list) {
                    ctx.current = item;
                    ctx.evalNext();
                }
                ctx.current = saved;
            } else if (current instanceof Map<?, ?> map) {
                Object saved = ctx.current;
                for (Object value : map.values()) {
                    ctx.current = value;
                    ctx.evalNext();
                }
                ctx.current = saved;
            }
        }
    }

    // ==================== Array slice: $[start:end:step] ====================

    record SliceSegment(Integer start, Integer end, int step) implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            Object current = ctx.current;
            if (!(current instanceof List<?> list)) {
                return;
            }
            int size = list.size();
            int s = normalizeSliceBound(start, size, step > 0 ? 0 : size - 1);
            int e = normalizeSliceBound(end, size, step > 0 ? size : -size - 1);

            Object saved = ctx.current;
            if (step > 0) {
                for (int i = s; i < e; i += step) {
                    if (i >= 0 && i < size) {
                        ctx.current = list.get(i);
                        ctx.evalNext();
                    }
                }
            } else {
                for (int i = s; i > e; i += step) {
                    if (i >= 0 && i < size) {
                        ctx.current = list.get(i);
                        ctx.evalNext();
                    }
                }
            }
            ctx.current = saved;
        }

        private static int normalizeSliceBound(Integer bound, int size, int defaultVal) {
            if (bound == null) {
                return defaultVal;
            }
            int b = bound;
            if (b < 0) {
                b = size + b;
            }
            return Math.max(0, Math.min(b, size));
        }
    }

    // ==================== Multi-index: $[0,2,4] ====================

    record MultiIndexSegment(int[] indices) implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            Object current = ctx.current;
            if (!(current instanceof List<?> list)) {
                return;
            }
            int size = list.size();
            Object saved = ctx.current;
            for (int index : indices) {
                int idx = index >= 0 ? index : size + index;
                if (idx >= 0 && idx < size) {
                    ctx.current = list.get(idx);
                    ctx.evalNext();
                }
            }
            ctx.current = saved;
        }
    }

    // ==================== Multi-name: $['a','b'] ====================

    record MultiNameSegment(String[] names) implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            Object current = ctx.current;
            if (!(current instanceof Map<?, ?> map)) {
                return;
            }
            Object saved = ctx.current;
            for (String name : names) {
                Object value = map.get(name);
                if (value != null || map.containsKey(name)) {
                    ctx.current = value;
                    ctx.evalNext();
                }
            }
            ctx.current = saved;
        }
    }

    // ==================== Recursive descent: $..name, $..[*] ====================

    record RecursiveDescentSegment(JSONPathSegment child) implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            // Apply child segment at current level, then recurse into children
            recurse(ctx, ctx.current, 0);
        }

        private void recurse(JSONPathContext ctx, Object node, int depth) {
            if (depth > 1024 || node == null) {
                return;
            }

            // Apply child segment at this level
            Object saved = ctx.current;
            ctx.current = node;
            child.eval(ctx);
            ctx.current = saved;

            // Recurse into children
            if (node instanceof List<?> list) {
                for (Object item : list) {
                    recurse(ctx, item, depth + 1);
                }
            } else if (node instanceof Map<?, ?> map) {
                for (Object value : map.values()) {
                    recurse(ctx, value, depth + 1);
                }
            }
        }
    }

    // ==================== Filter: $[?@.price < 10] ====================

    record FilterSegment(JSONPathFilter filter) implements JSONPathSegment {
        @Override
        public void eval(JSONPathContext ctx) {
            Object current = ctx.current;
            if (current instanceof List<?> list) {
                Object saved = ctx.current;
                for (Object item : list) {
                    if (filter.test(item, ctx.root)) {
                        ctx.current = item;
                        ctx.evalNext();
                    }
                }
                ctx.current = saved;
            } else if (current instanceof Map<?, ?>) {
                // Single object filter
                if (filter.test(current, ctx.root)) {
                    ctx.evalNext();
                }
            }
        }
    }
}
