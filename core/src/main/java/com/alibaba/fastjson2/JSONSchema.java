package com.alibaba.fastjson2;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

public abstract class JSONSchema {
    final String title;
    final String description;

    JSONSchema(JSONObject input) {
        this.title = input.getString("title");
        this.description = input.getString("description");
    }

    public static JSONSchema of(JSONObject input) {
        Type type = input.getObject("type", Type.class);
        if (type == null) {
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
                return new BooleanSchemaSchema(input);
            case Null:
                return new NullSchemaSchema(input);
            case Object:
                return new ObjectSchema(input);
            case Array:
                return new ArraySchemaSchema(input);
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

    public enum Type {
        Null,
        Boolean,
        Object,
        Array,
        Number,
        String,

        // extended type
        Integer,
    }

    public static final class StringSchema extends JSONSchema {
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
                    throw new JSONValidException("required");
                }
                return;
            }

            if (value instanceof String) {
                String str = (String) value;
                if (minLength >= 0 && str.length() < minLength) {
                    throw new JSONValidException("minLength not match, expect " + minLength + ", but " + str.length());
                }

                if (maxLength >= 0 && str.length() > maxLength) {
                    throw new JSONValidException("maxLength not match, expect " + maxLength + ", but " + str.length());
                }

                if (pattern != null) {
                    if (!pattern.matcher(str).find()) {
                        throw new JSONValidException("pattern not match, expect " + format + ", but " + str);
                    }
                }

                return;
            }

            throw new JSONValidException("type Integer not match : " + value.getClass().getName());
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

    public static final class IntegerSchema extends JSONSchema {
        final Long minimum;
        final Long maximum;

        IntegerSchema(JSONObject input) {
            super(input);

            this.minimum = input.getLong("minimum");
            this.maximum = input.getLong("maximum");
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
            ) {
                if (minimum != null) {
                    long longValue = ((Number) value).longValue();
                    if (longValue < minimum.longValue()) {
                        throw new JSONValidException("minimum not match, expect >= " + minimum + ", but " + value);
                    }
                }

                if (maximum != null) {
                    long longValue = ((Number) value).longValue();
                    if (longValue > maximum.longValue()) {
                        throw new JSONValidException("maximum not match, expect <= " + maximum + ", but " + value);
                    }
                }
                return;
            }

            throw new JSONValidException("type Integer not match : " + valueClass.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntegerSchema that = (IntegerSchema) o;
            return Objects.equals(title, that.title)
                    && Objects.equals(description, that.description)
                    && Objects.equals(minimum, that.minimum)
                    && Objects.equals(maximum, that.maximum)
                    ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description, minimum, maximum);
        }
    }

    public static final class NumberSchema extends JSONSchema {
        NumberSchema(JSONObject input) {
            super(input);
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
                return;
            }

            throw new JSONValidException("type Integer not match : " + value.getClass().getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NumberSchema that = (NumberSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    public static final class BooleanSchemaSchema extends JSONSchema {
        BooleanSchemaSchema(JSONObject input) {
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

            throw new JSONValidException("type Boolean not match : " + value.getClass().getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BooleanSchemaSchema that = (BooleanSchemaSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    public static final class NullSchemaSchema extends JSONSchema {
        NullSchemaSchema(JSONObject input) {
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

            throw new JSONValidException("type Null not match : " + value.getClass().getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NullSchemaSchema that = (NullSchemaSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    public static final class ArraySchemaSchema extends JSONSchema {
        final int maxLength;
        final int minLength;

        ArraySchemaSchema(JSONObject input) {
            super(input);
            this.minLength = input.getIntValue("minLength", -1);
            this.maxLength = input.getIntValue("maxLength", -1);
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

            if (value instanceof Object[]) {
                Object[] array = (Object[]) value;

                if (minLength >= 0 && array.length < minLength) {
                    throw new JSONValidException("minLength not match, expect " + minLength + ", but" + array.length);
                }

                if (maxLength >= 0) {
                    if (maxLength >= 0 && array.length > maxLength) {
                        throw new JSONValidException("maxLength not match, expect " + maxLength + ", but" + array.length);
                    }
                }
                return;
            }

            if (value instanceof Iterable) {
                return;
            }

            throw new JSONValidException("type Array not match : " + value.getClass().getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArraySchemaSchema that = (ArraySchemaSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    public static final class ObjectSchema extends JSONSchema {
        final JSONObject properties;
        final Set<String> required;

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

            JSONArray required = input.getJSONArray("required");
            if (required == null) {
                this.required = Collections.emptySet();
            } else {
                this.required = new LinkedHashSet<>(required.size());
                for (int i = 0; i < required.size(); i++) {
                    this.required.add(
                            required.getString(i)
                    );
                }
            }
        }

        @Override
        public Type getType() {
            return Type.Object;
        }

        @Override
        public void validate(Object value) {
            if (value instanceof Map) {
                Map map = (Map) value;

                for (String item : required) {
                    if (!map.containsKey(item)) {
                        throw new JSONValidException("require property '" + item + "'");
                    }
                }

                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String key = entry.getKey();
                    JSONSchema schema = (JSONSchema) entry.getValue();

                    Object propertyValue = map.get(key);
                    schema.validate(propertyValue);
                }

                return;
            }

            throw new UnsupportedOperationException();
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
            return Objects.equals(properties, that.properties) && Objects.equals(required, that.required);
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, required);
        }
    }
}
