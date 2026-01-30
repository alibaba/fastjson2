package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class ObjectSchema
        extends JSONSchema {
    final boolean typed;
    final Map<String, JSONSchema> definitions;
    final Map<String, JSONSchema> defs;
    final Map<String, JSONSchema> properties;
    final Set<String> required;
    final boolean additionalProperties;
    final JSONSchema additionalPropertySchema;
    final long[] requiredHashCode;

    final PatternProperty[] patternProperties;
    final JSONSchema propertyNames;
    final int minProperties;
    final int maxProperties;

    final Map<String, String[]> dependentRequired;
    final Map<Long, long[]> dependentRequiredHashCodes;

    final Map<String, JSONSchema> dependentSchemas;
    final Map<Long, JSONSchema> dependentSchemasHashMapping;

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
        this.encoded = input.getBooleanValue("encoded", false);

        JSONObject definitions = input.getJSONObject("definitions");
        if (definitions != null) {
            for (Map.Entry<String, Object> entry : definitions.entrySet()) {
                String entryKey = entry.getKey();
                JSONObject entryValue = (JSONObject) entry.getValue();
                JSONSchema schema = JSONSchema.of(entryValue, root == null ? this : root);
                this.definitions.put(entryKey, schema);
            }
        }

        JSONObject defs = input.getJSONObject("$defs");
        if (defs != null) {
            for (Map.Entry<String, Object> entry : defs.entrySet()) {
                String entryKey = entry.getKey();
                JSONObject entryValue = (JSONObject) entry.getValue();
                JSONSchema schema = JSONSchema.of(entryValue, root == null ? this : root);
                this.defs.put(entryKey, schema);
            }
            if (resolveTasks != null) {
                for (UnresolvedReference.ResolveTask resolveTask : resolveTasks) {
                    resolveTask.resolve(this);
                }
            }
        }

        JSONObject properties = input.getJSONObject("properties");
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String entryKey = entry.getKey();
                Object entryValue = entry.getValue();
                JSONSchema schema;
                if (entryValue instanceof Boolean) {
                    schema = (Boolean) entryValue ? Any.INSTANCE : Any.NOT_ANY;
                } else if (entryValue instanceof JSONSchema) {
                    schema = (JSONSchema) entryValue;
                } else {
                    schema = JSONSchema.of((JSONObject) entryValue, root == null ? this : root);
                }
                this.properties.put(entryKey, schema);
                if (schema instanceof UnresolvedReference) {
                    String refName = ((UnresolvedReference) schema).refName;
                    UnresolvedReference.PropertyResolveTask task
                            = new UnresolvedReference.PropertyResolveTask(this.properties, entryKey, refName);
                    JSONSchema resolveRoot = root == null ? this : root;
                    resolveRoot.addResolveTask(task);
                }
            }
        }

        JSONObject patternProperties = input.getJSONObject("patternProperties");
        if (patternProperties != null) {
            this.patternProperties = new PatternProperty[patternProperties.size()];

            int index = 0;
            for (Map.Entry<String, Object> entry : patternProperties.entrySet()) {
                String entryKey = entry.getKey();
                Object entryValue = entry.getValue();
                JSONSchema schema;
                if (entryValue instanceof Boolean) {
                    schema = (Boolean) entryValue ? Any.INSTANCE : Any.NOT_ANY;
                } else {
                    schema = JSONSchema.of((JSONObject) entryValue, root == null ? this : root);
                }

                this.patternProperties[index++] = new PatternProperty(Pattern.compile(entryKey), schema);
            }
        } else {
            this.patternProperties = new PatternProperty[0];
        }

        JSONArray required = input.getJSONArray("required");
        if (required == null || required.isEmpty()) {
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
            this.additionalProperties = (Boolean) additionalProperties;
        } else {
            if (additionalProperties instanceof JSONObject) {
                this.additionalPropertySchema = JSONSchema.of((JSONObject) additionalProperties, root);
                this.additionalProperties = false;
            } else {
                this.additionalPropertySchema = null;
                this.additionalProperties = true;
            }
        }

        Object propertyNames = input.get("propertyNames");
        if (propertyNames == null) {
            this.propertyNames = null;
        } else if (propertyNames instanceof Boolean) {
            this.propertyNames = (Boolean) propertyNames ? Any.INSTANCE : Any.NOT_ANY;
        } else {
            this.propertyNames = new StringSchema((JSONObject) propertyNames);
        }

        this.minProperties = input.getIntValue("minProperties", -1);
        this.maxProperties = input.getIntValue("maxProperties", -1);

        JSONObject dependentRequired = input.getJSONObject("dependentRequired");
        if (dependentRequired != null && !dependentRequired.isEmpty()) {
            this.dependentRequired = new LinkedHashMap<>(dependentRequired.size(), 1F);
            this.dependentRequiredHashCodes = new LinkedHashMap<>(dependentRequired.size(), 1F);
            Set<String> keys = dependentRequired.keySet();
            for (String key : keys) {
                String[] dependentRequiredProperties = dependentRequired.getObject(key, String[].class);
                long[] dependentRequiredPropertiesHash = new long[dependentRequiredProperties.length];
                for (int i = 0; i < dependentRequiredProperties.length; i++) {
                    dependentRequiredPropertiesHash[i] = Fnv.hashCode64(dependentRequiredProperties[i]);
                }
                this.dependentRequired.put(key, dependentRequiredProperties);
                this.dependentRequiredHashCodes.put(Fnv.hashCode64(key), dependentRequiredPropertiesHash);
            }
        } else {
            this.dependentRequired = null;
            this.dependentRequiredHashCodes = null;
        }

        JSONObject dependentSchemas = input.getJSONObject("dependentSchemas");
        if (dependentSchemas != null && !dependentSchemas.isEmpty()) {
            this.dependentSchemas = new LinkedHashMap<>(dependentSchemas.size(), 1F);
            this.dependentSchemasHashMapping = new LinkedHashMap<>(dependentSchemas.size(), 1F);
            Set<String> keys = dependentSchemas.keySet();
            for (String key : keys) {
                JSONSchema dependentSchema = dependentSchemas.getObject(key, JSONSchema::of);
                this.dependentSchemas.put(key, dependentSchema);
                this.dependentSchemasHashMapping.put(Fnv.hashCode64(key), dependentSchema);
            }
        } else {
            this.dependentSchemas = null;
            this.dependentSchemasHashMapping = null;
        }

        this.ifSchema = input.getObject("if", JSONSchema::of);
        this.elseSchema = input.getObject("else", JSONSchema::of);
        this.thenSchema = input.getObject("then", JSONSchema::of);

        allOf = allOf(input, null);
        anyOf = anyOf(input, null);
        oneOf = oneOf(input, null);
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

    public ValidateResult validate(Map map) {
        return validateMapInternal(map, null, "$");
    }

    private ValidateResult validateMapInternal(Map map, ValidationHandler handler, String path) {
        boolean totalSuccess = true;
        ValidateResult firstFail = null;

        for (String item : required) {
            if (!map.containsKey(item)) {
                ValidateResult raw = new ValidateResult(false, "required %s", item);
                ValidateResult r = handleError(handler, null, path + "." + item, raw);
                if (handler == null || r.isAbort()) {
                    return r;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = r;
                }
            }
        }

        for (Map.Entry<String, JSONSchema> entry : properties.entrySet()) {
            String key = entry.getKey();
            JSONSchema schema = entry.getValue();

            Object propertyValue = map.get(key);
            if (propertyValue == null && !map.containsKey(key)) {
                continue;
            }

            ValidateResult result = schema.validateInternal(propertyValue, handler, path + "." + key);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    ValidateResult wrapped = new ValidateResult(result, "property %s invalid", key);
                    if (result.isAbort()) {
                        wrapped.setAbort(true);
                    }
                    return wrapped;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = new ValidateResult(result, "property %s invalid", key);
                }
            }
        }

        for (PatternProperty patternProperty : patternProperties) {
            for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
                Object entryKey = entry.getKey();
                if (entryKey instanceof String) {
                    String strKey = (String) entryKey;
                    if (patternProperty.pattern.matcher(strKey).find()) {
                        ValidateResult result = patternProperty.schema.validateInternal(entry.getValue(), handler, path + "." + strKey);
                        if (!result.isSuccess()) {
                            if (handler == null || result.isAbort()) {
                                return result;
                            }

                            totalSuccess = false;
                            if (firstFail == null) {
                                firstFail = result;
                            }
                        }
                    }
                }
            }
        }

        if (!additionalProperties) {
            for_:
            for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
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
                    ValidateResult result = additionalPropertySchema.validateInternal(entry.getValue(), handler, path + "." + key);
                    if (!result.isSuccess()) {
                        if (handler == null || result.isAbort()) {
                            return result;
                        }

                        totalSuccess = false;
                        if (firstFail == null) {
                            firstFail = result;
                        }
                    }
                    continue;
                }

                ValidateResult raw = new ValidateResult(false, "add additionalProperties %s", key);
                ValidateResult r = handleError(handler, entry.getValue(), path + "." + key, raw);
                if (handler == null || r.isAbort()) {
                    return r;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = r;
                }
            }
        }

        if (propertyNames != null) {
            for (Object key : map.keySet()) {
                ValidateResult result = propertyNames.validateInternal(key, handler, path + ".key[" + key + "]");
                if (!result.isSuccess()) {
                    ValidateResult r = handleError(handler, key, path + ".key[" + key + "]", FAIL_PROPERTY_NAME);
                    if (handler == null || r.isAbort()) {
                        return r;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = FAIL_PROPERTY_NAME;
                    }
                }
            }
        }

        if (minProperties >= 0) {
            if (map.size() < minProperties) {
                ValidateResult raw = new ValidateResult(false, "minProperties not match, expect %s, but %s", minProperties, map.size());
                ValidateResult r = handleError(handler, map, path, raw);
                if (handler == null || r.isAbort()) {
                    return r;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = r;
                }
            }
        }

        if (maxProperties >= 0) {
            if (map.size() > maxProperties) {
                ValidateResult raw = new ValidateResult(false, "maxProperties not match, expect %s, but %s", maxProperties, map.size());
                ValidateResult r = handleError(handler, map, path, raw);
                if (handler == null || r.isAbort()) {
                    return r;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = r;
                }
            }
        }

        if (dependentRequired != null) {
            for (Map.Entry<String, String[]> entry : dependentRequired.entrySet()) {
                String key = entry.getKey();
                Object value = map.get(key);
                if (value != null) {
                    String[] dependentRequiredProperties = entry.getValue();
                    for (String dependentRequiredProperty : dependentRequiredProperties) {
                        if (!map.containsKey(dependentRequiredProperty)) {
                            ValidateResult raw = new ValidateResult(false, "property %s, dependentRequired property %s", key, dependentRequiredProperty);
                            ValidateResult r = handleError(handler, null, path + "." + dependentRequiredProperty, raw);
                            if (handler == null || r.isAbort()) {
                                return r;
                            }

                            totalSuccess = false;
                            if (firstFail == null) {
                                firstFail = r;
                            }
                        }
                    }
                }
            }
        }

        if (dependentSchemas != null) {
            for (Map.Entry<String, JSONSchema> entry : dependentSchemas.entrySet()) {
                String key = entry.getKey();
                Object fieldValue = map.get(key);
                if (fieldValue == null) {
                    continue;
                }

                JSONSchema schema = entry.getValue();
                ValidateResult result = schema.validateInternal(map, handler, path);
                if (!result.isSuccess()) {
                    if (handler == null || result.isAbort()) {
                        return result;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = result;
                    }
                }
            }
        }

        if (ifSchema != null) {
            // if 判断时不需要触发 handler
            ValidateResult ifResult = ifSchema.validateInternal(map, null, path);
            if (ifResult == SUCCESS) {
                if (thenSchema != null) {
                    ValidateResult thenResult = thenSchema.validateInternal(map, handler, path);
                    if (!thenResult.isSuccess()) {
                        if (handler == null || thenResult.isAbort()) {
                            return thenResult;
                        }

                        totalSuccess = false;
                        if (firstFail == null) {
                            firstFail = thenResult;
                        }
                    }
                }
            } else {
                if (elseSchema != null) {
                    ValidateResult elseResult = elseSchema.validateInternal(map, handler, path);
                    if (!elseResult.isSuccess()) {
                        if (handler == null || elseResult.isAbort()) {
                            return elseResult;
                        }

                        totalSuccess = false;
                        if (firstFail == null) {
                            firstFail = elseResult;
                        }
                    }
                }
            }
        }

        if (allOf != null) {
            ValidateResult result = allOf.validateInternal(map, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        if (anyOf != null) {
            ValidateResult result = anyOf.validateInternal(map, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        if (oneOf != null) {
            ValidateResult result = oneOf.validateInternal(map, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        return totalSuccess ? SUCCESS : (firstFail != null ? firstFail : new ValidateResult(false, "Object validation failed"));
    }

    @Override
    protected ValidateResult validateInternal(Object value, ValidationHandler handler, String path) {
        if (value == null) {
            return typed ? handleError(handler, null, path, FAIL_INPUT_NULL) : SUCCESS;
        }

        if (encoded) {
            if (value instanceof String) {
                try {
                    value = JSON.parseObject((String) value);
                } catch (JSONException e) {
                    return handleError(handler, value, path, FAIL_INPUT_NOT_ENCODED);
                }
            } else {
                return handleError(handler, value, path, FAIL_INPUT_NOT_ENCODED);
            }
        }

        if (value instanceof Map) {
            return validateMapInternal((Map) value, handler, path);
        }

        Class valueClass = value.getClass();
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);

        if (!(objectWriter instanceof ObjectWriterAdapter)) {
            if (typed) {
                ValidateResult raw = new ValidateResult(false, "expect type %s, but %s", Type.Object, valueClass);
                return handleError(handler, value, path, raw);
            }
            return SUCCESS;
        }

        boolean totalSuccess = true;
        ValidateResult firstFail = null;

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
                for (String itemName : this.required) {
                    if (j == i) {
                        fieldName = itemName;
                    }
                    j++;
                }
                ValidateResult raw = new ValidateResult(false, "required property %s", fieldName);
                ValidateResult r = handleError(handler, null, path + "." + fieldName, raw);
                if (handler == null || r.isAbort()) {
                    return r;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = r;
                }
            }
        }

        for (Map.Entry<String, JSONSchema> entry : properties.entrySet()) {
            String key = entry.getKey();
            long keyHash = Fnv.hashCode64(key);

            JSONSchema schema = entry.getValue();

            FieldWriter fieldWriter = objectWriter.getFieldWriter(keyHash);
            if (fieldWriter != null) {
                Object propertyValue = fieldWriter.getFieldValue(value);
                if (propertyValue == null) {
                    continue;
                }

                ValidateResult result = schema.validateInternal(propertyValue, handler, path + "." + key);
                if (!result.isSuccess()) {
                    if (handler == null || result.isAbort()) {
                        return result;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = result;
                    }
                }
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
                    ValidateResult raw = new ValidateResult(false, "minProperties not match, expect %s, but %s", minProperties, fieldValueCount);
                    ValidateResult r = handleError(handler, value, path, raw);
                    if (handler == null || r.isAbort()) {
                        return r;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = r;
                    }
                }
            }

            if (maxProperties >= 0) {
                if (fieldValueCount > maxProperties) {
                    ValidateResult raw = new ValidateResult(false, "maxProperties not match, expect %s, but %s", maxProperties, fieldValueCount);
                    ValidateResult r = handleError(handler, value, path, raw);
                    if (handler == null || r.isAbort()) {
                        return r;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = r;
                    }
                }
            }
        }

        if (dependentRequiredHashCodes != null) {
            int propertyIndex = 0;
            for (Map.Entry<Long, long[]> entry : dependentRequiredHashCodes.entrySet()) {
                Long keyHash = entry.getKey();
                long[] dependentRequiredProperties = entry.getValue();

                FieldWriter fieldWriter = objectWriter.getFieldWriter(keyHash);
                Object fieldValue = fieldWriter.getFieldValue(value);
                if (fieldValue == null) {
                    propertyIndex++;
                    continue;
                }

                for (int requiredIndex = 0; requiredIndex < dependentRequiredProperties.length; requiredIndex++) {
                    long dependentRequiredHash = dependentRequiredProperties[requiredIndex];
                    FieldWriter dependentFieldWriter = objectWriter.getFieldWriter(dependentRequiredHash);

                    if (dependentFieldWriter == null || dependentFieldWriter.getFieldValue(value) == null) {
                        int i = 0;
                        String property = null, dependentRequiredProperty = null;
                        for (Iterator<Map.Entry<String, String[]>> it = this.dependentRequired.entrySet().iterator(); it.hasNext(); ++i) {
                            if (propertyIndex == i) {
                                Map.Entry<String, String[]> dependentRequiredEntry = it.next();
                                property = dependentRequiredEntry.getKey();
                                dependentRequiredProperty = dependentRequiredEntry.getValue()[requiredIndex];
                            }
                        }
                        ValidateResult raw = new ValidateResult(false, "property %s, dependentRequired property %s", property, dependentRequiredProperty);
                        ValidateResult r = handleError(handler, null, path + "." + dependentRequiredProperty, raw);
                        if (handler == null || r.isAbort()) {
                            return r;
                        }

                        totalSuccess = false;
                        if (firstFail == null) {
                            firstFail = r;
                        }
                    }
                }

                propertyIndex++;
            }
        }

        if (dependentSchemasHashMapping != null) {
            for (Map.Entry<Long, JSONSchema> entry : dependentSchemasHashMapping.entrySet()) {
                Long keyHash = entry.getKey();

                FieldWriter fieldWriter = objectWriter.getFieldWriter(keyHash);
                if (fieldWriter == null || fieldWriter.getFieldValue(value) == null) {
                    continue;
                }

                JSONSchema schema = entry.getValue();
                ValidateResult result = schema.validateInternal(value, handler, path);
                if (!result.isSuccess()) {
                    if (handler == null || result.isAbort()) {
                        return result;
                    }

                    totalSuccess = false;
                    if (firstFail == null) {
                        firstFail = result;
                    }
                }
            }
        }

        if (ifSchema != null) {
            // if 判断时不需要触发 handler
            ValidateResult ifResult = ifSchema.validateInternal(value, null, path);
            if (ifResult.isSuccess()) {
                if (thenSchema != null) {
                    ValidateResult thenResult = thenSchema.validateInternal(value, handler, path);
                    if (!thenResult.isSuccess()) {
                        if (handler == null || thenResult.isAbort()) {
                            return thenResult;
                        }

                        totalSuccess = false;
                        if (firstFail == null) {
                            firstFail = thenResult;
                        }
                    }
                }
            } else {
                if (elseSchema != null) {
                    ValidateResult elseResult = elseSchema.validateInternal(value, handler, path);
                    if (!elseResult.isSuccess()) {
                        if (handler == null || elseResult.isAbort()) {
                            return elseResult;
                        }

                        totalSuccess = false;
                        if (firstFail == null) {
                            firstFail = elseResult;
                        }
                    }
                }
            }
        }

        if (allOf != null) {
            ValidateResult result = allOf.validateInternal(value, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        if (anyOf != null) {
            ValidateResult result = anyOf.validateInternal(value, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        if (oneOf != null) {
            ValidateResult result = oneOf.validateInternal(value, handler, path);
            if (!result.isSuccess()) {
                if (handler == null || result.isAbort()) {
                    return result;
                }

                totalSuccess = false;
                if (firstFail == null) {
                    firstFail = result;
                }
            }
        }

        return totalSuccess ? SUCCESS : (firstFail != null ? firstFail : new ValidateResult(false, "Object validation failed"));
    }

    public Map<String, JSONSchema> getProperties() {
        return properties;
    }

    public JSONSchema getProperty(String key) {
        return properties.get(key);
    }

    public Set<String> getRequired() {
        return required;
    }

    static final class PatternProperty {
        final Pattern pattern;
        final JSONSchema schema;

        public PatternProperty(Pattern pattern, JSONSchema schema) {
            this.pattern = pattern;
            this.schema = schema;
        }
    }

    @JSONField(value = true)
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();

        object.put("type", "object");

        if (title != null) {
            object.put("title", title);
        }

        if (description != null) {
            object.put("description", description);
        }

        if (!definitions.isEmpty()) {
            object.put("definitions", definitions);
        }

        if (!defs.isEmpty()) {
            object.put("defs", defs);
        }

        if (!properties.isEmpty()) {
            object.put("properties", properties);
        }

        if (!required.isEmpty()) {
            object.put("required", required);
        }

        if (!additionalProperties) {
            if (additionalPropertySchema != null) {
                object.put("additionalProperties", additionalPropertySchema);
            } else {
                object.put("additionalProperties", additionalProperties);
            }
        }

        if (patternProperties != null && patternProperties.length != 0) {
            object.put("patternProperties", patternProperties);
        }

        if (propertyNames != null) {
            object.put("propertyNames", propertyNames);
        }

        if (minProperties != -1) {
            object.put("minProperties", minProperties);
        }

        if (maxProperties != -1) {
            object.put("maxProperties", maxProperties);
        }

        if (dependentRequired != null && !dependentRequired.isEmpty()) {
            object.put("dependentRequired", dependentRequired);
        }

        if (dependentSchemas != null && !dependentSchemas.isEmpty()) {
            object.put("dependentSchemas", dependentSchemas);
        }

        if (ifSchema != null) {
            object.put("if", ifSchema);
        }

        if (thenSchema != null) {
            object.put("then", thenSchema);
        }

        if (elseSchema != null) {
            object.put("else", elseSchema);
        }

        return injectIfPresent(object, allOf, anyOf, oneOf);
    }

    public void accept(Predicate<JSONSchema> v) {
        if (v.test(this)) {
            this.properties.values().forEach(v::test);
        }
    }

    public JSONSchema getDefs(String def) {
        return this.defs.get(def);
    }
}
