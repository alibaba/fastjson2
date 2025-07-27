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
    }

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
