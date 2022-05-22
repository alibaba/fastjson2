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
 * A position in the bytecode of a method. Labels are used for jump, goto, and switch instructions,
 * and for try catch blocks. A label designates the <i>instruction</i> that is just after. Note
 * however that there can be other elements between a label and the instruction it designates (such
 * as other labels, stack map frames, line numbers, etc.).
 *
 * @author Eric Bruneton
 */
public class Label {
    /**
     * A flag indicating that a label is only used for debug attributes. Such a label is not the start
     * of a basic block, the target of a jump instruction, or an exception handler. It can be safely
     * ignored in control flow graph analysis algorithms (for optimization purposes).
     */
    static final int FLAG_DEBUG_ONLY = 1;

    /**
     * A flag indicating that a label is the target of a jump instruction, or the start of an
     * exception handler.
     */
    static final int FLAG_JUMP_TARGET = 2;

    /**
     * A flag indicating that the bytecode offset of a label is known.
     */
    static final int FLAG_RESOLVED = 4;

    /**
     * A flag indicating that a label corresponds to a reachable basic block.
     */
    static final int FLAG_REACHABLE = 8;

    /**
     * The number of elements to add to the {@link #forwardReferences} array when it needs to be
     * resized to store a new forward reference.
     */
    static final int FORWARD_REFERENCES_CAPACITY_INCREMENT = 6;

    /**
     * The bit mask to extract the type of a forward reference to this label. The extracted type is
     * either {@link #FORWARD_REFERENCE_TYPE_SHORT} or {@link #FORWARD_REFERENCE_TYPE_WIDE}.
     *
     * @see #forwardReferences
     */
    static final int FORWARD_REFERENCE_TYPE_MASK = 0xF0000000;

    /**
     * The type of forward references stored with two bytes in the bytecode. This is the case, for
     * instance, of a forward reference from an ifnull instruction.
     */
    static final int FORWARD_REFERENCE_TYPE_SHORT = 0x10000000;

    /**
     * The type of forward references stored in four bytes in the bytecode. This is the case, for
     * instance, of a forward reference from a lookupswitch instruction.
     */
    static final int FORWARD_REFERENCE_TYPE_WIDE = 0x20000000;

    /**
     * The bit mask to extract the 'handle' of a forward reference to this label. The extracted handle
     * is the bytecode offset where the forward reference value is stored (using either 2 or 4 bytes,
     * as indicated by the {@link #FORWARD_REFERENCE_TYPE_MASK}).
     *
     * @see #forwardReferences
     */
    static final int FORWARD_REFERENCE_HANDLE_MASK = 0x0FFFFFFF;

    /**
     * A sentinel element used to indicate the end of a list of labels.
     *
     * @see #nextListElement
     */
    static final Label EMPTY_LIST = new Label();

    /**
     * A user managed state associated with this label. Warning: this field is used by the ASM tree
     * package. In order to use it with the ASM tree package you must override the getLabelNode method
     * in MethodNode.
     */
    public Object info;

    short flags;

    /**
     * The offset of this label in the bytecode of its method, in bytes. This value is set if and only
     * if the {@link #FLAG_RESOLVED} flag is set.
     */
    int bytecodeOffset;

    /**
     * The forward references to this label. The first element is the number of forward references,
     * times 2 (this corresponds to the index of the last element actually used in this array). Then,
     * each forward reference is described with two consecutive integers noted
     * 'sourceInsnBytecodeOffset' and 'reference':
     *
     * <ul>
     *   <li>'sourceInsnBytecodeOffset' is the bytecode offset of the instruction that contains the
     *       forward reference,
     *   <li>'reference' contains the type and the offset in the bytecode where the forward reference
     *       value must be stored, which can be extracted with {@link #FORWARD_REFERENCE_TYPE_MASK}
     *       and {@link #FORWARD_REFERENCE_HANDLE_MASK}.
     * </ul>
     *
     * <p>For instance, for an ifnull instruction at bytecode offset x, 'sourceInsnBytecodeOffset' is
     * equal to x, and 'reference' is of type {@link #FORWARD_REFERENCE_TYPE_SHORT} with value x + 1
     * (because the ifnull instruction uses a 2 bytes bytecode offset operand stored one byte after
     * the start of the instruction itself). For the default case of a lookupswitch instruction at
     * bytecode offset x, 'sourceInsnBytecodeOffset' is equal to x, and 'reference' is of type {@link
     * #FORWARD_REFERENCE_TYPE_WIDE} with value between x + 1 and x + 4 (because the lookupswitch
     * instruction uses a 4 bytes bytecode offset operand stored one to four bytes after the start of
     * the instruction itself).
     */
    private int[] forwardReferences;

    /**
     * The maximum height reached by the output stack, relatively to the top of the input stack, in
     * the basic block corresponding to this label. This maximum is always positive or {@literal
     * null}.
     */
    short outputStackMax;

    Frame frame;

    Label nextBasicBlock;

    Edge outgoingEdges;

    Label nextListElement;

    // -----------------------------------------------------------------------------------------------
    // Constructor and accessors
    // -----------------------------------------------------------------------------------------------

    /**
     * Constructs a new label.
     */
    public Label() {
        // Nothing to do.
    }

    final Label getCanonicalInstance() {
        return frame == null ? this : frame.owner;
    }

    // -----------------------------------------------------------------------------------------------
    // Methods to compute offsets and to manage forward references
    // -----------------------------------------------------------------------------------------------

