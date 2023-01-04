package com.alibaba.fastjson.parser;

import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Properties;

public class ParserConfig {
    public static final String DENY_PROPERTY = "fastjson.parser.deny";
    public static final String AUTOTYPE_ACCEPT = "fastjson.parser.autoTypeAccept";

    public static ParserConfig global = new ParserConfig(JSONFactory.getDefaultObjectReaderProvider(), false);

    public static ParserConfig getGlobalInstance() {
        return global;
    }

    final ObjectReaderProvider provider;
    public final boolean fieldBase;
    private boolean asmEnable;
    private boolean autoTypeSupport;

    ParserConfig(ObjectReaderProvider provider, boolean fieldBase) {
        this.provider = provider;
        this.fieldBase = fieldBase;
    }

    public ParserConfig() {
        this(new ObjectReaderProvider(), false);
    }

    public ParserConfig(ClassLoader parentClassLoader) {
        this(new ObjectReaderProvider(), false);
    }

    public ParserConfig(boolean fieldBase) {
        this(new ObjectReaderProvider(), fieldBase);
    }

    public boolean isAsmEnable() {
        return asmEnable;
    }

    public void setAsmEnable(boolean asmEnable) {
        this.asmEnable = asmEnable;
    }

    public ObjectReaderProvider getProvider() {
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
        return autoTypeSupport;
    }

    public void setAutoTypeSupport(boolean autoTypeSupport) {
        this.autoTypeSupport = autoTypeSupport;
    }

    public void addAccept(String name) {
        getProvider().addAutoTypeAccept(name);
    }

    public void addDeny(String name) {
        getProvider().addAutoTypeDeny(name);
    }

    public void addDenyInternal(String name) {
        getProvider().addAutoTypeDeny(name);
    }

    @Deprecated
    public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
        // skip
    }

    public void addAutoTypeCheckHandler(AutoTypeCheckHandler h) {
        if (getProvider().getAutoTypeBeforeHandler() != null) {
            throw new JSONException("not support operation");
        }

        getProvider().setAutoTypeBeforeHandler(h);
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
        ObjectReader objectReader = getProvider().getObjectReader(type);
        if (objectReader instanceof ObjectDeserializer) {
            return (ObjectDeserializer) objectReader;
        }
        return new ObjectDeserializerWrapper(objectReader);
    }

    public ObjectDeserializer getDeserializer(Type type) {
        ObjectReader objectReader = getProvider().getObjectReader(type);
        if (objectReader instanceof ObjectDeserializer) {
            return (ObjectDeserializer) objectReader;
        }
        return new ObjectDeserializerWrapper(objectReader);
    }

    public ObjectDeserializer getDeserializer(Class<?> clazz, Type type) {
        if (type == null) {
            type = clazz;
        }

        ObjectReader objectReader = getProvider().getObjectReader(type);
        if (objectReader instanceof ObjectDeserializer) {
            return (ObjectDeserializer) objectReader;
        }
        return new ObjectDeserializerWrapper(objectReader);
    }

    /**
     * fieldName,field ，先生成fieldName的快照，减少之后的findField的轮询
     *
     * @param clazz
     * @param fieldCacheMap :map&lt;fieldName ,Field&gt;
     */
    public static void parserAllFieldToCache(Class<?> clazz, Map<String, Field> fieldCacheMap) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!fieldCacheMap.containsKey(fieldName)) {
                fieldCacheMap.put(fieldName, field);
            }
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            parserAllFieldToCache(clazz.getSuperclass(), fieldCacheMap);
        }
    }

    public static Field getFieldFromCache(String fieldName, Map<String, Field> fieldCacheMap) {
        Field field = fieldCacheMap.get(fieldName);

        if (field == null) {
            field = fieldCacheMap.get("_" + fieldName);
        }

        if (field == null) {
            field = fieldCacheMap.get("m_" + fieldName);
        }

        if (field == null) {
            char c0 = fieldName.charAt(0);
            if (c0 >= 'a' && c0 <= 'z') {
                char[] chars = fieldName.toCharArray();
                chars[0] -= 32; // lower
                String fieldNameX = new String(chars);
                field = fieldCacheMap.get(fieldNameX);
            }

            if (fieldName.length() > 2) {
                char c1 = fieldName.charAt(1);
                if (c0 >= 'a' && c0 <= 'z'
                        && c1 >= 'A' && c1 <= 'Z') {
                    for (Map.Entry<String, Field> entry : fieldCacheMap.entrySet()) {
                        if (fieldName.equalsIgnoreCase(entry.getKey())) {
                            field = entry.getValue();
                            break;
                        }
                    }
                }
            }
        }

        return field;
    }
}
