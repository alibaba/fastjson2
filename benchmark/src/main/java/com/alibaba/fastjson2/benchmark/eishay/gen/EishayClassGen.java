package com.alibaba.fastjson2.benchmark.eishay.gen;

import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import com.alibaba.fastjson2.util.TypeUtils;
import org.objectweb.asm.*;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class EishayClassGen {
    public Class genMedia(DynamicClassLoader classLoader, String packageName) throws Exception {
        Class playerClass = genEnum(classLoader, packageName + "/Media$Player", "JAVA", "FLASH");

        Class mediaClass;
        {
            Map<String, java.lang.reflect.Type> fields = new LinkedHashMap<>();
            fields.put("bitrate", long.class);
            fields.put("duration", long.class);
            fields.put("format", String.class);
            fields.put("persons", TypeReference.collectionType(List.class, String.class));
            fields.put("player", playerClass);
            fields.put("size", long.class);
            fields.put("title", String.class);
            fields.put("uri", String.class);
            fields.put("width", int.class);

            mediaClass = genClass(classLoader, packageName + "/Media", fields);
        }

        Class sizeClass = genEnum(classLoader, packageName + "/Image$Size", "SMALL", "LARGE");
        Class imageClass;
        {
            Map<String, java.lang.reflect.Type> fields = new LinkedHashMap<>();
            fields.put("height", int.class);
            fields.put("size", sizeClass);
            fields.put("title", String.class);
            fields.put("uri", String.class);
            fields.put("width", int.class);

            imageClass = genClass(classLoader, packageName + "/Image", fields);
        }

        Class mediaContentClass;
        {
            Map<String, java.lang.reflect.Type> fields = new LinkedHashMap<>();
            fields.put("media", mediaClass);
            fields.put("images", TypeReference.collectionType(List.class, imageClass));

            mediaContentClass = genClass(classLoader, packageName + "/MediaContent", fields);
        }

        return mediaContentClass;
    }

    static String getSignature(java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            java.lang.reflect.Type rawType = paramType.getRawType();
            java.lang.reflect.Type[] arguments = paramType.getActualTypeArguments();
            if (rawType == List.class) {
                // "Ljava/util/List<L" + type + ";>;",
                Class itemType = (Class) arguments[0];
                String signature = "Ljava/util/List<" + Type.getType(itemType) + ">;";
                return signature;
            }
        }
        return null;
    }

    public Class genClass(DynamicClassLoader classLoader, String type, Map<String, java.lang.reflect.Type> fields) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String desc = "L" + type + ";";
        cw.visit(
                Opcodes.V1_8,
                ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
                type,
                null,
                "java/lang/Object",
                null
        );

        fields.forEach((fieldName, fieldType) -> {
            Class fieldClass = TypeUtils.getClass(fieldType);
            String fieldDesc = Type.getDescriptor(fieldClass);
            String signature = getSignature(fieldType);
            FieldVisitor fv = cw.visitField(ACC_PUBLIC, fieldName, fieldDesc, signature, null);
            fv.visitEnd();
        });

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V",
                    "()V", null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();

        byte[] code = cw.toByteArray();
        String fullName = type.replace('/', '.');
        return classLoader.defineClassPublic(fullName, code, 0, code.length);
    }

    public Class genEnum(DynamicClassLoader classLoader, String type, String... values) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String desc = "L" + type + ";";
        cw.visit(
                Opcodes.V1_8,
                ACC_PUBLIC + ACC_FINAL + ACC_SUPER + ACC_ENUM,
                type,
                "Ljava/lang/Enum<L" + type + ";>;",
                "java/lang/Enum",
                null
        );

        for (String value : values) {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC + ACC_ENUM, value, desc, null, null);
            fv.visitEnd();
        }

        {
            FieldVisitor fv = cw.visitField(
                    ACC_PRIVATE + ACC_FINAL + ACC_STATIC + ACC_SYNTHETIC,
                    "$VALUES", "[" + desc, null, null);
            fv.visitEnd();
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "<init>", "(Ljava/lang/String;I)V",
                    "()V", null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Enum", "<init>",
                    "(Ljava/lang/String;I)V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "values",
                    "()[" + desc, null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, type, "$VALUES", "[" + desc);
            mv.visitMethodInsn(INVOKEVIRTUAL, "[" + desc, "clone",
                    "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "[" + desc);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "valueOf",
                    "(Ljava/lang/String;)" + desc, null, null);
            mv.visitCode();
            mv.visitLdcInsn(Type.getType(desc));
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Enum", "valueOf",
                    "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");
            mv.visitTypeInsn(CHECKCAST, type);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        {
            MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();

            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                mv.visitTypeInsn(NEW, type);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(value);
                mv.visitLdcInsn(i);
                mv.visitMethodInsn(INVOKESPECIAL, type, "<init>", "(Ljava/lang/String;I)V");
                mv.visitFieldInsn(PUTSTATIC, type, value, desc);
            }

            mv.visitLdcInsn(values.length);
            mv.visitTypeInsn(ANEWARRAY, type);

            for (int i = 0; i < values.length; i++) {
                String value = values[i];

                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                mv.visitFieldInsn(GETSTATIC, type, value, desc);
                mv.visitInsn(AASTORE);
            }

            mv.visitFieldInsn(PUTSTATIC, type, "$VALUES", "[" + desc);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();

        byte[] code = cw.toByteArray();
        return classLoader.defineClassPublic(type.replace('/', '.'), code, 0, code.length);
    }
}
