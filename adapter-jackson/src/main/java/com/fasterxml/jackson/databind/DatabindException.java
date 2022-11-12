package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DatabindException
        extends JsonProcessingException {
    public DatabindException() {
    }

    public DatabindException(String message) {
        super(message);
    }

    public DatabindException(String message, Throwable cause) {
        super(message, cause);
    }
}
