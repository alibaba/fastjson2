// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package com.alibaba.fastjson2.internal.asm;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.TypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ClassWriter {
    private Function<String, Class> typeProvider;
    /**
     * The minor_version and major_version fields of the JVMS ClassFile structure. minor_version is
     * stored in the 16 most significant bits, and major_version in the 16 least significant bits.
     */
    private int version;

    /**
     * The symbol table for this class (contains the constant_pool and the BootstrapMethods).
     */
    private final SymbolTable symbolTable;

    private int accessFlags;

    /**
     * The this_class field of the JVMS ClassFile structure.
     */
    private int thisClass;

    /**
     * The super_class field of the JVMS ClassFile structure.
     */
    private int superClass;

    /**
     * The interface_count field of the JVMS ClassFile structure.
     */
    private int interfaceCount;

    /**
     * The 'interfaces' array of the JVMS ClassFile structure.
     */
    private int[] interfaces;

    /**
     * The fields of this class, stored in a linked list of {@link FieldWriter} linked via their
     * {@link FieldWriter#fv} field. This field stores the first element of this list.
     */
    private FieldWriter firstField;

    /**
     * The fields of this class, stored in a linked list of {@link FieldWriter} linked via their
     * {@link FieldWriter#fv} field. This field stores the last element of this list.
     */
    private FieldWriter lastField;

    private MethodWriter firstMethod;

    private MethodWriter lastMethod;

    public ClassWriter(Function<String, Class> typeProvider) {
        symbolTable = new SymbolTable(this);
        this.typeProvider = typeProvider;
    }

    public final void visit(
            final int version,
            final int access,
            final String name,
            final String superName,
            final String[] interfaces) {
        this.version = version;
        this.accessFlags = access;
        this.thisClass = symbolTable.setMajorVersionAndClassName(version & 0xFFFF, name);
        this.superClass = superName == null ? 0 : symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, superName).index;
        if (interfaces != null && interfaces.length > 0) {
            interfaceCount = interfaces.length;
            this.interfaces = new int[interfaceCount];
            for (int i = 0; i < interfaceCount; ++i) {
                this.interfaces[i] = symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, interfaces[i]).index;
            }
        }
    }

    public final FieldWriter visitField(
            final int access,
            final String name,
            final String descriptor) {
        FieldWriter fieldWriter =
                new FieldWriter(symbolTable, access, name, descriptor);
        if (firstField == null) {
            firstField = fieldWriter;
        } else {
            lastField.fv = fieldWriter;
        }
        return lastField = fieldWriter;
    }

    public final MethodWriter visitMethod(
            int access,
            String name,
            String descriptor,
            int codeInitCapacity
    ) {
        MethodWriter methodWriter =
                new MethodWriter(symbolTable, access, name, descriptor, codeInitCapacity);
        if (firstMethod == null) {
            firstMethod = methodWriter;
        } else {
            lastMethod.mv = methodWriter;
        }
        return lastMethod = methodWriter;
    }

    /**
     * Returns the content of the class file that was built by this ClassWriter.
     *
     * @return the binary content of the JVMS ClassFile structure that was built by this ClassWriter.
     */
    public byte[] toByteArray() {
        // First step: compute the size in bytes of the ClassFile structure.
        // The magic field uses 4 bytes, 10 mandatory fields (minor_version, major_version,
        // constant_pool_count, access_flags, this_class, super_class, interfaces_count, fields_count,
        // methods_count and attributes_count) use 2 bytes each, and each interface uses 2 bytes too.
        int size = 24 + 2 * interfaceCount;
        int fieldsCount = 0;
        FieldWriter fieldWriter = firstField;
        while (fieldWriter != null) {
            ++fieldsCount;
            size += 8;
            fieldWriter = (FieldWriter) fieldWriter.fv;
        }
        int methodsCount = 0;
        MethodWriter methodWriter = firstMethod;
        while (methodWriter != null) {
            ++methodsCount;
            size += methodWriter.computeMethodInfoSize();
            methodWriter = (MethodWriter) methodWriter.mv;
        }

        // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
        int attributesCount = 0;
//        if (symbolTable.computeBootstrapMethodsSize() > 0) {
//            ++attributesCount;
//            size += symbolTable.computeBootstrapMethodsSize();
//        }

        // IMPORTANT: this must be the last part of the ClassFile size computation, because the previous
        // statements can add attribute names to the constant pool, thereby changing its size!
        size += symbolTable.constantPool.length;
        int constantPoolCount = symbolTable.constantPoolCount;
        if (constantPoolCount > 0xFFFF) {
            throw new JSONException("Class too large: " + symbolTable.className + ", constantPoolCount " + constantPoolCount);
        }

        // Second step: allocate a ByteVector of the correct size (in order to avoid any array copy in
        // dynamic resizes) and fill it with the ClassFile content.
        ByteVector result = new ByteVector(size);
        result.putInt(0xCAFEBABE).putInt(version);
        result.putShort(constantPoolCount)
                .putByteArray(
                        symbolTable.constantPool.data,
                        0,
                        symbolTable.constantPool.length
                ); // symbolTable.putConstantPool(result);
        int mask = 0;
        result.putShort(accessFlags & ~mask).putShort(thisClass).putShort(superClass);
        result.putShort(interfaceCount);
        for (int i = 0; i < interfaceCount; ++i) {
            result.putShort(interfaces[i]);
        }
        result.putShort(fieldsCount);
        fieldWriter = firstField;
        while (fieldWriter != null) {
            fieldWriter.putFieldInfo(result);
            fieldWriter = (FieldWriter) fieldWriter.fv;
        }
        result.putShort(methodsCount);
        boolean hasFrames = false;
        boolean hasAsmInstructions = false;
        methodWriter = firstMethod;
        while (methodWriter != null) {
            hasFrames |= methodWriter.stackMapTableNumberOfEntries > 0;
            hasAsmInstructions |= methodWriter.hasAsmInstructions;
            methodWriter.putMethodInfo(result);
            methodWriter = (MethodWriter) methodWriter.mv;
        }
        // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
        result.putShort(attributesCount);
//        symbolTable.putBootstrapMethods(result);

        // Third step: replace the ASM specific instructions, if any.
        if (hasAsmInstructions) {
            throw new UnsupportedOperationException();
        } else {
            return result.data;
        }
    }

    protected Class loadClass(String type) {
        switch (type) {
            case "java/util/List":
                return List.class;
            case "java/util/ArrayList":
                return ArrayList.class;
            case "java/lang/Object":
                return Object.class;
            default:
                break;
        }

        String className1 = type.replace('/', '.');

        Class clazz = null;
        if (typeProvider != null) {
            clazz = typeProvider.apply(className1);
        }

        if (clazz == null) {
            clazz = TypeUtils.loadClass(className1);
        }

        return clazz;
    }

    /**
     * Returns the common super type of the two given types. The default implementation of this method
     * <i>loads</i> the two given classes and uses the java.lang.Class methods to find the common
     * super class. It can be overridden to compute this common super type in other ways, in
     * particular without actually loading any class, or to take into account the class that is
     * currently being generated by this ClassWriter, which can of course not be loaded since it is
     * under construction.
     *
     * @param type1 the internal name of a class.
     * @param type2 the internal name of another class.
     * @return the internal name of the common super class of the two given classes.
     */
    protected String getCommonSuperClass(final String type1, final String type2) {
        Class<?> class1 = loadClass(type1);

        if (class1 == null) {
            throw new JSONException("class not found " + type1);
        }

        Class<?> class2 = loadClass(type2);

        if (class2 == null) {
            return "java/lang/Object";
        }

        if (class1.isAssignableFrom(class2)) {
            return type1;
        }
        if (class2.isAssignableFrom(class1)) {
            return type2;
        }
        if (class1.isInterface() || class2.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                class1 = class1.getSuperclass();
            } while (!class1.isAssignableFrom(class2));

            return class1.getName().replace('.', '/');
        }
    }
}
