package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Validates that a value is exactly equal to a constant, using type-strict deep comparison.
 * Per JSON Schema: false ≠ 0, true ≠ 1, null is its own type.
 */
public final class ConstSchema extends JSONSchema {
    private final Object constValue;

    public ConstSchema(Object constValue) {
        super("", "");
        this.constValue = constValue;
    }

    @Override
    public Type getType() {
        return Type.Const;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("const", constValue);
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (deepEquals(constValue, value)) {
            return SUCCESS;
        }
        return new ValidateResult(false, "const not match, expect %s, but %s", constValue, value);
    }

    /**
     * Type-strict deep equality. Different types are never equal:
     * false ≠ 0, true ≠ 1, null ≠ 0 ≠ "" ≠ false ≠ [] ≠ {}
     */
    static boolean deepEquals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        // Type-strict: Boolean never equals Number
        if (a instanceof Boolean && !(b instanceof Boolean)) {
            return false;
        }
        if (b instanceof Boolean && !(a instanceof Boolean)) {
            return false;
        }

        // Number comparison: integer 1 == float 1.0, but not boolean true
        if (a instanceof Number && b instanceof Number) {
            return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
        }

        // String comparison
        if (a instanceof String && b instanceof String) {
            return a.equals(b);
        }
        // String never equals non-String
        if (a instanceof String || b instanceof String) {
            return false;
        }

        // Array/List comparison
        if ((a instanceof List<?> || a instanceof JSONArray) && (b instanceof List<?> || b instanceof JSONArray)) {
            List<?> listA = a instanceof List<?> la ? la : (JSONArray) a;
            List<?> listB = b instanceof List<?> lb ? lb : (JSONArray) b;
            if (listA.size() != listB.size()) {
                return false;
            }
            for (int i = 0; i < listA.size(); i++) {
                if (!deepEquals(listA.get(i), listB.get(i))) {
                    return false;
                }
            }
            return true;
        }

        // Object/Map comparison
        if ((a instanceof Map<?, ?>) && (b instanceof Map<?, ?>)) {
            Map<?, ?> mapA = (Map<?, ?>) a;
            Map<?, ?> mapB = (Map<?, ?>) b;
            if (mapA.size() != mapB.size()) {
                return false;
            }
            for (Map.Entry<?, ?> entry : mapA.entrySet()) {
                Object key = entry.getKey();
                if (!mapB.containsKey(key)) {
                    return false;
                }
                if (!deepEquals(entry.getValue(), mapB.get(key))) {
                    return false;
                }
            }
            return true;
        }

        return a.equals(b);
    }
}
