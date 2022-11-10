package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Type;
import java.util.Properties;

public class ParserConfig {
    public static final String DENY_PROPERTY = "fastjson.parser.deny";
    public static final String AUTOTYPE_ACCEPT = "fastjson.parser.autoTypeAccept";

    public static ParserConfig global = new ParserConfig(null);

    public static ParserConfig getGlobalInstance() {
        return global;
    }

    private ObjectReaderProvider provider;

    public ParserConfig() {
    }

    public ParserConfig(ClassLoader parentClassLoader) {
    }

    public ObjectReaderProvider getProvider() {
        ObjectReaderProvider provider = this.provider;
        if (provider == null) {
            provider = JSONFactory.getDefaultObjectReaderProvider();
        }
        return provider;
    }

    public void putDeserializer(Type type, ObjectDeserializer deserializer) {
        getProvider().register(type, deserializer);
    }

    public Class<?> checkAutoType(Class type) {
        return JSONFactory.getDefaultObjectReaderProvider().checkAutoType(type.getName(), null, 0);
    }

    public boolean isSafeMode() {
        return ObjectReaderProvider.SAFE_MODE;
    }

    public void setSafeMode(boolean safeMode) {
        if (safeMode != ObjectReaderProvider.SAFE_MODE) {
            throw new JSONException("not support operation");
        }
    }

    public boolean isAutoTypeSupport() {
        return false;
    }

    public void setAutoTypeSupport(boolean autoTypeSupport) {
        if (autoTypeSupport) {
            throw new JSONException("not support operation");
        }
    }

    public void addAccept(String name) {
        provider.addAutoTypeAccept(name);
    }

    public void addDeny(String name) {
        provider.addAutoTypeDeny(name);
    }

    public void addDenyInternal(String name) {
        provider.addAutoTypeDeny(name);
    }

    @Deprecated
    public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        // skip
    }

    public void addAutoTypeCheckHandler(AutoTypeCheckHandler h) {
        if (provider.getAutoTypeBeforeHandler() != null) {
            throw new JSONException("not support operation");
        }

        provider.setAutoTypeBeforeHandler(h);
    }

    /**
     * @since 1.2.68
     */
    public interface AutoTypeCheckHandler
            extends JSONReader.AutoTypeBeforeHandler {
        Class<?> handler(String typeName, Class<?> expectClass, int features);

        default Class<?> apply(long typeNameHash, Class<?> expectClass, long features) {
            return null;
        }

        default Class<?> apply(String typeName, Class<?> expectClass, long features) {
            return handler(typeName, expectClass, (int) features);
        }
    }

    public void configFromPropety(Properties properties) {
        {
            String property = properties.getProperty(DENY_PROPERTY);
            String[] items = splitItemsFormProperty(property);
            addItemsToDeny(items);
        }
        {
            String property = properties.getProperty(AUTOTYPE_ACCEPT);
            String[] items = splitItemsFormProperty(property);
            addItemsToAccept(items);
        }
    }

    private void addItemsToDeny(final String[] items) {
        if (items == null) {
            return;
        }

        for (int i = 0; i < items.length; ++i) {
            String item = items[i];
            this.addDeny(item);
        }
    }

    private void addItemsToAccept(final String[] items) {
        if (items == null) {
            return;
        }

        for (int i = 0; i < items.length; ++i) {
            String item = items[i];
            this.addAccept(item);
        }
    }

    private static String[] splitItemsFormProperty(final String property) {
        if (property != null && property.length() > 0) {
            return property.split(",");
        }
        return null;
    }

    public ObjectDeserializer get(Type type) {
        ObjectReader objectReader = provider.getObjectReader(type);
        if (objectReader instanceof ObjectDeserializer) {
            return (ObjectDeserializer) objectReader;
        }
        return new ObjectDeserializerWrapper(objectReader);
    }

    public ObjectDeserializer getDeserializer(Type type) {
        ObjectReader objectReader = provider.getObjectReader(type);
        if (objectReader instanceof ObjectDeserializer) {
            return (ObjectDeserializer) objectReader;
        }
        return new ObjectDeserializerWrapper(objectReader);
    }

    public ObjectDeserializer getDeserializer(Class<?> clazz, Type type) {
        if (type == null) {
            type = clazz;
        }

        ObjectReader objectReader = provider.getObjectReader(type);
        if (objectReader instanceof ObjectDeserializer) {
            return (ObjectDeserializer) objectReader;
        }
        return new ObjectDeserializerWrapper(objectReader);
    }
}
