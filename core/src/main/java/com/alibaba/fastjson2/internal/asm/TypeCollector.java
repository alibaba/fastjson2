package com.alibaba.fastjson2.internal.asm;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * A class visitor that collects method information and parameter names from bytecode.
 * This collector is used to extract parameter names from compiled classes for reflection purposes.
 *
 * @see ClassReader
 * @see MethodCollector
 */
public class TypeCollector {
//    static final String JSON_TYPE = ASMUtils.desc(JSONType.class);
    static final Map<String, String> PRIMITIVES;

    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("int", "I");
        map.put("boolean", "Z");
        map.put("byte", "B");
        map.put("char", "C");
        map.put("short", "S");
        map.put("float", "F");
        map.put("long", "J");
        map.put("double", "D");
        PRIMITIVES = map;
    }

    final String methodName;

    final Class<?>[] parameterTypes;

    protected MethodCollector collector;

//    protected boolean jsonType;

    public TypeCollector(String methodName, Class<?>[] parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.collector = null;
    }

    /**
     * Visits a method in the class file and creates a collector if the method matches
     * the target method name and parameter types.
     *
     * @param access the method's access flags
     * @param name the method name
     * @param desc the method descriptor
     * @return a MethodCollector if this is the target method, null otherwise
     */
    protected MethodCollector visitMethod(int access, String name, String desc) {
        if (collector != null) {
            return null;
        }

        if (!name.equals(methodName)) {
            return null;
        }

        Type[] argTypes = Type.getArgumentTypes(desc);
        int longOrDoubleQuantity = 0;
        for (int i = 0; i < argTypes.length; i++) {
            Type t = argTypes[i];
            String className = t.getClassName();
            if ("long".equals(className) || "double".equals(className)) {
                longOrDoubleQuantity++;
            }
        }

        if (argTypes.length != this.parameterTypes.length) {
            return null;
        }
        for (int i = 0; i < argTypes.length; i++) {
            if (!correctTypeName(argTypes[i], this.parameterTypes[i].getName())) {
                return null;
            }
        }

        return collector = new MethodCollector(
                Modifier.isStatic(access) ? 0 : 1,
                argTypes.length + longOrDoubleQuantity);
    }
//
//    public void visitAnnotation(String desc) {
//        if (JSON_TYPE.equals(desc)) {
//            jsonType = true;
//        }
//    }

    private boolean correctTypeName(Type type, String paramTypeName) {
        String typeName = type.getClassName();
        // array notation needs cleanup.
        StringBuilder braces = new StringBuilder();
        while (typeName.endsWith("[]")) {
            braces.append('[');
            typeName = typeName.substring(0, typeName.length() - 2);
        }
        if (braces.length() != 0) {
            String symbol = PRIMITIVES.get(typeName);
            if (symbol != null) {
                typeName = braces.append(symbol).toString();
            } else {
                typeName = braces.append('L').append(typeName).append(';').toString();
            }
        }
        return typeName.equals(paramTypeName);
    }

    /**
     * Returns the collected parameter names for the target method.
     *
     * @return array of parameter names, or empty array if debug info is not present
     */
    public String[] getParameterNamesForMethod() {
        if (collector == null || !collector.debugInfoPresent) {
            return new String[0];
        }
        return collector.getResult().split(",");
    }
}
