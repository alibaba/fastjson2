package com.alibaba.fastjson2.time;

public class DateTimeException
        extends RuntimeException {
    /**
     * The error index in the text.
     */
    private final int errorIndex;

    public DateTimeException(String message) {
        super(message);
        this.errorIndex = 0;
    }

    public DateTimeException(String message, String parsedData, int errorIndex) {
        super(message);
        this.errorIndex = errorIndex;
    }
}
