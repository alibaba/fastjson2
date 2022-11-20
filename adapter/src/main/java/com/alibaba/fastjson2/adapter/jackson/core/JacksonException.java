package com.alibaba.fastjson2.adapter.jackson.core;

import java.io.IOException;

public abstract class JacksonException
        extends IOException {
    public JacksonException() {
    }

    public JacksonException(String message) {
        super(message);
    }

    public JacksonException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getOriginalMessage();
}
