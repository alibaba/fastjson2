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

/**
 * A Java field or method type. This class can be used to make it easier to manipulate type and
 * method descriptors.
 *
 * @author Eric Bruneton
 * @author Chris Nokleberg
 */
public final class Type {

    /**
     * The sort of the {@code void} type. See {@link #getSort}.
     */
    public static final int VOID = 0;

    /**
     * The sort of the {@code boolean} type. See {@link #getSort}.
     */
    public static final int BOOLEAN = 1;

    /**
     * The sort of the {@code char} type. See {@link #getSort}.
     */
    public static final int CHAR = 2;

    /**
     * The sort of the {@code byte} type. See {@link #getSort}.
     */
    public static final int BYTE = 3;

    /**
     * The sort of the {@code short} type. See {@link #getSort}.
     */
    public static final int SHORT = 4;

    /**
     * The sort of the {@code int} type. See {@link #getSort}.
     */
    public static final int INT = 5;

    /**
     * The sort of the {@code float} type. See {@link #getSort}.
     */
    public static final int FLOAT = 6;

    /**
     * The sort of the {@code long} type. See {@link #getSort}.
     */
    public static final int LONG = 7;

    /**
     * The sort of the {@code double} type. See {@link #getSort}.
     */
    public static final int DOUBLE = 8;

    /**
     * The sort of array reference types. See {@link #getSort}.
     */
    public static final int ARRAY = 9;

    /**
     * The sort of object reference types. See {@link #getSort}.
     */
    public static final int OBJECT = 10;

    /**
     * The sort of method types. See {@link #getSort}.
     */
    public static final int METHOD = 11;

    /**
     * The (private) sort of object reference types represented with an internal name.
     */
    private static final int INTERNAL = 12;

    /**
     * The descriptors of the primitive types.
     */
    private static final String PRIMITIVE_DESCRIPTORS = "VZCBSIFJD";

    /**
     * The {@code void} type.
     */
    public static final Type VOID_TYPE = new Type(VOID, PRIMITIVE_DESCRIPTORS, VOID, VOID + 1);

    /**
     * The {@code boolean} type.
     */
    public static final Type BOOLEAN_TYPE =
            new Type(BOOLEAN, PRIMITIVE_DESCRIPTORS, BOOLEAN, BOOLEAN + 1);

    /**
     * The {@code char} type.
     */
    public static final Type CHAR_TYPE = new Type(CHAR, PRIMITIVE_DESCRIPTORS, CHAR, CHAR + 1);

    /**
     * The {@code byte} type.
     */
    public static final Type BYTE_TYPE = new Type(BYTE, PRIMITIVE_DESCRIPTORS, BYTE, BYTE + 1);

    /**
     * The {@code short} type.
     */
    public static final Type SHORT_TYPE = new Type(SHORT, PRIMITIVE_DESCRIPTORS, SHORT, SHORT + 1);

    /**
     * The {@code int} type.
     */
    public static final Type INT_TYPE = new Type(INT, PRIMITIVE_DESCRIPTORS, INT, INT + 1);

    /**
     * The {@code float} type.
     */
    public static final Type FLOAT_TYPE = new Type(FLOAT, PRIMITIVE_DESCRIPTORS, FLOAT, FLOAT + 1);

    /**
     * The {@code long} type.
     */
    public static final Type LONG_TYPE = new Type(LONG, PRIMITIVE_DESCRIPTORS, LONG, LONG + 1);

    /**
     * The {@code double} type.
     */
    public static final Type DOUBLE_TYPE =
            new Type(DOUBLE, PRIMITIVE_DESCRIPTORS, DOUBLE, DOUBLE + 1);

    // -----------------------------------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------------------------------

    private final int sort;

    private final String valueBuffer;

    private final int valueBegin;

    private final int valueEnd;

    /**
     * Constructs a reference type.
     *
     * @param sort        the sort of this type, see {@link #sort}.
     * @param valueBuffer a buffer containing the value of this field or method type.
     * @param valueBegin  the beginning index, inclusive, of the value of this field or method type in
     *                    valueBuffer.
     * @param valueEnd    the end index, exclusive, of the value of this field or method type in
     *                    valueBuffer.
     */
    private Type(final int sort, final String valueBuffer, final int valueBegin, final int valueEnd) {
        this.sort = sort;
        this.valueBuffer = valueBuffer;
        this.valueBegin = valueBegin;
        this.valueEnd = valueEnd;
    }

