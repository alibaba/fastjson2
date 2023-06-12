package com.alibaba.fastjson2.internal.codegen;

import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.reader.FieldReader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.TreeSet;

public class ObjectReaderGen {
    final Class objectClass;
    final Type objectType;
    final boolean fieldBased;
    final boolean externalClass;
    final int objectClassModifiers;
    final BeanInfo beanInfo;
    final FieldReader[] fieldReaderArray;
    final Constructor defaultConstructor;
    final Appendable out;
    final String packageName;

    final Set<String> imports = new TreeSet<>();

    public ObjectReaderGen(
            Class objectClass,
            Type objectType,
            boolean fieldBased,
            boolean externalClass,
            int objectClassModifiers,
            BeanInfo beanInfo,
            FieldReader[] fieldReaderArray,
            Constructor defaultConstructor,
            Appendable out
    ) {
        this.objectClass = objectClass;
        this.objectType = objectType;
        this.fieldBased = fieldBased;
        this.externalClass = externalClass;
        this.objectClassModifiers = objectClassModifiers;
        this.beanInfo = beanInfo;
        this.fieldReaderArray = fieldReaderArray;
        this.defaultConstructor = defaultConstructor;
        this.out = out;
        this.packageName = objectClass.getPackage().getName();
    }

    public void gen() {
        println("package " + packageName + ";");
        println();

        // String className = "ORG_" + seed.incrementAndGet() + "_" + fieldReaderArray.length + "_" + objectClass.getSimpleName();

        // ClassWriter cw = new ClassWriter()
    }

    class MethodBuilder {
        final String name;
        final String[] parameters;

        public MethodBuilder(String name, String[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }
    }

    void println() {
        try {
            out.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    void println(String context) {
        try {
            out.append(context);
            out.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    void println(int tabCnt, String context) {
        try {
            for (int i = 0; i < tabCnt; ++i) {
                out.append('\t');
            }
            out.append(context);
            out.append('\n');
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
