package com.alibaba.fastjson2.time;

public class DateTimeException
        extends RuntimeException {
    private final String parsedString;
    /**
     * The error index in the text.
     */
    private final int errorIndex;

    public DateTimeException(String message) {
        super(message);
        this.parsedString = null;
        this.errorIndex = 0;
    }

    public DateTimeException(String message, String parsedData, int errorIndex) {
        super(message);
        this.parsedString = parsedData;
        this.errorIndex = errorIndex;
    }
}
