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
 * Defines additional JVM opcodes, access flags and constants which are not part of the ASM public
 * API.
 *
 * @author Eric Bruneton
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-6.html">JVMS 6</a>
 */
final class Constants {
    // The ClassFile attribute names, in the order they are defined in
    // https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.7-300.

//    static final String CONSTANT_VALUE = "ConstantValue";
    static final String CODE = "Code";
    static final String STACK_MAP_TABLE = "StackMapTable";
//    static final String EXCEPTIONS = "Exceptions";
//    static final String INNER_CLASSES = "InnerClasses";
//    static final String ENCLOSING_METHOD = "EnclosingMethod";
//    static final String SYNTHETIC = "Synthetic";
//    static final String SIGNATURE = "Signature";
//    static final String SOURCE_FILE = "SourceFile";
//    static final String SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
//    static final String LINE_NUMBER_TABLE = "LineNumberTable";
//    static final String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
//    static final String LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";
//    static final String DEPRECATED = "Deprecated";
//    static final String RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
//    static final String RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
//    static final String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
//    static final String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS =
//            "RuntimeInvisibleParameterAnnotations";
//    static final String RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
//    static final String RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = "RuntimeInvisibleTypeAnnotations";
//    static final String ANNOTATION_DEFAULT = "AnnotationDefault";
//    static final String BOOTSTRAP_METHODS = "BootstrapMethods";
//    static final String METHOD_PARAMETERS = "MethodParameters";
//    static final String MODULE = "Module";
//    static final String MODULE_PACKAGES = "ModulePackages";
//    static final String MODULE_MAIN_CLASS = "ModuleMainClass";
//    static final String NEST_HOST = "NestHost";
//    static final String NEST_MEMBERS = "NestMembers";
//    static final String PERMITTED_SUBCLASSES = "PermittedSubclasses";
//    static final String RECORD = "Record";

    // ASM specific access flags.
    // WARNING: the 16 least significant bits must NOT be used, to avoid conflicts with standard
    // access flags, and also to make sure that these flags are automatically filtered out when
    // written in class files (because access flags are stored using 16 bits only).

    static final int ACC_CONSTRUCTOR = 0x40000; // method access flag.
    // The JVM opcode values which are not part of the ASM public API.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html.

    static final int LDC_W = 19;
    static final int LDC2_W = 20;
    static final int ILOAD_0 = 26;
//    static final int ILOAD_1 = 27;
//    static final int ILOAD_2 = 28;
//    static final int ILOAD_3 = 29;
//    static final int LLOAD_0 = 30;
//    static final int LLOAD_1 = 31;
//    static final int LLOAD_2 = 32;
//    static final int LLOAD_3 = 33;
//    static final int FLOAD_0 = 34;
//    static final int FLOAD_1 = 35;
//    static final int FLOAD_2 = 36;
//    static final int FLOAD_3 = 37;
//    static final int DLOAD_0 = 38;
//    static final int DLOAD_1 = 39;
//    static final int DLOAD_2 = 40;
//    static final int DLOAD_3 = 41;
//    static final int ALOAD_0 = 42;
//    static final int ALOAD_1 = 43;
//    static final int ALOAD_2 = 44;
//    static final int ALOAD_3 = 45;
    static final int ISTORE_0 = 59;
//    static final int ISTORE_1 = 60;
//    static final int ISTORE_2 = 61;
//    static final int ISTORE_3 = 62;
//    static final int LSTORE_0 = 63;
//    static final int LSTORE_1 = 64;
//    static final int LSTORE_2 = 65;
//    static final int LSTORE_3 = 66;
//    static final int FSTORE_0 = 67;
//    static final int FSTORE_1 = 68;
//    static final int FSTORE_2 = 69;
//    static final int FSTORE_3 = 70;
//    static final int DSTORE_0 = 71;
//    static final int DSTORE_1 = 72;
//    static final int DSTORE_2 = 73;
//    static final int DSTORE_3 = 74;
//    static final int ASTORE_0 = 75;
//    static final int ASTORE_1 = 76;
//    static final int ASTORE_2 = 77;
//    static final int ASTORE_3 = 78;
    static final int WIDE = 196;
    static final int GOTO_W = 200;
//    static final int JSR_W = 201;

    // Constants to convert between normal and wide jump instructions.

    // The delta between the GOTO_W and JSR_W opcodes and GOTO and JUMP.
    static final int WIDE_JUMP_OPCODE_DELTA = GOTO_W - Opcodes.GOTO;

    // Constants to convert JVM opcodes to the equivalent ASM specific opcodes, and vice versa.

    // The delta between the ASM_IFEQ, ..., ASM_IF_ACMPNE, ASM_GOTO and ASM_JSR opcodes
    // and IFEQ, ..., IF_ACMPNE, GOTO and JSR.
    static final int ASM_OPCODE_DELTA = 49;

    // The delta between the ASM_IFNULL and ASM_IFNONNULL opcodes and IFNULL and IFNONNULL.
    static final int ASM_IFNULL_OPCODE_DELTA = 20;

    private Constants() {
    }
}
