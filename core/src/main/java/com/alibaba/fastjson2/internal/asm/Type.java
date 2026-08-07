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
    static final int VOID = 0;
    static final int BOOLEAN = 1;
    static final int CHAR = 2;
    static final int BYTE = 3;
    static final int SHORT = 4;
    static final int INT = 5;
    static final int FLOAT = 6;
    static final int LONG = 7;
    static final int DOUBLE = 8;
    static final int ARRAY = 9;
    static final int OBJECT = 10;
    static final int METHOD = 11;
    static final int INTERNAL = 12;

    static final Type VOID_TYPE = new Type(VOID, "VZCBSIFJD", VOID, VOID + 1);
    static final Type BOOLEAN_TYPE = new Type(BOOLEAN, "VZCBSIFJD", BOOLEAN, BOOLEAN + 1);
    static final Type CHAR_TYPE = new Type(CHAR, "VZCBSIFJD", CHAR, CHAR + 1);
    static final Type BYTE_TYPE = new Type(BYTE, "VZCBSIFJD", BYTE, BYTE + 1);
    static final Type SHORT_TYPE = new Type(SHORT, "VZCBSIFJD", SHORT, SHORT + 1);
    static final Type INT_TYPE = new Type(INT, "VZCBSIFJD", INT, INT + 1);
    static final Type FLOAT_TYPE = new Type(FLOAT, "VZCBSIFJD", FLOAT, FLOAT + 1);
    static final Type LONG_TYPE = new Type(LONG, "VZCBSIFJD", LONG, LONG + 1);
    static final Type DOUBLE_TYPE = new Type(DOUBLE, "VZCBSIFJD", DOUBLE, DOUBLE + 1);

    static final Type TYPE_CLASS = new Type(10, "Ljava/lang/Class;", 1, 16);
    static final Type TYPE_TYPE = new Type(10, "Ljava/lang/reflect/Type;", 1, 23);
    static final Type TYPE_OBJECT = new Type(10, "Ljava/lang/Object;", 1, 17);
    static final Type TYPE_STRING = new Type(10, "Ljava/lang/String;", 1, 17);
    static final Type TYPE_LIST = new Type(10, "Ljava/util/List;", 1, 15);
    static final Type TYPE_JSON_READER = new Type(10, "Lcom/alibaba/fastjson2/JSONReader;", 1, 33);
    static final Type TYPE_JSON_WRITER = new Type(10, "Lcom/alibaba/fastjson2/JSONWriter;", 1, 33);
    static final Type TYPE_SUPPLIER = new Type(10, "Ljava/util/function/Supplier;", 1, 28);

    static final Type[] TYPES_0 = new Type[] {TYPE_CLASS, TYPE_STRING, TYPE_STRING, LONG_TYPE, TYPE_LIST};
    static final Type[] TYPES_1 = new Type[] {TYPE_JSON_WRITER, TYPE_OBJECT, TYPE_OBJECT, TYPE_TYPE, LONG_TYPE};
    static final Type[] TYPES_2 = new Type[] {TYPE_CLASS, TYPE_SUPPLIER, TYPE_JSON_READER};
    static final Type[] TYPES_3 = new Type[] {LONG_TYPE};
    static final Type[] TYPES_4 = new Type[] {TYPE_JSON_READER, TYPE_TYPE, TYPE_OBJECT, LONG_TYPE};

    final int sort;
    final String valueBuffer;
    final int valueBegin;
    final int valueEnd;

    private Type(final int sort, final String valueBuffer, final int valueBegin, final int valueEnd) {
        this.sort = sort;
        this.valueBuffer = valueBuffer;
        this.valueBegin = valueBegin;
        this.valueEnd = valueEnd;
    }

    // -----------------------------------------------------------------------------------------------
    // Methods to get Type(s) from a descriptor, a reflected Method or Constructor, other types, etc.
    // -----------------------------------------------------------------------------------------------
