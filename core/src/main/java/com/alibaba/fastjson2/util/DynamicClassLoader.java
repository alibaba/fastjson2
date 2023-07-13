package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.writer.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PrivilegedAction;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicClassLoader
        extends ClassLoader {
    private static final java.security.ProtectionDomain DOMAIN;

    private static final Map<String, Class<?>> classMapping = new HashMap<>();

    private static final DynamicClassLoader instance = new DynamicClassLoader();

    private final Map<String, Class> classes = new ConcurrentHashMap<>();

    static {
        Class[] classes = new Class[]{
                Object.class,
                Type.class,
                Field.class,
                Method.class,

                Fnv.class,

                // reads
                JSONReader.class,
                FieldReader.class,
                ObjectReader.class,
                ObjectReader1.class,
                ObjectReader2.class,
                ObjectReader3.class,
                ObjectReader4.class,
                ObjectReader5.class,
                ObjectReader6.class,
                ObjectReader6.class,
                ObjectReader7.class,
                ObjectReader8.class,
                ObjectReader9.class,
                ObjectReader10.class,
                ObjectReader11.class,
                ObjectReader12.class,
                ObjectReaderAdapter.class,

                // writers

                JSONWriter.class,
                JSONWriter.Context.class,
                FieldWriter.class,

                PropertyPreFilter.class,
                PropertyFilter.class,
                NameFilter.class,
                ValueFilter.class,

                ObjectWriter.class,
                ObjectWriter1.class,
                ObjectWriter2.class,
                ObjectWriter3.class,
                ObjectWriter4.class,
                ObjectWriter5.class,
                ObjectWriter6.class,
                ObjectWriter7.class,
                ObjectWriter8.class,
                ObjectWriter9.class,
                ObjectWriter10.class,
                ObjectWriter11.class,
                ObjectWriter12.class,
                ObjectWriterAdapter.class,
                JDKUtils.class,
                TypeUtils.class,
                DateUtils.class,
                PropertyNamingStrategy.class,

                Collection.class,
                Set.class,
                List.class,
                ArrayList.class,
                LinkedList.class,
                Map.class,
                HashMap.class,
                LinkedHashMap.class,
                EnumSet.class,
                Optional.class,
                OptionalInt.class,
                OptionalLong.class,
                Date.class,
                Calendar.class,
                ConcurrentHashMap.class,

                java.util.function.Supplier.class,
                java.util.function.Consumer.class,
                Exception.class,
                Enum.class,
                Class.class,
                Boolean.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                String.class,
                BigInteger.class,
                BigDecimal.class,
                Instant.class,
                LocalTime.class,
                LocalDate.class,
                LocalDateTime.class,
                ZonedDateTime.class,
                CharArrayValueConsumer.class,
                ByteArrayValueConsumer.class
        };
        for (Class clazz : classes) {
            classMapping.put(clazz.getName(), clazz);
        }

        String[] strings = {
                "sun.misc.Unsafe",
                "java.sql.Timestamp",
                "java.sql.Date"
        };
        for (String string : strings) {
            try {
                Class<?> c = Class.forName(string);
                classMapping.put(string, c);
            } catch (ClassNotFoundException ignored) {
                // ignored
            }
        }
    }

    static {
        DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(
                (PrivilegedAction<Object>) DynamicClassLoader.class::getProtectionDomain
        );
    }

    public DynamicClassLoader() {
        this(getParentClassLoader());
    }

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    static ClassLoader getParentClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                contextClassLoader.loadClass(DynamicClassLoader.class.getName());
                return contextClassLoader;
            } catch (ClassNotFoundException e) {
                // skip
            }
        }
        return DynamicClassLoader.class.getClassLoader();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> mappingClass = classMapping.get(name);
        if (mappingClass != null) {
            return mappingClass;
        }

        Class clazz = classes.get(name);
        if (clazz != null) {
            return clazz;
        }

        ClassNotFoundException error = null;
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            error = e;
        }

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl != null && tcl != this) {
            try {
                return tcl.loadClass(name);
            } catch (ClassNotFoundException ignored) {
                // ignored
            }
        }

        throw error;
    }

    public void definePackage(String name) throws ClassFormatError {
        if (getPackage(name) != null) {
            return;
        }
        super.definePackage(name, "", "", "", "", "", "", null);
    }

    public Class<?> loadClass(String name, byte[] b, int off, int len) throws ClassFormatError {
        Class<?> clazz = defineClass(name, b, off, len, DOMAIN);
        classes.put(name, clazz);
        return clazz;
    }

    public Class<?> defineClassPublic(String name, byte[] b, int off, int len) throws ClassFormatError {
        return defineClass(name, b, off, len, DOMAIN);
    }

    public boolean isExternalClass(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();

        if (classLoader == null) {
            return false;
        }

        ClassLoader current = this;
        while (current != null) {
            if (current == classLoader) {
                return false;
            }

            current = current.getParent();
        }

        return true;
    }

    public static DynamicClassLoader getInstance() {
        return instance;
    }
}