    // -----------------------------------------------------------------------------------------------
    // Methods to get Type(s) from a descriptor, a reflected Method or Constructor, other types, etc.
    // -----------------------------------------------------------------------------------------------

    /**
     * Returns the {@link Type} corresponding to the given type descriptor.
     *
     * @param typeDescriptor a field or method type descriptor.
     * @return the {@link Type} corresponding to the given type descriptor.
     */
    public static Type getType(final String typeDescriptor) {
        return getTypeInternal(typeDescriptor, 0, typeDescriptor.length());
    }

    /**
     * Returns the {@link Type} values corresponding to the argument types of the given method
     * descriptor.
     *
     * @param methodDescriptor a method descriptor.
     * @return the {@link Type} values corresponding to the argument types of the given method
     * descriptor.
     */
    public static Type[] getArgumentTypes(final String methodDescriptor) {
        // First step: compute the number of argument types in methodDescriptor.
        int numArgumentTypes = 0;
        // Skip the first character, which is always a '('.
        int currentOffset = 1;
        // Parse the argument types, one at a each loop iteration.
        while (methodDescriptor.charAt(currentOffset) != ')') {
            while (methodDescriptor.charAt(currentOffset) == '[') {
                currentOffset++;
            }
            if (methodDescriptor.charAt(currentOffset++) == 'L') {
                // Skip the argument descriptor content.
                int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
                currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
            }
            ++numArgumentTypes;
        }

        // Second step: create a Type instance for each argument type.
        Type[] argumentTypes = new Type[numArgumentTypes];
        // Skip the first character, which is always a '('.
        currentOffset = 1;
        // Parse and create the argument types, one at each loop iteration.
        int currentArgumentTypeIndex = 0;
        while (methodDescriptor.charAt(currentOffset) != ')') {
            final int currentArgumentTypeOffset = currentOffset;
            while (methodDescriptor.charAt(currentOffset) == '[') {
                currentOffset++;
            }
            if (methodDescriptor.charAt(currentOffset++) == 'L') {
                // Skip the argument descriptor content.
                int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
                currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
            }
            argumentTypes[currentArgumentTypeIndex++] =
                    getTypeInternal(methodDescriptor, currentArgumentTypeOffset, currentOffset);
        }
        return argumentTypes;
    }

    /**
     * Returns the start index of the return type of the given method descriptor.
     *
     * @param methodDescriptor a method descriptor.
     * @return the start index of the return type of the given method descriptor.
     */
    static int getReturnTypeOffset(final String methodDescriptor) {
        // Skip the first character, which is always a '('.
        int currentOffset = 1;
        // Skip the argument types, one at a each loop iteration.
        while (methodDescriptor.charAt(currentOffset) != ')') {
            while (methodDescriptor.charAt(currentOffset) == '[') {
                currentOffset++;
            }
            if (methodDescriptor.charAt(currentOffset++) == 'L') {
                // Skip the argument descriptor content.
                int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
                currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
            }
        }
        return currentOffset + 1;
    }

