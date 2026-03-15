package com.alibaba.fastjson3.schema;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

public final class EnumSchema extends JSONSchema {
    private final Set<Object> items;

    public EnumSchema(Object[] enums) {
        super("", "");
        this.items = new LinkedHashSet<>(enums.length);
        for (Object item : enums) {
            items.add(normalize(item));
        }
    }

    @Override
    public Type getType() {
        return Type.Enum;
    }

    @Override
    public com.alibaba.fastjson3.JSONObject toJSONObject() {
        com.alibaba.fastjson3.JSONObject obj = new com.alibaba.fastjson3.JSONObject();
        obj.put("enum", new com.alibaba.fastjson3.JSONArray(new java.util.ArrayList<>(items)));
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        Object normalized = normalize(value);

        if (items.contains(normalized)) {
            return SUCCESS;
        }

        // Fallback: deep comparison with type-strict equality (handles numeric coercion in arrays/objects)
        for (Object item : items) {
            if (ConstSchema.deepEquals(item, normalized)) {
                return SUCCESS;
            }
        }

        return new ValidateResult(false, "enum not match, expect %s, but %s", items, value);
    }

    /**
     * Normalize Integer/Short/Byte to Long for consistent hash lookup.
     */
    private static Object normalize(Object val) {
        if (val instanceof Integer i) {
            return i.longValue();
        }
        if (val instanceof Short s) {
            return s.longValue();
        }
        if (val instanceof Byte b) {
            return b.longValue();
        }
        if (val instanceof BigInteger bi) {
            if (bi.bitLength() < 64) {
                return bi.longValue();
            }
        }
        if (val instanceof BigDecimal bd) {
            return bd.stripTrailingZeros();
        }
        return val;
    }
}
