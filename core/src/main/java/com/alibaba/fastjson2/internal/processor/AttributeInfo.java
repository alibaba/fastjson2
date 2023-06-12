package com.alibaba.fastjson2.internal.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class AttributeInfo {
    final String name;
    final TypeMirror type;
    VariableElement field;
    ExecutableElement getMethod;
    ExecutableElement setMethod;
    VariableElement argument;

    public AttributeInfo(
            String name,
            TypeMirror type,
            VariableElement field,
            ExecutableElement getMethod,
            ExecutableElement setMethod,
            VariableElement argument
    ) {
        this.name = name;
        this.type = type;
        this.field = field;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
        this.argument = argument;
    }
}
