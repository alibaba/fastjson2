package com.alibaba.fastjson3.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tracks which properties/indices have been "evaluated" by schema keywords
 * during validation. Used by unevaluatedProperties and unevaluatedItems.
 */
final class EvaluationContext {
    private final Set<String> evaluatedProperties;
    private final Set<Integer> evaluatedIndices;

    EvaluationContext() {
        this.evaluatedProperties = new LinkedHashSet<>();
        this.evaluatedIndices = new LinkedHashSet<>();
    }

    private EvaluationContext(Set<String> properties, Set<Integer> indices) {
        this.evaluatedProperties = new LinkedHashSet<>(properties);
        this.evaluatedIndices = new LinkedHashSet<>(indices);
    }

    void addProperty(String name) {
        evaluatedProperties.add(name);
    }

    void addAllProperties(Collection<String> names) {
        evaluatedProperties.addAll(names);
    }

    void addIndex(int index) {
        evaluatedIndices.add(index);
    }

    void addIndicesUpTo(int count) {
        for (int i = 0; i < count; i++) {
            evaluatedIndices.add(i);
        }
    }

    void addAllIndices(Collection<Integer> indices) {
        evaluatedIndices.addAll(indices);
    }

    boolean isPropertyEvaluated(String name) {
        return evaluatedProperties.contains(name);
    }

    boolean isIndexEvaluated(int index) {
        return evaluatedIndices.contains(index);
    }

    Set<String> getEvaluatedProperties() {
        return Collections.unmodifiableSet(evaluatedProperties);
    }

    Set<Integer> getEvaluatedIndices() {
        return Collections.unmodifiableSet(evaluatedIndices);
    }

    EvaluationContext branch() {
        return new EvaluationContext(evaluatedProperties, evaluatedIndices);
    }

    void merge(EvaluationContext branch) {
        evaluatedProperties.addAll(branch.evaluatedProperties);
        evaluatedIndices.addAll(branch.evaluatedIndices);
    }
}
