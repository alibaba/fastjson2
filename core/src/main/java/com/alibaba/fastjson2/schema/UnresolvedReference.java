package com.alibaba.fastjson2.schema;

import java.util.Map;

public class UnresolvedReference
        extends JSONSchema {
    final String refName;
    UnresolvedReference(String refName) {
        super(null, null);
        this.refName = refName;
    }

    @Override
    public Type getType() {
        return Type.UnresolvedReference;
    }

    @Override
    public ValidateResult validate(Object value) {
        return JSONSchema.SUCCESS;
    }

    abstract static class ResolveTask {
        abstract void resolve(JSONSchema root);
    }

    static class PropertyResolveTask
            extends ResolveTask {
        final Map<String, JSONSchema> properties;
        final String entryKey;
        final String refName;

        PropertyResolveTask(Map<String, JSONSchema> properties, String entryKey, String refName) {
            this.properties = properties;
            this.entryKey = entryKey;
            this.refName = refName;
        }

        @Override
        void resolve(JSONSchema root) {
            Map<String, JSONSchema> defs = null;
            if (root instanceof ObjectSchema) {
                defs = ((ObjectSchema) root).defs;
            } else if (root instanceof ArraySchema) {
                defs = ((ArraySchema) root).defs;
            }

            if (defs == null) {
                return;
            }

            JSONSchema refSchema = defs.get(refName);
            if (refSchema != null) {
                properties.put(entryKey, refSchema);
            }
        }
    }
}
