package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONObject;

public final class Not extends JSONSchema {
    private final JSONSchema schema;
    private final Type[] types;
    private final Boolean result;

    Not(JSONSchema schema, Type[] types, Boolean result) {
        super("", "");
        this.schema = schema;
        this.types = types;
        this.result = result;
    }

    @Override
    public Type getType() {
        return Type.Any;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        if (schema != null) {
            obj.put("not", schema.toJSONObject());
        } else if (result != null) {
            obj.put("not", result);
        } else if (types != null) {
            JSONObject notObj = new JSONObject();
            com.alibaba.fastjson3.JSONArray typeArr = new com.alibaba.fastjson3.JSONArray(types.length);
            for (Type t : types) {
                typeArr.add(t.name().toLowerCase());
            }
            notObj.put("type", typeArr);
            obj.put("not", notObj);
        }
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value, EvaluationContext ctx) {
        // Evaluations inside 'not' never propagate — use disposable context
        return validateInternal(value);
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (result != null) {
            return result ? FAIL_NOT : SUCCESS;
        }

        if (types != null) {
            Type valueType = typeOf(value);
            for (Type type : types) {
                if (type == Type.Any || type == valueType) {
                    return FAIL_NOT;
                }
            }
            return SUCCESS;
        }

        if (schema != null) {
            ValidateResult r = schema.validate(value);
            return r.isSuccess() ? FAIL_NOT : SUCCESS;
        }

        return SUCCESS;
    }

    @Override
    protected ValidateResult validateInternal(long value) {
        if (result != null) {
            return result ? FAIL_NOT : SUCCESS;
        }

        if (types != null) {
            for (Type type : types) {
                if (type == Type.Any || type == Type.Integer || type == Type.Number) {
                    return FAIL_NOT;
                }
            }
            return SUCCESS;
        }

        if (schema != null) {
            ValidateResult r = schema.validate(value);
            return r.isSuccess() ? FAIL_NOT : SUCCESS;
        }

        return SUCCESS;
    }

    @Override
    protected ValidateResult validateInternal(double value) {
        if (result != null) {
            return result ? FAIL_NOT : SUCCESS;
        }

        if (types != null) {
            for (Type type : types) {
                if (type == Type.Any || type == Type.Number) {
                    return FAIL_NOT;
                }
            }
            return SUCCESS;
        }

        if (schema != null) {
            ValidateResult r = schema.validate(value);
            return r.isSuccess() ? FAIL_NOT : SUCCESS;
        }

        return SUCCESS;
    }

    private static Type typeOf(Object value) {
        if (value == null) {
            return Type.Null;
        }
        if (value instanceof Boolean) {
            return Type.Boolean;
        }
        if (value instanceof String) {
            return Type.String;
        }
        if (value instanceof Integer || value instanceof Long
                || value instanceof Byte || value instanceof Short) {
            return Type.Integer;
        }
        if (value instanceof Number) {
            return Type.Number;
        }
        if (value instanceof java.util.Map) {
            return Type.Object;
        }
        if (value instanceof java.util.Collection || value.getClass().isArray()) {
            return Type.Array;
        }
        return Type.Object;
    }
}
