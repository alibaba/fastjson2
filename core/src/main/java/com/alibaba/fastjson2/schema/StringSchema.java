package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class StringSchema
        extends JSONSchema {
    static final Pattern EMAIL_PATTERN = Pattern.compile("^\\s*?(.+)@(.+?)\\s*$");
    static final Pattern IP_DOMAIN_PATTERN = Pattern.compile("^\\[(.*)\\]$");
    static final Pattern USER_PATTERN = Pattern.compile("^\\s*(((\\\\.)|[^\\s\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]]|')+|(\"[^\"]*\"))(\\.(((\\\\.)|[^\\s\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]]|')+|(\"[^\"]*\")))*$");

    final int maxLength;
    final int minLength;
    final String format;
    final String patternFormat;
    final Pattern pattern;
    final boolean typed;
    final AnyOf anyOf;
    final OneOf oneOf;
    final String constValue;
    final Set<String> enumValues;

    final Predicate<String> formatValidator;

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

        this.constValue = input.getString("const");

        {
            Set<String> enumValues = null;
            Object property = input.get("enum");
            if (property instanceof Collection) {
                Collection enums = (Collection) property;
                enumValues = new HashSet<>(enums.size());
                enumValues.addAll((Collection<String>) enums);
            }
            this.enumValues = enumValues;
        }

        if (format == null) {
            formatValidator = null;
        } else {
            switch (format) {
                case "email":
                    formatValidator = StringSchema::isEmail;
                    break;
                case "ipv4":
                    formatValidator = TypeUtils::validateIPv4;
                    break;
                case "ipv6":
                    formatValidator = TypeUtils::validateIPv6;
                    break;
                case "uri":
                    formatValidator = url -> {
                        if (url == null || url.isEmpty()) {
                            return false;
                        }

                        try {
                            new URI(url);
                            return true;
                        } catch (URISyntaxException ignored) {
                            return false;
                        }
                    };
                    break;
                case "date-time":
                    formatValidator = DateUtils::isDate;
                    break;
                case "date":
                    formatValidator = DateUtils::isLocalDate;
                    break;
                case "time":
                    formatValidator = DateUtils::isLocalTime;
                    break;
                case "duration":
                    formatValidator = str -> {
                        if (str == null || str.isEmpty()) {
                            return false;
                        }

                        try {
                            Duration.parse(str);
                            return true;
                        } catch (DateTimeParseException ignored) {
                            return false;
                        }
                    };
                    break;
                case "uuid":
                    formatValidator = TypeUtils::isUUID;
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
                if (!formatValidator.test(str)) {
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

            if (constValue != null) {
                if (!constValue.equals(str)) {
                    return new ValidateResult(false, "must be const %s, but %s", constValue, str);
                }
            }

            if (enumValues != null) {
                if (!enumValues.contains(str)) {
                    return new ValidateResult(false, "not in enum values, %s", str);
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

    public static boolean isEmail(String email) {
        if (email == null) {
            return false;
        }

        if (email.endsWith(".")) { // check this first - it's cheap!
            return false;
        }

        // Check the whole email address structure
        Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
        if (!emailMatcher.matches()) {
            return false;
        }

        String user = emailMatcher.group(1);
        if (user.length() > 64) {
            return false;
        }

        if (!USER_PATTERN.matcher(user).matches()) {
            return false;
        }

        String domain = emailMatcher.group(2);
        Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

        boolean validDomain;
        if (ipDomainMatcher.matches()) {
            String inetAddress = ipDomainMatcher.group(1);
            validDomain = TypeUtils.validateIPv4(inetAddress) || TypeUtils.validateIPv6(inetAddress);
        } else {
            validDomain = DomainValidator.isValid(domain) || DomainValidator.isValidTld(domain);
        }

        return validDomain;
    }
}
