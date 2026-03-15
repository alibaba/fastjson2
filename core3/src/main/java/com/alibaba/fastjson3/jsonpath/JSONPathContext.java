package com.alibaba.fastjson3.jsonpath;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable evaluation context for JSONPath execution.
 * Holds the root object, current node, segment chain, and result accumulator.
 */
public final class JSONPathContext {
    /** The root object ($) */
    final Object root;
    /** Current node being evaluated */
    Object current;
    /** Compiled segments */
    final JSONPathSegment[] segments;
    /** Current segment index */
    public int segmentIndex;
    /** Whether the path is definite (single result expected) */
    final boolean definite;
    /** Accumulated results for indefinite paths */
    final List<Object> results;
    /** Result value for definite paths */
    Object resultValue;

    public JSONPathContext(Object root, JSONPathSegment[] segments, boolean definite) {
        this.root = root;
        this.current = root;
        this.segments = segments;
        this.segmentIndex = 0;
        this.definite = definite;
        this.results = definite ? null : new ArrayList<>();
    }

    /**
     * Evaluate the next segment in the chain, or collect the result if at the end.
     */
    public void evalNext() {
        int nextIndex = segmentIndex + 1;
        if (nextIndex >= segments.length) {
            // End of chain — collect result
            if (definite) {
                resultValue = current; // save definite result
                return;
            }
            results.add(current);
            return;
        }

        // Save and advance
        int savedIndex = segmentIndex;
        Object savedCurrent = current;
        segmentIndex = nextIndex;
        segments[nextIndex].eval(this);
        segmentIndex = savedIndex;
        current = savedCurrent;
    }

    /**
     * Get the evaluation result.
     * For definite paths: returns the single value (or null).
     * For indefinite paths: returns the accumulated list.
     */
    public Object getResult() {
        if (definite) {
            return resultValue;
        }
        return results;
    }
}