    /**
     * Puts a reference to this label in the bytecode of a method. If the bytecode offset of the label
     * is known, the relative bytecode offset between the label and the instruction referencing it is
     * computed and written directly. Otherwise, a null relative offset is written and a new forward
     * reference is declared for this label.
     *
     * @param code                     the bytecode of the method. This is where the reference is appended.
     * @param sourceInsnBytecodeOffset the bytecode offset of the instruction that contains the
     *                                 reference to be appended.
     * @param wideReference            whether the reference must be stored in 4 bytes (instead of 2 bytes).
     */
    final void put(
            final ByteVector code, final int sourceInsnBytecodeOffset, final boolean wideReference) {
        if ((flags & FLAG_RESOLVED) == 0) {
            if (wideReference) {
                addForwardReference(sourceInsnBytecodeOffset, FORWARD_REFERENCE_TYPE_WIDE, code.length);
                code.putInt(-1);
            } else {
                addForwardReference(sourceInsnBytecodeOffset, FORWARD_REFERENCE_TYPE_SHORT, code.length);
                code.putShort(-1);
            }
        } else {
            if (wideReference) {
                code.putInt(bytecodeOffset - sourceInsnBytecodeOffset);
            } else {
                code.putShort(bytecodeOffset - sourceInsnBytecodeOffset);
            }
        }
    }

    /**
     * Adds a forward reference to this label. This method must be called only for a true forward
     * reference, i.e. only if this label is not resolved yet. For backward references, the relative
     * bytecode offset of the reference can be, and must be, computed and stored directly.
     *
     * @param sourceInsnBytecodeOffset the bytecode offset of the instruction that contains the
     *                                 reference stored at referenceHandle.
     * @param referenceType            either {@link #FORWARD_REFERENCE_TYPE_SHORT} or {@link
     *                                 #FORWARD_REFERENCE_TYPE_WIDE}.
     * @param referenceHandle          the offset in the bytecode where the forward reference value must be
     *                                 stored.
     */
    private void addForwardReference(
            final int sourceInsnBytecodeOffset, final int referenceType, final int referenceHandle) {
        if (forwardReferences == null) {
            forwardReferences = new int[FORWARD_REFERENCES_CAPACITY_INCREMENT];
        }
        int lastElementIndex = forwardReferences[0];
        if (lastElementIndex + 2 >= forwardReferences.length) {
            int[] newValues = new int[forwardReferences.length + FORWARD_REFERENCES_CAPACITY_INCREMENT];
            System.arraycopy(forwardReferences, 0, newValues, 0, forwardReferences.length);
            forwardReferences = newValues;
        }
        forwardReferences[++lastElementIndex] = sourceInsnBytecodeOffset;
        forwardReferences[++lastElementIndex] = referenceType | referenceHandle;
        forwardReferences[0] = lastElementIndex;
    }

    /**
     * Sets the bytecode offset of this label to the given value and resolves the forward references
     * to this label, if any. This method must be called when this label is added to the bytecode of
     * the method, i.e. when its bytecode offset becomes known. This method fills in the blanks that
     * where left in the bytecode by each forward reference previously added to this label.
     *
     * @param code           the bytecode of the method.
     * @param bytecodeOffset the bytecode offset of this label.
     * @return {@literal true} if a blank that was left for this label was too small to store the
     * offset. In such a case the corresponding jump instruction is replaced with an equivalent
     * ASM specific instruction using an unsigned two bytes offset. These ASM specific
     * instructions are later replaced with standard bytecode instructions with wider offsets (4
     * bytes instead of 2), in ClassReader.
     */
    final boolean resolve(final byte[] code, final int bytecodeOffset) {
        this.flags |= FLAG_RESOLVED;
        this.bytecodeOffset = bytecodeOffset;
        if (forwardReferences == null) {
            return false;
        }
        boolean hasAsmInstructions = false;
        for (int i = forwardReferences[0]; i > 0; i -= 2) {
            final int sourceInsnBytecodeOffset = forwardReferences[i - 1];
            final int reference = forwardReferences[i];
            final int relativeOffset = bytecodeOffset - sourceInsnBytecodeOffset;
            int handle = reference & FORWARD_REFERENCE_HANDLE_MASK;
            if ((reference & FORWARD_REFERENCE_TYPE_MASK) == FORWARD_REFERENCE_TYPE_SHORT) {
                if (relativeOffset < Short.MIN_VALUE || relativeOffset > Short.MAX_VALUE) {
                    // Change the opcode of the jump instruction, in order to be able to find it later in
                    // ClassReader. These ASM specific opcodes are similar to jump instruction opcodes, except
                    // that the 2 bytes offset is unsigned (and can therefore represent values from 0 to
                    // 65535, which is sufficient since the size of a method is limited to 65535 bytes).
                    int opcode = code[sourceInsnBytecodeOffset] & 0xFF;
                    if (opcode < Opcodes.IFNULL) {
                        // Change IFEQ ... JSR to ASM_IFEQ ... ASM_JSR.
                        code[sourceInsnBytecodeOffset] = (byte) (opcode + Constants.ASM_OPCODE_DELTA);
                    } else {
                        // Change IFNULL and IFNONNULL to ASM_IFNULL and ASM_IFNONNULL.
                        code[sourceInsnBytecodeOffset] = (byte) (opcode + Constants.ASM_IFNULL_OPCODE_DELTA);
                    }
                    hasAsmInstructions = true;
                }
                code[handle++] = (byte) (relativeOffset >>> 8);
                code[handle] = (byte) relativeOffset;
            } else {
                code[handle++] = (byte) (relativeOffset >>> 24);
                code[handle++] = (byte) (relativeOffset >>> 16);
                code[handle++] = (byte) (relativeOffset >>> 8);
                code[handle] = (byte) relativeOffset;
            }
        }
        return hasAsmInstructions;
    }
}
