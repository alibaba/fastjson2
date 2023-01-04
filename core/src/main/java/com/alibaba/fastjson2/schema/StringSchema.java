package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

final class StringSchema
        extends JSONSchema {
    final int maxLength;
    final int minLength;
    final String format;
    final String patternFormat;
    final Pattern pattern;
    final boolean typed;
    final AnyOf anyOf;
    final OneOf oneOf;

    final FormatValidator formatValidator;

    StringSchema(JSONObject input) {
        super(input);
        this.typed = "string".equalsIgnoreCase(input.getString("type"));
        this.minLength = input.getIntValue("minLength", -1);
        this.maxLength = input.getIntValue("maxLength", -1);
        this.patternFormat = input.getString("pattern");
        this.pattern = patternFormat == null ? null : Pattern.compile(patternFormat);
        this.format = input.getString("format");

        Object anyOf = input.get("anyOf");
        if (anyOf instanceof JSONArray) {
            this.anyOf = anyOf((JSONArray) anyOf, String.class);
        } else {
            this.anyOf = null;
        }

        Object oneOf = input.get("oneOf");
        if (oneOf instanceof JSONArray) {
            this.oneOf = oneOf((JSONArray) oneOf, String.class);
        } else {
            this.oneOf = null;
        }

        if (format == null) {
            formatValidator = null;
        } else {
            switch (format) {
                case "email":
                    formatValidator = EmailValidator.INSTANCE;
                    break;
                case "ipv4":
                    formatValidator = IPAddressValidator.IPV4;
                    break;
                case "ipv6":
                    formatValidator = IPAddressValidator.IPV6;
                    break;
                case "uri":
                    formatValidator = URIValidator.INSTANCE;
                    break;
                case "date-time":
                    formatValidator = DateTimeValidator.INSTANCE;
                    break;
                case "date":
                    formatValidator = DateValidator.INSTANCE;
                    break;
                case "time":
                    formatValidator = TimeValidator.INSTANCE;
                    break;
                case "duration":
                    formatValidator = DurationValidator.INSTANCE;
                    break;
                case "uuid":
                    formatValidator = UUIDValidator.INSTANCE;
                    break;
                default:
                    formatValidator = null;
                    break;
            }
        }
    }

    @Override
    public Type getType() {
        return Type.String;
    }

    @Override
    public ValidateResult validate(Object value) {
        if (value == null) {
            if (typed) {
                return REQUIRED_NOT_MATCH;
            }
            return SUCCESS;
        }

        if (value instanceof String) {
            String str = (String) value;

            if (minLength >= 0 || maxLength >= 0) {
                int count = str.codePointCount(0, str.length());
                if (minLength >= 0 && count < minLength) {
                    return new ValidateResult(false, "minLength not match, expect >= %s, but %s", minLength, str.length());
                }

                if (maxLength >= 0 && count > maxLength) {
                    return new ValidateResult(false, "maxLength not match, expect <= %s, but %s", maxLength, str.length());
                }
            }

            if (pattern != null) {
                if (!pattern.matcher(str).find()) {
                    return new ValidateResult(false, "pattern not match, expect %s, but %s", patternFormat, str);
                }
            }

            if (formatValidator != null) {
                if (!formatValidator.isValid(str)) {
                    return new ValidateResult(false, "format not match, expect %s, but %s", format, str);
                }
            }

            if (anyOf != null) {
                ValidateResult result = anyOf.validate(str);
                if (!result.isSuccess()) {
                    return result;
                }
            }

            if (oneOf != null) {
                ValidateResult result = oneOf.validate(str);
                if (!result.isSuccess()) {
                    return result;
                }
            }

            return SUCCESS;
        }

        if (!typed) {
            return SUCCESS;
        }

        return new ValidateResult(false, "expect type %s, but %s", Type.String, value.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringSchema that = (StringSchema) o;
        return maxLength == that.maxLength
                && minLength == that.minLength
                && typed == that.typed
                && Objects.equals(format, that.format)
                && Objects.equals(patternFormat, that.patternFormat)
                && Objects.equals(pattern, that.pattern)
                && Objects.equals(formatValidator, that.formatValidator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxLength, minLength, format, patternFormat, pattern, typed, formatValidator);
    }
}
