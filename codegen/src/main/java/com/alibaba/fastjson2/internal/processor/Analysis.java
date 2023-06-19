package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.BeanUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import java.util.*;

public class Analysis {
    final ProcessingEnvironment processingEnv;
    final Elements elements;
    private final Types types;
    final TypeElement jsonCompiledElement;
    final TypeElement jsonTypeElement;
    public final DeclaredType compiledJsonType;
    final Map<String, StructInfo> structs = new LinkedHashMap<>();

    public Analysis(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.jsonCompiledElement = elements.getTypeElement(JSONCompiled.class.getName());
        this.compiledJsonType = types.getDeclaredType(jsonCompiledElement);
        this.jsonTypeElement = elements.getTypeElement(JSONType.class.getName());
    }

    public void processAnnotation(DeclaredType currentAnnotationType, Set<? extends Element> targets) {
        Stack<String> path = new Stack<>();

        for (Element el : targets) {
            Element classElement;
            ExecutableElement factory = null;
            ExecutableElement builder = null;
            if (el instanceof TypeElement) {
                classElement = el;
            } else if (el instanceof ExecutableElement && el.getKind() == ElementKind.METHOD) {
                ExecutableElement ee = (ExecutableElement) el;
                Element returnClass = types.asElement(ee.getReturnType());
                Element enclosing = ee.getEnclosingElement();
                if (!el.getModifiers().contains(Modifier.STATIC)
                        && !types.isSameType(ee.getReturnType(), enclosing.asType())
                        && returnClass.toString().equals(enclosing.getEnclosingElement().toString())) {
                    builder = ee;
                }
                factory = ee;
                classElement = returnClass;
            } else {
                classElement = el.getEnclosingElement();
            }

            findStructs(classElement, currentAnnotationType, currentAnnotationType + " requires accessible public constructor", path, factory, builder);
        }
    }

    private void findStructs(
            Element el,
            DeclaredType discoveredBy,
            String errorMessage,
            Stack<String> path,
            ExecutableElement factory,
            ExecutableElement builder
    ) {
        if (!(el instanceof TypeElement)) {
            return;
        }

        String typeName = el.toString();

        final TypeElement element = (TypeElement) el;
        String name = "struct" + structs.size();
        String binaryName = elements.getBinaryName(element).toString();
        StructInfo info = new StructInfo(types, element, discoveredBy, name, binaryName);
        structs.put(typeName, info);
    }

    static int getModifiers(Set<Modifier> modifiers) {
        int modifierValue = 0;
        for (Modifier modifier : modifiers) {
            switch (modifier) {
                case PUBLIC:
                    modifierValue |= java.lang.reflect.Modifier.PUBLIC;
                    break;
                case PRIVATE:
                    modifierValue |= java.lang.reflect.Modifier.PRIVATE;
                    break;
                case FINAL:
                    modifierValue |= java.lang.reflect.Modifier.FINAL;
                    break;
                default:
                    break;
            }
        }
        return modifierValue;
    }

    public Map<String, StructInfo> analyze() {
        findRelatedReferences();

        return structs;
    }

    private void findRelatedReferences() {
        for (Map.Entry<String, StructInfo> entry : structs.entrySet()) {
            StructInfo info = entry.getValue();

            for (TypeElement inheritance : getTypeHierarchy(info.element)) {
                for (VariableElement field : ElementFilter.fieldsIn(inheritance.getEnclosedElements())) {
                    Set<Modifier> modifiers = field.getModifiers();
                    if (modifiers.contains(Modifier.TRANSIENT)) {
                        continue;
                    }

                    if (modifiers.contains(Modifier.STATIC)) {
                        // TODO enum
                        continue;
                    }

                    FieldInfo fieldInfo = new FieldInfo();

                    String name = field.getSimpleName().toString();
                    JSONField[] annotations = field.getAnnotationsByType(JSONField.class);
                    for (JSONField annotation : annotations) {
                        CodeGenUtils.getFieldInfo(fieldInfo, annotation, false);
                    }

                    if (fieldInfo.fieldName != null) {
                        name = fieldInfo.fieldName;
                    }

                    info.getAttributeByField(name, field);
                }

                for (ExecutableElement method : ElementFilter.methodsIn(inheritance.getEnclosedElements())) {
                    List<? extends VariableElement> parameters = method.getParameters();
                    int parameterCount = parameters.size();
                    String methodName = method.getSimpleName().toString();

                    if (parameterCount > 2) {
                        continue;
                    }

                    boolean ignored = false;
                    if (parameterCount == 0) {
                        switch (methodName) {
                            case "hashCode":
                                ignored = true;
                                break;
                            default:
                                break;
                        }
                    } else if (parameterCount == 1) {
                        ignored = methodName.equals("equals");
                    }
                    if (ignored) {
                        continue;
                    }

                    ExecutableElement getter = null, setter = null;
                    TypeMirror type = null;
                    String name = null;
                    if (parameters.size() == 0 && (methodName.startsWith("get") || methodName.startsWith("is"))) {
                        name = BeanUtils.getterName(methodName, null);
                        getter = method;
                        type = method.getReturnType();
                    } else if (methodName.startsWith("set") && method.getParameters().size() == 1) {
                        name = BeanUtils.setterName(methodName, null);
                        setter = method;
                        type = method.getParameters().get(0).asType();
                    } else {
                        continue;
                    }
                    AttributeInfo attr = info.getAttributeByMethod(name, type, getter, setter);
                }
            }
        }
    }

    private List<TypeElement> getTypeHierarchy(TypeElement element) {
        List<TypeElement> result = new ArrayList<TypeElement>();
        getAllTypes(element, result, new HashSet<TypeElement>());
        return result;
    }

    private void getAllTypes(TypeElement element, List<TypeElement> result, Set<TypeElement> processed) {
        if (!processed.add(element) || element.getQualifiedName().contentEquals("java.lang.Object")) {
            return;
        }
        result.add(element);
        for (TypeMirror type : types.directSupertypes(element.asType())) {
            Element current = types.asElement(type);
            if (current instanceof TypeElement) {
                getAllTypes((TypeElement) current, result, processed);
            }
        }
    }
}
