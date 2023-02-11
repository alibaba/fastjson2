package com.alibaba.fastjson2.diff;

public class JsonDiffException extends RuntimeException {
    private String message;

    public JsonDiffException(String message) {
        super(message);
        this.message = message;
    }
}
