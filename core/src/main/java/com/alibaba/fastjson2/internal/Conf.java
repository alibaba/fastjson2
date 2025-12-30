package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.util.IOUtils;

import java.io.InputStream;
import java.util.Properties;

public final class Conf {
    private static final boolean USE_UNSAFE;
    static final Properties DEFAULT_PROPERTIES;

    static {
        Properties properties = new Properties();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        final String resourceFile = "fastjson2.properties";

        InputStream inputStream = cl != null
                ? cl.getResourceAsStream(resourceFile)
                : ClassLoader.getSystemResourceAsStream(resourceFile);
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (java.io.IOException ignored) {
            } finally {
                IOUtils.close(inputStream);
            }
        }
        DEFAULT_PROPERTIES = properties;

        {
            String property = properties.getProperty("fastjson2.unsafe");
            USE_UNSAFE = property == null || Boolean.parseBoolean(property);
        }
    }

    static String getProperty(String key) {
        return DEFAULT_PROPERTIES.getProperty(key);
    }

    public static final ByteArray BYTES = USE_UNSAFE ? new ByteArrayUnsafe() : new ByteArray();
}
