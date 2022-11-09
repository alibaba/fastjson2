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
 * @author Eric Bruneton
 */
class Frame {
    // Constants used in the StackMapTable attribute.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.4.

    static final int SAME_FRAME = 0;
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME = 64;
//    static final int RESERVED = 128;
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247;
    static final int CHOP_FRAME = 248;
    static final int SAME_FRAME_EXTENDED = 251;
    static final int APPEND_FRAME = 252;
    static final int FULL_FRAME = 255;

    static final int ITEM_TOP = 0;
    static final int ITEM_INTEGER = 1;
    static final int ITEM_FLOAT = 2;
    static final int ITEM_DOUBLE = 3;
    static final int ITEM_LONG = 4;
    static final int ITEM_NULL = 5;
    static final int ITEM_UNINITIALIZED_THIS = 6;
    static final int ITEM_OBJECT = 7;
    static final int ITEM_UNINITIALIZED = 8;
    // Additional, ASM specific constants used in abstract types below.
    static final int ITEM_ASM_BOOLEAN = 9;
    static final int ITEM_ASM_BYTE = 10;
    static final int ITEM_ASM_CHAR = 11;
    static final int ITEM_ASM_SHORT = 12;

    // The size and offset in bits of each field of an abstract type.

    static final int DIM_SIZE = 6;
    static final int KIND_SIZE = 4;
    static final int FLAGS_SIZE = 2;
    static final int VALUE_SIZE = 32 - DIM_SIZE - KIND_SIZE - FLAGS_SIZE;

    static final int DIM_SHIFT = KIND_SIZE + FLAGS_SIZE + VALUE_SIZE;
    static final int KIND_SHIFT = FLAGS_SIZE + VALUE_SIZE;
    static final int FLAGS_SHIFT = VALUE_SIZE;

    // Bitmasks to get each field of an abstract type.

    static final int DIM_MASK = ((1 << DIM_SIZE) - 1) << DIM_SHIFT;
    static final int KIND_MASK = ((1 << KIND_SIZE) - 1) << KIND_SHIFT;
    static final int VALUE_MASK = (1 << VALUE_SIZE) - 1;

    // Constants to manipulate the DIM field of an abstract type.

    /**
     * The constant to be added to an abstract type to get one with one more array dimension.
     */
    static final int ARRAY_OF = +1 << DIM_SHIFT;

    /**
     * The constant to be added to an abstract type to get one with one less array dimension.
     */
    static final int ELEMENT_OF = -1 << DIM_SHIFT;

    // Possible values for the KIND field of an abstract type.

    static final int CONSTANT_KIND = 1 << KIND_SHIFT;
    static final int REFERENCE_KIND = 2 << KIND_SHIFT;
    static final int UNINITIALIZED_KIND = 3 << KIND_SHIFT;
    static final int LOCAL_KIND = 4 << KIND_SHIFT;
    static final int STACK_KIND = 5 << KIND_SHIFT;

    // Possible flags for the FLAGS field of an abstract type.

    /**
     * A flag used for LOCAL_KIND and STACK_KIND abstract types, indicating that if the resolved,
     * concrete type is LONG or DOUBLE, TOP should be used instead (because the value has been
     * partially overridden with an xSTORE instruction).
     */
    private static final int TOP_IF_LONG_OR_DOUBLE_FLAG = 1 << FLAGS_SHIFT;

    // Useful predefined abstract types (all the possible CONSTANT_KIND types).

    private static final int TOP = CONSTANT_KIND | ITEM_TOP;
    private static final int BOOLEAN = CONSTANT_KIND | ITEM_ASM_BOOLEAN;
    private static final int BYTE = CONSTANT_KIND | ITEM_ASM_BYTE;
    private static final int CHAR = CONSTANT_KIND | ITEM_ASM_CHAR;
    private static final int SHORT = CONSTANT_KIND | ITEM_ASM_SHORT;
    private static final int INTEGER = CONSTANT_KIND | ITEM_INTEGER;
    private static final int FLOAT = CONSTANT_KIND | ITEM_FLOAT;
    private static final int LONG = CONSTANT_KIND | ITEM_LONG;
    private static final int DOUBLE = CONSTANT_KIND | ITEM_DOUBLE;
    private static final int NULL = CONSTANT_KIND | ITEM_NULL;
    private static final int UNINITIALIZED_THIS = CONSTANT_KIND | ITEM_UNINITIALIZED_THIS;

    // -----------------------------------------------------------------------------------------------
    // Instance fields
    // -----------------------------------------------------------------------------------------------

    /**
     * The basic block to which these input and output stack map frames correspond.
     */
    Label owner;

    /**
     * The input stack map frame locals. This is an array of abstract types.
     */
    private int[] inputLocals;

    /**
     * The input stack map frame stack. This is an array of abstract types.
     */
    int[] inputStack;

    /**
     * The output stack map frame locals. This is an array of abstract types.
     */
    private int[] outputLocals;

    /**
     * The output stack map frame stack. This is an array of abstract types.
     */
    private int[] outputStack;

    /**
     * The start of the output stack, relatively to the input stack. This offset is always negative or
     * null. A null offset means that the output stack must be appended to the input stack. A -n
     * offset means that the first n output stack elements must replace the top n input stack
     * elements, and that the other elements must be appended to the input stack.
     */
    private short outputStackStart;

    /**
     * The index of the top stack element in {@link #outputStack}.
     */
    private short outputStackTop;

    /**
     * The number of types that are initialized in the basic block. See {@link #initializations}.
     */
    private int initializationCount;

    /**
     * The abstract types that are initialized in the basic block. A constructor invocation on an
     * UNINITIALIZED or UNINITIALIZED_THIS abstract type must replace <i>every occurrence</i> of this
     * type in the local variables and in the operand stack. This cannot be done during the first step
     * of the algorithm since, during this step, the local variables and the operand stack types are
     * still abstract. It is therefore necessary to store the abstract types of the constructors which
     * are invoked in the basic block, in order to do this replacement during the second step of the
     * algorithm, where the frames are fully computed. Note that this array can contain abstract types
     * that are relative to the input locals or to the input stack.
     */
    private int[] initializations;

