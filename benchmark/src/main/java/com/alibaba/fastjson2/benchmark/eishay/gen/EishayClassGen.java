package com.alibaba.fastjson2.benchmark.eishay.gen;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.benchmark.eishay.EishayFuryWriteNoneCache;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class EishayClassGen {
    public Class genMedia(DynamicClassLoader classLoader, String packageName) throws Exception {
        classLoader.definePackage(packageName.replace('/', '.'));

        LinkedHashMap<String, byte[]> codes = new LinkedHashMap();
        LinkedHashMap<String, Class> classes = new LinkedHashMap();

        genCode(packageName, codes);
        codes.forEach((name, code) -> {
            Class<?> clazz = classLoader.loadClass(name, code, 0, code.length);
            classes.put(name, clazz);
        });

        String mediaContentClassName = packageName.replace('/', '.') + ".MediaContent";
        return classes.get(mediaContentClassName);
    }

    public void genCode(String packageName, Map<String, byte[]> classBytes) {
        String playerType = packageName + "/Media$Player";
        String playerClassName = playerType.replace('/', '.');
        byte[] playerCode = genEnum(playerType, "JAVA", "FLASH");
        classBytes.put(playerClassName, playerCode);

        String mediaType = packageName + "/Media";
        String mediaClassName = mediaType.replace('/', '.');
        byte[] mediaCode = genClass(mediaType,
                new FieldInfo[]{
                        new FieldInfo("bitrate", long.class),
                        new FieldInfo("duration", long.class),
                        new FieldInfo("format", String.class),
                        new FieldInfo("height", int.class),
                        new FieldInfo("persons", TypeReference.collectionType(List.class, String.class)),
                        new FieldInfo("player", "L" + playerType + ";", null),
                        new FieldInfo("size", long.class),
                        new FieldInfo("title", String.class),
                        new FieldInfo("uri", String.class),
                        new FieldInfo("width", int.class),
                        new FieldInfo("copyright", String.class)
                });
        classBytes.put(mediaClassName, mediaCode);

        String sizeType = packageName + "/Image$Size";
        String sizeClassName = sizeType.replace('/', '.');
        byte[] sizeCode = genEnum(sizeType, "SMALL", "LARGE");
        classBytes.put(sizeClassName, sizeCode);

        String imageType = packageName + "/Image";
        String imageClassName = imageType.replace('/', '.');
        byte[] imageCode = genClass(imageType,
                new FieldInfo[]{
                        new FieldInfo("height", int.class),
                        new FieldInfo("size", "L" + sizeType + ";", null),
                        new FieldInfo("title", String.class),
                        new FieldInfo("uri", String.class),
                        new FieldInfo("width", int.class)
                }
        );
        classBytes.put(imageClassName, imageCode);

        String mediaContentType = packageName + "/MediaContent";
        String mediaContentClassName = mediaContentType.replace('/', '.');
        byte[] mediaContentCode = genClass(mediaContentType,
                new FieldInfo("media", "L" + mediaType + ";", null),
                new FieldInfo("images", "Ljava/util/List;", "Ljava/util/List<L" + imageType + ";>;")
        );
        classBytes.put(mediaContentClassName, mediaContentCode);
    }

    static class FieldInfo {
        final String name;
        final String desc;
        final String signature;

        FieldInfo(String name, java.lang.reflect.Type type) {
            this.name = name;
            Class fieldClass = TypeUtils.getClass(type);
            this.desc = Type.getDescriptor(fieldClass);
            this.signature = getSignature(type);
        }

        FieldInfo(String name, String desc, String signature) {
            this.name = name;
            this.desc = desc;
            this.signature = signature;
        }
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

    public byte[] genClass(String type, FieldInfo... fields) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(
                Opcodes.V1_8,
                ACC_PUBLIC + ACC_SUPER,
                type,
                null,
                "java/lang/Object",
                new String[] {
                        "java/io/Serializable"
                }
        );

        for (FieldInfo field : fields) {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC, field.name, field.desc, field.signature, null);
            fv.visitEnd();
        }

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

        return cw.toByteArray();
    }

    public byte[] genEnum(String type, String... values) {
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

        return cw.toByteArray();
    }

    public byte[][] genFastjsonJSONBBytes(int count, JSONWriter.Feature[] writerFeatures) throws Exception {
        try (InputStream is = EishayFuryWriteNoneCache.class.getClassLoader()
                .getResourceAsStream("data/eishay.json")
        ) {
            String str = IOUtils.toString(is, "UTF-8");

            DynamicClassLoader classLoader = new DynamicClassLoader();
            byte[][] bytes = new byte[count][];
            EishayClassGen gen = new EishayClassGen();
            for (int i = 0; i < count; i++) {
                String packageName = "com/alibaba/fastjson2/benchmark/eishay" + i;
                Class objectClass = gen.genMedia(classLoader, packageName);
                ObjectReaderProvider provider = new ObjectReaderProvider();
                Object object = JSONReader.of(str, JSONFactory.createReadContext(provider)).read(objectClass);

                ObjectWriterProvider writerProvider = new ObjectWriterProvider();
                bytes[i] = JSONB.toBytes(object, JSONFactory.createWriteContext(writerProvider, writerFeatures));
            }
            return bytes;
        }
    }

    public LinkedHashMap<String, byte[]> genCodes(int count) throws Exception {
        LinkedHashMap<String, byte[]> codeMap = new LinkedHashMap();
        EishayClassGen gen = new EishayClassGen();
        for (int i = 0; i < count; i++) {
            String packageName = "com/alibaba/fastjson2/benchmark/eishay" + i;
            gen.genCode(packageName, codeMap);
        }
        return codeMap;
    }

    public byte[][] genFuryBytes(int count) throws Exception {
//        io.fury.ThreadSafeFury fury = io.fury.Fury.builder()
//                .withLanguage(io.fury.Language.JAVA)
//                .withReferenceTracking(true)
//                .disableSecureMode()
//                .buildThreadSafeFury();
//
//        try (InputStream is = EishayFuryWriteNoneCache.class.getClassLoader()
//                .getResourceAsStream("data/eishay.json")
//        ) {
//            String str = IOUtils.toString(is, "UTF-8");
//
//            DynamicClassLoader classLoader = new DynamicClassLoader();
//            byte[][] bytes = new byte[count][];
//            EishayClassGen gen = new EishayClassGen();
//            for (int i = 0; i < count; i++) {
//                String packageName = "com/alibaba/fastjson2/benchmark/eishay" + i;
//                Class objectClass = gen.genMedia(classLoader, packageName);
//                ObjectReaderProvider provider = new ObjectReaderProvider();
//                Object object = JSONReader.of(str, JSONFactory.createReadContext(provider)).read(objectClass);
//                bytes[i] = fury.serialize(object);
//                System.out.println(java.time.LocalDateTime.now() + " write " + i + " done");
//            }
//            return bytes;
//        }
        throw new JSONException("TODO");
    }
}
