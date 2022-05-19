package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class StringSchema extends JSONSchema {
    final int maxLength;
    final int minLength;
    final boolean required;
    final String format;
    final String patternFormat;
    final Pattern pattern;

    final FormatValidator formatValidator;

    StringSchema(JSONObject input) {
        super(input);
        this.minLength = input.getIntValue("minLength", -1);
        this.maxLength = input.getIntValue("maxLength", -1);
        this.required = input.getBooleanValue("required");
        this.patternFormat = input.getString("pattern");
        this.pattern = patternFormat == null ? null : Pattern.compile(patternFormat);
        this.format = input.getString("format");

        if (format == null) {
            formatValidator = null;
        } else {
            switch (format) {
                case "email":
                    formatValidator = EmailValidator.INSTANCE;
                    break;
                case "ipv4":
                    formatValidator = IPV4AddressValidator.INSTANCE;
                    break;
                case "ipv6":
                    formatValidator = IPV6AddressValidator.INSTANCE;
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
            if (required) {
                return REQUIRED_NOT_MATCH;
            }
            return SUCCESS;
        }

        if (value instanceof String) {
            String str = (String) value;
            if (minLength >= 0 && str.length() < minLength) {
                return new ValidateResult.MinLengthFail(minLength, str.length());
            }

            if (maxLength >= 0 && str.length() > maxLength) {
                return new ValidateResult.MaxLengthFail(minLength, str.length());
            }

            if (pattern != null) {
                if (!pattern.matcher(str).find()) {
                    return new ValidateResult.PatternFail(patternFormat, str);
                }
            }

            if (formatValidator != null) {
                if (!formatValidator.isValid(str)) {
                    return new ValidateResult.FormatFail(format, str);
                }
            }

            return SUCCESS;
        }

        return new ValidateResult.TypeNotMatchFail(Type.Integer, value.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.alibaba.fastjson2.schema.StringSchema that = (com.alibaba.fastjson2.schema.StringSchema) o;
        return Objects.equals(title, that.title)
                && Objects.equals(description, that.description)
                && Objects.equals(minLength, that.minLength)
                && Objects.equals(maxLength, that.maxLength)
                && Objects.equals(required, that.required)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, minLength, maxLength, required);
    }
}
