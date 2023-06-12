package com.alibaba.fastjson2.internal.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ClassWriter {
    final String packageName;
    final String name;
    final Class superClass;
    final Class[] interfaces;

    private Set<String> imports = new TreeSet<>();
    private List<FieldWriter> fields = new ArrayList<>();
    private List<MethodWriter> methods = new ArrayList<>();

    public ClassWriter(String packageName, String name, Class superClass, Class[] interfaces) {
        this.packageName = packageName;
        this.name = name;
        this.superClass = superClass;
        this.interfaces = interfaces;
    }

    public MethodWriter method(int modifiers, String name, Class returnType, Class[] paramTypes, String[] paramNames) {
        MethodWriter mw = new MethodWriter(this, modifiers, name, returnType, paramTypes, paramNames);
        methods.add(mw);
        return mw;
    }

    public void addImport(Class type) {
        Package pkg = type.getPackage();
        if (pkg == null || pkg.getName().equals("java.lang")) {
            return;
        }
        String className = getTypeName(type);
        imports.add(className);
    }

    static String getTypeName(Class type) {
        Package pkg = type.getPackage();
        if (pkg != null && pkg.getName().equals("java.lang") && !type.isArray()) {
            return type.getSimpleName();
        }

        String className;
        if (type.isArray()) {
            className = getTypeName(type.getComponentType()) + "[]";
        } else {
            className = type.getName();
            className = className.replace('$', '.');
        }
        return className;
    }

    public FieldWriter field(int modifier, String name, Class fieldClass) {
        FieldWriter fw = new FieldWriter(modifier, name, fieldClass);
        fields.add(fw);
        return fw;
    }

    protected void toString(StringBuilder buf) {
        if (packageName != null && !packageName.isEmpty()) {
            buf.append("package ").append(packageName).append(";\n\n");
        }

        if (!imports.isEmpty()) {
            for (String item : imports) {
                buf.append("import ").append(item).append(";\n");
            }
        }

        buf.append("public final class ").append(name);

        if (superClass != null) {
            buf.append(" extends ").append(getTypeName(superClass));
        }

        buf.append(" {\n");

        if (!fields.isEmpty()) {
            buf.append("\n");
        }

        for (FieldWriter fw : fields) {
            buf.append('\t')
                    .append(getTypeName(fw.fieldClass))
                    .append(' ')
                    .append(fw.name)
                    .append(";\n");
        }

        for (int i = 0; i < methods.size(); i++) {
            if (i != 0) {
                buf.append("\n");
            }

            MethodWriter mw = methods.get(i);
            mw.toString(buf);
            buf.append("\n");
        }

        buf.append("}");
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        toString(buf);
        return buf.toString();
    }
}