    // -----------------------------------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------------------------------

    /**
     * Constructs a new Frame.
     *
     * @param owner the basic block to which these input and output stack map frames correspond.
     */
    Frame(final Label owner) {
        this.owner = owner;
    }
//
//    /**
//     * Returns the abstract type corresponding to the internal name of a class.
//     *
//     * @param symbolTable  the type table to use to lookup and store type {@link Symbol}.
//     * @param internalName the internal name of a class. This must <i>not</i> be an array type
//     *                     descriptor.
//     * @return the abstract type value corresponding to the given internal name.
//     */
//    static int getAbstractTypeFromInternalName(
//            final SymbolTable symbolTable, final String internalName) {
//        return REFERENCE_KIND | symbolTable.addType(internalName);
//    }

    /**
     * Returns the abstract type corresponding to the given type descriptor.
     *
     * @param symbolTable the type table to use to lookup and store type {@link Symbol}.
     * @param buffer      a string ending with a type descriptor.
     * @param offset      the start offset of the type descriptor in buffer.
     * @return the abstract type corresponding to the given type descriptor.
     */
    private static int getAbstractTypeFromDescriptor(
            final SymbolTable symbolTable, final String buffer, final int offset) {
        String internalName = null;
        switch (buffer.charAt(offset)) {
            case 'V':
                return 0;
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                return INTEGER;
            case 'F':
                return FLOAT;
            case 'J':
                return LONG;
            case 'D':
                return DOUBLE;
            case 'L':
                if (offset == 0) {
                    switch (buffer) {
                        case "Ljava/lang/Object;":
                            internalName = "java/lang/Object";
                            break;
                        case "Ljava/lang/Class;":
                            internalName = "java/lang/Class";
                            break;
                        case "Ljava/lang/String;":
                            internalName = "java/lang/String";
                            break;
                        case "Ljava/util/List;":
                            internalName = "java/util/List";
                            break;
                        case "Ljava/lang/reflect/Type;":
                            internalName = "java/lang/reflect/Type";
                            break;
                        case "Ljava/util/function/Supplier;":
                            internalName = "java/util/function/Supplier";
                            break;
                        case "Lsun/misc/Unsafe;":
                            internalName = "sun/misc/Unsafe";
                            break;
                        case "Lcom/alibaba/fastjson2/JSONReader;":
                            internalName = "com/alibaba/fastjson2/JSONReader";
                            break;
                        case "Lcom/alibaba/fastjson2/reader/FieldReader;":
                            internalName = "com/alibaba/fastjson2/reader/FieldReader";
                            break;
                        case "Lcom/alibaba/fastjson2/reader/ObjectReader;":
                            internalName = "com/alibaba/fastjson2/reader/ObjectReader";
                            break;
                        case "Lcom/alibaba/fastjson2/JSONWriter;":
                            internalName = "com/alibaba/fastjson2/JSONWriter";
                            break;
                        case "Lcom/alibaba/fastjson2/writer/FieldWriter;":
                            internalName = "com/alibaba/fastjson2/writer/FieldWriter";
                            break;
                        default:
                            break;
                    }
                } else if (offset == 2) {
                    switch (buffer) {
                        case "()Ljava/lang/Class;":
                            internalName = "java/lang/Class";
                            break;
                        case "()Ljava/lang/String;":
                            internalName = "java/lang/String";
                            break;
                        default:
                            break;
                    }
                } else if (offset == 3) {
                    switch (buffer) {
                        case "(J)Lcom/alibaba/fastjson2/reader/FieldReader;":
                            internalName = "com/alibaba/fastjson2/reader/FieldReader";
                            break;
                        case "(I)Ljava/lang/Object;":
                            internalName = "java/lang/Object";
                            break;
                        case "(I)Ljava/lang/Integer;":
                            internalName = "java/lang/Integer";
                            break;
                        default:
                            break;
                    }
                } else if (offset == 36) {
                    switch (buffer) {
                        case "(Lcom/alibaba/fastjson2/JSONReader;)Lcom/alibaba/fastjson2/reader/ObjectReader;":
                            internalName = "com/alibaba/fastjson2/reader/ObjectReader";
                            break;
                        case "(Lcom/alibaba/fastjson2/JSONReader;)Ljava/lang/Object;":
                            internalName = "java/lang/Object";
                            break;
                        default:
                            break;
                    }
                } else if (offset == 54) {
                    switch (buffer) {
                        case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/Class;J)Lcom/alibaba/fastjson2/reader/ObjectReader;":
                            internalName = "com/alibaba/fastjson2/reader/ObjectReader";
                            break;
                        case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/Class;J)Ljava/lang/Object;":
                            internalName = "java/lang/Object";
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (buffer) {
                        case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Class;)Lcom/alibaba/fastjson2/writer/ObjectWriter;":
                            if (offset == 53) {
                                internalName = "com/alibaba/fastjson2/writer/ObjectWriter";
                            }
                            break;
                        case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/reflect/Type;)Lcom/alibaba/fastjson2/writer/ObjectWriter;":
                            if (offset == 60) {
                                internalName = "com/alibaba/fastjson2/writer/ObjectWriter";
                            }
                            break;
                        case "(Lcom/alibaba/fastjson2/writer/FieldWriter;Ljava/lang/Object;)Ljava/lang/String;":
                            if (offset == 62) {
                                internalName = "java/lang/String";
                            }
                            break;
                        case "(Lcom/alibaba/fastjson2/JSONReader;Ljava/lang/reflect/Type;Ljava/lang/Object;J)Ljava/lang/Object;":
                            if (offset == 79) {
                                internalName = "java/lang/Object";
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (internalName == null) {
                    internalName = buffer.substring(offset + 1, buffer.length() - 1);
                }
                return REFERENCE_KIND | symbolTable.addType(internalName);
            case '[':
                int elementDescriptorOffset = offset + 1;
                while (buffer.charAt(elementDescriptorOffset) == '[') {
                    ++elementDescriptorOffset;
                }
                int typeValue;
                switch (buffer.charAt(elementDescriptorOffset)) {
                    case 'Z':
                        typeValue = BOOLEAN;
                        break;
                    case 'C':
                        typeValue = CHAR;
                        break;
                    case 'B':
                        typeValue = BYTE;
                        break;
                    case 'S':
                        typeValue = SHORT;
                        break;
                    case 'I':
                        typeValue = INTEGER;
                        break;
                    case 'F':
                        typeValue = FLOAT;
                        break;
                    case 'J':
                        typeValue = LONG;
                        break;
                    case 'D':
                        typeValue = DOUBLE;
                        break;
                    case 'L':
                        if (offset == 0) {
                            switch (buffer) {
                                case "[Lcom/alibaba/fastjson2/writer/FieldWriter;":
                                    internalName = "com/alibaba/fastjson2/reader/FieldReader";
                                    break;
                                case "[Lcom/alibaba/fastjson2/reader/FieldReader;":
                                    internalName = "Lcom/alibaba/fastjson2/reader/FieldReader";
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (internalName == null) {
                            internalName = buffer.substring(elementDescriptorOffset + 1, buffer.length() - 1);
                        }
                        typeValue = REFERENCE_KIND | symbolTable.addType(internalName);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                return ((elementDescriptorOffset - offset) << DIM_SHIFT) | typeValue;
            default:
                throw new IllegalArgumentException();
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Methods related to the input frame
    // -----------------------------------------------------------------------------------------------

    /**
     * Sets the input frame from the given method description. This method is used to initialize the
     * first frame of a method, which is implicit (i.e. not stored explicitly in the StackMapTable
     * attribute).
     *
     * @param symbolTable the type table to use to lookup and store type {@link Symbol}.
     * @param access      the method's access flags.
     * @param descriptor  the method descriptor.
     * @param maxLocals   the maximum number of local variables of the method.
     */
    final void setInputFrameFromDescriptor(
            final SymbolTable symbolTable,
            final int access,
            final String descriptor,
            final int maxLocals) {
        inputLocals = new int[maxLocals];
        inputStack = new int[0];
        int inputLocalIndex = 0;
        if ((access & Opcodes.ACC_STATIC) == 0) {
            if ((access & Constants.ACC_CONSTRUCTOR) == 0) {
                inputLocals[inputLocalIndex++] =
                        REFERENCE_KIND | symbolTable.addType(symbolTable.className);
            } else {
                inputLocals[inputLocalIndex++] = UNINITIALIZED_THIS;
            }
        }
        for (Type argumentType : Type.getArgumentTypes(descriptor)) {
            int abstractType =
                    getAbstractTypeFromDescriptor(symbolTable, argumentType.getDescriptor(), 0);
            inputLocals[inputLocalIndex++] = abstractType;
            if (abstractType == LONG || abstractType == DOUBLE) {
                inputLocals[inputLocalIndex++] = TOP;
            }
        }
        while (inputLocalIndex < maxLocals) {
            inputLocals[inputLocalIndex++] = TOP;
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Methods related to the output frame
    // -----------------------------------------------------------------------------------------------

    /**
     * Returns the abstract type stored at the given local variable index in the output frame.
     *
     * @param localIndex the index of the local variable whose value must be returned.
     * @return the abstract type stored at the given local variable index in the output frame.
     */
    private int getLocal(final int localIndex) {
        if (outputLocals == null || localIndex >= outputLocals.length) {
            // If this local has never been assigned in this basic block, it is still equal to its value
            // in the input frame.
            return LOCAL_KIND | localIndex;
        } else {
            int abstractType = outputLocals[localIndex];
            if (abstractType == 0) {
                // If this local has never been assigned in this basic block, so it is still equal to its
                // value in the input frame.
                abstractType = outputLocals[localIndex] = LOCAL_KIND | localIndex;
            }
            return abstractType;
        }
    }

    /**
     * Replaces the abstract type stored at the given local variable index in the output frame.
     *
     * @param localIndex   the index of the output frame local variable that must be set.
     * @param abstractType the value that must be set.
     */
    private void setLocal(final int localIndex, final int abstractType) {
        // Create and/or resize the output local variables array if necessary.
        if (outputLocals == null) {
            outputLocals = new int[10];
        }
        int outputLocalsLength = outputLocals.length;
        if (localIndex >= outputLocalsLength) {
            int[] newOutputLocals = new int[Math.max(localIndex + 1, 2 * outputLocalsLength)];
            System.arraycopy(outputLocals, 0, newOutputLocals, 0, outputLocalsLength);
            outputLocals = newOutputLocals;
        }
        // Set the local variable.
        outputLocals[localIndex] = abstractType;
    }

    /**
     * Pushes the given abstract type on the output frame stack.
     *
     * @param abstractType an abstract type.
     */
    private void push(final int abstractType) {
        // Create and/or resize the output stack array if necessary.
        if (outputStack == null) {
            outputStack = new int[10];
        }
        int outputStackLength = outputStack.length;
        if (outputStackTop >= outputStackLength) {
            int[] newOutputStack = new int[Math.max(outputStackTop + 1, 2 * outputStackLength)];
            System.arraycopy(outputStack, 0, newOutputStack, 0, outputStackLength);
            outputStack = newOutputStack;
        }
        // Pushes the abstract type on the output stack.
        outputStack[outputStackTop++] = abstractType;
        // Updates the maximum size reached by the output stack, if needed (note that this size is
        // relative to the input stack size, which is not known yet).
        short outputStackSize = (short) (outputStackStart + outputStackTop);
        if (outputStackSize > owner.outputStackMax) {
            owner.outputStackMax = outputStackSize;
        }
    }

    /**
     * Pushes the abstract type corresponding to the given descriptor on the output frame stack.
     *
     * @param symbolTable the type table to use to lookup and store type {@link Symbol}.
     * @param descriptor  a type or method descriptor (in which case its return type is pushed).
     */
    private void push(final SymbolTable symbolTable, final String descriptor) {
        final int typeDescriptorOffset;
        switch (descriptor) {
            case "()J":
            case "()V":
            case "()Z":
            case "()I":
            case "()Ljava/lang/Class;":
                typeDescriptorOffset = 2;
                break;
            case "(I)V":
            case "(J)V":
            case "(J)Z":
            case "(I)Ljava/lang/Object;":
            case "(I)Ljava/lang/Integer;":
                typeDescriptorOffset = 3;
                break;
            case "(Ljava/lang/Enum;)V":
                typeDescriptorOffset = 18;
                break;
            case "(Ljava/lang/Object;)Z":
            case "(Ljava/lang/String;)V":
            case "(Ljava/lang/Object;)V":
                typeDescriptorOffset = 20;
                break;
            case "(Lcom/alibaba/fastjson2/JSONWriter;)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;)Z":
                typeDescriptorOffset = 36;
                break;
            case "(Lcom/alibaba/fastjson2/JSONWriter;I)V":
            case "(Lcom/alibaba/fastjson2/JSONWriter;J)V":
                typeDescriptorOffset = 37;
                break;
            case "(Ljava/lang/Object;Ljava/lang/reflect/Type;)Z":
                typeDescriptorOffset = 44;
                break;
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Enum;)V":
                typeDescriptorOffset = 52;
                break;
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Class;)Lcom/alibaba/fastjson2/writer/ObjectWriter;":
            case "(Lcom/alibaba/fastjson2/JSONWriter;ZLjava/util/List;)V":
                typeDescriptorOffset = 53;
                break;
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/reflect/Type;)Lcom/alibaba/fastjson2/writer/ObjectWriter;":
                typeDescriptorOffset = 60;
                break;
            case "(Lcom/alibaba/fastjson2/writer/FieldWriter;Ljava/lang/Object;)Ljava/lang/String;":
                typeDescriptorOffset = 62;
                break;
            case "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V":
                typeDescriptorOffset = 72;
                break;
            case "(Lcom/alibaba/fastjson2/JSONWriter;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V":
                typeDescriptorOffset = 97;
                break;
            default:
                if (descriptor.charAt(0) == '(') {
                    int currentOffset = 1;
                    // Skip the argument types, one at a each loop iteration.
                    while (descriptor.charAt(currentOffset) != ')') {
                        while (descriptor.charAt(currentOffset) == '[') {
                            currentOffset++;
                        }
                        if (descriptor.charAt(currentOffset++) == 'L') {
                            // Skip the argument descriptor content.
                            int semiColumnOffset = descriptor.indexOf(';', currentOffset);
                            currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
                        }
                    }
                    typeDescriptorOffset = currentOffset + 1;
                } else {
                    typeDescriptorOffset = 0;
                }
                break;
        }

        int abstractType = getAbstractTypeFromDescriptor(symbolTable, descriptor, typeDescriptorOffset);
        if (abstractType != 0) {
            push(abstractType);
            if (abstractType == LONG || abstractType == DOUBLE) {
                push(TOP);
            }
        }
    }

    /**
     * Pops an abstract type from the output frame stack and returns its value.
     *
     * @return the abstract type that has been popped from the output frame stack.
     */
    private int pop() {
        if (outputStackTop > 0) {
            return outputStack[--outputStackTop];
        } else {
            // If the output frame stack is empty, pop from the input stack.
            return STACK_KIND | -(--outputStackStart);
        }
    }

    /**
     * Pops the given number of abstract types from the output frame stack.
     *
     * @param elements the number of abstract types that must be popped.
     */
    private void pop(final int elements) {
        if (outputStackTop >= elements) {
            outputStackTop -= elements;
        } else {
            // If the number of elements to be popped is greater than the number of elements in the output
            // stack, clear it, and pop the remaining elements from the input stack.
            outputStackStart -= elements - outputStackTop;
            outputStackTop = 0;
        }
    }

    /**
     * Pops as many abstract types from the output frame stack as described by the given descriptor.
     *
     * @param descriptor a type or method descriptor (in which case its argument types are popped).
     */
    private void pop(final String descriptor) {
        char firstDescriptorChar = descriptor.charAt(0);
        if (firstDescriptorChar == '(') {
            pop((Type.getArgumentsAndReturnSizes(descriptor) >> 2) - 1);
        } else if (firstDescriptorChar == 'J' || firstDescriptorChar == 'D') {
            pop(2);
        } else {
            pop(1);
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Methods to handle uninitialized types
    // -----------------------------------------------------------------------------------------------

    /**
     * Adds an abstract type to the list of types on which a constructor is invoked in the basic
     * block.
     *
     * @param abstractType an abstract type on a which a constructor is invoked.
     */
    private void addInitializedType(final int abstractType) {
        // Create and/or resize the initializations array if necessary.
        if (initializations == null) {
            initializations = new int[2];
        }
        int initializationsLength = initializations.length;
        if (initializationCount >= initializationsLength) {
            int[] newInitializations =
                    new int[Math.max(initializationCount + 1, 2 * initializationsLength)];
            System.arraycopy(initializations, 0, newInitializations, 0, initializationsLength);
            initializations = newInitializations;
        }
        // Store the abstract type.
        initializations[initializationCount++] = abstractType;
    }

    /**
     * Returns the "initialized" abstract type corresponding to the given abstract type.
     *
     * @param symbolTable  the type table to use to lookup and store type {@link Symbol}.
     * @param abstractType an abstract type.
     * @return the REFERENCE_KIND abstract type corresponding to abstractType if it is
     * UNINITIALIZED_THIS or an UNINITIALIZED_KIND abstract type for one of the types on which a
     * constructor is invoked in the basic block. Otherwise returns abstractType.
     */
    private int getInitializedType(final SymbolTable symbolTable, final int abstractType) {
        if (abstractType == UNINITIALIZED_THIS
                || (abstractType & (DIM_MASK | KIND_MASK)) == UNINITIALIZED_KIND) {
            for (int i = 0; i < initializationCount; ++i) {
                int initializedType = initializations[i];
                int dim = initializedType & DIM_MASK;
                int kind = initializedType & KIND_MASK;
                int value = initializedType & VALUE_MASK;
                if (kind == LOCAL_KIND) {
                    initializedType = dim + inputLocals[value];
                } else if (kind == STACK_KIND) {
                    initializedType = dim + inputStack[inputStack.length - value];
                }
                if (abstractType == initializedType) {
                    if (abstractType == UNINITIALIZED_THIS) {
                        return REFERENCE_KIND | symbolTable.addType(symbolTable.className);
                    } else {
                        return REFERENCE_KIND
                                | symbolTable.addType(symbolTable.typeTable[abstractType & VALUE_MASK].value);
                    }
                }
            }
        }
        return abstractType;
    }

    // -----------------------------------------------------------------------------------------------
    // Main method, to simulate the execution of each instruction on the output frame
    // -----------------------------------------------------------------------------------------------

    /**
     * Simulates the action of the given instruction on the output stack frame.
     *
     * @param opcode      the opcode of the instruction.
     * @param arg         the numeric operand of the instruction, if any.
     * @param argSymbol   the Symbol operand of the instruction, if any.
     * @param symbolTable the type table to use to lookup and store type {@link Symbol}.
     */
    void execute(
            final int opcode, final int arg, final Symbol argSymbol, final SymbolTable symbolTable) {
        final int CONSTANT_INTEGER_TAG = 3;
        final int CONSTANT_FLOAT_TAG = 4;
        final int CONSTANT_LONG_TAG = 5;
        final int CONSTANT_DOUBLE_TAG = 6;
        final int CONSTANT_CLASS_TAG = 7;
        final int CONSTANT_STRING_TAG = 8;
        final int CONSTANT_METHOD_HANDLE_TAG = 15;
        final int CONSTANT_METHOD_TYPE_TAG = 16;
        final int CONSTANT_DYNAMIC_TAG = 17;

        // Abstract types popped from the stack or read from local variables.
        int abstractType1;
        int abstractType2;
        int abstractType3;
        int abstractType4;
        switch (opcode) {
            case Opcodes.NOP:
            case Opcodes.INEG:
            case Opcodes.LNEG:
            case Opcodes.FNEG:
            case Opcodes.DNEG:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
            case Opcodes.GOTO:
            case Opcodes.RETURN:
                break;
            case Opcodes.ACONST_NULL:
                push(NULL);
                break;
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
            case Opcodes.ILOAD:
                push(INTEGER);
                break;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.LLOAD:
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.FLOAD:
                push(FLOAT);
                break;
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
            case Opcodes.DLOAD:
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.LDC:
                switch (argSymbol.tag) {
                    case CONSTANT_INTEGER_TAG:
                        push(INTEGER);
                        break;
                    case CONSTANT_LONG_TAG:
                        push(LONG);
                        push(TOP);
                        break;
                    case CONSTANT_FLOAT_TAG:
                        push(FLOAT);
                        break;
                    case CONSTANT_DOUBLE_TAG:
                        push(DOUBLE);
                        push(TOP);
                        break;
                    case CONSTANT_CLASS_TAG:
                        push(REFERENCE_KIND | symbolTable.addType("java/lang/Class"));
                        break;
                    case CONSTANT_STRING_TAG:
                        push(REFERENCE_KIND | symbolTable.addType("java/lang/String"));
                        break;
                    case CONSTANT_METHOD_TYPE_TAG:
                        push(REFERENCE_KIND | symbolTable.addType("java/lang/invoke/MethodType"));
                        break;
                    case CONSTANT_METHOD_HANDLE_TAG:
                        push(REFERENCE_KIND | symbolTable.addType("java/lang/invoke/MethodHandle"));
                        break;
                    case CONSTANT_DYNAMIC_TAG:
                        push(symbolTable, argSymbol.value);
                        break;
                    default:
                        throw new AssertionError();
                }
                break;
            case Opcodes.ALOAD:
                push(getLocal(arg));
                break;
            case Opcodes.LALOAD:
            case Opcodes.D2L:
                pop(2);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.AALOAD:
                pop(1);
                abstractType1 = pop();
                push(abstractType1 == NULL ? abstractType1 : ELEMENT_OF + abstractType1);
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.ASTORE:
                abstractType1 = pop();
                setLocal(arg, abstractType1);
                if (arg > 0) {
                    int previousLocalType = getLocal(arg - 1);
                    if (previousLocalType == LONG || previousLocalType == DOUBLE) {
                        setLocal(arg - 1, TOP);
                    } else if ((previousLocalType & KIND_MASK) == LOCAL_KIND
                            || (previousLocalType & KIND_MASK) == STACK_KIND) {
                        // The type of the previous local variable is not known yet, but if it later appears
                        // to be LONG or DOUBLE, we should then use TOP instead.
                        setLocal(arg - 1, previousLocalType | TOP_IF_LONG_OR_DOUBLE_FLAG);
                    }
                }
                break;
            case Opcodes.LSTORE:
            case Opcodes.DSTORE:
                pop(1);
                abstractType1 = pop();
                setLocal(arg, abstractType1);
                setLocal(arg + 1, TOP);
                if (arg > 0) {
                    int previousLocalType = getLocal(arg - 1);
                    if (previousLocalType == LONG || previousLocalType == DOUBLE) {
                        setLocal(arg - 1, TOP);
                    } else if ((previousLocalType & KIND_MASK) == LOCAL_KIND
                            || (previousLocalType & KIND_MASK) == STACK_KIND) {
                        // The type of the previous local variable is not known yet, but if it later appears
                        // to be LONG or DOUBLE, we should then use TOP instead.
                        setLocal(arg - 1, previousLocalType | TOP_IF_LONG_OR_DOUBLE_FLAG);
                    }
                }
                break;
            case Opcodes.POP:
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH:
            case Opcodes.ATHROW:
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                pop(1);
                break;
            case Opcodes.POP2:
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                pop(2);
                break;
            case Opcodes.DUP:
                abstractType1 = pop();
                push(abstractType1);
                push(abstractType1);
                break;
            case Opcodes.DUP2:
                abstractType1 = pop();
                abstractType2 = pop();
                push(abstractType2);
                push(abstractType1);
                push(abstractType2);
                push(abstractType1);
                break;
            case Opcodes.SWAP:
                abstractType1 = pop();
                abstractType2 = pop();
                push(abstractType1);
                push(abstractType2);
                break;
            case Opcodes.IALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.IXOR:
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
            case Opcodes.L2I:
            case Opcodes.D2I:
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
                pop(2);
                push(INTEGER);
                break;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                pop(4);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
                pop(3);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.IINC:
                setLocal(arg, INTEGER);
                break;
            case Opcodes.F2I:
            case Opcodes.ARRAYLENGTH:
            case Opcodes.INSTANCEOF:
                pop(1);
                push(INTEGER);
                break;
            case Opcodes.LCMP:
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                pop(4);
                push(INTEGER);
                break;
            case Opcodes.GETSTATIC:
                push(symbolTable, argSymbol.value);
                break;
            case Opcodes.PUTSTATIC:
                pop(argSymbol.value);
                break;
            case Opcodes.GETFIELD:
                pop(1);
                push(symbolTable, argSymbol.value);
                break;
            case Opcodes.PUTFIELD:
                pop(argSymbol.value);
                pop();
                break;
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE:
                pop(argSymbol.value);
                if (opcode != Opcodes.INVOKESTATIC) {
                    abstractType1 = pop();
                    if (opcode == Opcodes.INVOKESPECIAL && argSymbol.name.charAt(0) == '<') {
                        addInitializedType(abstractType1);
                    }
                }
                push(symbolTable, argSymbol.value);
                break;
            case Opcodes.NEW:
                push(UNINITIALIZED_KIND | symbolTable.addUninitializedType(argSymbol.value, arg));
                break;
            case Opcodes.CHECKCAST:
                String castType = argSymbol.value;
                pop();
                if (castType.charAt(0) == '[') {
                    push(symbolTable, castType);
                } else {
                    push(REFERENCE_KIND | symbolTable.addType(castType));
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Frame merging methods, used in the second step of the stack map frame computation algorithm
    // -----------------------------------------------------------------------------------------------

    /**
     * Computes the concrete output type corresponding to a given abstract output type.
     *
     * @param abstractOutputType an abstract output type.
     * @param numStack           the size of the input stack, used to resolve abstract output types of
     *                           STACK_KIND kind.
     * @return the concrete output type corresponding to 'abstractOutputType'.
     */
    private int getConcreteOutputType(final int abstractOutputType, final int numStack) {
        int dim = abstractOutputType & DIM_MASK;
        int kind = abstractOutputType & KIND_MASK;
        if (kind == LOCAL_KIND) {
            // By definition, a LOCAL_KIND type designates the concrete type of a local variable at
            // the beginning of the basic block corresponding to this frame (which is known when
            // this method is called, but was not when the abstract type was computed).
            int concreteOutputType = dim + inputLocals[abstractOutputType & VALUE_MASK];
            if ((abstractOutputType & TOP_IF_LONG_OR_DOUBLE_FLAG) != 0
                    && (concreteOutputType == LONG || concreteOutputType == DOUBLE)) {
                concreteOutputType = TOP;
            }
            return concreteOutputType;
        } else if (kind == STACK_KIND) {
            // By definition, a STACK_KIND type designates the concrete type of a local variable at
            // the beginning of the basic block corresponding to this frame (which is known when
            // this method is called, but was not when the abstract type was computed).
            int concreteOutputType = dim + inputStack[numStack - (abstractOutputType & VALUE_MASK)];
            if ((abstractOutputType & TOP_IF_LONG_OR_DOUBLE_FLAG) != 0
                    && (concreteOutputType == LONG || concreteOutputType == DOUBLE)) {
                concreteOutputType = TOP;
            }
            return concreteOutputType;
        } else {
            return abstractOutputType;
        }
    }

    final boolean merge(
            final SymbolTable symbolTable, final Frame dstFrame) {
        boolean frameChanged = false;

        // Compute the concrete types of the local variables at the end of the basic block corresponding
        // to this frame, by resolving its abstract output types, and merge these concrete types with
        // those of the local variables in the input frame of dstFrame.
        int numLocal = inputLocals.length;
        int numStack = inputStack.length;
        if (dstFrame.inputLocals == null) {
            dstFrame.inputLocals = new int[numLocal];
            frameChanged = true;
        }
        for (int i = 0; i < numLocal; ++i) {
            int concreteOutputType;
            if (outputLocals != null && i < outputLocals.length) {
                int abstractOutputType = outputLocals[i];
                if (abstractOutputType == 0) {
                    // If the local variable has never been assigned in this basic block, it is equal to its
                    // value at the beginning of the block.
                    concreteOutputType = inputLocals[i];
                } else {
                    concreteOutputType = getConcreteOutputType(abstractOutputType, numStack);
                }
            } else {
                // If the local variable has never been assigned in this basic block, it is equal to its
                // value at the beginning of the block.
                concreteOutputType = inputLocals[i];
            }
            // concreteOutputType might be an uninitialized type from the input locals or from the input
            // stack. However, if a constructor has been called for this class type in the basic block,
            // then this type is no longer uninitialized at the end of basic block.
            if (initializations != null) {
                concreteOutputType = getInitializedType(symbolTable, concreteOutputType);
            }
            frameChanged |= merge(symbolTable, concreteOutputType, dstFrame.inputLocals, i);
        }

        // Compute the concrete types of the stack operands at the end of the basic block corresponding
        // to this frame, by resolving its abstract output types, and merge these concrete types with
        // those of the stack operands in the input frame of dstFrame.
        int numInputStack = inputStack.length + outputStackStart;
        if (dstFrame.inputStack == null) {
            dstFrame.inputStack = new int[numInputStack + outputStackTop];
            frameChanged = true;
        }
        // First, do this for the stack operands that have not been popped in the basic block
        // corresponding to this frame, and which are therefore equal to their value in the input
        // frame (except for uninitialized types, which may have been initialized).
        for (int i = 0; i < numInputStack; ++i) {
            int concreteOutputType = inputStack[i];
            if (initializations != null) {
                concreteOutputType = getInitializedType(symbolTable, concreteOutputType);
            }
            frameChanged |= merge(symbolTable, concreteOutputType, dstFrame.inputStack, i);
        }
        // Then, do this for the stack operands that have pushed in the basic block (this code is the
        // same as the one above for local variables).
        for (int i = 0; i < outputStackTop; ++i) {
            int abstractOutputType = outputStack[i];
            int concreteOutputType = getConcreteOutputType(abstractOutputType, numStack);
            if (initializations != null) {
                concreteOutputType = getInitializedType(symbolTable, concreteOutputType);
            }
            frameChanged |=
                    merge(symbolTable, concreteOutputType, dstFrame.inputStack, numInputStack + i);
        }
        return frameChanged;
    }

    /**
     * Merges the type at the given index in the given abstract type array with the given type.
     * Returns {@literal true} if the type array has been modified by this operation.
     *
     * @param symbolTable the type table to use to lookup and store type {@link Symbol}.
     * @param sourceType  the abstract type with which the abstract type array element must be merged.
     *                    This type should be of {@link #CONSTANT_KIND}, {@link #REFERENCE_KIND} or {@link
     *                    #UNINITIALIZED_KIND} kind, with positive or {@literal null} array dimensions.
     * @param dstTypes    an array of abstract types. These types should be of {@link #CONSTANT_KIND},
     *                    {@link #REFERENCE_KIND} or {@link #UNINITIALIZED_KIND} kind, with positive or {@literal
     *                    null} array dimensions.
     * @param dstIndex    the index of the type that must be merged in dstTypes.
     * @return {@literal true} if the type array has been modified by this operation.
     */
    private static boolean merge(
            final SymbolTable symbolTable,
            final int sourceType,
            final int[] dstTypes,
            final int dstIndex) {
        int dstType = dstTypes[dstIndex];
        if (dstType == sourceType) {
            // If the types are equal, merge(sourceType, dstType) = dstType, so there is no change.
            return false;
        }
        int srcType = sourceType;
        if ((sourceType & ~DIM_MASK) == NULL) {
            if (dstType == NULL) {
                return false;
            }
            srcType = NULL;
        }
        if (dstType == 0) {
            // If dstTypes[dstIndex] has never been assigned, merge(srcType, dstType) = srcType.
            dstTypes[dstIndex] = srcType;
            return true;
        }
        int mergedType;
        if ((dstType & DIM_MASK) != 0 || (dstType & KIND_MASK) == REFERENCE_KIND) {
            // If dstType is a reference type of any array dimension.
            if (srcType == NULL) {
                // If srcType is the NULL type, merge(srcType, dstType) = dstType, so there is no change.
                return false;
            } else if ((srcType & (DIM_MASK | KIND_MASK)) == (dstType & (DIM_MASK | KIND_MASK))) {
                // If srcType has the same array dimension and the same kind as dstType.
                if ((dstType & KIND_MASK) == REFERENCE_KIND) {
                    // If srcType and dstType are reference types with the same array dimension,
                    // merge(srcType, dstType) = dim(srcType) | common super class of srcType and dstType.
                    mergedType =
                            (srcType & DIM_MASK)
                                    | REFERENCE_KIND
                                    | symbolTable.addMergedType(srcType & VALUE_MASK, dstType & VALUE_MASK);
                } else {
                    // If srcType and dstType are array types of equal dimension but different element types,
                    // merge(srcType, dstType) = dim(srcType) - 1 | java/lang/Object.
                    int mergedDim = ELEMENT_OF + (srcType & DIM_MASK);
                    mergedType = mergedDim | REFERENCE_KIND | symbolTable.addType("java/lang/Object");
                }
            } else if ((srcType & DIM_MASK) != 0 || (srcType & KIND_MASK) == REFERENCE_KIND) {
                // If srcType is any other reference or array type,
                // merge(srcType, dstType) = min(srcDdim, dstDim) | java/lang/Object
                // where srcDim is the array dimension of srcType, minus 1 if srcType is an array type
                // with a non reference element type (and similarly for dstDim).
                int srcDim = srcType & DIM_MASK;
                if (srcDim != 0 && (srcType & KIND_MASK) != REFERENCE_KIND) {
                    srcDim = ELEMENT_OF + srcDim;
                }
                int dstDim = dstType & DIM_MASK;
                if (dstDim != 0 && (dstType & KIND_MASK) != REFERENCE_KIND) {
                    dstDim = ELEMENT_OF + dstDim;
                }
                mergedType =
                        Math.min(srcDim, dstDim) | REFERENCE_KIND | symbolTable.addType("java/lang/Object");
            } else {
                // If srcType is any other type, merge(srcType, dstType) = TOP.
                mergedType = TOP;
            }
        } else if (dstType == NULL) {
            // If dstType is the NULL type, merge(srcType, dstType) = srcType, or TOP if srcType is not a
            // an array type or a reference type.
            mergedType =
                    (srcType & DIM_MASK) != 0 || (srcType & KIND_MASK) == REFERENCE_KIND ? srcType : TOP;
        } else {
            // If dstType is any other type, merge(srcType, dstType) = TOP whatever srcType.
            mergedType = TOP;
        }
        if (mergedType != dstType) {
            dstTypes[dstIndex] = mergedType;
            return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------------------------------
    // Frame output methods, to generate StackMapFrame attributes
    // -----------------------------------------------------------------------------------------------

    /**
     * Makes the given {@link MethodWriter} visit the input frame of this {@link Frame}. The visit is
     * done with the {@link MethodWriter#visitFrameStart}, {@link MethodWriter#visitAbstractType} and
     * {@link MethodWriter#visitFrameEnd} methods.
     *
     * @param methodWriter the {@link MethodWriter} that should visit the input frame of this {@link
     *                     Frame}.
     */
    final void accept(final MethodWriter methodWriter) {
        // Compute the number of locals, ignoring TOP types that are just after a LONG or a DOUBLE, and
        // all trailing TOP types.
        int[] localTypes = inputLocals;
        int numLocal = 0;
        int numTrailingTop = 0;
        int i = 0;
        while (i < localTypes.length) {
            int localType = localTypes[i];
            i += (localType == LONG || localType == DOUBLE) ? 2 : 1;
            if (localType == TOP) {
                numTrailingTop++;
            } else {
                numLocal += numTrailingTop + 1;
                numTrailingTop = 0;
            }
        }
        // Compute the stack size, ignoring TOP types that are just after a LONG or a DOUBLE.
        int[] stackTypes = inputStack;
        int numStack = 0;
        i = 0;
        while (i < stackTypes.length) {
            int stackType = stackTypes[i];
            i += (stackType == LONG || stackType == DOUBLE) ? 2 : 1;
            numStack++;
        }
        // Visit the frame and its content.
        int frameIndex = methodWriter.visitFrameStart(owner.bytecodeOffset, numLocal, numStack);
        i = 0;
        while (numLocal-- > 0) {
            int localType = localTypes[i];
            i += (localType == LONG || localType == DOUBLE) ? 2 : 1;
            methodWriter.visitAbstractType(frameIndex++, localType);
        }
        i = 0;
        while (numStack-- > 0) {
            int stackType = stackTypes[i];
            i += (stackType == LONG || stackType == DOUBLE) ? 2 : 1;
            methodWriter.visitAbstractType(frameIndex++, stackType);
        }
        methodWriter.visitFrameEnd();
    }
//
//    /**
//     * Put the given abstract type in the given ByteVector, using the JVMS verification_type_info
//     * format used in StackMapTable attributes.
//     *
//     * @param symbolTable  the type table to use to lookup and store type {@link Symbol}.
//     * @param abstractType an abstract type, restricted to {@link Frame#CONSTANT_KIND}, {@link
//     *                     Frame#REFERENCE_KIND} or {@link Frame#UNINITIALIZED_KIND} types.
//     * @param output       where the abstract type must be put.
//     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.4">JVMS
//     * 4.7.4</a>
//     */
//    static void putAbstractType(
//            final SymbolTable symbolTable, final int abstractType, final ByteVector output) {
//        int arrayDimensions = (abstractType & Frame.DIM_MASK) >> DIM_SHIFT;
//        if (arrayDimensions == 0) {
//            int typeValue = abstractType & VALUE_MASK;
//            switch (abstractType & KIND_MASK) {
//                case CONSTANT_KIND:
//                    output.putByte(typeValue);
//                    break;
//                case REFERENCE_KIND:
//                    output
//                            .putByte(ITEM_OBJECT)
//                            .putShort(symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, symbolTable.typeTable[typeValue].value).index);
//                    break;
//                case UNINITIALIZED_KIND:
//                    output.putByte(ITEM_UNINITIALIZED).putShort((int) symbolTable.typeTable[typeValue].data);
//                    break;
//                default:
//                    throw new AssertionError();
//            }
//        } else {
//            // Case of an array type, we need to build its descriptor first.
//            StringBuilder typeDescriptor = new StringBuilder();
//            while (arrayDimensions-- > 0) {
//                typeDescriptor.append('[');
//            }
//            if ((abstractType & KIND_MASK) == REFERENCE_KIND) {
//                typeDescriptor
//                        .append('L')
//                        .append(symbolTable.typeTable[abstractType & VALUE_MASK].value)
//                        .append(';');
//            } else {
//                switch (abstractType & VALUE_MASK) {
//                    case Frame.ITEM_ASM_BOOLEAN:
//                        typeDescriptor.append('Z');
//                        break;
//                    case Frame.ITEM_ASM_BYTE:
//                        typeDescriptor.append('B');
//                        break;
//                    case Frame.ITEM_ASM_CHAR:
//                        typeDescriptor.append('C');
//                        break;
//                    case Frame.ITEM_ASM_SHORT:
//                        typeDescriptor.append('S');
//                        break;
//                    case Frame.ITEM_INTEGER:
//                        typeDescriptor.append('I');
//                        break;
//                    case Frame.ITEM_FLOAT:
//                        typeDescriptor.append('F');
//                        break;
//                    case Frame.ITEM_LONG:
//                        typeDescriptor.append('J');
//                        break;
//                    case Frame.ITEM_DOUBLE:
//                        typeDescriptor.append('D');
//                        break;
//                    default:
//                        throw new AssertionError();
//                }
//            }
//            output
//                    .putByte(ITEM_OBJECT)
//                    .putShort(symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, typeDescriptor.toString()).index);
//        }
//    }
}
