package com.alibaba.fastjson2.schema;

import java.util.Map;

public class UnresolvedReference
        extends JSONSchema {
    final String refName;
    final Map<String, JSONSchema> schemas;

    UnresolvedReference(String refName, Map<String, JSONSchema> schemas) {
        super(null, null);
        this.refName = refName;
        this.schemas = schemas;
    }

    @Override
    public Type getType() {
        return Type.UnresolvedReference;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        return JSONSchema.SUCCESS;
    }

    abstract static class ResolveTask {
        final UnresolvedReference reference;

        ResolveTask(UnresolvedReference reference) {
            this.reference = reference;
        }

        JSONSchema getResolvedSchema() {
            return reference.schemas.get(reference.refName);
        }

        abstract void resolve();
    }

    static class PropertyResolveTask
            extends ResolveTask {
        final Map<String, JSONSchema> properties;
        final String entryKey;

        PropertyResolveTask(Map<String, JSONSchema> properties, String entryKey, UnresolvedReference reference) {
            super(reference);
            this.properties = properties;
            this.entryKey = entryKey;
        }

        @Override
        void resolve() {
            JSONSchema refSchema = getResolvedSchema();
            if (refSchema != null) {
                properties.put(entryKey, refSchema);
            }
        }
    }

    static class ArrayResolveTask
            extends ResolveTask {
        final ArraySchema arraySchema;
        final int itemIndex;

        ArrayResolveTask(ArraySchema arraySchema, int itemIndex, UnresolvedReference reference) {
            super(reference);
            this.arraySchema = arraySchema;
            this.itemIndex = itemIndex;
        }

        @Override
        void resolve() {
            JSONSchema refSchema = getResolvedSchema();
            if (refSchema != null) {
                if (itemIndex == -1) {
                    arraySchema.itemSchema = refSchema;
                } else {
                    arraySchema.prefixItems[itemIndex] = refSchema;
                }
            }
        }
    }
}
