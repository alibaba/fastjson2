package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONCreator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson2.util.UUIDUtils.parse4Nibbles;

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

    static Not ofNot(JSONObject input, Class type) {
        JSONObject object = input.getJSONObject("not");
        JSONSchema schema = of(object, type);
        return new Not(schema);
    }

    public static JSONSchema of(String schema) {
        return of(
                JSON.parseObject(schema)
        );
    }

    @JSONCreator
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

            if (input.containsKey("properties") || input.containsKey("dependentSchemas") || input.containsKey("if") || input.containsKey("required")) {
                return new ObjectSchema(input);
            }

            if (input.containsKey("pattern")) {
                return new StringSchema(input);
            }

            if (input.containsKey("allOf")) {
                return new AllOf(input);
            }

            if (input.containsKey("anyOf")) {
                return new AnyOf(input);
            }

            if (input.containsKey("oneOf")) {
                return new OneOf(input);
            }

            if (input.containsKey("not")) {
                return ofNot(input, null);
            }

            if (input.get("maximum") instanceof Number
                    || input.get("minimum") instanceof Number) {
                return new NumberSchema(input);
            }

            if (input.containsKey("maxItems")
                    || input.containsKey("minItems")
                    || input.containsKey("additionalItems")
                    || input.containsKey("items")
                    || input.containsKey("prefixItems")
            ) {
                return new ArraySchema(input);
            }

            if (input.isEmpty()) {
                return Any.INSTANCE;
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
        AllOf,
        Any,
    }

    static final ValidateResult.Success SUCCESS = new ValidateResult.Success();
    static final ValidateResult.Fail FAIL_INPUT_NULL = new ValidateResult.Fail("input null");
    static final ValidateResult.Fail FAIL_ANY_OF = new ValidateResult.Fail("anyOf fail");
    static final ValidateResult.Fail FAIL_ONE_OF = new ValidateResult.Fail("oneOf fail");
    static final ValidateResult.Fail FAIL_NOT = new ValidateResult.Fail("not fail");


    final static ValidateResult.Fail CONTAINS_NOT_MATCH = new ValidateResult.Fail("contains not match");
    final static ValidateResult.Fail UNIQUE_ITEMS_NOT_MATCH = new ValidateResult.Fail("uniqueItems not match");
    final static ValidateResult.Fail REQUIRED_NOT_MATCH = new ValidateResult.Fail("required");

}
