package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONReader;

import java.io.IOException;

public abstract class JsonParser {
    public abstract boolean isClosed();

    public abstract JsonToken nextToken() throws IOException;

    public abstract JSONReader getRaw();

    public abstract ObjectCodec getCodec();

    public enum Feature {
        AUTO_CLOSE_SOURCE(true),
        ALLOW_COMMENTS(false),
        ALLOW_YAML_COMMENTS(false),
        ALLOW_UNQUOTED_FIELD_NAMES(false),
        ALLOW_SINGLE_QUOTES(false),
        @Deprecated
        ALLOW_UNQUOTED_CONTROL_CHARS(false),
        @Deprecated
        ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false),
        @Deprecated
        ALLOW_NUMERIC_LEADING_ZEROS(false),
        @Deprecated
        ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS(false),
        @Deprecated
        ALLOW_NON_NUMERIC_NUMBERS(false),
        @Deprecated
        ALLOW_MISSING_VALUES(false),
        @Deprecated
        ALLOW_TRAILING_COMMA(false),
        STRICT_DUPLICATE_DETECTION(false),
        IGNORE_UNDEFINED(false),
        INCLUDE_SOURCE_IN_LOCATION(true);

        /**
         * Whether feature is enabled or disabled by default.
         */
        private final boolean defaultState;

        private final int mask;

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         *
         * @return Bit mask of all features that are enabled by default
         */
        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }

        Feature(boolean defaultState) {
            mask = (1 << ordinal());
            this.defaultState = defaultState;
        }

        public boolean enabledByDefault() {
            return defaultState;
        }

        public boolean enabledIn(int flags) {
            return (flags & mask) != 0;
        }

        public int getMask() {
            return mask;
        }
    }
}
