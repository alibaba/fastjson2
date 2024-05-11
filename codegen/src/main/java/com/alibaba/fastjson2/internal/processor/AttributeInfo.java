package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.util.Fnv;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Name;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class AttributeInfo
        implements Comparable<AttributeInfo> {
    final String name;
    final long nameHashCode;
    final TypeMirror type;
    VariableElement field;
    ExecutableElement getMethod;
    ExecutableElement setMethod;
    VariableElement argument;
    long readerFeatures;

    public AttributeInfo(
            String name,
            TypeMirror type,
            VariableElement field,
            ExecutableElement getMethod,
            ExecutableElement setMethod,
            VariableElement argument
    ) {
        if (field instanceof Symbol.VarSymbol) {
            Name symbolName = ((Symbol.VarSymbol) field).name;
            if (symbolName != null) {
                String fieldName = symbolName.toString();
                if (!name.equals(fieldName)) {
                    name = fieldName;
                }
            }
        }
        this.name = name;
        this.nameHashCode = Fnv.hashCode64(name);
        this.type = type;
        this.field = field;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
        this.argument = argument;
    }

    public boolean supportSet() {
        return field != null || setMethod != null;
    }

    @Override
    public int compareTo(AttributeInfo o) {
        return this.name.compareTo(o.name);
    }
}
