package com.alibaba.fastjson2.schema;

/**
 * Represents the result of a JSON schema validation operation.
 * Contains validation status and detailed error messages if validation fails.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * JSONSchema schema = JSONSchema.parseSchema("{\"type\":\"integer\",\"minimum\":10}");
 * ValidateResult result = schema.validate(5);
 * if (!result.isSuccess()) {
 *     System.out.println("Validation failed: " + result.getMessage());
 * }
 * }</pre>
 */
public final class ValidateResult {
    private final boolean success;
    final String format;
    final Object[] args;
    final ValidateResult cause;
    String message;

    /**
     * Constructs a failed validation result with a causing validation failure.
     *
     * @param cause the underlying validation failure that caused this failure
     * @param format the message format string (supports String.format syntax)
     * @param args the arguments for the format string
     */
    public ValidateResult(ValidateResult cause, String format, Object... args) {
        this.success = false;
        this.format = format;
        this.args = args;
        this.cause = cause;
        if (args.length == 0) {
            message = format;
        }
    }

    /**
     * Constructs a validation result with the specified success status.
     *
     * @param success true if validation succeeded, false otherwise
     * @param format the message format string (supports String.format syntax)
     * @param args the arguments for the format string
     */
    public ValidateResult(boolean success, String format, Object... args) {
        this.success = success;
        this.format = format;
        this.args = args;
        this.cause = null;
        if (args.length == 0) {
            message = format;
        }
    }

    /**
     * Returns whether the validation was successful.
     *
     * @return true if validation succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns a detailed message describing the validation result.
     * For failures, includes information about what constraint was violated.
     *
     * @return the validation message, or null if no message is available
     */
    public String getMessage() {
        if (message == null) {
            if (format != null && args.length > 0) {
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
