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

/**
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public final class MethodWriter {
    protected MethodWriter mv;

    /**
     * Where the constants used in this MethodWriter must be stored.
     */
    private final SymbolTable symbolTable;

    private final int accessFlags;

    /**
     * The name_index field of the method_info JVMS structure.
     */
    private final int nameIndex;

    /**
     * The name of this method.
     */
    private final String name;

    /**
     * The descriptor_index field of the method_info JVMS structure.
     */
    private final int descriptorIndex;

    /**
     * The descriptor of this method.
     */
    private final String descriptor;

    // Code attribute fields and sub attributes:

    /**
     * The max_stack field of the Code attribute.
     */
    private int maxStack;

    /**
     * The max_locals field of the Code attribute.
     */
    private int maxLocals;

    /**
     * The 'code' field of the Code attribute.
     */
    private final ByteVector code;

    /**
     * The number_of_entries field of the StackMapTable code attribute.
     */
    int stackMapTableNumberOfEntries;

    /**
     * The 'entries' array of the StackMapTable code attribute.
     */
    private ByteVector stackMapTableEntries;
    /**
     * The first basic block of the method. The next ones (in bytecode offset order) can be accessed
     * with the {@link Label#nextBasicBlock} field.
     */
    private Label firstBasicBlock;

    /**
     * The last basic block of the method (in bytecode offset order). This field is updated each time
     * a basic block is encountered, and is used to append it at the end of the basic block list.
     */
    private Label lastBasicBlock;

    private Label currentBasicBlock;

    /**
     * The last frame that was written in {@link #stackMapTableEntries}. This field has the same
     * format as {@link #currentFrame}.
     */
    private int[] previousFrame;
    private int[] currentFrame;

    boolean hasAsmInstructions;

    /**
     * The start offset of the last visited instruction. Used to set the offset field of type
     * annotations of type 'offset_target' (see <a
     * href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.20.1">JVMS
     * 4.7.20.1</a>).
     */
    private int lastBytecodeOffset;

    // -----------------------------------------------------------------------------------------------
    // Constructor and accessors
    // -----------------------------------------------------------------------------------------------

    /**
     * Constructs a new {@link MethodWriter}.
     *
     * @param symbolTable where the constants used in this AnnotationWriter must be stored.
     * @param access      the method's access flags (see {@link Opcodes}).
     * @param name        the method's name.
     * @param descriptor  the method's descriptor (see {@link Type}).
     */
    MethodWriter(
            SymbolTable symbolTable,
            int access,
            String name,
            String descriptor,
            int codeInitCapacity
    ) {
        this.symbolTable = symbolTable;
        this.accessFlags = "<init>".equals(name) ? access | Constants.ACC_CONSTRUCTOR : access;
        this.nameIndex = symbolTable.addConstantUtf8(name);
        this.name = name;
        this.descriptorIndex = symbolTable.addConstantUtf8(descriptor);
        this.descriptor = descriptor;
        this.code = new ByteVector(codeInitCapacity);

        // Update maxLocals and currentLocals.
        int argumentsSize = Type.getArgumentsAndReturnSizes(descriptor) >> 2;
        if ((access & Opcodes.ACC_STATIC) != 0) {
            --argumentsSize;
        }
        maxLocals = argumentsSize;
        // Create and visit the label for the first basic block.
        firstBasicBlock = new Label();
        visitLabel(firstBasicBlock);
    }

    // -----------------------------------------------------------------------------------------------
    // Implementation of the MethodVisitor abstract class
    // -----------------------------------------------------------------------------------------------

    public void visitInsn(final int opcode) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        code.putByte(opcode);
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(opcode, 0, null, null);
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                endCurrentBasicBlockWithNoSuccessor();
            }
        }
    }

    public void visitIntInsn(final int opcode, final int operand) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        if (opcode == Opcodes.SIPUSH) {
            code.put12(opcode, operand);
        } else { // BIPUSH or NEWARRAY
            code.put11(opcode, operand);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(opcode, operand, null, null);
        }
    }

    public void visitVarInsn(final int opcode, final int var) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        if (var < 4 && opcode != Opcodes.RET) {
            int optimizedOpcode;
            if (opcode < Opcodes.ISTORE) {
                optimizedOpcode = Constants.ILOAD_0 + ((opcode - Opcodes.ILOAD) << 2) + var;
            } else {
                optimizedOpcode = Constants.ISTORE_0 + ((opcode - Opcodes.ISTORE) << 2) + var;
            }
            code.putByte(optimizedOpcode);
        } else if (var >= 256) {
            code.putByte(Constants.WIDE).put12(opcode, var);
        } else {
            code.put11(opcode, var);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(opcode, var, null, null);
        }

        int currentMaxLocals;
        if (opcode == Opcodes.LLOAD
                || opcode == Opcodes.DLOAD
                || opcode == Opcodes.LSTORE
                || opcode == Opcodes.DSTORE) {
            currentMaxLocals = var + 2;
        } else {
            currentMaxLocals = var + 1;
        }
        if (currentMaxLocals > maxLocals) {
            maxLocals = currentMaxLocals;
        }
    }

    public void visitTypeInsn(final int opcode, final String type) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        Symbol typeSymbol = symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, type);
        code.put12(opcode, typeSymbol.index);
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(opcode, lastBytecodeOffset, typeSymbol, symbolTable);
        }
    }

    public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        Symbol fieldrefSymbol = symbolTable.addConstantMemberReference(/*CONSTANT_FIELDREF_TAG*/ 9, owner, name, descriptor);
        code.put12(opcode, fieldrefSymbol.index);
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(opcode, 0, fieldrefSymbol, symbolTable);
        }
    }

    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        Symbol methodrefSymbol = symbolTable.addConstantMemberReference(
                isInterface ? /*CONSTANT_INTERFACE_METHODREF_TAG*/ 11 : /*CONSTANT_METHODREF_TAG*/ 10,
                owner,
                name,
                descriptor
        );
        if (opcode == Opcodes.INVOKEINTERFACE) {
            code.put12(Opcodes.INVOKEINTERFACE, methodrefSymbol.index)
                    .put11(methodrefSymbol.getArgumentsAndReturnSizes() >> 2, 0);
        } else {
            code.put12(opcode, methodrefSymbol.index);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(opcode, 0, methodrefSymbol, symbolTable);
        }
    }

    public void visitJumpInsn(final int opcode, final Label label) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        // Compute the 'base' opcode, i.e. GOTO or JSR if opcode is GOTO_W or JSR_W, otherwise opcode.
        int baseOpcode =
                opcode >= Constants.GOTO_W ? opcode - Constants.WIDE_JUMP_OPCODE_DELTA : opcode;
        boolean nextInsnIsJumpTarget = false;
        if ((label.flags & Label.FLAG_RESOLVED) != 0
                && label.bytecodeOffset - code.length < Short.MIN_VALUE) {
            throw new JSONException("not supported");
        } else if (baseOpcode != opcode) {
            // Case of a GOTO_W or JSR_W specified by the user (normally ClassReader when used to remove
            // ASM specific instructions). In this case we keep the original instruction.
            code.putByte(opcode);
            label.put(code, code.length - 1, true);
        } else {
            // Case of a jump with an offset >= -32768, or of a jump with an unknown offset. In these
            // cases we store the offset in 2 bytes (which will be increased via a ClassReader ->
            // ClassWriter round trip if it turns out that 2 bytes are not sufficient).
            code.putByte(baseOpcode);
            label.put(code, code.length - 1, false);
        }

        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            Label nextBasicBlock = null;
            currentBasicBlock.frame.execute(baseOpcode, 0, null, null);
            // Record the fact that 'label' is the target of a jump instruction.
            label.getCanonicalInstance().flags |= Label.FLAG_JUMP_TARGET;
            // Add 'label' as a successor of the current basic block.
            addSuccessorToCurrentBasicBlock(label);
            if (baseOpcode != Opcodes.GOTO) {
                // The next instruction starts a new basic block (except for GOTO: by default the code
                // following a goto is unreachable - unless there is an explicit label for it - and we
                // should not compute stack frame types for its instructions).
                nextBasicBlock = new Label();
            }

            // If the next instruction starts a new basic block, call visitLabel to add the label of this
            // instruction as a successor of the current block, and to start a new basic block.
            if (nextBasicBlock != null) {
                if (nextInsnIsJumpTarget) {
                    nextBasicBlock.flags |= Label.FLAG_JUMP_TARGET;
                }
                visitLabel(nextBasicBlock);
            }
            if (baseOpcode == Opcodes.GOTO) {
                endCurrentBasicBlockWithNoSuccessor();
            }
        }
    }

    public void visitLabel(final Label label) {
        // Resolve the forward references to this label, if any.
        hasAsmInstructions |= label.resolve(code.data, code.length);
        // visitLabel starts a new basic block (except for debug only labels), so we need to update the
        // previous and current block references and list of successors.
        if ((label.flags & Label.FLAG_DEBUG_ONLY) != 0) {
            return;
        }

        if (currentBasicBlock != null) {
            if (label.bytecodeOffset == currentBasicBlock.bytecodeOffset) {
                // We use {@link Label#getCanonicalInstance} to store the state of a basic block in only
                // one place, but this does not work for labels which have not been visited yet.
                // Therefore, when we detect here two labels having the same bytecode offset, we need to
                // - consolidate the state scattered in these two instances into the canonical instance:
                currentBasicBlock.flags |= (label.flags & Label.FLAG_JUMP_TARGET);
                // - make sure the two instances share the same Frame instance (the implementation of
                // {@link Label#getCanonicalInstance} relies on this property; here label.frame should be
                // null):
                label.frame = currentBasicBlock.frame;
                // - and make sure to NOT assign 'label' into 'currentBasicBlock' or 'lastBasicBlock', so
                // that they still refer to the canonical instance for this bytecode offset.
                return;
            }
            // End the current basic block (with one new successor).
            addSuccessorToCurrentBasicBlock(label);
        }
        // Append 'label' at the end of the basic block list.
        if (lastBasicBlock != null) {
            if (label.bytecodeOffset == lastBasicBlock.bytecodeOffset) {
                // Same comment as above.
                lastBasicBlock.flags |= (label.flags & Label.FLAG_JUMP_TARGET);
                // Here label.frame should be null.
                label.frame = lastBasicBlock.frame;
                currentBasicBlock = lastBasicBlock;
                return;
            }
            lastBasicBlock.nextBasicBlock = label;
        }
        lastBasicBlock = label;
        // Make it the new current basic block.
        currentBasicBlock = label;
        // Here label.frame should be null.
        label.frame = new Frame(label);
    }