    /**
     * Returns the {@link Type} corresponding to the given field or method descriptor.
     *
     * @param descriptorBuffer a buffer containing the field or method descriptor.
     * @param descriptorBegin  the beginning index, inclusive, of the field or method descriptor in
     *                         descriptorBuffer.
     * @param descriptorEnd    the end index, exclusive, of the field or method descriptor in
     *                         descriptorBuffer.
     * @return the {@link Type} corresponding to the given type descriptor.
     */
    private static Type getTypeInternal(
            final String descriptorBuffer, final int descriptorBegin, final int descriptorEnd) {
        switch (descriptorBuffer.charAt(descriptorBegin)) {
            case 'V':
                return VOID_TYPE;
            case 'Z':
                return BOOLEAN_TYPE;
            case 'C':
                return CHAR_TYPE;
            case 'B':
                return BYTE_TYPE;
            case 'S':
                return SHORT_TYPE;
            case 'I':
                return INT_TYPE;
            case 'F':
                return FLOAT_TYPE;
            case 'J':
                return LONG_TYPE;
            case 'D':
                return DOUBLE_TYPE;
            case '[':
                return new Type(ARRAY, descriptorBuffer, descriptorBegin, descriptorEnd);
            case 'L':
                return new Type(OBJECT, descriptorBuffer, descriptorBegin + 1, descriptorEnd - 1);
            case '(':
                return new Type(METHOD, descriptorBuffer, descriptorBegin, descriptorEnd);
            default:
                throw new IllegalArgumentException();
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Methods to get class names, internal names or descriptors.
    // -----------------------------------------------------------------------------------------------

    /**
     * Returns the internal name of the class corresponding to this object or array type. The internal
     * name of a class is its fully qualified name (as returned by Class.getName(), where '.' are
     * replaced by '/'). This method should only be used for an object or array type.
     *
     * @return the internal name of the class corresponding to this object type.
     */
    public String getInternalName() {
        return valueBuffer.substring(valueBegin, valueEnd);
    }

    /**
     * Returns the descriptor corresponding to this type.
     *
     * @return the descriptor corresponding to this type.
     */
    public String getDescriptor() {
        if (sort == OBJECT) {
            return valueBuffer.substring(valueBegin - 1, valueEnd + 1);
        } else if (sort == INTERNAL) {
            return 'L' + valueBuffer.substring(valueBegin, valueEnd) + ';';
        } else {
            return valueBuffer.substring(valueBegin, valueEnd);
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Methods to get the sort, dimension, size, and opcodes corresponding to a Type or descriptor.
    // -----------------------------------------------------------------------------------------------

    public int getSort() {
        return sort == INTERNAL ? OBJECT : sort;
    }

    /**
     * Computes the size of the arguments and of the return value of a method.
     *
     * @param methodDescriptor a method descriptor.
     * @return the size of the arguments of the method (plus one for the implicit this argument),
     * argumentsSize, and the size of its return value, returnSize, packed into a single int i =
     * {@code (argumentsSize &lt;&lt; 2) | returnSize} (argumentsSize is therefore equal to {@code
     * i &gt;&gt; 2}, and returnSize to {@code i &amp; 0x03}).
     */
    public static int getArgumentsAndReturnSizes(final String methodDescriptor) {
        int argumentsSize = 1;
        // Skip the first character, which is always a '('.
        int currentOffset = 1;
        int currentChar = methodDescriptor.charAt(currentOffset);
        // Parse the argument types and compute their size, one at a each loop iteration.
        while (currentChar != ')') {
            if (currentChar == 'J' || currentChar == 'D') {
                currentOffset++;
                argumentsSize += 2;
            } else {
                while (methodDescriptor.charAt(currentOffset) == '[') {
                    currentOffset++;
                }
                if (methodDescriptor.charAt(currentOffset++) == 'L') {
                    // Skip the argument descriptor content.
                    int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
                    currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
                }
                argumentsSize += 1;
            }
            currentChar = methodDescriptor.charAt(currentOffset);
        }
        currentChar = methodDescriptor.charAt(currentOffset + 1);
        if (currentChar == 'V') {
            return argumentsSize << 2;
        } else {
            int returnSize = (currentChar == 'J' || currentChar == 'D') ? 2 : 1;
            return argumentsSize << 2 | returnSize;
        }
    }

    public String getClassName() {
        switch (sort) {
            case VOID:
                return "void";
            case BOOLEAN:
                return "boolean";
            case CHAR:
                return "char";
            case BYTE:
                return "byte";
            case SHORT:
                return "short";
            case INT:
                return "int";
            case FLOAT:
                return "float";
            case LONG:
                return "long";
            case DOUBLE:
                return "double";
            case ARRAY:
                StringBuilder stringBuilder = new StringBuilder(getElementType().getClassName());
                for (int i = getDimensions(); i > 0; --i) {
                    stringBuilder.append("[]");
                }
                return stringBuilder.toString();
            case OBJECT:
            case INTERNAL:
                return valueBuffer.substring(valueBegin, valueEnd).replace('/', '.');
            default:
                throw new AssertionError();
        }
    }

    public Type getElementType() {
        final int numDimensions = getDimensions();
        return getTypeInternal(valueBuffer, valueBegin + numDimensions, valueEnd);
    }

    public int getDimensions() {
        int numDimensions = 1;
        while (valueBuffer.charAt(valueBegin + numDimensions) == '[') {
            numDimensions++;
        }
        return numDimensions;
    }
}
