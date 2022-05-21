package com.alibaba.fastjson2.schema;

public final class ValidateResult {
    private final boolean success;
    final String format;
    final Object[] args;
    String message;

    public ValidateResult(boolean success, String format, Object... args) {
        this.success = success;
        this.format = format;
        this.args = args;
        if (args.length == 0) {
            message = format;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        if (message == null) {
            if (format != null && args != null && args.length > 0) {
                return message = String.format(format, args);
            }
        }

        return message;
    }
}