//
//  public void visitLdcInsn(final Object value) {
//    lastBytecodeOffset = code.length;
//    // Add the instruction to the bytecode of the method.
//    Symbol constantSymbol = symbolTable.addConstant(value);
//    int constantIndex = constantSymbol.index;
//    char firstDescriptorChar;
//    boolean isLongOrDouble =
//            constantSymbol.tag == Symbol.CONSTANT_LONG_TAG
//                    || constantSymbol.tag == Symbol.CONSTANT_DOUBLE_TAG
//                    || (constantSymbol.tag == Symbol.CONSTANT_DYNAMIC_TAG
//                    && ((firstDescriptorChar = constantSymbol.value.charAt(0)) == 'J'
//                    || firstDescriptorChar == 'D'));
//    if (isLongOrDouble) {
//      code.put12(Constants.LDC2_W, constantIndex);
//    } else if (constantIndex >= 256) {
//      code.put12(Constants.LDC_W, constantIndex);
//    } else {
//      code.put11(Opcodes.LDC, constantIndex);
//    }
//    // If needed, update the maximum stack size and number of locals, and stack map frames.
//    if (currentBasicBlock != null) {
//      currentBasicBlock.frame.execute(Opcodes.LDC, 0, constantSymbol, symbolTable);
//    }
//  }

    public void visitLdcInsn(final String value) {
        final int CONSTANT_STRING_TAG = 8;

        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        Symbol constantSymbol = symbolTable.addConstantUtf8Reference(CONSTANT_STRING_TAG, value);
        int constantIndex = constantSymbol.index;
        if (constantIndex >= 256) {
            code.put12(Constants.LDC_W, constantIndex);
        } else {
            code.put11(Opcodes.LDC, constantIndex);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(Opcodes.LDC, 0, constantSymbol, symbolTable);
        }
    }

    public void visitLdcInsn(Class value) {
        // getTypeInternal(typeDescriptor, 0, typeDescriptor.length())
        String desc = ASMUtils.desc(value);
        Type type = Type.getTypeInternal(desc, 0, desc.length());
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        Symbol constantSymbol;
//        int typeSort = type.getSort();
        int typeSort = type.sort == Type.INTERNAL ? Type.OBJECT : type.sort;
        if (typeSort == Type.OBJECT) {
            // type.valueBuffer.substring(type.valueBegin, type.valueEnd)
            constantSymbol = symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, type.valueBuffer.substring(type.valueBegin, type.valueEnd));
        } else { // type is a primitive or array type.
            constantSymbol = symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, type.getDescriptor());
        }
        int constantIndex = constantSymbol.index;
        if (constantIndex >= 256) {
            code.put12(Constants.LDC_W, constantIndex);
        } else {
            code.put11(Opcodes.LDC, constantIndex);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(Opcodes.LDC, 0, constantSymbol, symbolTable);
        }
    }

    public void visitLdcInsn(final int value) {
        final int CONSTANT_INTEGER_TAG = 3;

        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        Symbol constantSymbol = symbolTable.addConstantIntegerOrFloat(CONSTANT_INTEGER_TAG, value);
        int constantIndex = constantSymbol.index;
        if (constantIndex >= 256) {
            code.put12(Constants.LDC_W, constantIndex);
        } else {
            code.put11(Opcodes.LDC, constantIndex);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(Opcodes.LDC, 0, constantSymbol, symbolTable);
        }
    }

    public void visitLdcInsn(final long value) {
        final int CONSTANT_LONG_TAG = 5;
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        Symbol constantSymbol = symbolTable.addConstantLongOrDouble(CONSTANT_LONG_TAG, value);
        int constantIndex = constantSymbol.index;
        code.put12(Constants.LDC2_W, constantIndex);
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(Opcodes.LDC, 0, constantSymbol, symbolTable);
        }
    }

    public void visitIincInsn(final int var, final int increment) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        if ((var > 255) || (increment > 127) || (increment < -128)) {
            code.putByte(Constants.WIDE).put12(Opcodes.IINC, var).putShort(increment);
        } else {
            code.putByte(Opcodes.IINC).put11(var, increment);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(Opcodes.IINC, var, null, null);
        }
    }

    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        lastBytecodeOffset = code.length;
        // Add the instruction to the bytecode of the method.
        code.putByte(Opcodes.LOOKUPSWITCH).putByteArray(null, 0, (4 - code.length % 4) % 4);
        dflt.put(code, lastBytecodeOffset, true);
        code.putInt(labels.length);
        for (int i = 0; i < labels.length; ++i) {
            code.putInt(keys[i]);
            labels[i].put(code, lastBytecodeOffset, true);
        }
        // If needed, update the maximum stack size and number of locals, and stack map frames.
        visitSwitchInsn(dflt, labels);
    }

    private void visitSwitchInsn(final Label dflt, final Label[] labels) {
        if (currentBasicBlock != null) {
            currentBasicBlock.frame.execute(Opcodes.LOOKUPSWITCH, 0, null, null);
            // Add all the labels as successors of the current basic block.
            addSuccessorToCurrentBasicBlock(dflt);
            dflt.getCanonicalInstance().flags |= Label.FLAG_JUMP_TARGET;
            for (Label label : labels) {
                addSuccessorToCurrentBasicBlock(label);
                label.getCanonicalInstance().flags |= Label.FLAG_JUMP_TARGET;
            }
            // End the current basic block.
            endCurrentBasicBlockWithNoSuccessor();
        }
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
        /**
         * Computes all the stack map frames of the method, from scratch.
         */
        // computeAllFrames();
        // Create and visit the first (implicit) frame.
        Frame firstFrame = firstBasicBlock.frame;
        firstFrame.setInputFrameFromDescriptor(symbolTable, accessFlags, descriptor, this.maxLocals);
        firstFrame.accept(this);

        // Fix point algorithm: add the first basic block to a list of blocks to process (i.e. blocks
        // whose stack map frame has changed) and, while there are blocks to process, remove one from
        // the list and update the stack map frames of its successor blocks in the control flow graph
        // (which might change them, in which case these blocks must be processed too, and are thus
        // added to the list of blocks to process). Also compute the maximum stack size of the method,
        // as a by-product.
        Label listOfBlocksToProcess = firstBasicBlock;
        listOfBlocksToProcess.nextListElement = Label.EMPTY_LIST;
        int maxStackSize = 0;
        while (listOfBlocksToProcess != Label.EMPTY_LIST) {
            // Remove a basic block from the list of blocks to process.
            Label basicBlock = listOfBlocksToProcess;
            listOfBlocksToProcess = listOfBlocksToProcess.nextListElement;
            basicBlock.nextListElement = null;
            // By definition, basicBlock is reachable.
            basicBlock.flags |= Label.FLAG_REACHABLE;
            // Update the (absolute) maximum stack size.
            int maxBlockStackSize = basicBlock.frame.inputStack.length + basicBlock.outputStackMax;
            if (maxBlockStackSize > maxStackSize) {
                maxStackSize = maxBlockStackSize;
            }
            // Update the successor blocks of basicBlock in the control flow graph.
            Edge outgoingEdge = basicBlock.outgoingEdges;
            while (outgoingEdge != null) {
                Label successorBlock = outgoingEdge.successor.getCanonicalInstance();
                boolean successorBlockChanged =
                        basicBlock.frame.merge(symbolTable, successorBlock.frame);
                if (successorBlockChanged && successorBlock.nextListElement == null) {
                    // If successorBlock has changed it must be processed. Thus, if it is not already in the
                    // list of blocks to process, add it to this list.
                    successorBlock.nextListElement = listOfBlocksToProcess;
                    listOfBlocksToProcess = successorBlock;
                }
                outgoingEdge = outgoingEdge.nextEdge;
            }
        }

        // Loop over all the basic blocks and visit the stack map frames that must be stored in the
        // StackMapTable attribute. Also replace unreachable code with NOP* ATHROW, and remove it from
        // exception handler ranges.
        Label basicBlock = firstBasicBlock;
        while (basicBlock != null) {
            if ((basicBlock.flags & (Label.FLAG_JUMP_TARGET | Label.FLAG_REACHABLE))
                    == (Label.FLAG_JUMP_TARGET | Label.FLAG_REACHABLE)) {
                basicBlock.frame.accept(this);
            }
            if ((basicBlock.flags & Label.FLAG_REACHABLE) == 0) {
                // Find the start and end bytecode offsets of this unreachable block.
                Label nextBasicBlock = basicBlock.nextBasicBlock;
                int startOffset = basicBlock.bytecodeOffset;
                int endOffset = (nextBasicBlock == null ? code.length : nextBasicBlock.bytecodeOffset) - 1;
                if (endOffset >= startOffset) {
                    // Replace its instructions with NOP ... NOP ATHROW.
                    for (int i = startOffset; i < endOffset; ++i) {
                        code.data[i] = Opcodes.NOP;
                    }
                    code.data[endOffset] = (byte) Opcodes.ATHROW;
                    // Emit a frame for this unreachable block, with no local and a Throwable on the stack
                    // (so that the ATHROW could consume this Throwable if it were reachable).
                    int frameIndex = visitFrameStart(startOffset, /* numLocal = */ 0, /* numStack = */ 1);
                    currentFrame[frameIndex] = Frame.REFERENCE_KIND | symbolTable.addType("java/lang/Throwable");
                    visitFrameEnd();
                    // The maximum stack size is now at least one, because of the Throwable declared above.
                    maxStackSize = Math.max(maxStackSize, 1);
                }
            }
            basicBlock = basicBlock.nextBasicBlock;
        }

        this.maxStack = maxStackSize;
    }

    // -----------------------------------------------------------------------------------------------
    // Utility methods: control flow analysis algorithm
    // -----------------------------------------------------------------------------------------------

    /**
     * Adds a successor to {@link #currentBasicBlock} in the control flow graph.
     *
     * @param successor the successor block to be added to the current basic block.
     */
    private void addSuccessorToCurrentBasicBlock(final Label successor) {
        currentBasicBlock.outgoingEdges = new Edge(successor, currentBasicBlock.outgoingEdges);
    }

    /**
     * Ends the current basic block. This method must be used in the case where the current basic
     * block does not have any successor.
     *
     * <p>WARNING: this method must be called after the currently visited instruction has been put in
     * {@link #code} (if frames are computed, this method inserts a new Label to start a new basic
     * block after the current instruction).
     */
    private void endCurrentBasicBlockWithNoSuccessor() {
        Label nextBasicBlock = new Label();
        nextBasicBlock.frame = new Frame(nextBasicBlock);
        nextBasicBlock.resolve(code.data, code.length);
        lastBasicBlock.nextBasicBlock = nextBasicBlock;
        lastBasicBlock = nextBasicBlock;
        currentBasicBlock = null;
    }

    // -----------------------------------------------------------------------------------------------
    // Utility methods: stack map frames
    // -----------------------------------------------------------------------------------------------

    /**
     * Starts the visit of a new stack map frame, stored in {@link #currentFrame}.
     *
     * @param offset   the bytecode offset of the instruction to which the frame corresponds.
     * @param numLocal the number of local variables in the frame.
     * @param numStack the number of stack elements in the frame.
     * @return the index of the next element to be written in this frame.
     */
    int visitFrameStart(final int offset, final int numLocal, final int numStack) {
        int frameLength = 3 + numLocal + numStack;
        if (currentFrame == null || currentFrame.length < frameLength) {
            currentFrame = new int[frameLength];
        }
        currentFrame[0] = offset;
        currentFrame[1] = numLocal;
        currentFrame[2] = numStack;
        return 3;
    }

    /**
     * Sets an abstract type in {@link #currentFrame}.
     *
     * @param frameIndex   the index of the element to be set in {@link #currentFrame}.
     * @param abstractType an abstract type.
     */
    void visitAbstractType(final int frameIndex, final int abstractType) {
        currentFrame[frameIndex] = abstractType;
    }

    /**
     * Ends the visit of {@link #currentFrame} by writing it in the StackMapTable entries and by
     * updating the StackMapTable number_of_entries (except if the current frame is the first one,
     * which is implicit in StackMapTable). Then resets {@link #currentFrame} to {@literal null}.
     */
    void visitFrameEnd() {
        if (previousFrame != null) {
            if (stackMapTableEntries == null) {
                stackMapTableEntries = new ByteVector(2048);
            }
            putFrame();
            ++stackMapTableNumberOfEntries;
        }
        previousFrame = currentFrame;
        currentFrame = null;
    }

    /**
     * Compresses and writes {@link #currentFrame} in a new StackMapTable entry.
     */
    private void putFrame() {
        final int numLocal = currentFrame[1];
        final int numStack = currentFrame[2];
        final int offsetDelta =
                stackMapTableNumberOfEntries == 0
                        ? currentFrame[0]
                        : currentFrame[0] - previousFrame[0] - 1;
        final int previousNumlocal = previousFrame[1];
        final int numLocalDelta = numLocal - previousNumlocal;
        int type = Frame.FULL_FRAME;
        if (numStack == 0) {
            switch (numLocalDelta) {
                case -3:
                case -2:
                case -1:
                    type = Frame.CHOP_FRAME;
                    break;
                case 0:
                    type = offsetDelta < 64 ? Frame.SAME_FRAME : Frame.SAME_FRAME_EXTENDED;
                    break;
                case 1:
                case 2:
                case 3:
                    type = Frame.APPEND_FRAME;
                    break;
                default:
                    // Keep the FULL_FRAME type.
                    break;
            }
        } else if (numLocalDelta == 0 && numStack == 1) {
            type =
                    offsetDelta < 63
                            ? Frame.SAME_LOCALS_1_STACK_ITEM_FRAME
                            : Frame.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED;
        }
        if (type != Frame.FULL_FRAME) {
            // Verify if locals are the same as in the previous frame.
            int frameIndex = 3;
            for (int i = 0; i < previousNumlocal && i < numLocal; i++) {
                if (currentFrame[frameIndex] != previousFrame[frameIndex]) {
                    type = Frame.FULL_FRAME;
                    break;
                }
                frameIndex++;
            }
        }
        switch (type) {
            case Frame.SAME_FRAME:
                stackMapTableEntries.putByte(offsetDelta);
                break;
            case Frame.SAME_LOCALS_1_STACK_ITEM_FRAME:
                stackMapTableEntries.putByte(Frame.SAME_LOCALS_1_STACK_ITEM_FRAME + offsetDelta);
                putAbstractTypes(3 + numLocal, 4 + numLocal);
                break;
            case Frame.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED:
                stackMapTableEntries
                        .putByte(Frame.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED)
                        .putShort(offsetDelta);
                putAbstractTypes(3 + numLocal, 4 + numLocal);
                break;
            case Frame.SAME_FRAME_EXTENDED:
                stackMapTableEntries.putByte(Frame.SAME_FRAME_EXTENDED).putShort(offsetDelta);
                break;
            case Frame.CHOP_FRAME:
                stackMapTableEntries
                        .putByte(Frame.SAME_FRAME_EXTENDED + numLocalDelta)
                        .putShort(offsetDelta);
                break;
            case Frame.APPEND_FRAME:
                stackMapTableEntries
                        .putByte(Frame.SAME_FRAME_EXTENDED + numLocalDelta)
                        .putShort(offsetDelta);
                putAbstractTypes(3 + previousNumlocal, 3 + numLocal);
                break;
            case Frame.FULL_FRAME:
            default:
                stackMapTableEntries.putByte(Frame.FULL_FRAME).putShort(offsetDelta).putShort(numLocal);
                putAbstractTypes(3, 3 + numLocal);
                stackMapTableEntries.putShort(numStack);
                putAbstractTypes(3 + numLocal, 3 + numLocal + numStack);
                break;
        }
    }

    /**
     * Puts some abstract types of {@link #currentFrame} in {@link #stackMapTableEntries} , using the
     * JVMS verification_type_info format used in StackMapTable attributes.
     *
     * @param start index of the first type in {@link #currentFrame} to write.
     * @param end   index of last type in {@link #currentFrame} to write (exclusive).
     */
    private void putAbstractTypes(final int start, final int end) {
        for (int i = start; i < end; ++i) {
            final int abstractType = currentFrame[i];
            final ByteVector output = stackMapTableEntries;

            int arrayDimensions = (abstractType & Frame.DIM_MASK) >> Frame.DIM_SHIFT;
            if (arrayDimensions == 0) {
                int typeValue = abstractType & Frame.VALUE_MASK;
                switch (abstractType & Frame.KIND_MASK) {
                    case Frame.CONSTANT_KIND:
                        output.putByte(typeValue);
                        break;
                    case Frame.REFERENCE_KIND:
                        output
                                .putByte(Frame.ITEM_OBJECT)
                                .putShort(symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, symbolTable.typeTable[typeValue].value).index);
                        break;
                    case Frame.UNINITIALIZED_KIND:
                        output.putByte(Frame.ITEM_UNINITIALIZED).putShort((int) symbolTable.typeTable[typeValue].data);
                        break;
                    default:
                        throw new AssertionError();
                }
            } else {
                // Case of an array type, we need to build its descriptor first.
                StringBuilder typeDescriptor = new StringBuilder();
                while (arrayDimensions-- > 0) {
                    typeDescriptor.append('[');
                }
                if ((abstractType & Frame.KIND_MASK) == Frame.REFERENCE_KIND) {
                    typeDescriptor
                            .append('L')
                            .append(symbolTable.typeTable[abstractType & Frame.VALUE_MASK].value)
                            .append(';');
                } else {
                    switch (abstractType & Frame.VALUE_MASK) {
                        case Frame.ITEM_ASM_BOOLEAN:
                            typeDescriptor.append('Z');
                            break;
                        case Frame.ITEM_ASM_BYTE:
                            typeDescriptor.append('B');
                            break;
                        case Frame.ITEM_ASM_CHAR:
                            typeDescriptor.append('C');
                            break;
                        case Frame.ITEM_ASM_SHORT:
                            typeDescriptor.append('S');
                            break;
                        case Frame.ITEM_INTEGER:
                            typeDescriptor.append('I');
                            break;
                        case Frame.ITEM_FLOAT:
                            typeDescriptor.append('F');
                            break;
                        case Frame.ITEM_LONG:
                            typeDescriptor.append('J');
                            break;
                        case Frame.ITEM_DOUBLE:
                            typeDescriptor.append('D');
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
                output
                        .putByte(Frame.ITEM_OBJECT)
                        .putShort(symbolTable.addConstantUtf8Reference(/*CONSTANT_CLASS_TAG*/ 7, typeDescriptor.toString()).index);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Utility methods
    // -----------------------------------------------------------------------------------------------

    /**
     * Returns the size of the method_info JVMS structure generated by this MethodWriter. Also add the
     * names of the attributes of this method in the constant pool.
     *
     * @return the size in bytes of the method_info JVMS structure.
     */
    int computeMethodInfoSize() {
        // 2 bytes each for access_flags, name_index, descriptor_index and attributes_count.
        int size = 8;
        // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
        if (code.length > 0) {
            if (code.length > 65535) {
                throw new JSONException("Method too large: " + symbolTable.className + "." + name + " " + descriptor + ", length " + code.length);
            }
            symbolTable.addConstantUtf8(Constants.CODE);
            // The Code attribute has 6 header bytes, plus 2, 2, 4 and 2 bytes respectively for max_stack,
            // max_locals, code_length and attributes_count, plus the bytecode and the exception table.
            size += 16 + code.length + 2;
            if (stackMapTableEntries != null) {
                boolean useStackMapTable = true;
                symbolTable.addConstantUtf8(useStackMapTable ? Constants.STACK_MAP_TABLE : "StackMap");
                // 6 header bytes and 2 bytes for number_of_entries.
                size += 8 + stackMapTableEntries.length;
            }
        }
        return size;
    }

    /**
     * Puts the content of the method_info JVMS structure generated by this MethodWriter into the
     * given ByteVector.
     *
     * @param output where the method_info structure must be put.
     */
    void putMethodInfo(final ByteVector output) {
        int mask = 0;
        output.putShort(accessFlags & ~mask).putShort(nameIndex).putShort(descriptorIndex);
        // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
        int attributeCount = 0;
        if (code.length > 0) {
            ++attributeCount;
        }
        // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
        output.putShort(attributeCount);
        if (code.length > 0) {
            // 2, 2, 4 and 2 bytes respectively for max_stack, max_locals, code_length and
            // attributes_count, plus the bytecode and the exception table.
            int size = 10 + code.length + 2;
            int codeAttributeCount = 0;
            if (stackMapTableEntries != null) {
                // 6 header bytes and 2 bytes for number_of_entries.
                size += 8 + stackMapTableEntries.length;
                ++codeAttributeCount;
            }
            output
                    .putShort(symbolTable.addConstantUtf8(Constants.CODE))
                    .putInt(size)
                    .putShort(maxStack)
                    .putShort(maxLocals)
                    .putInt(code.length)
                    .putByteArray(code.data, 0, code.length);
            output.putShort(0); // putExceptionTable
            output.putShort(codeAttributeCount);
            if (stackMapTableEntries != null) {
                boolean useStackMapTable = true;
                output
                        .putShort(
                                symbolTable.addConstantUtf8(
                                        useStackMapTable ? Constants.STACK_MAP_TABLE : "StackMap"))
                        .putInt(2 + stackMapTableEntries.length)
                        .putShort(stackMapTableNumberOfEntries)
                        .putByteArray(stackMapTableEntries.data, 0, stackMapTableEntries.length);
            }
        }
    }
}
