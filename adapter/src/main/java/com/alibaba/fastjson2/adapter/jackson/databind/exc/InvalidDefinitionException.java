package com.alibaba.fastjson2.adapter.jackson.databind.exc;

import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonMappingException;

public class InvalidDefinitionException
        extends JsonMappingException {
    protected final JavaType type;

    public InvalidDefinitionException(JavaType type) {
        this.type = type;
    }

    public JavaType getType() {
        return type;
    }
}
