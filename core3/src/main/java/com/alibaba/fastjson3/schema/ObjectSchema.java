package com.alibaba.fastjson3.schema;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONArray;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONObject;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class ObjectSchema extends JSONSchema {
    final boolean typed;
    final Map<String, JSONSchema> definitions;
    final Map<String, JSONSchema> defs;
    final Map<String, JSONSchema> properties;
    final Set<String> required;
    final boolean additionalProperties;
    final JSONSchema additionalPropertySchema;

    final PatternProperty[] patternProperties;
    final JSONSchema propertyNames;
    final int minProperties;
    final int maxProperties;

    final Map<String, String[]> dependentRequired;
    final Map<String, JSONSchema> dependentSchemas;

    final JSONSchema ifSchema;
    final JSONSchema thenSchema;
    final JSONSchema elseSchema;
    final AllOf allOf;
    final AnyOf anyOf;
    final OneOf oneOf;
    final boolean encoded;

    transient List<UnresolvedReference.ResolveTask> resolveTasks;

    public ObjectSchema(JSONObject input) {
        this(input, null);
    }

    public ObjectSchema(JSONObject input, JSONSchema root) {
        super(input);

        this.typed = "object".equalsIgnoreCase(input.getString("type"));
        this.properties = new LinkedHashMap<>();
        this.definitions = new LinkedHashMap<>();
        this.defs = new LinkedHashMap<>();
        this.encoded = getBool(input, "encoded", false);

        // Parse definitions (draft 4)
        JSONObject definitionsObj = input.getJSONObject("definitions");
        if (definitionsObj != null) {
            for (Map.Entry<String, Object> entry : definitionsObj.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                JSONSchema schema;
                if (value instanceof Boolean b) {
                    schema = b ? Any.INSTANCE : Any.NOT_ANY;
                } else {
                    schema = JSONSchema.of((JSONObject) value, root == null ? this : root);
                }
                this.definitions.put(key, schema);
            }
        }

        // Parse $defs (draft 2020-12)
        JSONObject defsObj = input.getJSONObject("$defs");
        if (defsObj != null) {
            for (Map.Entry<String, Object> entry : defsObj.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                JSONSchema schema;
                if (value instanceof Boolean b) {
                    schema = b ? Any.INSTANCE : Any.NOT_ANY;
                } else {
                    schema = JSONSchema.of((JSONObject) value, root == null ? this : root);
                }
                this.defs.put(key, schema);
            }
            if (resolveTasks != null) {
                for (UnresolvedReference.ResolveTask task : resolveTasks) {
                    task.resolve(this);
                }
            }
        }

        // Parse properties
        JSONObject propertiesObj = input.getJSONObject("properties");
        if (propertiesObj != null) {
            for (Map.Entry<String, Object> entry : propertiesObj.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                JSONSchema schema;
                if (value instanceof Boolean b) {
                    schema = b ? Any.INSTANCE : Any.NOT_ANY;
                } else if (value instanceof JSONSchema s) {
                    schema = s;
                } else {
                    schema = JSONSchema.of((JSONObject) value, root == null ? this : root);
                }
                this.properties.put(key, schema);
                if (schema instanceof UnresolvedReference ur) {
                    UnresolvedReference.PropertyResolveTask task =
                            new UnresolvedReference.PropertyResolveTask(this.properties, key, ur.refName);
                    JSONSchema resolveRoot = root == null ? this : root;
                    resolveRoot.addResolveTask(task);
                }
            }
        }

        // Parse patternProperties
        JSONObject patternPropsObj = input.getJSONObject("patternProperties");
        if (patternPropsObj != null) {
            this.patternProperties = new PatternProperty[patternPropsObj.size()];
            int index = 0;
            for (Map.Entry<String, Object> entry : patternPropsObj.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                JSONSchema schema;
                if (value instanceof Boolean b) {
                    schema = b ? Any.INSTANCE : Any.NOT_ANY;
                } else {
                    schema = JSONSchema.of((JSONObject) value, root == null ? this : root);
                }
                this.patternProperties[index++] = new PatternProperty(
                        Pattern.compile(key, Pattern.UNICODE_CHARACTER_CLASS), schema);
            }
        } else {
            this.patternProperties = new PatternProperty[0];
        }

        // Parse required
        JSONArray requiredArr = input.getJSONArray("required");
        if (requiredArr != null && !requiredArr.isEmpty()) {
            this.required = new LinkedHashSet<>(requiredArr.size());
            for (int i = 0; i < requiredArr.size(); i++) {
                this.required.add(requiredArr.getString(i));
            }
        } else {
            this.required = Collections.emptySet();
        }

        // Parse additionalProperties
        Object additionalProps = input.get("additionalProperties");
        if (additionalProps instanceof Boolean b) {
            this.additionalPropertySchema = null;
            this.additionalProperties = b;
        } else if (additionalProps instanceof JSONObject addObj) {
            this.additionalPropertySchema = JSONSchema.of(addObj, root);
            this.additionalProperties = false;
        } else {
            this.additionalPropertySchema = null;
            this.additionalProperties = true;
        }

        // Parse propertyNames
        Object propertyNamesObj = input.get("propertyNames");
        if (propertyNamesObj instanceof Boolean b) {
            this.propertyNames = b ? Any.INSTANCE : Any.NOT_ANY;
        } else if (propertyNamesObj instanceof JSONObject pnObj) {
            this.propertyNames = new StringSchema(pnObj);
        } else {
            this.propertyNames = null;
        }

        this.minProperties = getInt(input, "minProperties", -1);
        this.maxProperties = getInt(input, "maxProperties", -1);

        // Parse dependentRequired
        JSONObject depReqObj = input.getJSONObject("dependentRequired");
        if (depReqObj != null && !depReqObj.isEmpty()) {
            this.dependentRequired = new LinkedHashMap<>(depReqObj.size());
            for (String key : depReqObj.keySet()) {
                JSONArray arr = depReqObj.getJSONArray(key);
                String[] deps = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    deps[i] = arr.getString(i);
                }
                this.dependentRequired.put(key, deps);
            }
        } else {
            this.dependentRequired = null;
        }

        // Parse dependentSchemas (values can be Boolean or JSONObject)
        JSONObject depSchObj = input.getJSONObject("dependentSchemas");
        if (depSchObj != null && !depSchObj.isEmpty()) {
            this.dependentSchemas = new LinkedHashMap<>(depSchObj.size());
            for (String key : depSchObj.keySet()) {
                Object val = depSchObj.get(key);
                if (val instanceof Boolean b) {
                    this.dependentSchemas.put(key, b ? Any.INSTANCE : Any.NOT_ANY);
                } else {
                    this.dependentSchemas.put(key, JSONSchema.of((JSONObject) val));
                }
            }
        } else {
            this.dependentSchemas = null;
        }

        // Parse if/then/else (each can be Boolean or JSONObject)
        this.ifSchema = parseSchemaProp(input, "if");
        this.thenSchema = parseSchemaProp(input, "then");
        this.elseSchema = parseSchemaProp(input, "else");

        // Parse composition
        allOf = JSONSchema.allOf(input, null);
        anyOf = JSONSchema.anyOf(input, null);
        oneOf = JSONSchema.oneOf(input, null);
    }

    @Override
    void addResolveTask(UnresolvedReference.ResolveTask task) {
        if (resolveTasks == null) {
            resolveTasks = new ArrayList<>();
        }
        resolveTasks.add(task);
    }

    @Override
    public Type getType() {
        return Type.Object;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ValidateResult validate(Map map) {
        // Check required
        for (String item : required) {
            if (!map.containsKey(item)) {
                return new ValidateResult(false, "required %s", item);
            }
        }

        // Validate properties
        for (Map.Entry<String, JSONSchema> entry : properties.entrySet()) {
            String key = entry.getKey();
            JSONSchema schema = entry.getValue();
            Object propertyValue = map.get(key);
            if (propertyValue == null && !map.containsKey(key)) {
                continue;
            }
            ValidateResult result = schema.validate(propertyValue);
            if (!result.isSuccess()) {
                return result.atPath(key);
            }
        }

        // Validate patternProperties
        for (PatternProperty pp : patternProperties) {
            for (Object entryObj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                Object entryKey = entry.getKey();
                if (entryKey instanceof String strKey && pp.pattern.matcher(strKey).find()) {
                    ValidateResult result = pp.schema.validate(entry.getValue());
                    if (!result.isSuccess()) {
                        return result;
                    }
                }
            }
        }

        // Check additionalProperties
        if (!additionalProperties) {
            for (Object entryObj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                Object key = entry.getKey();
                if (properties.containsKey(key)) {
                    continue;
                }

                boolean matchedPattern = false;
                for (PatternProperty pp : patternProperties) {
                    if (key instanceof String strKey && pp.pattern.matcher(strKey).find()) {
                        matchedPattern = true;
                        break;
                    }
                }
                if (matchedPattern) {
                    continue;
                }

                if (additionalPropertySchema != null) {
                    ValidateResult result = additionalPropertySchema.validate(entry.getValue());
                    if (!result.isSuccess()) {
                        return result;
                    }
                    continue;
                }

                return new ValidateResult(false, "additional property '%s' not allowed", key);
            }
        }

        // Validate propertyNames
        if (propertyNames != null) {
            for (Object key : map.keySet()) {
                ValidateResult result = propertyNames.validate(key);
                if (!result.isSuccess()) {
                    return FAIL_PROPERTY_NAME;
                }
            }
        }

        // Check minProperties/maxProperties
        if (minProperties >= 0 && map.size() < minProperties) {
            return new ValidateResult(false, "minProperties not match, expect %s, but %s", minProperties, map.size());
        }
        if (maxProperties >= 0 && map.size() > maxProperties) {
            return new ValidateResult(false, "maxProperties not match, expect %s, but %s", maxProperties, map.size());
        }

        // Check dependentRequired
        if (dependentRequired != null) {
            for (Map.Entry<String, String[]> entry : dependentRequired.entrySet()) {
                String key = entry.getKey();
                if (map.containsKey(key)) {
                    for (String depProp : entry.getValue()) {
                        if (!map.containsKey(depProp)) {
                            return new ValidateResult(false, "property %s, dependentRequired property %s", key, depProp);
                        }
                    }
                }
            }
        }

        // Check dependentSchemas
        if (dependentSchemas != null) {
            for (Map.Entry<String, JSONSchema> entry : dependentSchemas.entrySet()) {
                if (map.containsKey(entry.getKey())) {
                    ValidateResult result = entry.getValue().validate(map);
                    if (!result.isSuccess()) {
                        return result;
                    }
                }
            }
        }

        // Check if/then/else
        if (ifSchema != null) {
            ValidateResult ifResult = ifSchema.validate(map);
            if (ifResult.isSuccess()) {
                if (thenSchema != null) {
                    ValidateResult thenResult = thenSchema.validate(map);
                    if (!thenResult.isSuccess()) {
                        return thenResult;
                    }
                }
            } else {
                if (elseSchema != null) {
                    ValidateResult elseResult = elseSchema.validate(map);
                    if (!elseResult.isSuccess()) {
                        return elseResult;
                    }
                }
            }
        }

        // Check composition
        if (allOf != null) {
            ValidateResult result = allOf.validate(map);
            if (!result.isSuccess()) {
                return result;
            }
        }
        if (anyOf != null) {
            ValidateResult result = anyOf.validate(map);
            if (!result.isSuccess()) {
                return result;
            }
        }
        if (oneOf != null) {
            ValidateResult result = oneOf.validate(map);
            if (!result.isSuccess()) {
                return result;
            }
        }

        return SUCCESS;
    }

    @Override
    protected ValidateResult validateInternal(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        if (encoded) {
            if (value instanceof String str) {
                try {
                    value = JSON.parseObject(str);
                } catch (JSONException e) {
                    return FAIL_INPUT_NOT_ENCODED;
                }
            } else {
                return FAIL_INPUT_NOT_ENCODED;
            }
        }

        if (value instanceof Map<?, ?> map) {
            return validate(map);
        }

        // POJO validation: extract fields via reflection, then validate as Map
        Class<?> valueClass = value.getClass();
        if (!valueClass.isPrimitive()
                && !(value instanceof Number)
                && !(value instanceof CharSequence)
                && !(value instanceof Boolean)
                && !(value instanceof java.util.Collection)
                && !valueClass.isArray()) {
            Map<String, Object> fieldMap = extractFieldValues(value);
            return validate(fieldMap);
        }

        // For non-object values: still apply if/then/else and composition if present
        if (!typed) {
            return validateConditionalAndComposition(value);
        }
        return new ValidateResult(false, "expect type %s, but %s", Type.Object, valueClass);
    }

    // ==================== Serialization ====================

    @Override
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("type", "object");
        if (title != null) {
            obj.put("title", title);
        }
        if (description != null) {
            obj.put("description", description);
        }
        if (!definitions.isEmpty()) {
            obj.put("definitions", definitions);
        }
        if (!defs.isEmpty()) {
            obj.put("$defs", defs);
        }
        if (!properties.isEmpty()) {
            obj.put("properties", properties);
        }
        if (!required.isEmpty()) {
            obj.put("required", required);
        }
        if (!additionalProperties) {
            if (additionalPropertySchema != null) {
                obj.put("additionalProperties", additionalPropertySchema.toJSONObject());
            } else {
                obj.put("additionalProperties", false);
            }
        }
        if (patternProperties.length > 0) {
            JSONObject pp = new JSONObject();
            for (PatternProperty p : patternProperties) {
                pp.put(p.pattern.pattern(), p.schema.toJSONObject());
            }
            obj.put("patternProperties", pp);
        }
        if (propertyNames != null) {
            obj.put("propertyNames", propertyNames.toJSONObject());
        }
        if (minProperties >= 0) {
            obj.put("minProperties", minProperties);
        }
        if (maxProperties >= 0) {
            obj.put("maxProperties", maxProperties);
        }
        if (dependentRequired != null) {
            obj.put("dependentRequired", dependentRequired);
        }
        if (dependentSchemas != null) {
            obj.put("dependentSchemas", dependentSchemas);
        }
        if (ifSchema != null) {
            obj.put("if", ifSchema.toJSONObject());
        }
        if (thenSchema != null) {
            obj.put("then", thenSchema.toJSONObject());
        }
        if (elseSchema != null) {
            obj.put("else", elseSchema.toJSONObject());
        }
        if (allOf != null) {
            obj.put("allOf", allOf.toJSONObject().get("allOf"));
        }
        if (anyOf != null) {
            obj.put("anyOf", anyOf.toJSONObject().get("anyOf"));
        }
        if (oneOf != null) {
            obj.put("oneOf", oneOf.toJSONObject().get("oneOf"));
        }
        return obj;
    }

    // ==================== Accessors ====================

    public Map<String, JSONSchema> getProperties() {
        return properties;
    }

    public JSONSchema getProperty(String key) {
        return properties.get(key);
    }

    public Set<String> getRequired() {
        return required;
    }

    public JSONSchema getDefs(String def) {
        return this.defs.get(def);
    }

    // ==================== Visitor ====================

    @Override
    public void accept(Predicate<JSONSchema> v) {
        if (v.test(this)) {
            this.properties.values().forEach(v::test);
        }
    }

    private ValidateResult validateConditionalAndComposition(Object value) {
        if (ifSchema != null) {
            ValidateResult ifResult = ifSchema.validate(value);
            if (ifResult.isSuccess()) {
                if (thenSchema != null) {
                    ValidateResult thenResult = thenSchema.validate(value);
                    if (!thenResult.isSuccess()) {
                        return thenResult;
                    }
                }
            } else {
                if (elseSchema != null) {
                    ValidateResult elseResult = elseSchema.validate(value);
                    if (!elseResult.isSuccess()) {
                        return elseResult;
                    }
                }
            }
        }
        if (allOf != null) {
            ValidateResult result = allOf.validate(value);
            if (!result.isSuccess()) {
                return result;
            }
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

    private static JSONSchema parseSchemaProp(JSONObject input, String key) {
        Object val = input.get(key);
        if (val instanceof Boolean b) {
            return b ? Any.INSTANCE : Any.NOT_ANY;
        }
        if (val instanceof JSONObject obj) {
            return JSONSchema.of(obj);
        }
        return null;
    }

    // ==================== POJO Field Access (Cached + Unsafe) ====================

    private static final java.util.concurrent.ConcurrentHashMap<Class<?>, FieldMeta[]> POJO_FIELD_CACHE
            = new java.util.concurrent.ConcurrentHashMap<>();

    private static FieldMeta[] getFieldMetas(Class<?> clazz) {
        return POJO_FIELD_CACHE.computeIfAbsent(clazz, ObjectSchema::buildFieldMetas);
    }

    private static FieldMeta[] buildFieldMetas(Class<?> clazz) {
        java.util.List<FieldMeta> metas = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(mod)
                        || java.lang.reflect.Modifier.isTransient(mod)) {
                    continue;
                }
                if (seen.add(f.getName())) {
                    f.setAccessible(true);
                    long offset = com.alibaba.fastjson3.util.JDKUtils.UNSAFE_AVAILABLE
                            ? com.alibaba.fastjson3.util.JDKUtils.objectFieldOffset(f) : -1;
                    metas.add(new FieldMeta(f.getName(), f, offset, f.getType()));
                }
            }
            c = c.getSuperclass();
        }
        return metas.toArray(new FieldMeta[0]);
    }

    private static Map<String, Object> extractFieldValues(Object pojo) {
        FieldMeta[] metas = getFieldMetas(pojo.getClass());
        Map<String, Object> map = new LinkedHashMap<>(metas.length);
        for (FieldMeta m : metas) {
            Object value = m.getValue(pojo);
            if (value != null || m.fieldType.isPrimitive()) {
                map.put(m.name, value);
            }
        }
        return map;
    }

    static final class FieldMeta {
        final String name;
        final java.lang.reflect.Field field;
        final long offset;
        final Class<?> fieldType;

        FieldMeta(String name, java.lang.reflect.Field field, long offset, Class<?> fieldType) {
            this.name = name;
            this.field = field;
            this.offset = offset;
            this.fieldType = fieldType;
        }

        Object getValue(Object bean) {
            if (offset >= 0) {
                if (fieldType == int.class) {
                    return com.alibaba.fastjson3.util.JDKUtils.getInt(bean, offset);
                }
                if (fieldType == long.class) {
                    return com.alibaba.fastjson3.util.JDKUtils.getLongField(bean, offset);
                }
                if (fieldType == boolean.class) {
                    return com.alibaba.fastjson3.util.JDKUtils.getBoolean(bean, offset);
                }
                if (fieldType == double.class) {
                    return com.alibaba.fastjson3.util.JDKUtils.getDouble(bean, offset);
                }
                if (fieldType == float.class) {
                    return com.alibaba.fastjson3.util.JDKUtils.getFloat(bean, offset);
                }
                if (!fieldType.isPrimitive()) {
                    return com.alibaba.fastjson3.util.JDKUtils.getObject(bean, offset);
                }
            }
            // Fallback for short/byte/char or when Unsafe unavailable
            try {
                return field.get(bean);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    // ==================== Inner Classes ====================

    static final class PatternProperty {
        final Pattern pattern;
        final JSONSchema schema;

        PatternProperty(Pattern pattern, JSONSchema schema) {
            this.pattern = pattern;
            this.schema = schema;
        }
    }
}
