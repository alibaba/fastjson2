package com.alibaba.fastjson2;

import java.math.BigInteger;
import java.util.*;

public abstract class JSONSchema {
    final String title;
    final String description;

    JSONSchema(JSONObject input) {
        this.title = input.getString("title");
        this.description = input.getString("description");
    }

    public static JSONSchema of(JSONObject input) {
        return new ObjectSchema(input);
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
        StringSchema(JSONObject input) {
            super(input);
        }

        @Override
        public Type getType() {
            return Type.String;
        }

        @Override
        public void validate(Object value) {
            if (value == null) {
                return;
            }

            if (value instanceof String) {
                return;
            }

            throw new JSONValidException("type Integer not match : " + value.getClass().getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringSchema that = (StringSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
        }
    }

    public static final class IntegerSchema extends JSONSchema {
        IntegerSchema(JSONObject input) {
            super(input);
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
                return;
            }

            throw new JSONValidException("type Integer not match : " + valueClass.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntegerSchema that = (IntegerSchema) o;
            return Objects.equals(title, that.title) && Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, description);
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
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
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
        ArraySchemaSchema(JSONObject input) {
            super(input);
        }

        @Override
        public Type getType() {
            return Type.Array;
        }

        @Override
        public void validate(Object value) {
            throw new UnsupportedOperationException();
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
                    Type type = entryValue.getObject("type", Type.class);
                    if (type == null) {
                        throw new JSONException("type required");
                    }

                    JSONSchema schema;
                    switch (type) {
                        case String:
                            schema = new StringSchema(entryValue);
                            break;
                        case Integer:
                            schema = new IntegerSchema(entryValue);
                            break;
                        case Number:
                            schema = new NumberSchema(entryValue);
                            break;
                        case Null:
                            schema = new NullSchemaSchema(entryValue);
                            break;
                        case Boolean:
                            schema = new BooleanSchemaSchema(entryValue);
                            break;
                        case Object:
                            schema = new ObjectSchema(entryValue);
                            break;
                        case Array:
                            schema = new ArraySchemaSchema(entryValue);
                            break;
                        default:
                            throw new JSONException("not support type : " + type);
                    }

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