//
//    /**
//     * Returns the {@link Type} corresponding to the given type descriptor.
//     *
//     * @param typeDescriptor a field or method type descriptor.
//     * @return the {@link Type} corresponding to the given type descriptor.
//     */
//    public static Type getType(final String typeDescriptor) {
//        return getTypeInternal(typeDescriptor, 0, typeDescriptor.length());
//    }

    /**
     * Returns the {@link Type} values corresponding to the argument types of the given method
     * descriptor.
     *
     * @param methodDescriptor a method descriptor.
     * @return the {@link Type} values corresponding to the argument types of the given method
     * descriptor.
     */
    static Type[] getArgumentTypes(final String methodDescriptor) {
        switch (methodDescriptor) {
            case "()V":
                return new Type[0];
            case "(J)Lcom/alibaba/fastjson2/reader/FieldReader;":
            case "(J)Ljava/lang/Object;":
                return TYPES_3;
            case "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V":
                return TYPES_0;
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V":
                return TYPES_1;
            case "(Ljava/lang/Class;Ljava/util/function/Supplier;[Lcom/alibaba/fastjson2/reader/FieldReader;)V":
                return TYPES_2;
            case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/reflect/Type;Ljava/lang/Object;J)Ljava/lang/Object;":
                return TYPES_4;
            default:
                break;
        }

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
//
//    /**
//     * Returns the start index of the return type of the given method descriptor.
//     *
//     * @param descriptor a method descriptor.
//     * @return the start index of the return type of the given method descriptor.
//     */
//    static int getReturnTypeOffset(final String descriptor) {
//        // Skip the first character, which is always a '('.
//        int currentOffset = 1;
//        // Skip the argument types, one at a each loop iteration.
//        while (descriptor.charAt(currentOffset) != ')') {
//            while (descriptor.charAt(currentOffset) == '[') {
//                currentOffset++;
//            }
//            if (descriptor.charAt(currentOffset++) == 'L') {
//                // Skip the argument descriptor content.
//                int semiColumnOffset = descriptor.indexOf(';', currentOffset);
//                currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
//            }
//        }
//        return currentOffset + 1;
//    }

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
    static Type getTypeInternal(
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
                int len = descriptorEnd - descriptorBegin;
                switch (len) {
                    case 16:
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_LIST.valueBuffer, 0, len)) {
                            return TYPE_LIST;
                        }
                        break;
                    case 17:
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_CLASS.valueBuffer, 0, len)) {
                            return TYPE_CLASS;
                        }
                        break;
                    case 18:
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_STRING.valueBuffer, 0, len)) {
                            return TYPE_STRING;
                        }
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_OBJECT.valueBuffer, 0, len)) {
                            return TYPE_OBJECT;
                        }
                        break;
                    case 24:
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_TYPE.valueBuffer, 0, len)) {
                            return TYPE_TYPE;
                        }
                        break;
                    case 29:
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_SUPPLIER.valueBuffer, 0, len)) {
                            return TYPE_SUPPLIER;
                        }
                        break;
                    case 34:
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_JSON_WRITER.valueBuffer, 0, len)) {
                            return TYPE_JSON_WRITER;
                        }
                        if (descriptorBuffer.regionMatches(descriptorBegin, TYPE_JSON_READER.valueBuffer, 0, len)) {
                            return TYPE_JSON_READER;
                        }
                        break;
                    default:
                        break;
                }

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
//
//    /**
//     * Returns the internal name of the class corresponding to this object or array type. The internal
//     * name of a class is its fully qualified name (as returned by Class.getName(), where '.' are
//     * replaced by '/'). This method should only be used for an object or array type.
//     *
//     * @return the internal name of the class corresponding to this object type.
//     */
//    public String getInternalName() {
//        return valueBuffer.substring(valueBegin, valueEnd);
//    }

    /**
     * Returns the descriptor corresponding to this type.
     *
     * @return the descriptor corresponding to this type.
     */
    public String getDescriptor() {
        if (sort == OBJECT) {
            switch (valueBuffer) {
                case "(Ljava/lang/Class;Ljava/util/function/Supplier;[Lcom/alibaba/fastjson2/reader/FieldReader;)V":
                    if (valueBegin == 2 && valueEnd == 17) {
                        return "Ljava/lang/Class;";
                    }
                    if (valueBegin == 19 && valueEnd == 46) {
                        return "Ljava/util/function/Supplier;";
                    }
                    break;
                case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/reflect/Type;Ljava/lang/Object;J)Ljava/lang/Object;":
                    if (valueBegin == 2 && valueEnd == 34) {
                        return "Lcom/alibaba/fastjson2/JSONReader;";
                    }
                    if (valueBegin == 36 && valueEnd == 58) {
                        return "Ljava/lang/reflect/Type;";
                    }
                    if (valueBegin == 60 && valueEnd == 76) {
                        return "Ljava/lang/Object;";
                    }
                    break;
                case "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V":
                    if (valueBegin == 2 && valueEnd == 17) {
                        return "Ljava/lang/Class;";
                    }
                    if (valueBegin == 19 && valueEnd == 35) {
                        return "Ljava/lang/String;";
                    }
                    if (valueBegin == 37 && valueEnd == 53) {
                        return "Ljava/lang/String;";
                    }
                    if (valueBegin == 56 && valueEnd == 70) {
                        return "Ljava/util/List;";
                    }
                    break;
                case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V":
                    if (valueBegin == 2 && valueEnd == 34) {
                        return "Lcom/alibaba/fastjson2/JSONWriter;";
                    }
                    if (valueBegin == 36 && valueEnd == 52) {
                        return "Ljava/lang/Object;";
                    }
                    if (valueBegin == 54 && valueEnd == 70) {
                        return "Ljava/lang/Object;";
                    }
                    if (valueBegin == 72 && valueEnd == 94) {
                        return "Ljava/lang/reflect/Type;";
                    }
                    break;
                default:
                    break;
            }

            if (valueBegin == 1 && valueEnd + 1 == valueBuffer.length()) {
                return valueBuffer;
            }
            return valueBuffer.substring(valueBegin - 1, valueEnd + 1);
        } else if (sort == INTERNAL) {
            return 'L' + valueBuffer.substring(valueBegin, valueEnd) + ';';
        } else {
            switch (valueBuffer) {
                case "VZCBSIFJD":
                    if (valueBegin == 7 && valueEnd == 8) {
                        return "J";
                    }
                    break;
                case "(Ljava/lang/Class;Ljava/util/function/Supplier;[Lcom/alibaba/fastjson2/reader/FieldReader;)V":
                    if (valueBegin == 47 && valueEnd == 90) {
                        return "[Lcom/alibaba/fastjson2/reader/FieldReader;";
                    }
                    break;
                default:
                    break;
            }
            return valueBuffer.substring(valueBegin, valueEnd);
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Methods to get the sort, dimension, size, and opcodes corresponding to a Type or descriptor.
    // -----------------------------------------------------------------------------------------------
//
//    public int getSort() {
//        return sort == INTERNAL ? OBJECT : sort;
//    }

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
        switch (methodDescriptor) {
            case "()V":
                return 4;
            case "()I":
            case "()Z":
            case "()Ljava/lang/String;":
            case "()Ljava/lang/Class;":
                return 5;
            case "()J":
                return 6;
            case "(I)V":
            case "(Ljava/lang/String;)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;)V":
            case "(Ljava/lang/Object;)V":
            case "(Ljava/lang/Enum;)V":
                return 8;
            case "(C)Z":
            case "(Lcom/alibaba/fastjson2/JSONReader;)Lcom/alibaba/fastjson2/reader/ObjectReader;":
            case "(Ljava/lang/Object;)Z":
            case "(I)Ljava/lang/Object;":
            case "(Lcom/alibaba/fastjson2/JSONReader;)Ljava/lang/Object;":
            case "(Lcom/alibaba/fastjson2/JSONWriter;)Z":
            case "(I)Ljava/lang/Integer;":
                return 9;
            case "(J)V":
            case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/Object;)V":
            case "(Ljava/util/List;Ljava/lang/reflect/Type;)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Enum;)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;I)V":
                return 12;
            case "(J)Z":
            case "(J)Ljava/lang/Object;":
            case "(J)Lcom/alibaba/fastjson2/reader/FieldReader;":
            case "(Ljava/lang/Object;Ljava/lang/reflect/Type;)Z":
            case "(Lcom/alibaba/fastjson2/writer/FieldWriter;Ljava/lang/Object;)Ljava/lang/String;":
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Class;)Lcom/alibaba/fastjson2/writer/ObjectWriter;":
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/reflect/Type;)Lcom/alibaba/fastjson2/writer/ObjectWriter;":
                return 13;
            case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/Object;Ljava/lang/String;)V":
            case "(Ljava/lang/Class;Ljava/util/function/Supplier;[Lcom/alibaba/fastjson2/reader/FieldReader;)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;ZLjava/util/List;)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;J)V":
                return 16;
            case "(Ljava/lang/Object;JLjava/lang/Object;)V":
            case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/util/List;ILjava/lang/String;)V":
                return 20;
            case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/Class;J)Ljava/lang/Object;":
            case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/Class;J)Lcom/alibaba/fastjson2/reader/ObjectReader;":
                return 21;
            case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/reflect/Type;Ljava/lang/Object;J)Ljava/lang/Object;":
                return 25;
            case "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V":
                return 28;
            case "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLcom/alibaba/fastjson2/schema/JSONSchema;Ljava/util/function/Supplier;Ljava/util/function/Function;[Lcom/alibaba/fastjson2/reader/FieldReader;)V":
                return 40;
            default:
                break;
        }

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
                Type elementType = getTypeInternal(valueBuffer, valueBegin + getDimensions(), valueEnd);
                StringBuilder stringBuilder = new StringBuilder(elementType.getClassName());
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
//
//    public Type getElementType() {
//        return getTypeInternal(valueBuffer, valueBegin + getDimensions(), valueEnd);
//    }

    public int getDimensions() {
        int numDimensions = 1;
        while (valueBuffer.charAt(valueBegin + numDimensions) == '[') {
            numDimensions++;
        }
        return numDimensions;
    }
}
