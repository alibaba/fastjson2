package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.core.JsonProcessingException;

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
