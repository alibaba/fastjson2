package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.DomainValidator;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.InetAddressValidator;
import com.alibaba.fastjson2.util.InetAddresses;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.fastjson2.JSONFactory.UUIDUtils.parse4Nibbles;

public abstract class JSONSchema {
    final String title;
    final String description;

    final static JSONReader.Context CONTEXT = JSONFactory.createReadContext();

    JSONSchema(JSONObject input) {
        this.title = input.getString("title");
        this.description = input.getString("description");
    }

    JSONSchema(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public static JSONSchema of(JSONObject input, Class objectClass) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        if (objectClass == null) {
            return of(input);
        }

        if (objectClass == byte.class
                || objectClass == short.class
                || objectClass == int.class
                || objectClass == long.class
                || objectClass == Byte.class
                || objectClass == Short.class
                || objectClass == Integer.class
                || objectClass == Long.class
                || objectClass == BigInteger.class
                || objectClass == AtomicInteger.class
                || objectClass == AtomicLong.class
        ) {
            return new IntegerSchema(input);
        }

        if (objectClass == BigDecimal.class
                || objectClass == float.class
                || objectClass == double.class
                || objectClass == Float.class
                || objectClass == Double.class
                || objectClass == Number.class
        ) {
            return new NumberSchema(input);
        }

        if (objectClass == boolean.class
                || objectClass == Boolean.class) {
            return new BooleanSchema(input);
        }

        if (objectClass == String.class) {
            return new StringSchema(input);
        }

        if (Iterable.class.isAssignableFrom(objectClass)) {
            return new ArraySchema(input);
        }

        if (objectClass.isArray()) {
            return new ArraySchema(input);
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            return new ObjectSchema(input);
        }

        return new ObjectSchema(input);
    }

    static class ConstString extends JSONSchema {
        final String value;
        ConstString(String value) {
            super(null, null);
            this.value = value;
        }

        @Override
        public Type getType() {
            return Type.Const;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (!this.value.equals(value)) {
                return new ConstFail(this.value, value);
            }

            return SUCCESS;
        }
    }

