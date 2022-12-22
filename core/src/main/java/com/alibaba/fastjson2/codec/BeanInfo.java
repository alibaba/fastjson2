package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.filter.Filter;

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
