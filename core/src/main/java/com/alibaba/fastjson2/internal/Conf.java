package com.alibaba.fastjson2.internal;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.function.ToLongFunction;

public final class Conf {
    private static final boolean USE_UNSAFE;
    public static final Properties DEFAULT_PROPERTIES;

    static {
        Properties properties = new Properties();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        final String resourceFile = "fastjson2.properties";

        InputStream is = cl != null
                ? cl.getResourceAsStream(resourceFile)
                : ClassLoader.getSystemResourceAsStream(resourceFile);
        if (is != null) {
            try {
                properties.load(is);
            } catch (java.io.IOException ignored) {
                // ignore
            } finally {
                try {
                    is.close();
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }
        DEFAULT_PROPERTIES = properties;

        {
            String property = properties.getProperty("fastjson2.unsafe");
            USE_UNSAFE = property == null || Boolean.parseBoolean(property);
        }
    }

    public static String getProperty(String key) {
        return DEFAULT_PROPERTIES.getProperty(key);
    }

    public static boolean getPropertyBool(String name, boolean defaultValue) {
        Properties properties = DEFAULT_PROPERTIES;
        boolean propertyValue = defaultValue;

        String property = System.getProperty(name);
        if (property != null) {
            property = property.trim();
            if (property.isEmpty()) {
                property = properties.getProperty(name);
                if (property != null) {
                    property = property.trim();
                }
            }
            if (defaultValue) {
                if ("false".equals(property)) {
                    propertyValue = false;
                }
            } else {
                if ("true".equals(property)) {
                    propertyValue = true;
                }
            }
        }

        return propertyValue;
    }

    public static int getPropertyInt(String name, int defaultValue) {
        Properties properties = DEFAULT_PROPERTIES;
        int propertyValue = defaultValue;

        String property = System.getProperty(name);
        if (property != null) {
            property = property.trim();
            if (property.isEmpty()) {
                property = properties.getProperty(name);
                if (property != null) {
                    property = property.trim();
                }
            }
        }
        try {
            propertyValue = Integer.parseInt(property);
        } catch (NumberFormatException ignored) {
            // ignore
        }

        return propertyValue;
    }

    public static final ByteArray BYTES = USE_UNSAFE
            ? new ByteArrayUnsafe()
            : new ByteArray();

    public static final PropertyAccessorFactory PROPERTY_ACCESSOR_FACTORY = USE_UNSAFE
            ? new PropertyAccessorFactoryUnsafe()
            : new PropertyAccessorFactory();

    public static final ToLongFunction<BigDecimal> DECIMAL_INT_COMPACT;
    static {
        ToLongFunction<BigDecimal> intCompact = null;
        for (Field field : BigDecimal.class.getDeclaredFields()) {
            String fieldName = field.getName();
            if (fieldName.equals("intCompact")
                    || fieldName.equals("smallValue") // android
            ) {
                PropertyAccessor propertyAccessor = PROPERTY_ACCESSOR_FACTORY.create(field);
                intCompact = propertyAccessor::getLong;
                break;
            }
        }
        DECIMAL_INT_COMPACT = intCompact;
    }
}