    public static JSONSchema of(JSONObject input) {
        Type type = input.getObject("type", Type.class);
        if (type == null) {
            String[] enums = input.getObject("enum", String[].class);
            if (enums != null) {
                return new EnumSchema(enums);
            }

            Object constValue = input.get("const");
            if (constValue instanceof String) {
                return new ConstString((String) constValue);
            }

            if (input.containsKey("properties")) {
                return new ObjectSchema(input);
            }

            throw new JSONException("type required");
        }

        switch (type) {
            case String:
                return new StringSchema(input);
            case Integer:
                return new IntegerSchema(input);
            case Number:
                return new NumberSchema(input);
            case Boolean:
                return new BooleanSchema(input);
            case Null:
                return new NullSchema(input);
            case Object:
                return new ObjectSchema(input);
            case Array:
                return new ArraySchema(input);
            default:
                throw new JSONException("not support type : " + type);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public abstract Type getType();

    public abstract ValidateResult validate(Object value);

    public boolean isValid(Object value) {
        return validate(value)
                .isSuccess();
    }

    public boolean isValid(long value) {
        return validate(value)
                .isSuccess();
    }

    public boolean isValid(double value) {
        return validate(value)
                .isSuccess();
    }

    public boolean isValid(Double value) {
        return validate(value)
                .isSuccess();
    }

    public boolean isValid(float value) {
        return validate(value)
                .isSuccess();
    }

    public boolean isValid(Float value) {
        return validate(value)
                .isSuccess();
    }

    public boolean isValid(Integer value) {
        return validate(value)
                .isSuccess();
    }

    public boolean isValid(Long value) {
        return validate(value)
                .isSuccess();
    }

    public ValidateResult validate(long value) {
        return validate((Object) Long.valueOf(value));
    }

    public ValidateResult validate(double value) {
        return validate((Object) Double.valueOf(value));
    }

    public ValidateResult validate(Float value) {
        return validate((Object) value);
    }

    public ValidateResult validate(Double value) {
        return validate((Object) value);
    }

    public ValidateResult validate(Integer value) {
        return validate((Object) value);
    }

    public ValidateResult validate(Long value) {
        return validate((Object) value);
    }

    public void assertValidate(Object value) {
        ValidateResult result = validate(value);
        if (result.isSuccess()) {
            return;
        }
        throw new JSONSchemaValidException(result.getMessage());
    }

    public void assertValidate(Integer value) {
        ValidateResult result = validate(value);
        if (result.isSuccess()) {
            return;
        }
        throw new JSONSchemaValidException(result.getMessage());
    }

    public void assertValidate(Long value) {
        ValidateResult result = validate(value);
        if (result.isSuccess()) {
            return;
        }
        throw new JSONSchemaValidException(result.getMessage());
    }

    public void assertValidate(Double value) {
        ValidateResult result = validate(value);
        if (result.isSuccess()) {
            return;
        }
        throw new JSONSchemaValidException(result.getMessage());
    }

    public void assertValidate(Float value) {
        ValidateResult result = validate(value);
        if (result.isSuccess()) {
            return;
        }
        throw new JSONSchemaValidException(result.getMessage());
    }

    public void assertValidate(long value) {
        ValidateResult result = validate(value);
        if (result.isSuccess()) {
            return;
        }
        throw new JSONSchemaValidException(result.getMessage());
    }

    public void assertValidate(double value) {
        ValidateResult result = validate(value);
        if (result.isSuccess()) {
            return;
        }
        throw new JSONSchemaValidException(result.getMessage());
    }

    public enum Type {
        Null,
        Boolean,
        Object,
        Array,
        Number,
        String,

        // extended type
        Integer,
        Enum,
        Const,
    }

    static abstract class FormatValidator {
        public abstract boolean isValid(String input);
    }

    static final class EmailValidator extends FormatValidator {
        private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
        private static final String VALID_CHARS = "(\\\\.)|[^\\s" + SPECIAL_CHARS + "]";
        private static final String QUOTED_USER = "(\"[^\"]*\")";
        private static final String WORD = "((" + VALID_CHARS + "|')+|" + QUOTED_USER + ")";

        private static final String EMAIL_REGEX = "^\\s*?(.+)@(.+?)\\s*$";
        private static final String IP_DOMAIN_REGEX = "^\\[(.*)\\]$";
        private static final String USER_REGEX = "^\\s*" + WORD + "(\\." + WORD + ")*$";

        private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
        private static final Pattern IP_DOMAIN_PATTERN = Pattern.compile(IP_DOMAIN_REGEX);
        private static final Pattern USER_PATTERN = Pattern.compile(USER_REGEX);

        final static EmailValidator INSTANCE = new EmailValidator();

        @Override
        public boolean isValid(String email) {
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

            if (!isValidUser(emailMatcher.group(1))) {
                return false;
            }

            if (!isValidDomain(emailMatcher.group(2))) {
                return false;
            }

            return true;
        }


        protected static boolean isValidDomain(String domain) {
            // see if domain is an IP address in brackets
            Matcher ipDomainMatcher = IP_DOMAIN_PATTERN.matcher(domain);

            if (ipDomainMatcher.matches()) {
                InetAddressValidator inetAddressValidator =
                        InetAddressValidator.getInstance();
                return inetAddressValidator.isValid(ipDomainMatcher.group(1));
            }
            // Domain is symbolic name
            return DomainValidator.isValid(domain) || DomainValidator.isValidTld(domain);
        }

        protected static boolean isValidUser(String user) {

            if (user == null || user.length() > 64) {
                return false;
            }

            return USER_PATTERN.matcher(user).matches();
        }
    }

    static final class IPV4AddressValidator extends FormatValidator {
        final static IPV4AddressValidator INSTANCE = new IPV4AddressValidator();
        @Override
        public boolean isValid(String address) {
            if (address == null) {
                return false;
            }
            return InetAddresses.isInetAddress(address) && address.indexOf('.') != -1;
        }
    }

    static final class IPV6AddressValidator extends FormatValidator {
        final static IPV6AddressValidator INSTANCE = new IPV6AddressValidator();
        @Override
        public boolean isValid(String address) {
            if (address == null) {
                return false;
            }
            return InetAddresses.isInetAddress(address) && address.indexOf(':') != -1;
        }
    }

    static final class URIValidator extends FormatValidator {
        final static URIValidator INSTANCE = new URIValidator();
        @Override
        public boolean isValid(String url) {
            if (url == null || url.isEmpty()) {
                return false;
            }

            try {
                new URI(url);
                return true;
            } catch (URISyntaxException ignored) {
                return false;
            }
        }
    }

    static final class DateTimeValidator extends FormatValidator {
        final static DateTimeValidator INSTANCE = new DateTimeValidator();

        @Override
        public boolean isValid(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }

            char c10;
            if (str.length() == 19
                    && str.charAt(4) == '-'
                    && str.charAt(7) == '-'
                    && ((c10 = str.charAt(10)) == ' ' || c10 == 'T')
                    && str.charAt(13) == ':'
                    && str.charAt(16) == ':'
            ) {
                // yyyy-MM-dd hh:mm:ss
                char y0 = str.charAt(0);
                char y1 = str.charAt(1);
                char y2 = str.charAt(2);
                char y3 = str.charAt(3);
                char m0 = str.charAt(5);
                char m1 = str.charAt(6);
                char d0 = str.charAt(8);
                char d1 = str.charAt(9);
                char h0 = str.charAt(11);
                char h1 = str.charAt(12);
                char i0 = str.charAt(14);
                char i1 = str.charAt(15);
                char s0 = str.charAt(17);
                char s1 = str.charAt(18);

                if (y0 < '0' || y0 > '9'
                        || y1 < '0' || y1 > '9'
                        || y2 < '0' || y2 > '9'
                        || y3 < '0' || y3 > '9'
                        || m0 < '0' || m0 > '9'
                        || m1 < '0' || m1 > '9'
                        || d0 < '0' || d0 > '9'
                        || d1 < '0' || d1 > '9'
                        || h0 < '0' || h0 > '9'
                        || h1 < '0' || h1 > '9'
                        || i0 < '0' || i0 > '9'
                        || i1 < '0' || i1 > '9'
                        || s0 < '0' || s0 > '9'
                        || s1 < '0' || s1 > '9'
                ) {
                    return false;
                }

                int yyyy = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
                int mm = (m0 - '0') * 10 + (m1 - '0');
                int dd = (d0 - '0') * 10 + (d1 - '0');
                int hh = (h0 - '0') * 10 + (h1 - '0');
                int ii = (i0 - '0') * 10 + (i1 - '0');
                int ss = (s0 - '0') * 10 + (s1 - '0');

                if (mm > 12) {
                    return false;
                }

                if (dd > 28) {
                    int dom = 31;
                    switch (mm) {
                        case 2:
                            boolean isLeapYear = ((yyyy & 3) == 0) && ((yyyy % 100) != 0 || (yyyy % 400) == 0);
                            dom = isLeapYear ? 29 : 28;
                            break;
                        case 4:
                        case 6:
                        case 9:
                        case 11:
                            dom = 30;
                            break;
                    }
                    if (dd > dom) {
                        return false;
                    }
                } else if (dd > 31) {
                    return false;
                }

                if (hh > 24) {
                    return false;
                }

                if (ii > 60) {
                    return false;
                }

                if (ss > 61) {
                    return false;
                }

                return true;
            }

            try {
                char[] chars = new char[str.length() + 2];
                chars[0] = '"';
                str.getChars(0, str.length(), chars, 1);
                chars[chars.length - 1] = '"';

                return JSONReader.of(CONTEXT, chars).isLocalDateTime();
            } catch (DateTimeException | JSONException ignored) {
                return false;
            }
        }
    }

    static final class DateValidator extends FormatValidator {
        final static DateValidator INSTANCE = new DateValidator();

        @Override
        public boolean isValid(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }

            if (str.length() == 10
                    && str.charAt(4) == '-'
                    && str.charAt(7) == '-'
            ) {
                // yyyy-MM-dd
                char y0 = str.charAt(0);
                char y1 = str.charAt(1);
                char y2 = str.charAt(2);
                char y3 = str.charAt(3);
                char m0 = str.charAt(5);
                char m1 = str.charAt(6);
                char d0 = str.charAt(8);
                char d1 = str.charAt(9);

                int yyyy = (y0 - '0') * 1000 + (y1 - '0') * 100 + (y2 - '0') * 10 + (y3 - '0');
                int mm = (m0 - '0') * 10 + (m1 - '0');
                int dd = (d0 - '0') * 10 + (d1 - '0');

                if (mm > 12) {
                    return false;
                }

                if (dd > 28) {
                    int dom = 31;
                    switch (mm) {
                        case 2:
                            boolean isLeapYear = ((yyyy & 3) == 0) && ((yyyy % 100) != 0 || (yyyy % 400) == 0);
                            dom = isLeapYear ? 29 : 28;
                            break;
                        case 4:
                        case 6:
                        case 9:
                        case 11:
                            dom = 30;
                            break;
                    }
                    if (dd > dom) {
                        return false;
                    }
                } else if (dd > 31) {
                    return false;
                }

                return true;
            }

            try {
                char[] chars = new char[str.length() + 2];
                chars[0] = '"';
                str.getChars(0, str.length(), chars, 1);
                chars[chars.length - 1] = '"';

                return JSONReader.of(CONTEXT, chars)
                        .isLocalDate();
            } catch (DateTimeException | JSONException ignored) {
                return false;
            }
        }
    }

