package com.alibaba.fastjson3.schema;

import java.util.Map;

public final class UnresolvedReference extends JSONSchema {
    final String refName;

    UnresolvedReference(String refName) {
        super("", "");
        this.refName = refName;
    }

    @Override
    public Type getType() {
        return Type.UnresolvedReference;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        return FAIL_TYPE_NOT_MATCH;
    }

    // ==================== Resolution ====================

    interface ResolveTask {
        void resolve(ObjectSchema root);
    }

    static final class PropertyResolveTask implements ResolveTask {
        private final Map<String, JSONSchema> properties;
        private final String propertyKey;
        private final String refName;

        PropertyResolveTask(Map<String, JSONSchema> properties, String propertyKey, String refName) {
            this.properties = properties;
            this.propertyKey = propertyKey;
            this.refName = refName;
        }

        @Override
        public void resolve(ObjectSchema root) {
            JSONSchema resolved = root.defs.get(refName);
            if (resolved != null) {
                properties.put(propertyKey, resolved);
            }
        }
    }
}
