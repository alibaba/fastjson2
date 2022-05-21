package com.alibaba.fastjson2.schema;

public final class ValidateResult {
    private final boolean success;
    final String format;
    final ValidateResult cause;
    final Object[] args;
    String message;

    public ValidateResult(boolean success) {
        this.success = success;
        this.message = this.format = null;
        this.cause = null;
        this.args = new Object[0];
    }

    public ValidateResult(String message) {
        this.success = false;
        this.message = this.format = message;
        this.cause = null;
        this.args = new Object[0];
    }

    public ValidateResult(String format, ValidateResult cause) {
        this.success = false;
        this.format = this.message =format;
        this.args = new Object[0];
        this.cause = cause;
    }

    public ValidateResult(String format, Object... args) {
        this.success = false;
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
                return message = String.format(format, args);
            }
        }

        return message;
    }

    public ValidateResult getCause() {
        return cause;
    }

    public static ValidateResult fail(String message) {
        return new ValidateResult(message);
    }

    public static ValidateResult fail(String message, ValidateResult cause) {
        return new ValidateResult(message, cause);
    }

    public static ValidateResult fail(String message, Object... args) {
        return new ValidateResult(message, args);
    }
}
