package com.alibaba.fastjson2.internal.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import java.util.*;
import java.util.stream.Collectors;

public class StructInfo {
    final int modifiers;
    String typeKey;
    int readerFeatures;
    int writerFeatures;
    final TypeElement element;
    final DeclaredType discoveredBy;
    final String name;
    final String binaryName;
    final Map<String, AttributeInfo> attributes = new LinkedHashMap<>();

    public StructInfo(
            TypeElement element,
            DeclaredType discoveredBy,
            String name,
            String binaryName
    ) {
        this.element = element;
        this.discoveredBy = discoveredBy;
        this.name = name;
        this.binaryName = binaryName;

        this.modifiers = Analysis.getModifiers(element.getModifiers());
    }

    public AttributeInfo getAttributeByField(String name, VariableElement field) {
        AttributeInfo attr = attributes.get(name);
        TypeMirror type = field.asType();

        if (attr == null) {
            attr = new AttributeInfo(name, field.asType(), field, null, null, null);
            AttributeInfo origin = attributes.putIfAbsent(name, attr);
            if (origin != null) {
                attr = origin;
            }
        }

        attr.field = field;
        return attr;
    }

    public AttributeInfo getAttributeByMethod(String name,
                                              TypeMirror type,
                                              ExecutableElement getter,
                                              ExecutableElement setter) {
        AttributeInfo attr = attributes.get(name);
        if (attr == null) {
            attr = new AttributeInfo(name, type, null, getter, setter, null);
            AttributeInfo origin = attributes.putIfAbsent(name, attr);
            if (origin != null) {
                attr = origin;
            }
        }

        if (getter != null) {
            attr.getMethod = getter;
        }
        if (setter != null) {
            attr.setMethod = setter;
        }

        return attr;
    }

    public List<AttributeInfo> getReaderAttributes() {
        return attributes.values()
                .stream()
                .filter(AttributeInfo::supportSet)
                .sorted()
                .collect(Collectors.toList());
    }
}
