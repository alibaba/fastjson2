package com.alibaba.fastjson2.internal.codegen;

import java.lang.reflect.Modifier;

import static com.alibaba.fastjson2.internal.codegen.ClassWriter.getTypeName;

public class MethodWriter
        extends Block {
    final ClassWriter classWriter;
    final int modifiers;
    final String name;
    final Class returnType;
    final Class[] paramTypes;
    final String[] paramNames;

    MethodWriter(ClassWriter classWriter, int modifiers, String name, Class returnType, Class[] paramTypes, String[] paramNames) {
        this.classWriter = classWriter;
        this.modifiers = modifiers;
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
        this.paramNames = paramNames;
    }

    public void toString(StringBuilder buf) {
        if (Modifier.isPublic(modifiers)) {
            buf.append("\tpublic ");
        }

        if (name.equals("<init>")) {
            buf.append(classWriter.name);
        } else {
            buf.append(getTypeName(returnType)).append(' ').append(name);
        }

        boolean newLine = paramTypes.length > 3;
        buf.append('(');
        if (newLine) {
            buf.append("\n\t\t\t");
        }

        for (int i = 0; i < paramTypes.length; i++) {
            if (i != 0) {
                if (newLine) {
                    buf.append(",\n\t\t\t");
                } else {
                    buf.append(", ");
                }
            }
            Class paramType = paramTypes[i];
            String paramName = paramNames[i];
            buf.append(getTypeName(paramType)).append(' ').append(paramName);
        }

        if (newLine) {
            buf.append("\n\t");
        }

        buf.append(") {\n");

        for (int i = 0; i < statements.size(); i++) {
            if (i != 0) {
                buf.append('\n');
            }
            Statement stmt = statements.get(i);
            stmt.toString(this, buf, 2);
        }

        buf.append("\n\t}");
    }

    public void ident(StringBuilder buf, int indent) {
        for (int i = 0; i < indent; i++) {
            buf.append('\t');
        }
    }
}
