package com.alibaba.fastjson2.schema;

public final class ValidateResult {
    private final boolean success;
    final String format;
    final Object[] args;
    final ValidateResult cause;
    String message;

    public ValidateResult(ValidateResult cause, String format, Object... args) {
        this.success = false;
        this.format = format;
        this.args = args;
        this.cause = cause;
        if (args.length == 0) {
            message = format;
        }
    }

    public ValidateResult(boolean success, String format, Object... args) {
        this.success = success;
        this.format = format;
        this.args = args;
        this.cause = null;
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
                String s = String.format(format, args);
                if (cause != null) {
                    s += "; " + cause.getMessage();
                }
                return message = s;
            }
        }

        return message;
    }
}