    static final class TimeValidator extends FormatValidator {
        final static TimeValidator INSTANCE = new TimeValidator();

        @Override
        public boolean isValid(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }

            char h0, h1, m0, m1, s0, s1;
            if (str.length() == 8 && str.charAt(2) == ':' && str.charAt(5) == ':') {
                h0 = str.charAt(0);
                h1 = str.charAt(1);
                m0 = str.charAt(3);
                m1 = str.charAt(4);
                s0 = str.charAt(6);
                s1 = str.charAt(7);
            } else {
                try {
                    LocalTime.parse(str);
                    return true;
                } catch (DateTimeParseException ignored) {
                    return false;
                }
            }

            if (h0 >= '0' && h0 <= '2'
                    && h1 >= '0' && h1 <= '9'
                    && m0 >= '0' && m0 <= '6'
                    && m1 >= '0' && m0 <= '9'
                    && s0 >= '0' && s0 <= '6'
                    && s1 >= '0' && s0 <= '9'
            ) {
                int hh = (h0 - '0') * 10 + (h1 - '0');
                if (hh > 24) {
                    return false;
                }

                int mm = (m0 - '0') * 10 + (m1 - '0');
                if (mm > 60) {
                    return false;
                }

                int ss = (s0 - '0') * 10 + (s1 - '0');
                if (ss > 61) {
                    return false;
                }

                return true;
            }

