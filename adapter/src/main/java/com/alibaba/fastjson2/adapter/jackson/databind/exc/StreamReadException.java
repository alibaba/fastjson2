package com.alibaba.fastjson2.adapter.jackson.databind.exc;

import com.alibaba.fastjson2.adapter.jackson.core.JsonProcessingException;

public class StreamReadException
        extends JsonProcessingException {
    public StreamReadException() {
    }

    public StreamReadException(String message) {
        super(message);
    }

    public StreamReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
