package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
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
        for (String item : required) {
            if (!map.containsKey(item)) {
                return new ValidateResult(false, "required %s", item);
            }
        }

        for (Map.Entry<String, JSONSchema> entry : properties.entrySet()) {
            String key = entry.getKey();
            JSONSchema schema = entry.getValue();

            Object propertyValue = map.get(key);
            if (propertyValue == null && !map.containsKey(key)) {
                continue;
            }

            ValidateResult result = schema.validate(propertyValue);
            if (!result.isSuccess()) {
                return new ValidateResult(result, "property %s invalid", key);
            }
        }

        for (PatternProperty patternProperty : patternProperties) {
            for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
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
                    ValidateResult result = additionalPropertySchema.validate(entry.getValue());
                    if (!result.isSuccess()) {
                        return result;
                    }
                    continue;
                }

                return new ValidateResult(false, "add additionalProperties %s", key);
            }
        }

        if (propertyNames != null) {
            for (Object key : map.keySet()) {
                ValidateResult result = propertyNames.validate(key);
                if (!result.isSuccess()) {
                    return FAIL_PROPERTY_NAME;
                }
            }
        }

        if (minProperties >= 0) {
            if (map.size() < minProperties) {
                return new ValidateResult(false, "minProperties not match, expect %s, but %s", minProperties, map.size());
            }
        }

        if (maxProperties >= 0) {
            if (map.size() > maxProperties) {
                return new ValidateResult(false, "maxProperties not match, expect %s, but %s", maxProperties, map.size());
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
                            return new ValidateResult(false, "property %s, dependentRequired property %s", key, dependentRequiredProperty);
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
                ValidateResult result = schema.validate(map);
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }

        if (ifSchema != null) {
            ValidateResult ifResult = ifSchema.validate(map);
            if (ifResult == SUCCESS) {
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
    public ValidateResult validate(Object value) {
        if (value == null) {
            return typed ? FAIL_INPUT_NULL : SUCCESS;
        }

        if (value instanceof Map) {
            return validate((Map) value);
        }

        Class valueClass = value.getClass();
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(valueClass);

        if (!(objectWriter instanceof ObjectWriterAdapter)) {
            return typed ? new ValidateResult(false, "expect type %s, but %s", Type.Object, valueClass) : SUCCESS;
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
                for (String itemName : this.required) {
                    if (j == i) {
                        fieldName = itemName;
                    }
                    j++;
                }
                return new ValidateResult(false, "required property %s", fieldName);
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

                ValidateResult result = schema.validate(propertyValue);
                if (!result.isSuccess()) {
                    return result;
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
                    return new ValidateResult(false, "minProperties not match, expect %s, but %s", minProperties, fieldValueCount);
                }
            }

            if (maxProperties >= 0) {
                if (fieldValueCount > maxProperties) {
                    return new ValidateResult(false, "maxProperties not match, expect %s, but %s", maxProperties, fieldValueCount);
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
                        return new ValidateResult(false, "property %s, dependentRequired property %s", property, dependentRequiredProperty);
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
                ValidateResult result = schema.validate(value);
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }

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

        if (allOf != null) {
            object.put("allOf", allOf);
        }

        if (anyOf != null) {
            object.put("anyOf", anyOf);
        }

        if (oneOf != null) {
            object.put("oneOf", oneOf);
        }

        return object;
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
