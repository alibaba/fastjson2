package com.alibaba.fastjson2.schema;

import java.time.Duration;
import java.time.format.DateTimeParseException;

final class DurationValidator
        implements FormatValidator {
    static final DurationValidator INSTANCE = new DurationValidator();

    @Override
    public boolean isValid(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        try {
            Duration.parse(input);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }
}
