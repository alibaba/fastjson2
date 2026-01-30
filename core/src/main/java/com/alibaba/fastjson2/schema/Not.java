package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.reader.ObjectReaderBean;

import java.math.BigInteger;
import java.util.Collection;
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
    protected ValidateResult validateInternal(Object value, ValidationHandler handler, String path) {
        if (schema != null) {
            if (schema.validateInternal(value, null, path).isSuccess()) {
                return handleError(handler, value, path, FAIL_NOT);
            }
        }

        if (types != null) {
            for (Type type : types) {
                boolean match = false;
                switch (type) {
                    case String:
                        if (value instanceof String) {
                            match = true;
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
                            match = true;
                        }
                        break;
                    case Number:
                        if (value instanceof Number) {
                            match = true;
                        }
                        break;
                    case Null:
                        if (value == null) {
                            match = true;
                        }
                        break;
                    case Array:
                        if (value instanceof Object[] || value instanceof Collection || (value != null && value.getClass().isArray())) {
                            match = true;
                        }
                        break;
                    case Object:
                        if (value instanceof Map) {
                            match = true;
                        }
                        if (value != null && JSONSchema.CONTEXT.getObjectReader(value.getClass()) instanceof ObjectReaderBean) {
                            match = true;
                        }
                        break;
                    case Boolean:
                        if (value instanceof Boolean) {
                            match = true;
                        }
                        break;
                    case Any:
                        match = true;
                    default:
                        break;
                }

                if (match) {
                    return handleError(handler, value, path, FAIL_NOT);
                }
            }
        }

        if (result != null) {
            return result ? handleError(handler, value, path, FAIL_NOT) : SUCCESS;
        }
        return SUCCESS;
    }
}
