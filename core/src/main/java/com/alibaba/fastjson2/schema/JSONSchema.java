package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONCreator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class JSONSchema {
    static final Map<String, JSONSchema> CACHE = new ConcurrentHashMap<>();

    final String title;
    final String description;

    static final JSONReader.Context CONTEXT = JSONFactory.createReadContext();

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

        if (objectClass == null || objectClass == Object.class) {
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
            if (input.containsKey("AnyOf")) {
                return anyOf(input, objectClass);
            }

            if (input.containsKey("anyOf")) {
                return anyOf(input, objectClass);
            }

            if (input.containsKey("oneOf")) {
                return oneOf(input, objectClass);
            }

            if (input.containsKey("not")) {
                return ofNot(input, objectClass);
            }

            return new IntegerSchema(input);
        }

        if (objectClass == BigDecimal.class
                || objectClass == float.class
                || objectClass == double.class
                || objectClass == Float.class
                || objectClass == Double.class
                || objectClass == Number.class
        ) {
            if (input.containsKey("AnyOf")) {
                return anyOf(input, objectClass);
            }

            if (input.containsKey("anyOf")) {
                return anyOf(input, objectClass);
            }

            if (input.containsKey("oneOf")) {
                return oneOf(input, objectClass);
            }

            if (input.containsKey("not")) {
                return ofNot(input, objectClass);
            }

            return new NumberSchema(input);
        }

        if (objectClass == boolean.class
                || objectClass == Boolean.class) {
            return new BooleanSchema(input);
        }

        if (objectClass == String.class) {
            return new StringSchema(input);
        }

        if (Collection.class.isAssignableFrom(objectClass)) {
            return new ArraySchema(input, null);
        }

        if (objectClass.isArray()) {
            return new ArraySchema(input, null);
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            return new ObjectSchema(input, null);
        }

        return new ObjectSchema(input, null);
    }

    static Not ofNot(JSONObject input, Class objectClass) {
        Object not = input.get("not");
        if (not instanceof Boolean) {
            return new Not(null, null, (Boolean) not);
        }

        JSONObject object = (JSONObject) not;

        if (object == null || object.isEmpty()) {
            return new Not(null, new Type[] {Type.Any}, null);
        }

        if (object.size() == 1) {
            Object type = object.get("type");
            if (type instanceof JSONArray) {
                JSONArray array = (JSONArray) type;
                Type[] types = new Type[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    types[i] = array.getObject(i, Type.class);
                }
                return new Not(null, types, null);
            }
        }

        JSONSchema schema = of(object, objectClass);
        return new Not(schema, null, null);
    }

    public static JSONSchema parseSchema(String schema) {
        Object input = JSON.parse(schema);
        if (input instanceof JSONObject) {
            return of((JSONObject) input);
        }

        if (input instanceof Boolean) {
            return ((Boolean) input).booleanValue() ? Any.INSTANCE : Any.NOT_ANY;
        }

        return null;
    }

    @JSONCreator
    public static JSONSchema of(JSONObject input) {
        return of(input, (JSONSchema) null);
    }

    @JSONCreator
    public static JSONSchema of(JSONObject input, JSONSchema parent) {
        Type type = Type.of(
                input.getString("type")
        );

        if (type == null) {
            Object[] enums = input.getObject("enum", Object[].class);
            if (enums != null) {
                return new EnumSchema(enums);
            }

            Object constValue = input.get("const");
            if (constValue instanceof String) {
                return new ConstString((String) constValue);
            } else if (constValue instanceof Integer || constValue instanceof Long) {
                return new ConstLong(((Number) constValue).longValue());
            }

            if (input.size() == 1) {
                String ref = input.getString("$ref");
                if (ref != null && !ref.isEmpty()) {
                    if ("http://json-schema.org/draft-04/schema#".equals(ref)) {
                        JSONSchema schema = CACHE.get(ref);
                        if (schema == null) {
                            URL draf4Resource = JSONSchema.class.getClassLoader().getResource("schema/draft-04.json");
                            schema = JSONSchema.of(
                                    JSON.parseObject(draf4Resource),
                                    (JSONSchema) null
                            );
                            JSONSchema origin = CACHE.putIfAbsent(ref, schema);
                            if (origin != null) {
                                schema = origin;
                            }
                        }
                        return schema;
                    }

                    if ("#".equals(ref)) {
                        return parent;
                    }

                    Map<String, JSONSchema> definitions = null, defs = null, properties = null;
                    if (parent instanceof ObjectSchema) {
                        ObjectSchema objectSchema = (ObjectSchema) parent;

                        definitions = objectSchema.definitions;
                        defs = objectSchema.defs;
                        properties = objectSchema.properties;
                    } else if (parent instanceof ArraySchema) {
                        definitions = ((ArraySchema) parent).definitions;
                        defs = ((ArraySchema) parent).defs;
                    }

                    if (definitions != null) {
                        if (ref.startsWith("#/definitions/")) {
                            final int PREFIX_LEN = 14; // "#/definitions/".length();
                            String refName = ref.substring(PREFIX_LEN);
                            JSONSchema refSchema = definitions.get(refName);
                            return refSchema;
                        }
                    }

                    if (defs != null) {
                        if (ref.startsWith("#/$defs/")) {
                            final int PREFIX_LEN = 8; // "#/$defs/".length();
                            String refName = ref.substring(PREFIX_LEN);
                            refName = URLDecoder.decode(refName);
                            JSONSchema refSchema = defs.get(refName);
                            if (refSchema == null) {
                                refSchema = Any.NOT_ANY;
                            }
                            return refSchema;
                        }
                    }

                    if (properties != null) {
                        if (ref.startsWith("#/properties/")) {
                            final int PREFIX_LEN = 13; // "#/properties/".length();
                            String refName = ref.substring(PREFIX_LEN);
                            JSONSchema refSchema = properties.get(refName);
                            return refSchema;
                        }
                    }

                    if (ref.startsWith("#/prefixItems/") && parent instanceof ArraySchema) {
                        final int PREFIX_LEN = 14; // "#/properties/".length();
                        int index = Integer.parseInt(ref.substring(PREFIX_LEN));
                        JSONSchema refSchema = ((ArraySchema) parent).prefixItems[index];
                        return refSchema;
                    }
                }

                Object exclusiveMaximum = input.get("exclusiveMaximum");
                Object exclusiveMinimum = input.get("exclusiveMinimum");
                if (exclusiveMaximum instanceof Integer
                        || exclusiveMinimum instanceof Integer
                        || exclusiveMaximum instanceof Long
                        || exclusiveMinimum instanceof Long) {
                    return new IntegerSchema(input);
                }

                if (exclusiveMaximum instanceof Number || exclusiveMinimum instanceof Number) {
                    return new NumberSchema(input);
                }
            }

            if (input.containsKey("properties")
                    || input.containsKey("dependentSchemas")
                    || input.containsKey("if")
                    || input.containsKey("required")
                    || input.containsKey("patternProperties")
                    || input.containsKey("additionalProperties")
                    || input.containsKey("minProperties")
                    || input.containsKey("maxProperties")
                    || input.containsKey("propertyNames")
                    || input.containsKey("$ref")
            ) {
                return new ObjectSchema(input, parent);
            }

            if (input.containsKey("maxItems")
                    || input.containsKey("minItems")
                    || input.containsKey("additionalItems")
                    || input.containsKey("items")
                    || input.containsKey("prefixItems")
                    || input.containsKey("uniqueItems")
                    || input.containsKey("maxContains")
                    || input.containsKey("minContains")
            ) {
                return new ArraySchema(input, parent);
            }

            if (input.containsKey("pattern")
                    || input.containsKey("format")
                    || input.containsKey("minLength")
                    || input.containsKey("maxLength")
            ) {
                return new StringSchema(input);
            }

            boolean allOf = input.containsKey("allOf");
            boolean anyOf = input.containsKey("anyOf");
            boolean oneOf = input.containsKey("oneOf");

            if (allOf || anyOf || oneOf) {
                int count = (allOf ? 1 : 0) + (anyOf ? 1 : 0) + (oneOf ? 1 : 0);
                if (count == 1) {
                    if (allOf) {
                        return new AllOf(input, parent);
                    }

                    if (anyOf) {
                        return new AnyOf(input, parent);
                    }

                    if (oneOf) {
                        return new OneOf(input, parent);
                    }
                }
                JSONSchema[] items = new JSONSchema[count];
                int index = 0;
                if (allOf) {
                    items[index++] = new AllOf(input, parent);
                }
                if (anyOf) {
                    items[index++] = new AnyOf(input, parent);
                }
                if (oneOf) {
                    items[index++] = new OneOf(input, parent);
                }
                return new AllOf(items);
            }

            if (input.containsKey("not")) {
                return ofNot(input, null);
            }

            if (input.get("maximum") instanceof Number
                    || input.get("minimum") instanceof Number
                    || input.containsKey("multipleOf")
            ) {
                return new NumberSchema(input);
            }

            if (input.isEmpty()) {
                return Any.INSTANCE;
            }

            if (input.size() == 1) {
                Object propertyType = input.get("type");
                if (propertyType instanceof JSONArray) {
                    JSONArray array = (JSONArray) propertyType;
                    JSONSchema[] typeSchemas = new JSONSchema[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        Type itemType = Type.of(array.getString(i));
                        switch (itemType) {
                            case String:
                                typeSchemas[i] = new StringSchema(JSONObject.of("type", "string"));
                                break;
                            case Integer:
                                typeSchemas[i] = new IntegerSchema(JSONObject.of("type", "integer"));
                                break;
                            case Number:
                                typeSchemas[i] = new NumberSchema(JSONObject.of("type", "number"));
                                break;
                            case Boolean:
                                typeSchemas[i] = new BooleanSchema(JSONObject.of("type", "boolean"));
                                break;
                            case Null:
                                typeSchemas[i] = new NullSchema(JSONObject.of("type", "null"));
                                break;
                            case Object:
                                typeSchemas[i] = new ObjectSchema(JSONObject.of("type", "object"));
                                break;
                            case Array:
                                typeSchemas[i] = new ArraySchema(JSONObject.of("type", "array"), null);
                                break;
                            default:
                                throw new JSONException("not support type : " + itemType);
                        }
                    }
                    return new AnyOf(typeSchemas);
                }
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
                return new ObjectSchema(input, parent);
            case Array:
                return new ArraySchema(input, parent);
            default:
                throw new JSONException("not support type : " + type);
        }
    }

    static AnyOf anyOf(JSONObject input, Class type) {
        JSONArray array = input.getJSONArray("anyOf");
        if (array == null || array.isEmpty()) {
            return null;
        }
        JSONSchema[] items = new JSONSchema[array.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = JSONSchema.of(array.getJSONObject(i), type);
        }
        AnyOf anyOf = new AnyOf(items);

        return anyOf;
    }

    static AnyOf anyOf(JSONArray array, Class type) {
        if (array == null || array.isEmpty()) {
            return null;
        }
        JSONSchema[] items = new JSONSchema[array.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = JSONSchema.of(array.getJSONObject(i), type);
        }
        AnyOf anyOf = new AnyOf(items);

        return anyOf;
    }

    static AllOf allOf(JSONObject input, Class type) {
        JSONArray array = input.getJSONArray("allOf");
        if (array == null || array.isEmpty()) {
            return null;
        }

        JSONSchema[] items = new JSONSchema[array.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = JSONSchema.of(array.getJSONObject(i), type);
        }
        return new AllOf(items);
    }

    static OneOf oneOf(JSONObject input, Class type) {
        JSONArray array = input.getJSONArray("oneOf");
        if (array == null || array.isEmpty()) {
            return null;
        }

        JSONSchema[] items = new JSONSchema[array.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = JSONSchema.of(array.getJSONObject(i), type);
        }
        return new OneOf(items);
    }

    static OneOf oneOf(JSONArray array, Class type) {
        if (array == null || array.isEmpty()) {
            return null;
        }

        JSONSchema[] items = new JSONSchema[array.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = JSONSchema.of(array.getJSONObject(i), type);
        }
        return new OneOf(items);
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
        OneOf,
        AllOf,
        AnyOf,
        Any;

        public static Type of(String typeStr) {
            if (typeStr == null) {
                return null;
            }

            switch (typeStr) {
                case "Null":
                case "null":
                    return Type.Null;
                case "String":
                case "string":
                    return Type.String;
                case "Integer":
                case "integer":
                    return Type.Integer;
                case "Number":
                case "number":
                    return Type.Number;
                case "Boolean":
                case "boolean":
                    return Type.Boolean;
                case "Object":
                case "object":
                    return Type.Object;
                case "Array":
                case "array":
                    return Type.Array;
                default:
                    return null;
            }
        }
    }

    static final ValidateResult SUCCESS = new ValidateResult(true, "success");
    static final ValidateResult FAIL_INPUT_NULL = new ValidateResult(false, "input null");
    static final ValidateResult FAIL_ANY_OF = new ValidateResult(false, "anyOf fail");
    static final ValidateResult FAIL_ONE_OF = new ValidateResult(false, "oneOf fail");
    static final ValidateResult FAIL_NOT = new ValidateResult(false, "not fail");
    static final ValidateResult FAIL_TYPE_NOT_MATCH = new ValidateResult(false, "type not match");
    static final ValidateResult FAIL_PROPERTY_NAME = new ValidateResult(false, "propertyName not match");

    static final ValidateResult CONTAINS_NOT_MATCH = new ValidateResult(false, "contains not match");
    static final ValidateResult UNIQUE_ITEMS_NOT_MATCH = new ValidateResult(false, "uniqueItems not match");
    static final ValidateResult REQUIRED_NOT_MATCH = new ValidateResult(false, "required");
}
