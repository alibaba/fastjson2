package com.alibaba.fastjson2.internal.asm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TypeReviewRegressionTest {
    @Test
    public void fieldArraysKeepTheirTypesAcrossStackFrames() throws Exception {
        for (Class<?> arrayClass : new Class<?>[]{com.alibaba.fastjson2.reader.FieldReader[].class,
                com.alibaba.fastjson2.writer.FieldWriter[].class}) {
            ClassWriter writer = new ClassWriter(null);
            writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "ReviewArrayFrame", "java/lang/Object", null);
            MethodWriter method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "identity",
                    "(Ljava/lang/Object;)Ljava/lang/Object;", 64);
            Label target = new Label();
            method.aload(0);
            method.checkcast(ASMUtils.desc(arrayClass));
            method.astore(1);
            method.aload(1);
            method.ifnull(target);
            method.aload(1);
            method.areturn();
            method.visitLabel(target);
            method.aload(1);
            method.areturn();
            method.visitMaxs(0, 0);
            Class<?> generated = new Loader().define(writer.toByteArray());
            Object value = java.lang.reflect.Array.newInstance(arrayClass.getComponentType(), 0);
            assertSame(value, generated.getMethod("identity", Object.class).invoke(null, value));
        }
    }

    @Test
    public void cachedConstructorDescriptorRetainsFieldReaderArray() {
        Type[] arguments = Type.getArgumentTypes(
                "(Ljava/lang/Class;Ljava/util/function/Supplier;[Lcom/alibaba/fastjson2/reader/FieldReader;)V");
        assertEquals(3, arguments.length);
        assertEquals("Ljava/lang/Class;", arguments[0].getDescriptor());
        assertEquals("Ljava/util/function/Supplier;", arguments[1].getDescriptor());
        assertEquals("[Lcom/alibaba/fastjson2/reader/FieldReader;", arguments[2].getDescriptor());
        assertEquals(Type.ARRAY, arguments[2].sort);
    }

    static class Loader extends ClassLoader {
        Class<?> define(byte[] bytes) {
            return defineClass(null, bytes, 0, bytes.length);
        }
    }
}
