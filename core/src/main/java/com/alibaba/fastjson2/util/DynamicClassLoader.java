package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.lang.reflect.Type;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {
    final static String FASTJSON_PACKAGE;
    final static ClassLoader FASTJSON_CLASSLOADER;

    private static java.security.ProtectionDomain DOMAIN;
    
    private static Map<String, Class<?>> classMapping = new HashMap<String, Class<?>>();
    static {
        FASTJSON_PACKAGE = JSON.class.getPackage().getName() + ".";
        FASTJSON_CLASSLOADER = JSON.class.getClassLoader();

        Class[] classes = new Class[] {
                Object.class,
                Type.class,

                Fnv.class,

                // reads
                JSONReader.class,

                FieldReader.class,

                ObjectReader.class,
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
                ObjectWriterAdapter.class,

                java.util.List.class,
                java.util.Map.class,
                java.util.function.Supplier.class,
        };
        for (Class clazz : classes) {
            classMapping.put(clazz.getName(), clazz);
        }
    }

    static {
        DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                return DynamicClassLoader.class.getProtectionDomain();
            }
        });
        
    }

    private final ClassLoader parent;
    
    public DynamicClassLoader(){
        this(getParentClassLoader());
    }

    public DynamicClassLoader(ClassLoader parent){
        super (parent);
        this.parent = parent;
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

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> mappingClass = classMapping.get(name);
        if (mappingClass != null) {
            return mappingClass;
        }

//        if (name != null && name.startsWith(FASTJSON_PACKAGE) && name.indexOf("_") == -1) {
//            return FASTJSON_CLASSLOADER.loadClass(name);
//        }

        return super.loadClass(name, resolve);
    }

    public Class<?> defineClassPublic(String name, byte[] b, int off, int len) throws ClassFormatError {
        Class<?> clazz = defineClass(name, b, off, len, DOMAIN);

        return clazz;
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
}
