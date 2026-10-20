package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONException;

import java.util.IdentityHashMap;
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
        throw new JSONException("schema error, unresolved reference : " + refName);
    }

    abstract static class ResolveTask {
        final UnresolvedReference reference;

        ResolveTask(UnresolvedReference reference) {
            this.reference = reference;
        }

        JSONSchema getResolvedSchema() {
            JSONSchema schema = reference;
            Map<UnresolvedReference, Boolean> visited = new IdentityHashMap<>();
            while (schema instanceof UnresolvedReference) {
                UnresolvedReference unresolved = (UnresolvedReference) schema;
                if (visited.put(unresolved, Boolean.TRUE) != null) {
                    throw new JSONException("schema error, circular reference : " + reference.refName);
                }
                schema = unresolved.schemas.get(unresolved.refName);
            }
            if (schema == null) {
                throw new JSONException("schema error, unresolved reference : " + reference.refName);
            }
            return schema;
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
            properties.put(entryKey, getResolvedSchema());
        }
    }

    static class ArrayResolveTask
            extends ResolveTask {
        static final int ITEM = -1;
        static final int ADDITIONAL_ITEM = -2;
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
            if (itemIndex == ITEM) {
                arraySchema.itemSchema = refSchema;
            } else if (itemIndex == ADDITIONAL_ITEM) {
                arraySchema.additionalItem = refSchema;
            } else {
                arraySchema.prefixItems[itemIndex] = refSchema;
            }
        }
    }

    static class ObjectResolveTask
            extends ResolveTask {
        final ObjectSchema objectSchema;
        final int patternIndex;

        ObjectResolveTask(ObjectSchema objectSchema, int patternIndex, UnresolvedReference reference) {
            super(reference);
            this.objectSchema = objectSchema;
            this.patternIndex = patternIndex;
        }

        @Override
        void resolve() {
            JSONSchema schema = getResolvedSchema();
            if (patternIndex == -1) {
                objectSchema.additionalPropertySchema = schema;
            } else {
                ObjectSchema.PatternProperty property = objectSchema.patternProperties[patternIndex];
                objectSchema.patternProperties[patternIndex] = new ObjectSchema.PatternProperty(property.pattern, schema);
            }
        }
    }

    static class ItemsResolveTask
            extends ResolveTask {
        final JSONSchema[] items;
        final int index;

        ItemsResolveTask(JSONSchema[] items, int index, UnresolvedReference reference) {
            super(reference);
            this.items = items;
            this.index = index;
        }

        @Override
        void resolve() {
            items[index] = getResolvedSchema();
        }
    }
}
