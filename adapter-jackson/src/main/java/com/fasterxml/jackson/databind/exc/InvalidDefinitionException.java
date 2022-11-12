package com.fasterxml.jackson.databind.exc;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;

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
