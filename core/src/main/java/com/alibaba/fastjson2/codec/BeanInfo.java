package com.alibaba.fastjson2.codec;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class BeanInfo {
    public String typeKey;
    public String typeName;
    public Class builder;
    public Method buildMethod;
    public String builderWithPrefix;
    public Class[] seeAlso;
    public String[] seeAlsoNames;
    public Constructor creatorConstructor;
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
}
