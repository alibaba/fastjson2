package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.internal.asm.ClassWriter;
import com.alibaba.fastjson2.internal.asm.MethodWriter;
import com.alibaba.fastjson2.internal.asm.Opcodes;
import com.alibaba.fastjson2.util.DynamicClassLoader;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ObjIntConsumer;

public class LambdaGenerator {
    static final AtomicInteger counter = new AtomicInteger();

    public static <T> ObjIntConsumer<T> createSetterInt(Class<T> objectClass, String methodName) throws Throwable {
        ClassWriter cw = new ClassWriter(null);

        final String JAVA_LANG_OBJECT = "java/lang/Object";
        String[] interfaces = {"java/util/function/ObjIntConsumer"};

        String lambdaClassName = "SetInt$Lambda$" + counter.incrementAndGet();
//        if (JDKUtils.JVM_VERSION > 16) {
//            String pkgName = objectClass.getPackage().getName();
//            pkgName = pkgName.replace('.', '/');
//            lambdaClassName = pkgName + '/' + lambdaClassName;
//        }
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, lambdaClassName, JAVA_LANG_OBJECT, interfaces);

        final int THIS = 0;
        {
            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "()V",
                    64
            );
            mw.visitVarInsn(Opcodes.ALOAD, THIS);

            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "accept",
                "(Ljava/lang/Object;I)V",
                64
        );

        String TYPE_OBJECT = ASMUtils.type(objectClass);
        int OBJECT = 1, VALUE = 2;
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
        mw.visitVarInsn(Opcodes.ILOAD, VALUE);

        Class returnType = Void.TYPE;
        String methodDesc;
        if (returnType == Void.TYPE) {
            methodDesc = "(I)V";
        } else {
            methodDesc = "(I)" + ASMUtils.desc(returnType);
        }

        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT, methodName, methodDesc, false);
        if (returnType != Void.TYPE) {
            mw.visitInsn(Opcodes.POP);
        }

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(2, 2);

        byte[] code = cw.toByteArray();

        Class functionClass = DynamicClassLoader.getInstance().defineClassPublic(lambdaClassName, code, 0, code.length);

        Constructor ctr = functionClass.getDeclaredConstructor();
        Object inst = ctr.newInstance();
        ConstantCallSite callSite = new ConstantCallSite(MethodHandles.constant(ObjIntConsumer.class, inst));
        return (ObjIntConsumer<T>) callSite.getTarget().invokeExact();
    }
}
