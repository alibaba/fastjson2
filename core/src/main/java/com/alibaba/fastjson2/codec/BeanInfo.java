package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Locale;

public class BeanInfo {
    public String typeKey;
    public String typeName;
    public Class builder;
    public Method buildMethod;
    public String builderWithPrefix;
    public Class[] seeAlso;
    public String[] seeAlsoNames;
    public Class seeAlsoDefault;
    public Constructor creatorConstructor;
    public Constructor markerConstructor;
    public Method createMethod;
    public String[] createParameterNames;

    public long readerFeatures;
    public long writerFeatures;

    public boolean writeEnumAsJavaBean;

    public String namingStrategy;
    public String[] ignores;
    public String[] orders;
    public String[] includes;

    public boolean mixIn;
    public boolean kotlin;

    public Class serializer;
    public Class deserializer;
    public Class<? extends Filter>[] serializeFilters;
    public String schema;
    public String format;
    public Locale locale;
    public boolean alphabetic = true;
    public String objectWriterFieldName;
    public String objectReaderFieldName;
    public Class<? extends JSONReader.AutoTypeBeforeHandler> autoTypeBeforeHandler;
    public String rootName;
    public boolean skipTransient = true;

    /**
     * Constructs a new BeanInfo instance with default settings from JSONFactory.
     * Initializes reader and writer features based on global JSON factory configuration,
     * including auto-type, reference detection, JSONB, array mapping, and smart match settings.
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * BeanInfo beanInfo = new BeanInfo();
     * // Features are automatically set based on JSONFactory configuration
     * }</pre>
     */
    public BeanInfo() {
        if (JSONFactory.isDisableAutoType()) {
            writerFeatures |= FieldInfo.DISABLE_AUTO_TYPE;
            readerFeatures |= FieldInfo.DISABLE_AUTO_TYPE;
        }
        if (JSONFactory.isDisableReferenceDetect()) {
            writerFeatures |= FieldInfo.DISABLE_REFERENCE_DETECT;
            readerFeatures |= FieldInfo.DISABLE_REFERENCE_DETECT;
        }
        if (JSONFactory.isDisableJSONB()) {
            writerFeatures |= FieldInfo.DISABLE_JSONB;
            readerFeatures |= FieldInfo.DISABLE_JSONB;
        }
        if (JSONFactory.isDisableArrayMapping()) {
            writerFeatures |= FieldInfo.DISABLE_ARRAY_MAPPING;
            readerFeatures |= FieldInfo.DISABLE_ARRAY_MAPPING;
        }
        if (JSONFactory.isDisableSmartMatch()) {
            readerFeatures |= FieldInfo.DISABLE_SMART_MATCH;
        }
    }

    /**
     * Constructs a new BeanInfo instance with settings from the specified ObjectReaderProvider.
     * Initializes reader features and naming strategy based on the provider's configuration.
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * ObjectReaderProvider provider = new ObjectReaderProvider();
     * BeanInfo beanInfo = new BeanInfo(provider);
     * // Reader features are set based on provider configuration
     * }</pre>
     *
     * @param provider the ObjectReaderProvider to obtain configuration from
     */
    public BeanInfo(ObjectReaderProvider provider) {
        if (provider.isDisableAutoType()) {
            readerFeatures |= FieldInfo.DISABLE_AUTO_TYPE;
        }
        if (provider.isDisableReferenceDetect()) {
            readerFeatures |= FieldInfo.DISABLE_REFERENCE_DETECT;
        }
        if (provider.isDisableJSONB()) {
            readerFeatures |= FieldInfo.DISABLE_JSONB;
        }
        if (provider.isDisableArrayMapping()) {
            readerFeatures |= FieldInfo.DISABLE_ARRAY_MAPPING;
        }
        if (provider.isDisableSmartMatch()) {
            readerFeatures |= FieldInfo.DISABLE_SMART_MATCH;
        }
        PropertyNamingStrategy naming = provider.getNamingStrategy();
        if (naming != null) {
            namingStrategy = naming.name();
        }
    }

    /**
     * Constructs a new BeanInfo instance with settings from the specified ObjectWriterProvider.
     * Initializes writer features, alphabetic ordering, and transient field handling based on the provider's configuration.
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * ObjectWriterProvider provider = new ObjectWriterProvider();
     * BeanInfo beanInfo = new BeanInfo(provider);
     * // Writer features are set based on provider configuration
     * }</pre>
     *
     * @param provider the ObjectWriterProvider to obtain configuration from
     */
    public BeanInfo(ObjectWriterProvider provider) {
        if (provider.isDisableAutoType()) {
            writerFeatures |= FieldInfo.DISABLE_AUTO_TYPE;
        }
        if (provider.isDisableReferenceDetect()) {
            writerFeatures |= FieldInfo.DISABLE_REFERENCE_DETECT;
        }
        if (provider.isDisableJSONB()) {
            writerFeatures |= FieldInfo.DISABLE_JSONB;
        }
        if (provider.isDisableArrayMapping()) {
            writerFeatures |= FieldInfo.DISABLE_ARRAY_MAPPING;
        }
        alphabetic = provider.isAlphabetic();
        skipTransient = provider.isSkipTransient();
    }

    /**
     * Marks the specified field as required in the JSON schema.
     * If no schema exists, creates a new schema with the required field.
     * If a schema exists, adds the field to the existing required fields array.
     *
     * <p><b>Usage Examples:</b></p>
     * <pre>{@code
     * BeanInfo beanInfo = new BeanInfo();
     * beanInfo.required("id");
     * beanInfo.required("name");
     * // Schema now contains: {"required":["id","name"]}
     * }</pre>
     *
     * @param fieldName the name of the field to mark as required
     */
    public void required(String fieldName) {
        if (schema == null) {
            schema = JSONObject.of("required", JSONArray.of(fieldName)).toString();
        } else {
            JSONObject object = JSONObject.parseObject(schema);
            JSONArray array = object.getJSONArray("required");
            array.add(fieldName);
            schema = object.toString();
        }
    }
}
