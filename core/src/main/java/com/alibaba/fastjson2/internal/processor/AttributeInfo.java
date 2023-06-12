package com.alibaba.fastjson2.internal.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class AttributeInfo {
    final String name;

    VariableElement field;
    ExecutableElement getMethod;
    ExecutableElement setMethod;
    VariableElement argument;

    public AttributeInfo(
            String name,
            VariableElement field,
            ExecutableElement getMethod,
            ExecutableElement setMethod,
            VariableElement argument
    ) {
        this.name = name;
        this.field = field;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
        this.argument = argument;
    }
}
