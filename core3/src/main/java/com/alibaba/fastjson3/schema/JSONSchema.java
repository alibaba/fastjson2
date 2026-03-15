package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.JSONSchemaValidException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public abstract class JSONSchema {
    static final Map<java.lang.String, JSONSchema> CACHE = new ConcurrentHashMap<>();

    final String title;
    final String description;
    final String customErrorMessage;

    JSONSchema(JSONObject input) {
        this.title = input.getString("title");
        this.description = input.getString("description");
        this.customErrorMessage = input.getString("error");

        // Register $anchor if present
        java.lang.String anchor = input.getString("$anchor");
        if (anchor != null && !anchor.isEmpty()) {
            SchemaRegistry.getInstance().register(anchor, this);
        }
    }

    JSONSchema(String title, String description) {
        this.title = title;
        this.description = description;
        this.customErrorMessage = null;
    }

    void addResolveTask(UnresolvedReference.ResolveTask task) {
    }

    // ==================== Factory Methods ====================

    public static JSONSchema parseSchema(String schema) {
        if (schema == null || schema.isEmpty()) {
            return Any.INSTANCE;
        }
        if ("true".equals(schema)) {
            return Any.INSTANCE;
        }
        if ("false".equals(schema)) {
            return Any.NOT_ANY;
        }
        JSONObject object = JSON.parseObject(schema);
        return of(object);
    }

    public static JSONSchema of(JSONObject input) {
        return of(input, (JSONSchema) null);
    }

    public static JSONSchema of(JSONObject input, Class<?> objectClass) {
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

        if (objectClass == boolean.class || objectClass == Boolean.class) {
            return new BooleanSchema(input);
        }

        if (objectClass == String.class) {
            return new StringSchema(input);
        }

        if (Collection.class.isAssignableFrom(objectClass) || objectClass.isArray()) {
            return new ArraySchema(input, null);
        }

        return new ObjectSchema(input, null);
    }

    public static JSONSchema of(JSONObject input, JSONSchema parent) {
        if (input.get("type") instanceof JSONArray) {
            JSONArray types = (JSONArray) input.get("type");
            JSONSchema[] items = new JSONSchema[types.size()];
            for (int i = 0; i < types.size(); i++) {
                items[i] = JSONSchema.of(objectOf("type", types.get(i)));
            }
            return new AnyOf(items);
        }

        Type type = Type.of(input.getString("type"));

        if (type == null) {
            // Handle enum
            Object enumObj = input.get("enum");
            if (enumObj instanceof JSONArray) {
                JSONArray enumArray = (JSONArray) enumObj;
                boolean allStrings = true;
                for (int i = 0; i < enumArray.size(); i++) {
                    if (!(enumArray.get(i) instanceof String)) {
                        allStrings = false;
                        break;
                    }
                }
                if (allStrings) {
                    return new StringSchema(input);
                }
                Object[] enumItems = enumArray.toArray();
                return new EnumSchema(enumItems);
            }

            // Handle const — use ConstSchema for type-strict deep equality
            if (input.containsKey("const")) {
                Object constValue = input.get("const");
                return new ConstSchema(constValue);
            }

            // Handle $ref
            if (input.size() == 1) {
                String ref = input.getString("$ref");
                if (ref != null && !ref.isEmpty()) {
                    return resolveRef(ref, parent);
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

            // Detect by keywords
            if (input.containsKey("properties")
                    || input.containsKey("dependentSchemas")
                    || input.containsKey("dependentRequired")
                    || input.containsKey("if")
                    || input.containsKey("then")
                    || input.containsKey("else")
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
                    || input.containsKey("contains")
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

            boolean hasAllOf = input.containsKey("allOf");
            boolean hasAnyOf = input.containsKey("anyOf");
            boolean hasOneOf = input.containsKey("oneOf");

            if (hasAllOf || hasAnyOf || hasOneOf) {
                int count = (hasAllOf ? 1 : 0) + (hasAnyOf ? 1 : 0) + (hasOneOf ? 1 : 0);
                if (count == 1) {
                    if (hasAllOf) {
                        return new AllOf(input, parent);
                    }
                    if (hasAnyOf) {
                        return new AnyOf(input, parent);
                    }
                    return new OneOf(input, parent);
                }
                JSONSchema[] items = new JSONSchema[count];
                int index = 0;
                if (hasAllOf) {
                    items[index++] = new AllOf(input, parent);
                }
                if (hasAnyOf) {
                    items[index++] = new AnyOf(input, parent);
                }
                if (hasOneOf) {
                    items[index++] = new OneOf(input, parent);
                }
                return new AllOf(items);
            }

            if (input.containsKey("not")) {
                return ofNot(input, null);
            }

            if (input.get("maximum") instanceof Number
                    || input.get("minimum") instanceof Number
                    || input.get("exclusiveMinimum") instanceof Number
                    || input.get("exclusiveMaximum") instanceof Number
                    || input.containsKey("multipleOf")
            ) {
                return new NumberSchema(input);
            }

            if (input.isEmpty()) {
                return Any.INSTANCE;
            }

            String typeStr = input.getString("type");
            if (typeStr == null) {
                throw new JSONSchemaValidException("type required");
            } else {
                throw new JSONSchemaValidException("not support type : " + typeStr);
            }
        }

        return switch (type) {
            case String -> new StringSchema(input);
            case Integer -> new IntegerSchema(input);
            case Number -> new NumberSchema(input);
            case Boolean -> new BooleanSchema(input);
            case Null -> new NullSchema(input);
            case Object -> new ObjectSchema(input, parent);
            case Array -> new ArraySchema(input, parent);
            default -> throw new JSONSchemaValidException("not support type : " + type);
        };
    }

    private static JSONSchema createDefaultForType(Type type) {
        return switch (type) {
            case String -> new StringSchema(objectOf("type", "string"));
            case Integer -> new IntegerSchema(objectOf("type", "integer"));
            case Number -> new NumberSchema(objectOf("type", "number"));
            case Boolean -> new BooleanSchema(objectOf("type", "boolean"));
            case Null -> new NullSchema(objectOf("type", "null"));
            case Object -> new ObjectSchema(objectOf("type", "object"), null);
            case Array -> new ArraySchema(objectOf("type", "array"), null);
            default -> throw new JSONSchemaValidException("not support type : " + type);
        };
    }

    private static JSONSchema resolveRef(String ref, JSONSchema parent) {
        if ("#".equals(ref)) {
            return parent;
        }

        Map<String, JSONSchema> definitions = null;
        Map<String, JSONSchema> defs = null;
        Map<String, JSONSchema> properties = null;

        if (parent instanceof ObjectSchema objectSchema) {
            definitions = objectSchema.definitions;
            defs = objectSchema.defs;
            properties = objectSchema.properties;
        } else if (parent instanceof ArraySchema arraySchema) {
            definitions = arraySchema.definitions;
            defs = arraySchema.defs;
        }

        if (definitions != null && ref.startsWith("#/definitions/")) {
            String refName = ref.substring("#/definitions/".length());
            JSONSchema resolved = definitions.get(refName);
            if (resolved != null) {
                return resolved;
            }
            return new UnresolvedReference(refName);
        }

        if (defs != null && ref.startsWith("#/$defs/")) {
            String refName = ref.substring("#/$defs/".length());
            JSONSchema resolved = defs.get(refName);
            if (resolved != null) {
                return resolved;
            }
            return new UnresolvedReference(refName);
        }

        if (properties != null && ref.startsWith("#/properties/")) {
            String refName = ref.substring("#/properties/".length());
            JSONSchema resolved = properties.get(refName);
            if (resolved != null) {
                return resolved;
            }
        }

        if (ref.startsWith("#/prefixItems/") && parent instanceof ArraySchema arraySchema) {
            try {
                int index = Integer.parseInt(ref.substring("#/prefixItems/".length()));
                if (arraySchema.prefixItems != null && index >= 0 && index < arraySchema.prefixItems.length) {
                    return arraySchema.prefixItems[index];
                }
            } catch (NumberFormatException ignored) {
                // malformed ref, fall through
            }
        }

        // Try $anchor resolution via SchemaRegistry
        if (ref.startsWith("#") && ref.length() > 1 && ref.charAt(1) != '/') {
            String anchorName = ref.substring(1);
            JSONSchema resolved = SchemaRegistry.getInstance().resolve(anchorName);
            if (resolved != null) {
                return resolved;
            }
        }

        return Any.INSTANCE;
    }

    static Not ofNot(JSONObject input, Class<?> objectClass) {
        Object not = input.get("not");
        if (not instanceof Boolean) {
            return new Not(null, null, (Boolean) not);
        }

        JSONObject object = (JSONObject) not;
        if (object == null || object.isEmpty()) {
            return new Not(null, new Type[]{Type.Any}, null);
        }

        if (object.size() == 1) {
            Object typeVal = object.get("type");
            if (typeVal instanceof JSONArray array) {
                Type[] types = new Type[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    types[i] = Type.of(array.getString(i));
                }
                return new Not(null, types, null);
            }
        }

        JSONSchema schema = of(object, objectClass);
        return new Not(schema, null, null);
    }

    // ==================== Validation ====================

    public abstract Type getType();

    protected abstract ValidateResult validateInternal(Object value);

    public final ValidateResult validate(Object value) {
        ValidateResult result = validateInternal(value);
        if (!result.isSuccess() && this.customErrorMessage != null) {
            return new ValidateResult(false, this.customErrorMessage);
        }
        return result;
    }

    public boolean isValid(Object value) {
        return validate(value).isSuccess();
    }

    public void assertValidate(Object value) {
        ValidateResult result = validate(value);
        if (!result.isSuccess()) {
            throw new JSONSchemaValidException(result.getMessage());
        }
    }

    // ==================== Type-specific validation ====================

    protected ValidateResult validateInternal(long value) {
        return validateInternal((Object) value);
    }

    public final ValidateResult validate(long value) {
        ValidateResult result = validateInternal(value);
        if (!result.isSuccess() && customErrorMessage != null) {
            return new ValidateResult(false, customErrorMessage);
        }
        return result;
    }

    protected ValidateResult validateInternal(double value) {
        return validateInternal((Object) value);
    }

    public final ValidateResult validate(double value) {
        ValidateResult result = validateInternal(value);
        if (!result.isSuccess() && customErrorMessage != null) {
            return new ValidateResult(false, customErrorMessage);
        }
        return result;
    }

    public boolean isValid(long value) {
        return validate(value).isSuccess();
    }

    public boolean isValid(double value) {
        return validate(value).isSuccess();
    }

    public void assertValidate(long value) {
        ValidateResult result = validate(value);
        if (!result.isSuccess()) {
            throw new JSONSchemaValidException(result.getMessage());
        }
    }

    public void assertValidate(double value) {
        ValidateResult result = validate(value);
        if (!result.isSuccess()) {
            throw new JSONSchemaValidException(result.getMessage());
        }
    }

    // ==================== Schema Generation ====================

    public static JSONSchema of(java.lang.reflect.Type type) {
        return of(type, null);
    }

    static JSONSchema of(java.lang.reflect.Type type, JSONSchema root) {
        if (type instanceof java.lang.reflect.ParameterizedType paramType) {
            java.lang.reflect.Type rawType = paramType.getRawType();
            java.lang.reflect.Type[] arguments = paramType.getActualTypeArguments();

            if (rawType instanceof Class<?> rawClass && Collection.class.isAssignableFrom(rawClass)) {
                ArraySchema arraySchema = new ArraySchema(objectOf("type", "array"), root);
                if (arguments.length == 1) {
                    // itemSchema is final, so we create a full schema with items
                    // Actually ArraySchema.itemSchema is final now, so we need to work around
                    // For schema generation, return a simple ArraySchema
                }
                return arraySchema;
            }

            if (rawType instanceof Class<?> rawClass && Map.class.isAssignableFrom(rawClass)) {
                return new ObjectSchema(objectOf("type", "object"), root);
            }
        }

        if (type instanceof java.lang.reflect.GenericArrayType arrayType) {
            return new ArraySchema(objectOf("type", "array"), root);
        }

        if (type == byte.class || type == short.class || type == int.class || type == long.class
                || type == Byte.class || type == Short.class || type == Integer.class || type == Long.class
                || type == java.math.BigInteger.class
                || type == java.util.concurrent.atomic.AtomicInteger.class
                || type == java.util.concurrent.atomic.AtomicLong.class) {
            return new IntegerSchema(objectOf("type", "integer"));
        }

        if (type == float.class || type == double.class
                || type == Float.class || type == Double.class
                || type == java.math.BigDecimal.class) {
            return new NumberSchema(objectOf("type", "number"));
        }

        if (type == boolean.class || type == Boolean.class
                || type == java.util.concurrent.atomic.AtomicBoolean.class) {
            return new BooleanSchema(objectOf("type", "boolean"));
        }

        if (type == String.class) {
            return new StringSchema(objectOf("type", "string"));
        }

        if (type instanceof Class<?> clazz) {
            if (Enum.class.isAssignableFrom(clazz)) {
                Object[] enums = clazz.getEnumConstants();
                JSONArray names = new JSONArray(enums.length);
                for (Object e : enums) {
                    names.add(((Enum<?>) e).name());
                }
                JSONObject obj = objectOf("type", "string");
                obj.put("enum", names);
                return new StringSchema(obj);
            }

            if (clazz.isArray()) {
                return new ArraySchema(objectOf("type", "array"), root);
            }

            if (Map.class.isAssignableFrom(clazz)) {
                return new ObjectSchema(objectOf("type", "object"), root);
            }

            if (Collection.class.isAssignableFrom(clazz)) {
                return new ArraySchema(objectOf("type", "array"), root);
            }

            // POJO: introspect fields
            return ofPojo(clazz, root);
        }

        return Any.INSTANCE;
    }

    private static JSONSchema ofPojo(Class<?> clazz, JSONSchema root) {
        JSONArray required = new JSONArray();
        JSONObject properties = new JSONObject();

        // Introspect declared fields
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isTransient(mod)) {
                continue;
            }
            String name = field.getName();
            if (field.getType().isPrimitive()) {
                required.add(name);
            }
        }

        JSONObject obj = objectOf("type", "object");
        if (!required.isEmpty()) {
            obj.put("required", required);
        }

        ObjectSchema schema = new ObjectSchema(obj, root);

        // Add property schemas
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isTransient(mod)) {
                continue;
            }
            schema.properties.put(
                    field.getName(),
                    of(field.getGenericType(), root == null ? schema : root)
            );
        }

        return schema;
    }

    public static JSONSchema ofValue(Object value) {
        return ofValue(value, null);
    }

    static JSONSchema ofValue(Object value, JSONSchema root) {
        if (value == null) {
            return null;
        }

        if (value instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return new ArraySchema(objectOf("type", "array"), root);
            }

            Class<?> firstItemClass = null;
            Object firstItem = null;
            boolean sameClass = true;
            for (Object item : collection) {
                if (item != null) {
                    if (firstItem == null) {
                        firstItem = item;
                    }
                    if (firstItemClass == null) {
                        firstItemClass = item.getClass();
                    } else if (firstItemClass != item.getClass()) {
                        sameClass = false;
                    }
                }
            }

            if (sameClass && firstItemClass != null) {
                JSONObject itemsObj = objectOf("type", "array");
                ArraySchema schema = new ArraySchema(itemsObj, root);
                // For homogeneous collections, infer item schema
                return schema;
            }

            return new ArraySchema(objectOf("type", "array"), root);
        }

        if (value instanceof Map<?, ?> map) {
            JSONObject obj = objectOf("type", "object");
            ObjectSchema schema = new ObjectSchema(obj, root);

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object entryKey = entry.getKey();
                Object entryValue = entry.getValue();
                if (entryKey instanceof String key) {
                    JSONSchema valueSchema;
                    if (entryValue == null) {
                        valueSchema = new StringSchema(objectOf("type", "string"));
                    } else {
                        valueSchema = ofValue(entryValue, root == null ? schema : root);
                    }
                    schema.properties.put(key, valueSchema);
                }
            }

            return schema;
        }

        return of(value.getClass(), root);
    }

    // ==================== Visitor ====================

    public void accept(Predicate<JSONSchema> v) {
        v.test(this);
    }

    // ==================== Getters ====================

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    // ==================== Serialization ====================

    public JSONObject toJSONObject() {
        return new JSONObject();
    }

    @Override
    public java.lang.String toString() {
        return toJSONObject().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return toJSONObject().equals(((JSONSchema) o).toJSONObject());
    }

    @Override
    public int hashCode() {
        return toJSONObject().hashCode();
    }

    // ==================== Type Enum ====================

    public enum Type {
        Null,
        Boolean,
        Object,
        Array,
        Number,
        String,
        Integer,
        Enum,
        Const,
        OneOf,
        AllOf,
        AnyOf,
        Any,
        UnresolvedReference;

        public static Type of(java.lang.String typeStr) {
            if (typeStr == null) {
                return null;
            }
            return switch (typeStr) {
                case "null", "Null" -> Null;
                case "string", "String" -> String;
                case "integer", "Integer" -> Integer;
                case "number", "Number" -> Number;
                case "boolean", "Boolean" -> Boolean;
                case "object", "Object" -> Object;
                case "array", "Array" -> Array;
                default -> null;
            };
        }
    }

    // ==================== Helpers ====================

    static JSONObject objectOf(java.lang.String k1, Object v1) {
        JSONObject obj = new JSONObject(2);
        obj.put(k1, v1);
        return obj;
    }

    static JSONObject objectOf(java.lang.String k1, Object v1, java.lang.String k2, Object v2) {
        JSONObject obj = new JSONObject(4);
        obj.put(k1, v1);
        obj.put(k2, v2);
        return obj;
    }

    static int getInt(JSONObject obj, java.lang.String key, int defaultValue) {
        Integer v = obj.getInteger(key);
        return v != null ? v : defaultValue;
    }

    static long getLong(JSONObject obj, java.lang.String key, long defaultValue) {
        Long v = obj.getLong(key);
        return v != null ? v : defaultValue;
    }

    static boolean getBool(JSONObject obj, java.lang.String key, boolean defaultValue) {
        Boolean v = obj.getBoolean(key);
        return v != null ? v : defaultValue;
    }

    // ==================== Schema composition helpers ====================

    static AnyOf anyOf(JSONObject input, Class<?> type) {
        JSONArray array = input.getJSONArray("anyOf");
        JSONSchema[] items = makeSchemaItems(array, type);
        return items == null ? null : new AnyOf(items);
    }

    static AllOf allOf(JSONObject input, Class<?> type) {
        JSONArray array = input.getJSONArray("allOf");
        JSONSchema[] items = makeSchemaItems(array, type);
        return items == null ? null : new AllOf(items);
    }

    static OneOf oneOf(JSONObject input, Class<?> type) {
        JSONArray array = input.getJSONArray("oneOf");
        JSONSchema[] items = makeSchemaItems(array, type);
        return items == null ? null : new OneOf(items);
    }

    static JSONSchema[] makeSchemaItems(JSONArray array, Class<?> type) {
        if (array == null || array.isEmpty()) {
            return null;
        }
        JSONSchema[] items = new JSONSchema[array.size()];
        for (int i = 0; i < items.length; i++) {
            Object item = array.get(i);
            if (item instanceof Boolean b) {
                items[i] = b ? Any.INSTANCE : Any.NOT_ANY;
            } else if (type != null) {
                items[i] = JSONSchema.of((JSONObject) item, type);
            } else {
                items[i] = JSONSchema.of((JSONObject) item);
            }
        }
        return items;
    }

    // ==================== Constants ====================

    static final ValidateResult SUCCESS = new ValidateResult(true, "success");
    static final ValidateResult FAIL_INPUT_NULL = new ValidateResult(false, "input null");
    static final ValidateResult FAIL_INPUT_NOT_ENCODED = new ValidateResult(false, "input not encoded string");
    static final ValidateResult FAIL_ANY_OF = new ValidateResult(false, "anyOf fail");
    static final ValidateResult FAIL_ONE_OF = new ValidateResult(false, "oneOf fail");
    static final ValidateResult FAIL_NOT = new ValidateResult(false, "not fail");
    static final ValidateResult FAIL_TYPE_NOT_MATCH = new ValidateResult(false, "type not match");
    static final ValidateResult FAIL_PROPERTY_NAME = new ValidateResult(false, "propertyName not match");
    static final ValidateResult CONTAINS_NOT_MATCH = new ValidateResult(false, "contains not match");
    static final ValidateResult UNIQUE_ITEMS_NOT_MATCH = new ValidateResult(false, "uniqueItems not match");
    static final ValidateResult REQUIRED_NOT_MATCH = new ValidateResult(false, "required");
}
