package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public abstract class JSONSchema {
    final String title;
    final String description;

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

    public static JSONSchema of(JSONObject input) {
        Type type = input.getObject("type", Type.class);
        if (type == null) {
            String[] enums = input.getObject("enum", String[].class);
            if (enums != null) {
                return new EnumSchema(enums);
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

    public abstract void validate(Object value);

    public void validate(long value) {
        validate((Object) Long.valueOf(value));
    }

    public void validate(Integer value) {
        validate((Object) value);
    }

    public void validate(Long value) {
        validate((Object) value);
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
    }

    static final class StringSchema extends JSONSchema {
        final int maxLength;
        final int minLength;
        final boolean required;
        final String format;
        final Pattern pattern;

        StringSchema(JSONObject input) {
            super(input);
            this.minLength = input.getIntValue("minLength", -1);
            this.maxLength = input.getIntValue("maxLength", -1);
            this.required = input.getBooleanValue("required");
            this.format = input.getString("pattern");
            this.pattern = format == null ? null : Pattern.compile(format);
        }

        @Override
        public Type getType() {
            return Type.String;
        }

        @Override
        public void validate(Object value) {
            if (value == null) {
                if (required) {
                    throw new JSONSchemaValidException("required");
                }
                return;
            }

            if (value instanceof String) {
                String str = (String) value;
                if (minLength >= 0 && str.length() < minLength) {
                    throw new JSONSchemaValidException("minLength not match, expect " + minLength + ", but " + str.length());
                }

                if (maxLength >= 0 && str.length() > maxLength) {
                    throw new JSONSchemaValidException("maxLength not match, expect " + maxLength + ", but " + str.length());
                }

                if (pattern != null) {
                    if (!pattern.matcher(str).find()) {
                        throw new JSONSchemaValidException("pattern not match, expect " + format + ", but " + str);
                    }
                }

                return;
            }

            throw new JSONSchemaValidException("type Integer not match : " + value.getClass().getName());
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
        final Long minimum;
        final Long exclusiveMinimum;
        final Long maximum;
        final Long exclusiveMaximum;
        final Long multipleOf;

        IntegerSchema(JSONObject input) {
            super(input);
            Object exclusiveMinimum = input.get("exclusiveMinimum");
            Long minimum = input.getLong("minimum");
            if (exclusiveMinimum == Boolean.TRUE) {
                this.minimum = null;
                this.exclusiveMinimum = minimum;
            } else {
                this.minimum = minimum;
                this.exclusiveMinimum = input.getLong("exclusiveMinimum");;
            }

            Long maximum = input.getLong("maximum");
            Object exclusiveMaximum = input.get("exclusiveMaximum");
            if (exclusiveMaximum == Boolean.TRUE) {
                this.maximum = null;
                this.exclusiveMaximum = maximum;
            } else {
                this.maximum = maximum;
                this.exclusiveMaximum = input.getLong("exclusiveMaximum");
            }

            this.multipleOf = input.getLong("multipleOf");
        }

        @Override
        public Type getType() {
            return Type.Integer;
        }

        @Override
        public void validate(Object value) {
            if (value == null) {
                return;
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
                if (minimum != null) {
                    long longValue = ((Number) value).longValue();
                    if (longValue < minimum.longValue()) {
                        throw new JSONSchemaValidException("minimum not match, expect >= " + minimum + ", but " + value);
                    }
                }

                if (exclusiveMinimum != null) {
                    long longValue = ((Number) value).longValue();
                    if (longValue <= exclusiveMinimum.longValue()) {
                        throw new JSONSchemaValidException("exclusiveMinimum not match, expect > " + exclusiveMinimum + ", but " + value);
                    }
                }

                if (maximum != null) {
                    long longValue = ((Number) value).longValue();
                    if (longValue > maximum.longValue()) {
                        throw new JSONSchemaValidException("maximum not match, expect <= " + maximum + ", but " + value);
                    }
                }

                if (exclusiveMaximum != null) {
                    long longValue = ((Number) value).longValue();
                    if (longValue >= exclusiveMaximum.longValue()) {
                        throw new JSONSchemaValidException("exclusiveMinimum not match, expect < " + exclusiveMaximum + ", but " + value);
                    }
                }

                if (multipleOf != null) {
                    long longValue = ((Number) value).longValue();
                    if (longValue % multipleOf.longValue() != 0) {
                        throw new JSONSchemaValidException("multipleOf not match, expect multipleOf " + multipleOf + ", but " + value);
                    }
                }
                return;
            }

            throw new JSONSchemaValidException("type Integer not match : " + valueClass.getName());
        }

        @Override
        public void validate(long longValue) {
            if (minimum != null) {
                if (longValue < minimum.longValue()) {
                    throw new JSONSchemaValidException("minimum not match, expect >= " + minimum + ", but " + longValue);
                }
            }

            if (exclusiveMinimum != null) {
                if (longValue <= exclusiveMinimum.longValue()) {
                    throw new JSONSchemaValidException("exclusiveMinimum not match, expect > " + exclusiveMinimum + ", but " + longValue);
                }
            }

            if (maximum != null) {
                if (longValue > maximum.longValue()) {
                    throw new JSONSchemaValidException("maximum not match, expect <= " + maximum + ", but " + longValue);
                }
            }

            if (exclusiveMaximum != null) {
                if (longValue >= exclusiveMaximum.longValue()) {
                    throw new JSONSchemaValidException("exclusiveMinimum not match, expect < " + exclusiveMaximum + ", but " + longValue);
                }
            }

            if (multipleOf != null) {
                if (longValue % multipleOf.longValue() != 0) {
                    throw new JSONSchemaValidException("multipleOf not match, expect multipleOf " + multipleOf + ", but " + longValue);
                }
            }
            return;
        }

        @Override
        public void validate(Long value) {
            if (value == null) {
                return;
            }

            long longValue = value.longValue();
            if (minimum != null) {
                if (longValue < minimum.longValue()) {
                    throw new JSONSchemaValidException("minimum not match, expect >= " + minimum + ", but " + longValue);
                }
            }

            if (exclusiveMinimum != null) {
                if (longValue <= exclusiveMinimum.longValue()) {
                    throw new JSONSchemaValidException("exclusiveMinimum not match, expect > " + exclusiveMinimum + ", but " + longValue);
                }
            }

            if (maximum != null) {
                if (longValue > maximum.longValue()) {
                    throw new JSONSchemaValidException("maximum not match, expect <= " + maximum + ", but " + longValue);
                }
            }

            if (exclusiveMaximum != null) {
                if (longValue >= exclusiveMaximum.longValue()) {
                    throw new JSONSchemaValidException("exclusiveMinimum not match, expect < " + exclusiveMaximum + ", but " + longValue);
                }
            }

            if (multipleOf != null) {
                if (longValue % multipleOf.longValue() != 0) {
                    throw new JSONSchemaValidException("multipleOf not match, expect multipleOf " + multipleOf + ", but " + longValue);
                }
            }
            return;
        }

        @Override
        public void validate(Integer value) {
            if (value == null) {
                return;
            }

            long longValue = value.longValue();
            if (minimum != null) {
                if (longValue < minimum.longValue()) {
                    throw new JSONSchemaValidException("minimum not match, expect >= " + minimum + ", but " + longValue);
                }
            }

            if (exclusiveMinimum != null) {
                if (longValue <= exclusiveMinimum.longValue()) {
                    throw new JSONSchemaValidException("exclusiveMinimum not match, expect > " + exclusiveMinimum + ", but " + longValue);
                }
            }

            if (maximum != null) {
                if (longValue > maximum.longValue()) {
                    throw new JSONSchemaValidException("maximum not match, expect <= " + maximum + ", but " + longValue);
                }
            }

            if (exclusiveMaximum != null) {
                if (longValue >= exclusiveMaximum.longValue()) {
                    throw new JSONSchemaValidException("exclusiveMinimum not match, expect < " + exclusiveMaximum + ", but " + longValue);
                }
            }

            if (multipleOf != null) {
                if (longValue % multipleOf.longValue() != 0) {
                    throw new JSONSchemaValidException("multipleOf not match, expect multipleOf " + multipleOf + ", but " + longValue);
                }
            }
            return;
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
        final BigDecimal exclusiveMinimum;
        final BigDecimal maximum;
        final BigDecimal exclusiveMaximum;
        final BigInteger multipleOf;

        NumberSchema(JSONObject input) {
            super(input);

            Object exclusiveMinimum = input.get("exclusiveMinimum");
            BigDecimal minimum = input.getBigDecimal("minimum");
            if (exclusiveMinimum == Boolean.TRUE) {
                this.minimum = null;
                this.exclusiveMinimum = minimum;
            } else {
                this.minimum = minimum;
                this.exclusiveMinimum = input.getBigDecimal("exclusiveMinimum");;
            }

            BigDecimal maximum = input.getBigDecimal("maximum");
            Object exclusiveMaximum = input.get("exclusiveMaximum");
            if (exclusiveMaximum == Boolean.TRUE) {
                this.maximum = null;
                this.exclusiveMaximum = maximum;
            } else {
                this.maximum = maximum;
                this.exclusiveMaximum = input.getBigDecimal("exclusiveMaximum");
            }
            this.multipleOf = input.getBigInteger("multipleOf");
        }

        @Override
        public Type getType() {
            return Type.Number;
        }

        @Override
        public void validate(Object value) {
            if (value == null) {
                return;
            }

            if (value instanceof Number) {
                Number number = (Number) value;

                BigDecimal decimalValue;
                if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
                    decimalValue = BigDecimal.valueOf(number.longValue());
                } else if (number instanceof Float || number instanceof Double) {
                    decimalValue = BigDecimal.valueOf((double) number.doubleValue());
                } else if (number instanceof BigInteger) {
                    decimalValue = new BigDecimal((BigInteger) number);
                } else if (number instanceof BigDecimal) {
                    decimalValue = (BigDecimal) number;
                } else {
                    throw new JSONSchemaValidException("type Number not match : " + value.getClass().getName());
                }

                if (minimum != null) {
                    if (minimum.compareTo(decimalValue) > 0) {
                        throw new JSONSchemaValidException("minimum not match, expect >= " + minimum + ", but " + value);
                    }
                }

                if (exclusiveMinimum != null) {
                    if (exclusiveMinimum.compareTo(decimalValue) >= 0) {
                        throw new JSONSchemaValidException("exclusiveMinimum not match, expect > " + exclusiveMinimum + ", but " + value);
                    }
                }

                if (maximum != null) {
                    if (maximum.compareTo(decimalValue) < 0) {
                        throw new JSONSchemaValidException("maximum not match, expect <= " + maximum + ", but " + value);
                    }
                }

                if (exclusiveMaximum != null) {
                    if (exclusiveMaximum.compareTo(decimalValue) <= 0) {
                        throw new JSONSchemaValidException("exclusiveMaximum not match, expect < " + exclusiveMaximum + ", but " + value);
                    }
                }

                if (multipleOf != null) {
                    BigInteger bigInteger = decimalValue.toBigInteger();
                    if (!decimalValue.equals(new BigDecimal(bigInteger)) || !bigInteger.mod(multipleOf).equals(BigInteger.ZERO)) {
                        throw new JSONSchemaValidException("multipleOf not match, expect multipleOf " + multipleOf + ", but " + value);
                    }
                }
                return;
            }

            throw new JSONSchemaValidException("type Number not match : " + value.getClass().getName());
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
        public void validate(Object value) {
            if (value == null) {
                return;
            }

            if (value instanceof Boolean) {
                return;
            }

            throw new JSONSchemaValidException("type Boolean not match : " + value.getClass().getName());
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
        public void validate(Object value) {
            if (value == null) {
                return;
            }

            throw new JSONSchemaValidException("type Null not match : " + value.getClass().getName());
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
        public void validate(Object value) {
            if (value == null) {
                return;
            }

            if (!items.contains(value)) {
                throw new JSONSchemaValidException("type Enum not match : " + value);
            }
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
        public void validate(Object value) {
            if (value == null) {
                return;
            }

            Set uniqueItemsSet = null;

            if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                final int size = array.length;

                if (minLength >= 0 && size < minLength) {
                    throw new JSONSchemaValidException("minLength not match, expect " + minLength + ", but " + size);
                }

                if (maxLength >= 0) {
                    if (maxLength >= 0 && size > maxLength) {
                        throw new JSONSchemaValidException("maxLength not match, expect " + maxLength + ", but " + size);
                    }
                }

                int containsCount = 0;
                for (int index = 0; index < array.length; index++) {
                    Object item = array[index];

                    if (itemSchema != null) {
                        itemSchema.validate(item);
                    }

                    if (index < prefixItems.length) {
                        prefixItems[index].validate(item);
                    }

                    if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                        try {
                            this.contains.validate(item);
                            containsCount++;
                        } catch (JSONSchemaValidException ignored) {}
                    }

                    if (uniqueItems) {
                        if (uniqueItemsSet == null) {
                            uniqueItemsSet = new HashSet(size);
                        }

                        if (!uniqueItemsSet.add(item)) {
                            throw new JSONSchemaValidException("uniqueItems not match");
                        }
                    }
                }
                if (this.contains != null && containsCount == 0) {
                    throw new JSONSchemaValidException("contains not match");
                }
                if (minContains >= 0 && containsCount < minContains) {
                    throw new JSONSchemaValidException("minContains not match, expect " + minContains + ", but " + containsCount);
                }
                if (maxContains >= 0 && containsCount > maxContains) {
                    throw new JSONSchemaValidException("maxContains not match, expect " + maxContains + ", but " + containsCount);
                }

                if (!additionalItems) {
                    if (size > prefixItems.length) {
                        throw new JSONSchemaValidException("additional items not match, max size " + size + ", but " + size);
                    }
                }
                return;
            }
            if (value.getClass().isArray()) {
                final int size = Array.getLength(value);

                if (minLength >= 0 && size < minLength) {
                    throw new JSONSchemaValidException("minLength not match, expect " + minLength + ", but " + size);
                }

                if (maxLength >= 0) {
                    if (maxLength >= 0 && size > maxLength) {
                        throw new JSONSchemaValidException("maxLength not match, expect " + maxLength + ", but " + size);
                    }
                }

                int containsCount = 0;
                for (int index = 0; index < size; index++) {
                    Object item = Array.get(value, index);

                    if (itemSchema != null) {
                        itemSchema.validate(item);
                    }

                    if (index < prefixItems.length) {
                        prefixItems[index].validate(item);
                    }

                    if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                        try {
                            this.contains.validate(item);
                            containsCount++;
                        } catch (JSONSchemaValidException ignored) {}
                    }

                    if (uniqueItems) {
                        if (uniqueItemsSet == null) {
                            uniqueItemsSet = new HashSet(size);
                        }

                        if (!uniqueItemsSet.add(item)) {
                            throw new JSONSchemaValidException("uniqueItems not match");
                        }
                    }
                }
                if (this.contains != null && containsCount == 0) {
                    throw new JSONSchemaValidException("contains not match");
                }
                if (minContains >= 0 && containsCount < minContains) {
                    throw new JSONSchemaValidException("minContains not match, expect " + minContains + ", but " + containsCount);
                }
                if (maxContains >= 0 && containsCount > maxContains) {
                    throw new JSONSchemaValidException("maxContains not match, expect " + maxContains + ", but " + containsCount);
                }

                if (!additionalItems) {
                    if (size > prefixItems.length) {
                        throw new JSONSchemaValidException("additional items not match, max size " + size + ", but " + size);
                    }
                }
                return;
            }

            if (value instanceof Iterable) {
                if (value instanceof Collection) {
                    int size = ((Collection<?>) value).size();
                    if (minLength >= 0 && size < minLength) {
                        throw new JSONSchemaValidException("minLength not match, expect " + minLength + ", but " + size);
                    }

                    if (maxLength >= 0) {
                        if (maxLength >= 0 && size > maxLength) {
                            throw new JSONSchemaValidException("maxLength not match, expect " + maxLength + ", but " + size);
                        }
                    }

                    if (!additionalItems) {
                        if (size > prefixItems.length) {
                            throw new JSONSchemaValidException("additional items not match, max size " + size + ", but " + size);
                        }
                    }
                }

                int index = 0;
                int containsCount = 0;
                for (Iterator it = ((Iterable) value).iterator(); it.hasNext(); index++) {
                    Object item = it.next();

                    if (itemSchema != null) {
                        itemSchema.validate(item);
                    }

                    if (index < prefixItems.length) {
                        prefixItems[index].validate(item);
                    }

                    if (this.contains != null && (minContains > 0 || maxContains > 0 || containsCount == 0)) {
                        try {
                            this.contains.validate(item);
                            containsCount++;
                        } catch (JSONSchemaValidException ignored) {}
                    }

                    if (uniqueItems) {
                        if (uniqueItemsSet == null) {
                            uniqueItemsSet = new HashSet();
                        }

                        if (!uniqueItemsSet.add(item)) {
                            throw new JSONSchemaValidException("uniqueItems not match");
                        }
                    }
                }
                if (this.contains != null && containsCount == 0) {
                    throw new JSONSchemaValidException("contains not match");
                }
                if (minContains >= 0 && containsCount < minContains) {
                    throw new JSONSchemaValidException("minContains not match, expect " + minContains + ", but " + containsCount);
                }
                if (maxContains >= 0 && containsCount > maxContains) {
                    throw new JSONSchemaValidException("maxContains not match, expect " + maxContains + ", but " + containsCount);
                }

                return;
            }

            throw new JSONSchemaValidException("type Array not match : " + value.getClass().getName());
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
        public void validate(Object value) {
            if (value == null) {
                return;
            }

            if (value instanceof Map) {
                Map map = (Map) value;

                for (String item : required) {
                    if (map.get(item) == null) {
                        throw new JSONSchemaValidException("require property '" + item + "'");
                    }
                }

                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String key = entry.getKey();
                    JSONSchema schema = (JSONSchema) entry.getValue();

                    Object propertyValue = map.get(key);
                    schema.validate(propertyValue);
                }

                for (PatternProperty patternProperty : patternProperties) {
                    for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext();) {
                        Map.Entry entry = it.next();
                        Object entryKey = entry.getKey();
                        if (entryKey instanceof String) {
                            String strKey = (String) entryKey;
                            if (patternProperty.pattern.matcher(strKey).find()) {
                                patternProperty.schema.validate(entry.getValue());
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
                            additionalPropertySchema.validate(entry.getValue());
                            continue;
                        }

                        throw new JSONSchemaValidException("add additionalProperties '" + key + "'");
                    }
                }

                if (propertyNamesPattern != null) {
                    for (Object key : map.keySet()) {
                        String strKey = key.toString();
                        if (!propertyNamesPattern.matcher(strKey).find()) {
                            throw new JSONSchemaValidException("propertyNames pattern not match, expect '" + propertyNamesPattern + "', but " + strKey);
                        }
                    }
                }

                if (minProperties >= 0) {
                    if (map.size() < minProperties) {
                        throw new JSONSchemaValidException("minProperties not match, expect >= '" + minProperties + "', but " + map.size());
                    }
                }

                if (maxProperties >= 0) {
                    if (map.size() > maxProperties) {
                        throw new JSONSchemaValidException("maxProperties not match, expect <= '" + maxProperties + "', but " + map.size());
                    }
                }

                return;
            }

            Class valueClass = value.getClass();
            ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);

            if(!(objectWriter instanceof ObjectWriterAdapter)) {
                throw new JSONSchemaValidException("type Object not match : " + value.getClass().getName());
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
                    throw new JSONSchemaValidException("type Object not match : " + fieldName);
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
                        throw new JSONSchemaValidException("minProperties not match, expect >= '" + minProperties + "', but " + fieldValueCount);
                    }
                }

                if (maxProperties >= 0) {
                    if (fieldValueCount > maxProperties) {
                        throw new JSONSchemaValidException("maxProperties not match, expect <= '" + maxProperties + "', but " + fieldValueCount);
                    }
                }
            }

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
}