           return false;
        }
    }

    static final class DurationValidator extends FormatValidator {
        final static DurationValidator INSTANCE = new DurationValidator();

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

    static final class UUIDValidator extends FormatValidator {
        final static UUIDValidator INSTANCE = new UUIDValidator();

        @Override
        public boolean isValid(String str) {
            if (str == null) {
                return false;
            }

            if (str.length() == 32) {
                long msb1 = parse4Nibbles(str, 0);
                long msb2 = parse4Nibbles(str, 4);
                long msb3 = parse4Nibbles(str, 8);
                long msb4 = parse4Nibbles(str, 12);
                long lsb1 = parse4Nibbles(str, 16);
                long lsb2 = parse4Nibbles(str, 20);
                long lsb3 = parse4Nibbles(str, 24);
                long lsb4 = parse4Nibbles(str, 28);

                return (msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0;
            }

            if (str.length() == 36) {
                char ch1 = str.charAt(8);
                char ch2 = str.charAt(13);
                char ch3 = str.charAt(18);
                char ch4 = str.charAt(23);
                if (ch1 == '-' && ch2 == '-' && ch3 == '-' && ch4 == '-') {
                    long msb1 = parse4Nibbles(str, 0);
                    long msb2 = parse4Nibbles(str, 4);
                    long msb3 = parse4Nibbles(str, 9);
                    long msb4 = parse4Nibbles(str, 14);
                    long lsb1 = parse4Nibbles(str, 19);
                    long lsb2 = parse4Nibbles(str, 24);
                    long lsb3 = parse4Nibbles(str, 28);
                    long lsb4 = parse4Nibbles(str, 32);
                    return (msb1 | msb2 | msb3 | msb4 | lsb1 | lsb2 | lsb3 | lsb4) >= 0;
                }
            }
            return false;
        }
    }

    static final class StringSchema extends JSONSchema {
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
                    return new MinLengthFail(minLength, str.length());
                }

                if (maxLength >= 0 && str.length() > maxLength) {
                    return new MaxLengthFail(minLength, str.length());
                }

                if (pattern != null) {
                    if (!pattern.matcher(str).find()) {
                        return new PatternFail(patternFormat, str);
                    }
                }

                if (formatValidator != null) {
                    if (!formatValidator.isValid(str)) {
                        return new FormatFail(format, str);
                    }
                }

                return SUCCESS;
            }

            return new TypeNotMatchFail(Type.Integer, value.getClass());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringSchema that = (StringSchema) o;
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

    static final class IntegerSchema extends JSONSchema {
        final long minimum;
        final boolean exclusiveMinimum;

        final long maximum;
        final boolean exclusiveMaximum;

        final long multipleOf;

        IntegerSchema(JSONObject input) {
            super(input);
            Object exclusiveMinimum = input.get("exclusiveMinimum");

            long minimum = input.getLongValue("minimum", Long.MIN_VALUE);
            if (exclusiveMinimum == Boolean.TRUE) {
                this.exclusiveMinimum = true;
                this.minimum = minimum;
            } else if (exclusiveMinimum instanceof Number) {
                this.exclusiveMinimum = true;
                this.minimum = input.getLongValue("exclusiveMinimum");
            } else {
                this.minimum = minimum;
                this.exclusiveMinimum = false;
            }

            long maximum = input.getLongValue("maximum", Long.MIN_VALUE);
            Object exclusiveMaximum = input.get("exclusiveMaximum");
            if (exclusiveMaximum == Boolean.TRUE) {
                this.exclusiveMaximum = true;
                this.maximum = maximum;
            } else if (exclusiveMaximum instanceof Number) {
                this.exclusiveMaximum = true;
                this.maximum = input.getLongValue("exclusiveMaximum");
            } else {
                this.exclusiveMaximum = false;
                this.maximum = maximum;
            }

            this.multipleOf = input.getLongValue("multipleOf", 0);
        }

        @Override
        public Type getType() {
            return Type.Integer;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            Class valueClass = value.getClass();
            if (valueClass == Byte.class
                    || valueClass == Short.class
                    || valueClass == Integer.class
                    || valueClass == Long.class
                    || valueClass == BigInteger.class
                    || valueClass == AtomicInteger.class
                    || valueClass == AtomicLong.class
            ) {
                if (minimum != Long.MIN_VALUE) {
                    long longValue = ((Number) value).longValue();
                    if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                        return new MinimumFail(minimum , value, exclusiveMinimum);
                    }
                }

                if (maximum != Long.MIN_VALUE) {
                    long longValue = ((Number) value).longValue();
                    if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                        return new MaximumFail(maximum , value, exclusiveMaximum);
                    }
                }

                if (multipleOf != 0) {
                    long longValue = ((Number) value).longValue();
                    if (longValue % multipleOf != 0) {
                        return new MultipleOfFail(multipleOf, (Number) value);
                    }
                }
                return SUCCESS;
            }

            return new TypeNotMatchFail(Type.Integer, valueClass);
        }

        @Override
        public ValidateResult validate(long longValue) {
            if (minimum != Long.MIN_VALUE) {
                if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                    return new MinimumFail(minimum , longValue, exclusiveMinimum);
                }
            }

            if (maximum != Long.MIN_VALUE) {
                if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                    return new MaximumFail(maximum , longValue, exclusiveMaximum);
                }
            }

            if (multipleOf != 0) {
                if (longValue % multipleOf != 0) {
                    return new MultipleOfFail(multipleOf, longValue);
                }
            }
            return SUCCESS;
        }

        @Override
        public ValidateResult validate(Long value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            long longValue = value.longValue();
            if (minimum != Long.MIN_VALUE) {
                if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                    return new MinimumFail(minimum , value, exclusiveMinimum);
                }
            }

            if (maximum != Long.MIN_VALUE) {
                if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                    return new MaximumFail(maximum , value, exclusiveMaximum);
                }
            }

            if (multipleOf != 0) {
                if (longValue % multipleOf != 0) {
                    return new MultipleOfFail(multipleOf, longValue);
                }
            }
            return SUCCESS;
        }

        @Override
        public ValidateResult validate(Integer value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            long longValue = value.longValue();
            if (minimum != Long.MIN_VALUE) {
                if (exclusiveMinimum ? longValue <= minimum : longValue < minimum) {
                    return new MinimumFail(minimum , value, exclusiveMinimum);
                }
            }

            if (maximum != Long.MIN_VALUE) {
                if (exclusiveMaximum ? longValue >= maximum : longValue > maximum) {
                    return new MaximumFail(maximum , value, exclusiveMaximum);
                }
            }

            if (multipleOf != 0) {
                if (longValue % multipleOf != 0) {
                    return new MultipleOfFail(multipleOf, longValue);
                }
            }
            return SUCCESS;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntegerSchema that = (IntegerSchema) o;
            return Objects.equals(title, that.title)
                    && Objects.equals(description, that.description)
                    && Objects.equals(minimum, that.minimum)
                    && Objects.equals(exclusiveMinimum, that.exclusiveMinimum)
                    && Objects.equals(maximum, that.maximum)
                    && Objects.equals(exclusiveMaximum, that.exclusiveMaximum)
                    && Objects.equals(multipleOf, that.multipleOf)
                    ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description, minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf);
        }
    }

    static final class NumberSchema extends JSONSchema {
        final BigDecimal minimum;
        final long minimumLongValue;
        final boolean exclusiveMinimum;

        final BigDecimal maximum;
        final long maximumLongValue;
        final boolean exclusiveMaximum;

        final BigInteger multipleOf;

        NumberSchema(JSONObject input) {
            super(input);

            Object exclusiveMinimum = input.get("exclusiveMinimum");
            BigDecimal minimum = input.getBigDecimal("minimum");
            if (exclusiveMinimum == Boolean.TRUE) {
                this.minimum = minimum;
                this.exclusiveMinimum = true;
            } else if (exclusiveMinimum instanceof Number) {
                this.minimum = input.getBigDecimal("exclusiveMinimum");
                this.exclusiveMinimum = true;
            } else {
                this.minimum = minimum;
                this.exclusiveMinimum = false;
            }

            if (this.minimum == null || !this.minimum.equals(BigDecimal.valueOf(this.minimum.longValue()))) {
                minimumLongValue = Long.MIN_VALUE;
            } else {
                minimumLongValue = this.minimum.longValue();
            }

            BigDecimal maximum = input.getBigDecimal("maximum");
            Object exclusiveMaximum = input.get("exclusiveMaximum");
            if (exclusiveMaximum == Boolean.TRUE) {
                this.maximum = maximum;
                this.exclusiveMaximum = true;
            } else if (exclusiveMaximum instanceof Number) {
                this.maximum = input.getBigDecimal("exclusiveMaximum");
                this.exclusiveMaximum = true;
            } else {
                this.maximum = maximum;
                this.exclusiveMaximum = false;
            }

            if (this.maximum == null || !this.maximum.equals(BigDecimal.valueOf(this.maximum.longValue()))) {
                maximumLongValue = Long.MIN_VALUE;
            } else {
                maximumLongValue = this.maximum.longValue();
            }

            this.multipleOf = input.getBigInteger("multipleOf");
        }

        @Override
        public Type getType() {
            return Type.Number;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            if (value instanceof Number) {
                Number number = (Number) value;


                if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
                    return validate(number.longValue());
                }

                if (number instanceof Float || number instanceof Double) {
                    return validate(number.doubleValue());
                }

                BigDecimal decimalValue;
                if (number instanceof BigInteger) {
                    decimalValue = new BigDecimal((BigInteger) number);
                } else if (number instanceof BigDecimal) {
                    decimalValue = (BigDecimal) number;
                } else {
                    return new TypeNotMatchFail(Type.Number, value.getClass());
                }

                if (minimum != null) {
                    if (exclusiveMinimum
                            ? minimum.compareTo(decimalValue) >= 0
                            : minimum.compareTo(decimalValue) > 0) {
                        return new MinimumFail(minimum, decimalValue, exclusiveMinimum);
                    }
                }

                if (maximum != null) {
                    if (exclusiveMaximum
                            ? maximum.compareTo(decimalValue) <= 0
                            : maximum.compareTo(decimalValue) < 0) {
                        return new MaximumFail(maximum, decimalValue, exclusiveMaximum);
                    }
                }

                if (multipleOf != null) {
                    BigInteger bigInteger = decimalValue.toBigInteger();
                    if (!decimalValue.equals(new BigDecimal(bigInteger)) || !bigInteger.mod(multipleOf).equals(BigInteger.ZERO)) {
                        return new MultipleOfFail(multipleOf, decimalValue);
                    }
                }
                return SUCCESS;
            }

            return new TypeNotMatchFail(Type.Number, value.getClass());
        }

        @Override
        public ValidateResult validate(Integer value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            return validate(value.longValue());
        }

        public ValidateResult validate(Float value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            return validate(value.doubleValue());
        }

        public ValidateResult validate(Double value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            return validate(value.doubleValue());
        }

        @Override
        public ValidateResult validate(Long value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            return validate(value.longValue());
        }

        @Override
        public ValidateResult validate(long value) {
            BigDecimal decimalValue = BigDecimal.valueOf(value);

            if (minimum != null) {
                if (minimumLongValue != Long.MIN_VALUE) {
                    if (exclusiveMinimum ? value <= minimumLongValue : value < minimumLongValue) {
                        return new MinimumFail(minimum, decimalValue, exclusiveMinimum);
                    }
                } else {
                    if (exclusiveMinimum
                            ? minimum.compareTo(decimalValue) >= 0
                            : minimum.compareTo(decimalValue) > 0) {
                        return new MinimumFail(minimum, decimalValue, exclusiveMaximum);
                    }
                }
            }

            if (maximum != null) {
                if (maximumLongValue != Long.MIN_VALUE) {
                    if (exclusiveMaximum ? value >= maximumLongValue : value > maximumLongValue) {
                        return new MaximumFail(maximum, minimum, exclusiveMinimum);
                    }
                } else if (exclusiveMaximum
                        ? maximum.compareTo(decimalValue) <= 0
                        : maximum.compareTo(decimalValue) < 0) {
                    return new MaximumFail(maximum, minimum, exclusiveMaximum);
                }
            }

            if (multipleOf != null) {
                BigInteger bigInteger = decimalValue.toBigInteger();
                if (!decimalValue.equals(new BigDecimal(bigInteger)) || !bigInteger.mod(multipleOf).equals(BigInteger.ZERO)) {
                    return new MultipleOfFail(multipleOf, decimalValue);
                }
            }
            return SUCCESS;
        }

        @Override
        public ValidateResult validate(double value) {

            if (minimum != null) {
                if (minimumLongValue != Long.MIN_VALUE) {
                    if (exclusiveMinimum ? value <= minimumLongValue : value < minimumLongValue) {
                        return new MinimumFail(minimum, value, exclusiveMinimum);
                    }
                } else {
                    double minimumDoubleValue = minimum.doubleValue();
                    if (exclusiveMinimum ? value <= minimumDoubleValue : value < minimumDoubleValue) {
                        return new MinimumFail(minimum, value, exclusiveMinimum);
                    }
                }
            }

            if (maximum != null) {
                if (maximumLongValue != Long.MIN_VALUE) {
                    if (exclusiveMaximum ? value >= maximumLongValue : value > maximumLongValue) {
                        return new MaximumFail(maximum, value, exclusiveMinimum);
                    }
                } else {
                    double maximumDoubleValue = maximum.doubleValue();
                    if (exclusiveMaximum ? value >= maximumDoubleValue : value > maximumDoubleValue) {
                        return new MaximumFail(maximum, value, exclusiveMinimum);
                    }
                }
            }

            if (multipleOf != null) {
                long multipleOfLongValue = multipleOf.longValue();
                if (value % multipleOfLongValue != 0) {
                    return new MultipleOfFail(multipleOf, value);
                }
            }
            return SUCCESS;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NumberSchema that = (NumberSchema) o;
            return Objects.equals(title, that.title)
                    && Objects.equals(description, that.description)
                    && Objects.equals(minimum, that.minimum)
                    && Objects.equals(exclusiveMinimum, that.exclusiveMinimum)
                    && Objects.equals(maximum, that.maximum)
                    && Objects.equals(exclusiveMaximum, that.exclusiveMaximum)
                    && Objects.equals(multipleOf, that.multipleOf)
                    ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description, minimum, exclusiveMinimum, maximum, exclusiveMaximum, multipleOf);
        }
    }

    static final class BooleanSchema extends JSONSchema {
        BooleanSchema(JSONObject input) {
            super(input);
        }

        @Override
        public Type getType() {
            return Type.Boolean;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            if (value instanceof Boolean) {
                return SUCCESS;
            }

            return new TypeNotMatchFail(Type.Boolean, value.getClass());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BooleanSchema that = (BooleanSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    static final class NullSchema extends JSONSchema {
        NullSchema(JSONObject input) {
            super(input);
        }

        @Override
        public Type getType() {
            return Type.Null;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (value == null) {
                return SUCCESS;
            }

            return new TypeNotMatchFail(Type.Null, value.getClass());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NullSchema that = (NullSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    static final class EnumSchema extends JSONSchema {
        Set<Object> items;

        EnumSchema(Object[] items) {
            super(null, null);
            this.items = new LinkedHashSet<>(items.length);
            for (Object name : items) {
                this.items.add(name);
            }
        }

        @Override
        public Type getType() {
            return Type.Enum;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            if (!items.contains(value)) {
                return new TypeNotMatchFail(Type.Enum, value.getClass());
            }

            return SUCCESS;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnumSchema that = (EnumSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    static final class ArraySchema extends JSONSchema {
        final int maxLength;
        final int minLength;
        final JSONSchema itemSchema;
        final JSONSchema[] prefixItems;
        final boolean additionalItems;
        final JSONSchema contains;
        final int minContains;
        final int maxContains;
        final boolean uniqueItems;

        ArraySchema(JSONObject input) {
            super(input);
            this.minLength = input.getIntValue("minItems", -1);
            this.maxLength = input.getIntValue("maxItems", -1);

            Object items = input.get("items");
            if (items == null) {
                this.additionalItems = true;
                this.itemSchema = null;
            } else if (items instanceof Boolean) {
                this.additionalItems = ((Boolean) items).booleanValue();
                this.itemSchema = null;
            } else {
                this.additionalItems = true;
                this.itemSchema = JSONSchema.of((JSONObject) items);
            }

            JSONArray prefixItems = input.getJSONArray("prefixItems");
            if (prefixItems == null) {
                this.prefixItems = new JSONSchema[0];
            } else {
                this.prefixItems = new JSONSchema[prefixItems.size()];
                for (int i = 0; i < prefixItems.size(); i++) {
                    this.prefixItems[i] = prefixItems.getObject(i, JSONSchema::of);
                }
            }

            this.contains = input.getObject("contains", JSONSchema::of);
            this.minContains = input.getIntValue("minContains", -1);
            this.maxContains = input.getIntValue("maxContains", -1);

            this.uniqueItems = input.getBooleanValue("uniqueItems");
        }

        @Override
        public Type getType() {
            return Type.Array;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            Set uniqueItemsSet = null;

            if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                final int size = array.length;

                if (minLength >= 0 && size < minLength) {
                    return new MinLengthFail(minLength, size);
                }

                if (maxLength >= 0) {
                    if (maxLength >= 0 && size > maxLength) {
                        return new MaxLengthFail(maxLength, size);
                    }
                }

                int containsCount = 0;
                for (int index = 0; index < array.length; index++) {
                    Object item = array[index];

                    if (itemSchema != null) {
                        ValidateResult result = itemSchema.validate(item);
                        if (!result.isSuccess()) {
                            return result;
                        }
                    }

                    if (index < prefixItems.length) {
                        ValidateResult result = prefixItems[index].validate(item);
                        if (!result.isSuccess()) {
                            return result;
                        }
                    }

                    if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                        ValidateResult result = this.contains.validate(item);
                        if (result.isSuccess()) {
                            containsCount++;
                        }
                    }

                    if (uniqueItems) {
                        if (uniqueItemsSet == null) {
                            uniqueItemsSet = new HashSet(size);
                        }

                        if (!uniqueItemsSet.add(item)) {
                            return UNIQUE_ITEMS_NOT_MATCH;
                        }
                    }
                }

                if (this.contains != null && containsCount == 0) {
                    return CONTAINS_NOT_MATCH;
                }

                if (minContains >= 0 && containsCount < minContains) {
                    return new MinContainsFail(minContains, containsCount);
                }

                if (maxContains >= 0 && containsCount > maxContains) {
                    return new MaxContainsFail(maxContains, containsCount);
                }

                if (!additionalItems) {
                    if (size > prefixItems.length) {
                        return new AdditionalItemsFail(prefixItems.length, size);
                    }
                }
                return SUCCESS;
            }
            if (value.getClass().isArray()) {
                final int size = Array.getLength(value);

                if (minLength >= 0 && size < minLength) {
                    return new MinLengthFail(minLength, size);
                }

                if (maxLength >= 0) {
                    if (maxLength >= 0 && size > maxLength) {
                        return new MaxLengthFail(maxLength, size);
                    }
                }

                int containsCount = 0;
                for (int index = 0; index < size; index++) {
                    Object item = Array.get(value, index);

                    if (itemSchema != null) {
                        ValidateResult result = itemSchema.validate(item);
                        if (!result.isSuccess()) {
                            return result;
                        }
                    }

                    if (index < prefixItems.length) {
                        ValidateResult result = prefixItems[index].validate(item);
                        if (!result.isSuccess()) {
                            return result;
                        }
                    }

                    if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                        ValidateResult result = this.contains.validate(item);
                        if (result.isSuccess()) {
                            containsCount++;
                        }
                    }

                    if (uniqueItems) {
                        if (uniqueItemsSet == null) {
                            uniqueItemsSet = new HashSet(size);
                        }

                        if (!uniqueItemsSet.add(item)) {
                            return UNIQUE_ITEMS_NOT_MATCH;
                        }
                    }
                }
                if (this.contains != null && containsCount == 0) {
                    return CONTAINS_NOT_MATCH;
                }
                if (minContains >= 0 && containsCount < minContains) {
                    return new MinContainsFail(minContains, containsCount);
                }
                if (maxContains >= 0 && containsCount > maxContains) {
                    return new MaxContainsFail(maxContains, containsCount);
                }

                if (!additionalItems) {
                    if (size > prefixItems.length) {
                        return new AdditionalItemsFail(prefixItems.length, size);
                    }
                }
                return SUCCESS;
            }

            if (value instanceof Collection) {
                int size = ((Collection<?>) value).size();
                if (minLength >= 0 && size < minLength) {
                    return new MinLengthFail(minLength, size);
                }

                if (maxLength >= 0) {
                    if (maxLength >= 0 && size > maxLength) {
                        return new MaxLengthFail(maxLength, size);
                    }
                }

                if (!additionalItems) {
                    if (size > prefixItems.length) {
                        return new AdditionalItemsFail(prefixItems.length, size);
                    }
                }

                int index = 0;
                int containsCount = 0;
                for (Iterator it = ((Iterable) value).iterator(); it.hasNext(); index++) {
                    Object item = it.next();

                    if (itemSchema != null) {
                        ValidateResult result = itemSchema.validate(item);
                        if (!result.isSuccess()) {
                            return result;
                        }
                    }

                    if (index < prefixItems.length) {
                        ValidateResult result = prefixItems[index].validate(item);
                        if (!result.isSuccess()) {
                            return result;
                        }
                    }

                    if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                        ValidateResult result = this.contains.validate(item);
                        if (result.isSuccess()) {
                            containsCount++;
                        }
                    }

                    if (uniqueItems) {
                        if (uniqueItemsSet == null) {
                            uniqueItemsSet = new HashSet();
                        }

                        if (!uniqueItemsSet.add(item)) {
                            return UNIQUE_ITEMS_NOT_MATCH;
                        }
                    }
                }

                if (this.contains != null && containsCount == 0) {
                    return CONTAINS_NOT_MATCH;
                }

                if (minContains >= 0 && containsCount < minContains) {
                    return new MinContainsFail(minContains, containsCount);
                }

                if (maxContains >= 0 && containsCount > maxContains) {
                    return new MaxContainsFail(maxContains, containsCount);
                }

                return SUCCESS;
            }

            return new TypeNotMatchFail(Type.Array, value.getClass());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArraySchema that = (ArraySchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    static final class PatternProperty {
        final Pattern pattern;
        final JSONSchema schema;

        public PatternProperty(Pattern pattern, JSONSchema schema) {
            this.pattern = pattern;
            this.schema = schema;
        }
    }

    static final class ObjectSchema extends JSONSchema {
        final JSONObject properties;
        final Set<String> required;
        final boolean additionalProperties;
        final JSONSchema additionalPropertySchema;
        final long[] requiredHashCode;

        final PatternProperty[] patternProperties;
        final Pattern propertyNamesPattern;
        final int minProperties;
        final int maxProperties;

        public ObjectSchema(JSONObject input) {
            super(input);
            this.properties = new JSONObject();

            JSONObject properties = input.getJSONObject("properties");
            if (properties != null) {
                for (Iterator<Map.Entry<String, Object>> it = properties.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Object> entry = it.next();
                    String entryKey = entry.getKey();
                    JSONObject entryValue = (JSONObject) entry.getValue();
                    JSONSchema schema = JSONSchema.of(entryValue);
                    this.properties.put(entryKey, schema);
                }
            }

            JSONObject patternProperties = input.getJSONObject("patternProperties");
            if (patternProperties != null) {
                this.patternProperties = new PatternProperty[patternProperties.size()];

                int index = 0;
                for (Iterator<Map.Entry<String, Object>> it = patternProperties.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Object> entry = it.next();
                    String entryKey = entry.getKey();
                    JSONObject entryValue = (JSONObject) entry.getValue();
                    JSONSchema schema = JSONSchema.of(entryValue);
                    this.patternProperties[index++] = new PatternProperty(Pattern.compile(entryKey), schema);
                }
            } else {
                this.patternProperties = new PatternProperty[0];
            }


            JSONArray required = input.getJSONArray("required");
            if (required == null) {
                this.required = Collections.emptySet();
                this.requiredHashCode = new long[0];
            } else {
                this.required = new LinkedHashSet<>(required.size());
                for (int i = 0; i < required.size(); i++) {
                    this.required.add(
                            required.getString(i)
                    );
                }
                this.requiredHashCode = new long[this.required.size()];
                int i = 0;
                for (String item : this.required) {
                    this.requiredHashCode[i++] = Fnv.hashCode64(item);
                }
            }


            Object additionalProperties = input.get("additionalProperties");
            if (additionalProperties instanceof Boolean) {
                this.additionalPropertySchema = null;
                this.additionalProperties = ((Boolean) additionalProperties).booleanValue();
            } else {
                if (additionalProperties instanceof JSONObject) {
                    this.additionalPropertySchema = JSONSchema.of((JSONObject) additionalProperties);
                    this.additionalProperties = false;
                } else {
                    this.additionalPropertySchema = null;
                    this.additionalProperties = true;
                }
            }

            JSONObject propertyNames = input.getJSONObject("propertyNames");
            if (propertyNames == null) {
                this.propertyNamesPattern = null;
            } else {
                String pattern = propertyNames.getString("pattern");
                if (pattern == null) {
                    this.propertyNamesPattern = null;
                } else {
                    this.propertyNamesPattern = Pattern.compile(pattern);
                }
            }

            this.minProperties = input.getIntValue("minProperties", -1);
            this.maxProperties = input.getIntValue("maxProperties", -1);
        }

        @Override
        public Type getType() {
            return Type.Object;
        }

        @Override
        public ValidateResult validate(Object value) {
            if (value == null) {
                return FAIL_INPUT_NULL;
            }

            if (value instanceof Map) {
                Map map = (Map) value;

                for (String item : required) {
                    if (map.get(item) == null) {
                        return new RequiredFail(item);
                    }
                }

                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String key = entry.getKey();
                    JSONSchema schema = (JSONSchema) entry.getValue();

                    Object propertyValue = map.get(key);
                    if (propertyValue == null && !map.containsKey(key)) {
                        continue;
                    }

                    ValidateResult result = schema.validate(propertyValue);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }

                for (PatternProperty patternProperty : patternProperties) {
                    for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext();) {
                        Map.Entry entry = it.next();
                        Object entryKey = entry.getKey();
                        if (entryKey instanceof String) {
                            String strKey = (String) entryKey;
                            if (patternProperty.pattern.matcher(strKey).find()) {
                                ValidateResult result = patternProperty.schema.validate(entry.getValue());
                                if (!result.isSuccess()) {
                                    return result;
                                }
                            }
                        }
                    }
                }

                if (!additionalProperties) {
                    for_:
                    for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext();) {
                        Map.Entry entry = it.next();
                        Object key = entry.getKey();

                        if (properties.containsKey(key)) {
                            continue;
                        }

                        for (PatternProperty patternProperty : patternProperties) {
                            if (key instanceof String) {
                                String strKey = (String) key;
                                if (patternProperty.pattern.matcher(strKey).find()) {
                                    continue for_;
                                }
                            }
                        }

                        if (additionalPropertySchema != null) {
                            ValidateResult result = additionalPropertySchema.validate(entry.getValue());
                            if (!result.isSuccess()) {
                                return result;
                            }
                            continue;
                        }

                        return new AdditionalPropertiesFail(key);
                    }
                }

                if (propertyNamesPattern != null) {
                    for (Object key : map.keySet()) {
                        String strKey = key.toString();
                        if (!propertyNamesPattern.matcher(strKey).find()) {
                            return new PropertyPatternFail( propertyNamesPattern.pattern(), strKey);
                        }
                    }
                }

                if (minProperties >= 0) {
                    if (map.size() < minProperties) {
                        return new MinPropertiesFail(minProperties, map.size());
                    }
                }

                if (maxProperties >= 0) {
                    if (map.size() > maxProperties) {
                        return new MaxPropertiesFail(maxProperties, map.size());
                    }
                }

                return SUCCESS;
            }

            Class valueClass = value.getClass();
            ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);

            if(!(objectWriter instanceof ObjectWriterAdapter)) {
                return new TypeNotMatchFail(Type.Object, valueClass);
            }

            for (int i = 0; i < this.requiredHashCode.length; i++) {
                long nameHash = requiredHashCode[i];
                FieldWriter fieldWriter = objectWriter.getFieldWriter(nameHash);

                Object fieldValue = null;
                if (fieldWriter != null) {
                    fieldValue = fieldWriter.getFieldValue(value);
                }

                if (fieldValue == null) {
                    String fieldName = null;
                    int j = 0;
                    for (Iterator<String> it = this.required.iterator(); it.hasNext();) {
                        String itemName = it.next();
                        j++;
                        if (j == i) {
                            fieldName = itemName;
                        }
                    }
                    return new RequiredFail(fieldName);
                }
            }

            if (minProperties >= 0 || maxProperties >= 0) {
                int fieldValueCount = 0;
                List<FieldWriter> fieldWriters = objectWriter.getFieldWriters();
                for (FieldWriter fieldWriter : fieldWriters) {
                    Object fieldValue = fieldWriter.getFieldValue(value);
                    if (fieldValue != null) {
                        fieldValueCount++;
                    }
                }

                if (minProperties >= 0) {
                    if (fieldValueCount < minProperties) {
                        return new MinPropertiesFail(minProperties, fieldValueCount);
                    }
                }

                if (maxProperties >= 0) {
                    if (fieldValueCount > maxProperties) {
                        return new MaxPropertiesFail(maxProperties, fieldValueCount);
                    }
                }
            }

            return SUCCESS;
        }

        public Map<String, JSONSchema> getProperties() {
            return properties;
        }

        public JSONSchema getProperty(String key) {
            return (JSONSchema) properties.get(key);
        }

        public Set<String> getRequired() {
            return required;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ObjectSchema that = (ObjectSchema) o;
            return Objects.equals(properties, that.properties)
                    && Objects.equals(required, that.required);
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, required);
        }
    }

    public interface ValidateResult {
        boolean isSuccess();
        String getMessage();
        ValidateResult getCause();
    }

    static final Success SUCCESS = new Success();
    static final Fail FAIL_INPUT_NULL = new Fail("input null");

    private static final class Success implements ValidateResult {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            return "success";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MinimumFail implements ValidateResult {
        final Object minimum;
        final Object value;
        final boolean exclusive;
        private String message;

        public MinimumFail(Object minimum, Object value, boolean exclusive) {
            this.minimum = minimum;
            this.exclusive = exclusive;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = ((exclusive ? "exclusiveMinimum not match, expect >= " : "minimum not match, expect >= ") + minimum + ", but " + value);
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MaximumFail implements ValidateResult {
        final Object maximum;
        final Object value;
        final boolean exclusive;
        private String message;


        public MaximumFail(Object maximum, Object value, boolean exclusive) {
            this.maximum = maximum;
            this.value = value;
            this.exclusive = exclusive;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = ((exclusive ? "exclusiveMaximum not match, expect >= " : "maximum not match, expect >= ") + maximum + ", but " + value);
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MultipleOfFail implements ValidateResult {
        final Number multipleOf;
        final Number value;
        private String message;

        public MultipleOfFail(Number multipleOf, Number value) {
            this.multipleOf = multipleOf;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "multipleOf not match, expect multipleOf " + multipleOf + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class TypeNotMatchFail implements ValidateResult {
        final Type expectType;
        final Class inputType;
        private String message;

        public TypeNotMatchFail(Type expectType, Class inputType) {
            this.expectType = expectType;
            this.inputType = inputType;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "type " + expectType + " not match : " + inputType.getName();
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MaxLengthFail implements ValidateResult {
        final int maxLength;
        final int size;
        private String message;

        public MaxLengthFail(int maxLength, int size) {
            this.maxLength = maxLength;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "maxLength not match, expect " + maxLength + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MinLengthFail implements ValidateResult {
        final int minLength;
        final int size;
        private String message;

        public MinLengthFail(int minLength, int size) {
            this.minLength = minLength;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "minLength not match, expect " + minLength + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MinPropertiesFail implements ValidateResult {
        final int minProperties;
        final int size;
        private String message;

        public MinPropertiesFail(int minProperties, int size) {
            this.minProperties = minProperties;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "minProperties not match, expect " + minProperties + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MaxPropertiesFail implements ValidateResult {
        final int maxProperties;
        final int size;
        private String message;

        public MaxPropertiesFail(int maxProperties, int size) {
            this.maxProperties = maxProperties;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "minProperties not match, expect " + maxProperties + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MinContainsFail implements ValidateResult {
        final int minContains;
        final int size;
        private String message;

        public MinContainsFail(int minContains, int size) {
            this.minContains = minContains;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "maxContains not match, expect " + minContains + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class MaxContainsFail implements ValidateResult {
        final int maxContains;
        final int size;
        private String message;

        public MaxContainsFail(int maxContains, int size) {
            this.maxContains = maxContains;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "maxContains not match, expect " + maxContains + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class AdditionalItemsFail implements ValidateResult {
        final int maxSize;
        final int size;
        private String message;

        public AdditionalItemsFail(int maxSize, int size) {
            this.maxSize = maxSize;
            this.size = size;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "additional items not match, max size " + maxSize + ", but " + size;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class PatternFail implements ValidateResult {
        final String pattern;
        final String value;
        private String message;

        public PatternFail(String pattern, String value) {
            this.pattern = pattern;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "pattern not match, expect " + pattern + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class FormatFail implements ValidateResult {
        final String format;
        final String value;
        private String message;

        public FormatFail(String format, String value) {
            this.format = format;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "format not match, expect " + format + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class ConstFail implements ValidateResult {
        final Object constValue;
        final Object value;
        private String message;

        public ConstFail(Object constValue, Object value) {
            this.constValue = constValue;
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "const not match, expect " + constValue + ", but " + value;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class PropertyPatternFail implements ValidateResult {
        final String propertyPattern;
        final Object property;
        private String message;

        public PropertyPatternFail(String propertyPattern, Object property) {
            this.propertyPattern = propertyPattern;
            this.property = property;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "propertyNames pattern not match, expect '" + propertyPattern + ", but " + property;
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class AdditionalPropertiesFail implements ValidateResult {
        final Object property;
        private String message;

        public AdditionalPropertiesFail(Object property) {
            this.property = property;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "add additionalProperties '" + property + "'";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    private static final class RequiredFail implements ValidateResult {
        final String property;
        private String message;

        public RequiredFail(String property) {
            this.property = property;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            if (message != null) {
                return message;
            }
            return message = "requried property '" + property + "'";
        }

        @Override
        public ValidateResult getCause() {
            return null;
        }
    }

    final static Fail CONTAINS_NOT_MATCH = new Fail("contains not match");
    final static Fail UNIQUE_ITEMS_NOT_MATCH = new Fail("uniqueItems not match");
    final static Fail REQUIRED_NOT_MATCH = new Fail("required");

    private static final class Fail implements ValidateResult {
        final String message;
        final ValidateResult cause;

        public Fail(String message) {
            this.message = message;
            this.cause = null;
        }

        public Fail(String message, ValidateResult cause) {
            this.message = message;
            this.cause = cause;
        }

        public String getMessage() {
            return message;
        }

        public ValidateResult getCause() {
            return cause;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
    }
}
