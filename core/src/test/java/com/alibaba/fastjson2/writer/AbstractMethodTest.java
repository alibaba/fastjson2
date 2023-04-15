package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.internal.asm.ClassWriter;
import com.alibaba.fastjson2.internal.asm.MethodWriter;
import com.alibaba.fastjson2.internal.asm.Opcodes;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractMethodTest {
    @Test
    public void test() throws Exception {
        DynamicClassLoader classLoader = new DynamicClassLoader(AbstractMethodTest.class.getClassLoader());

        ClassWriter cw = new ClassWriter(e -> null);

        String className = "AbstractMethodBean";
        String classNameType;
        String classNameFull;

        String packageName = "asm";
        int packageNameLength = packageName.length();
        int charsLength = packageNameLength + 1 + className.length();
        char[] chars = new char[charsLength];
        packageName.getChars(0, packageName.length(), chars, 0);
        chars[packageNameLength] = '.';
        className.getChars(0, className.length(), chars, packageNameLength + 1);
        classNameFull = new String(chars);

        chars[packageNameLength] = '/';
        for (int i = 0; i < packageNameLength; ++i) {
            if (chars[i] == '.') {
                chars[i] = '/';
            }
        }
        classNameType = new String(chars);

        cw.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                classNameType,
                "java/lang/Object",
                new String[]{
                        ASMUtils.type(IUser.class)
                }
        );

        {
            final int THIS = 0;

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "()V",
                    32
            );
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        byte[] code = cw.toByteArray();

        Class<?> consumerClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);
        Constructor<?> constructor = consumerClass.getConstructor();
        Object instance = constructor.newInstance();
        assertEquals("{}", JSON.toJSONString(instance));
    }

    public interface IUser {
        Long gerId();
    }

    @Test
    public void test1() throws Exception {
        DynamicClassLoader classLoader = new DynamicClassLoader(AbstractMethodTest.class.getClassLoader());

        ClassWriter cw = new ClassWriter(e -> null);

        String className = "AbstractMethodBean";
        String classNameType;
        String classNameFull;

        String packageName = "asm";
        int packageNameLength = packageName.length();
        int charsLength = packageNameLength + 1 + className.length();
        char[] chars = new char[charsLength];
        packageName.getChars(0, packageName.length(), chars, 0);
        chars[packageNameLength] = '.';
        className.getChars(0, className.length(), chars, packageNameLength + 1);
        classNameFull = new String(chars);

        chars[packageNameLength] = '/';
        for (int i = 0; i < packageNameLength; ++i) {
            if (chars[i] == '.') {
                chars[i] = '/';
            }
        }
        classNameType = new String(chars);

        String superClassType = ASMUtils.type(AbstractUser.class);
        cw.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                classNameType,
                superClassType,
                new String[]{}
        );

        {
            final int THIS = 0;

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "()V",
                    32
            );
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassType, "<init>", "()V", false);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        byte[] code = cw.toByteArray();

        Class<?> consumerClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);
        Constructor<?> constructor = consumerClass.getConstructor();
        Object instance = constructor.newInstance();
        assertEquals("{}", JSON.toJSONString(instance));
    }

    public abstract static class AbstractUser {
        public AbstractUser() {
        }

        public abstract Long gerId();
    }
}
