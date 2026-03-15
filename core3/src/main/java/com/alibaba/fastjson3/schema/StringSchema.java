package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringSchema extends JSONSchema {
    final int minLength;
    final int maxLength;
    final String format;
    final String patternFormat;
    final Pattern pattern;
    final boolean typed;
    final AnyOf anyOf;
    final OneOf oneOf;
    final String constValue;
    final Set<String> enumValues;
    final Predicate<String> formatValidator;

    public StringSchema(JSONObject input) {
        super(input);
        this.typed = "string".equalsIgnoreCase(input.getString("type"));
        this.minLength = getInt(input, "minLength", -1);
        this.maxLength = getInt(input, "maxLength", -1);
        this.format = input.getString("format");
        this.patternFormat = input.getString("pattern");
        this.pattern = patternFormat != null ? Pattern.compile(patternFormat, Pattern.UNICODE_CHARACTER_CLASS) : null;

        Object constObj = input.get("const");
        this.constValue = constObj instanceof String s ? s : null;

        // Parse enum values
        Object enumObj = input.get("enum");
        if (enumObj instanceof JSONArray arr) {
            this.enumValues = new LinkedHashSet<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                this.enumValues.add(arr.getString(i));
            }
        } else if (enumObj instanceof Object[] enums) {
            this.enumValues = new LinkedHashSet<>(enums.length);
            for (Object e : enums) {
                this.enumValues.add(e != null ? e.toString() : null);
            }
        } else {
            this.enumValues = null;
        }

        // Parse anyOf/oneOf
        JSONArray anyOfArr = input.getJSONArray("anyOf");
        if (anyOfArr != null) {
            JSONSchema[] items = new JSONSchema[anyOfArr.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = JSONSchema.of(anyOfArr.getJSONObject(i));
            }
            this.anyOf = new AnyOf(items);
        } else {
            this.anyOf = null;
        }

        JSONArray oneOfArr = input.getJSONArray("oneOf");
        if (oneOfArr != null) {
            JSONSchema[] items = new JSONSchema[oneOfArr.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = JSONSchema.of(oneOfArr.getJSONObject(i));
            }
            this.oneOf = new OneOf(items);
        } else {
            this.oneOf = null;
        }

        // Setup format validator
        this.formatValidator = format != null ? resolveFormatValidator(format) : null;
    }

    private static Predicate<String> resolveFormatValidator(String format) {
        return switch (format) {
            case "email" -> StringSchema::isEmail;
            case "hostname" -> DomainValidator::isValid;
            case "ipv4" -> StringSchema::isIPv4;
            case "ipv6" -> StringSchema::isIPv6;
            case "uri" -> StringSchema::isURI;
            case "uri-reference" -> StringSchema::isURIReference;
            case "date-time" -> StringSchema::isDateTime;
            case "date" -> StringSchema::isDate;
            case "time" -> StringSchema::isTime;
            case "duration" -> StringSchema::isDuration;
            case "uuid" -> StringSchema::isUUID;
            case "json-pointer" -> StringSchema::isJSONPointer;
            case "relative-json-pointer" -> StringSchema::isRelativeJSONPointer;
            case "regex" -> StringSchema::isRegex;
            default -> null;
        };
    }

    @Override
    public Type getType() {
        return Type.String;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = objectOf("type", "string");
        if (title != null) {
            obj.put("title", title);
        }
        if (description != null) {
            obj.put("description", description);
        }
        if (minLength != -1) {
            obj.put("minLength", minLength);
        }
        if (maxLength != -1) {
            obj.put("maxLength", maxLength);
        }
        if (patternFormat != null) {
            obj.put("pattern", patternFormat);
        }
        if (format != null) {
            obj.put("format", format);
        }
        if (constValue != null) {
            obj.put("const", constValue);
        }
        if (enumValues != null && !enumValues.isEmpty()) {
            obj.put("enum", enumValues);
        }
        return obj;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        if (!(value instanceof String str)) {
            return typed ? FAIL_TYPE_NOT_MATCH : SUCCESS;
        }

        if (constValue != null && !constValue.equals(str)) {
            return new ValidateResult(false, "const not match, expect %s, but %s", constValue, str);
        }

        if (enumValues != null && !enumValues.contains(str)) {
            return new ValidateResult(false, "enum not match, expect %s, but %s", enumValues, str);
        }

        int codePointCount = str.codePointCount(0, str.length());
        if (minLength >= 0 && codePointCount < minLength) {
            return new ValidateResult(false, "minLength not match, expect >= %s, but %s", minLength, codePointCount);
        }

        if (maxLength >= 0 && codePointCount > maxLength) {
            return new ValidateResult(false, "maxLength not match, expect <= %s, but %s", maxLength, codePointCount);
        }

        if (pattern != null && !pattern.matcher(str).find()) {
            return new ValidateResult(false, "pattern not match, expect %s, but %s", patternFormat, str);
        }

        if (formatValidator != null && !formatValidator.test(str)) {
            return new ValidateResult(false, "format '%s' not match, value %s", format, str);
        }

        if (anyOf != null) {
            ValidateResult result = anyOf.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }

        if (oneOf != null) {
            ValidateResult result = oneOf.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
        }

        return SUCCESS;
    }

    // ==================== Format Validators ====================

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\s*?(.+)@(.+?)\\s*$");
    private static final Pattern USER_PATTERN = Pattern.compile(
            "^\\s*(((\\.)|[^\\s\\p{Cntrl}()\\[\\]\\\\.,;:@\"]+(\\.?))*)" +
                    "<?((\\.)|[^\\s\\p{Cntrl}()\\[\\]\\\\.,;:@\"]+(\\.?))*>?\\s*$"
    );
    private static final Pattern IP_DOMAIN_PATTERN = Pattern.compile("^\\[(.*)\\]$");

    static boolean isEmail(String str) {
        if (str == null || str.isEmpty() || str.endsWith(".")) {
            return false;
        }
        Matcher emailMatcher = EMAIL_PATTERN.matcher(str);
        if (!emailMatcher.matches()) {
            return false;
        }
        String user = emailMatcher.group(1);
        String domain = emailMatcher.group(2);
        if (user == null || user.length() > 64) {
            return false;
        }
        if (!USER_PATTERN.matcher(user).matches()) {
            return false;
        }
        Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);
        if (ipDomainMatcher.matches()) {
            String ip = ipDomainMatcher.group(1);
            return isIPv4(ip) || isIPv6(ip);
        }
        return DomainValidator.isValid(domain);
    }

    static boolean isIPv4(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        String[] parts = str.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) {
                return false;
            }
            try {
                int val = Integer.parseInt(part);
                if (val < 0 || val > 255) {
                    return false;
                }
                if (part.length() > 1 && part.charAt(0) == '0') {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    static boolean isIPv6(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            if (str.contains(":")) {
                java.net.InetAddress addr = java.net.InetAddress.getByName(str);
                return addr instanceof java.net.Inet6Address;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    static boolean isURI(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(str);
            return uri.isAbsolute();
        } catch (Exception e) {
            return false;
        }
    }

    static boolean isDateTime(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            OffsetDateTime.parse(str);
            return true;
        } catch (DateTimeParseException e) {
            try {
                ZonedDateTime.parse(str);
                return true;
            } catch (DateTimeParseException e2) {
                return false;
            }
        }
    }

    static boolean isDate(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(str);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    static boolean isTime(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            LocalTime.parse(str);
            return true;
        } catch (DateTimeParseException e) {
            try {
                OffsetTime.parse(str);
                return true;
            } catch (DateTimeParseException e2) {
                return false;
            }
        }
    }

    static boolean isDuration(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Duration.parse(str);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    static boolean isUUID(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return UUID_PATTERN.matcher(str).matches();
    }

    static boolean isURIReference(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            new URI(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean isJSONPointer(String str) {
        if (str == null) {
            return false;
        }
        if (str.isEmpty()) {
            return true; // empty string is a valid JSON Pointer (root)
        }
        if (str.charAt(0) != '/') {
            return false;
        }
        // Validate escape sequences: only ~0 and ~1 are valid
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '~') {
                if (i + 1 >= str.length()) {
                    return false;
                }
                char next = str.charAt(i + 1);
                if (next != '0' && next != '1') {
                    return false;
                }
            }
        }
        return true;
    }

    private static final Pattern RELATIVE_JSON_POINTER = Pattern.compile(
            "^(0|[1-9][0-9]*)(#|(/[^/]*)*)$"
    );

    static boolean isRelativeJSONPointer(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return RELATIVE_JSON_POINTER.matcher(str).matches();
    }

    static boolean isRegex(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Pattern.compile(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
