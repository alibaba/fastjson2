package com.alibaba.fastjson3.schema;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for named schemas, supporting $anchor resolution and manual schema registration.
 *
 * <pre>
 * SchemaRegistry registry = SchemaRegistry.getInstance();
 * registry.register("address", addressSchema);
 * // Later: {"$ref": "#address"} resolves via registry
 * </pre>
 */
public final class SchemaRegistry {
    private static final SchemaRegistry INSTANCE = new SchemaRegistry();

    private final ConcurrentHashMap<String, JSONSchema> anchors = new ConcurrentHashMap<>();

    private SchemaRegistry() {
    }

    public static SchemaRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a schema by anchor name or URI.
     */
    public void register(String name, JSONSchema schema) {
        if (name != null && schema != null) {
            anchors.put(name, schema);
        }
    }

    /**
     * Resolve a schema by anchor name or URI.
     */
    public JSONSchema resolve(String name) {
        if (name == null) {
            return null;
        }
        return anchors.get(name);
    }

    /**
     * Remove a registered schema.
     */
    public void unregister(String name) {
        if (name != null) {
            anchors.remove(name);
        }
    }

    /**
     * Clear all registered schemas.
     */
    public void clear() {
        anchors.clear();
    }
}
