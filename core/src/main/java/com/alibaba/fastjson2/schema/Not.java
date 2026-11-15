package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.reader.ObjectReaderBean;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

final class Not
        extends JSONSchema {
    final JSONSchema schema;
    final Type[] types;
    final Boolean result;

    public Not(JSONSchema schema, Type[] types, Boolean result) {
        super(null, null);
        this.schema = schema;
        this.types = types;
        this.result = result;
    }

    @Override
    public Type getType() {
        return Type.AllOf;
    }

    @Override
    public JSONObject toJSONObject() {
        if (schema != null) {
            return JSONObject.of("not", schema.toJSONObject());
        }

        if (types != null) {
            if (types.length == 1 && types[0] == Type.Any) {
                // "not anything", the same schema as the JSON Schema false boolean schema
                return JSONObject.of("not", new JSONObject());
            }

            // a type name the parser did not recognize is kept as null, so reparse gives the same state
            JSONArray typeNames = new JSONArray(types.length);
            for (int i = 0; i < types.length; i++) {
                typeNames.add(types[i] == null ? null : types[i].name().toLowerCase(Locale.ROOT));
            }
            return JSONObject.of("not", JSONObject.of("type", typeNames));
        }

        return JSONObject.of("not", result);
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (schema != null) {
            if (schema.validate(value).isSuccess()) {
                return FAIL_NOT;
            }
        }

        if (types != null) {
            for (Type type : types) {
                switch (type) {
                    case String:
                        if (value instanceof String) {
                            return FAIL_NOT;
                        }
                        break;
                    case Integer:
                        if (value instanceof Byte
                                || value instanceof Short
                                || value instanceof Integer
                                || value instanceof Long
                                || value instanceof BigInteger
                                || value instanceof AtomicInteger
                                || value instanceof AtomicLong
                        ) {
                            return FAIL_NOT;
                        }
                        break;
                    case Number:
                        if (value instanceof Number) {
                            return FAIL_NOT;
                        }
                        break;
                    case Null:
                        if (value == null) {
                            return FAIL_NOT;
                        }
                        break;
                    case Array:
                        if (value instanceof Object[] || value instanceof Collection || (value != null && value.getClass().isArray())) {
                            return FAIL_NOT;
                        }
                        break;
                    case Object:
                        if (value instanceof Map) {
                            return FAIL_NOT;
                        }
                        if (value != null && JSONSchema.CONTEXT.getObjectReader(value.getClass()) instanceof ObjectReaderBean) {
                            return FAIL_NOT;
                        }
                        break;
                    case Boolean:
                        if (value instanceof Boolean) {
                            return FAIL_NOT;
                        }
                        break;
                    case Any:
                        return FAIL_NOT;
                    default:
                        break;
                }
            }
        }

        if (result != null) {
            return result ? FAIL_NOT : SUCCESS;
        }

        return SUCCESS;
    }
}
