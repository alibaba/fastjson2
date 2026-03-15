package com.alibaba.fastjson3.schema;

public final class ValidateResult {
    private final boolean success;
    private final String format;
    private final Object[] args;
    private final ValidateResult cause;
    private final String path;
    private volatile String message;

    public ValidateResult(boolean success, String message) {
        this.success = success;
        this.format = message;
        this.args = null;
        this.cause = null;
        this.path = null;
    }

    public ValidateResult(boolean success, String format, Object... args) {
        this.success = success;
        this.format = format;
        this.args = args;
        this.cause = null;
        this.path = null;
    }

    public ValidateResult(ValidateResult cause, String format, Object... args) {
        this.success = false;
        this.format = format;
        this.args = args;
        this.cause = cause;
        this.path = null;
    }

    private ValidateResult(boolean success, String format, Object[] args, ValidateResult cause, String path) {
        this.success = success;
        this.format = format;
        this.args = args;
        this.cause = cause;
        this.path = path;
    }

    /**
     * Create a copy of this result with a JSON path segment prepended.
     * Builds up paths like "address.zip" → "$.address.zip" in getMessage().
     */
    public ValidateResult atPath(String segment) {
        String newPath;
        if (this.path != null) {
            newPath = segment + "." + this.path;
        } else if (cause != null && cause.path != null) {
            newPath = segment + "." + cause.path;
        } else {
            newPath = segment;
        }
        return new ValidateResult(this.success, this.format, this.args, this.cause, newPath);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        if (message == null) {
            StringBuilder sb = new StringBuilder();
            if (path != null) {
                sb.append("$.").append(path).append(": ");
            }
            if (args != null && args.length > 0) {
                sb.append(String.format(format, args));
            } else {
                sb.append(format);
            }
            if (cause != null) {
                String causeMsg = cause.getBaseMessage();
                if (causeMsg != null && !causeMsg.isEmpty()) {
                    sb.append(", ").append(causeMsg);
                }
            }
            message = sb.toString();
        }
        return message;
    }

    /**
     * Get the message without path prefix (for cause chaining).
     */
    String getBaseMessage() {
        if (args != null && args.length > 0) {
            return String.format(format, args);
        }
        return format;
    }
}
