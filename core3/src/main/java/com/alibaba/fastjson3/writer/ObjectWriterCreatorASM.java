package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import com.alibaba.fastjson3.annotation.NamingStrategy;
import com.alibaba.fastjson3.internal.asm.ClassWriter;
import com.alibaba.fastjson3.internal.asm.Label;
import com.alibaba.fastjson3.internal.asm.MethodWriter;
import com.alibaba.fastjson3.internal.asm.Opcodes;
import com.alibaba.fastjson3.util.DynamicClassLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson3.internal.asm.ASMUtils.DESC_JSON_GENERATOR;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_JSON_GENERATOR;
import static com.alibaba.fastjson3.internal.asm.ASMUtils.TYPE_OBJECT_WRITER;

/**
 * Creates {@link ObjectWriter} instances via ASM bytecode generation.
 * Generated classes directly access bean fields (getfield) and call
 * JSONGenerator methods, avoiding FieldWriter switch dispatch and reflection.
 *
 * <p>Fallback: if ASM generation fails for a type, delegates to
 * {@link ObjectWriterCreator#createObjectWriter(Class)} (reflection).</p>
 */
@com.alibaba.fastjson3.annotation.JVMOnly
public final class ObjectWriterCreatorASM {
    private static final AtomicLong SEED = new AtomicLong();
    private static final DynamicClassLoader CLASS_LOADER = DynamicClassLoader.getInstance();

    // Generated class implements ObjectWriter interface
    private static final String[] INTERFACES = {TYPE_OBJECT_WRITER};

    // ObjectWriter.write method descriptor
    private static final String METHOD_DESC_WRITE =
            "(" + DESC_JSON_GENERATOR + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";

    private ObjectWriterCreatorASM() {
    }

    /**
     * Create an ObjectWriter for the given type via ASM bytecode generation.
     * Falls back to reflection if ASM generation fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type) {
        // native-image: runtime bytecode generation is not supported, fall back to reflection
        if (com.alibaba.fastjson3.util.JDKUtils.NATIVE_IMAGE) {
            return ObjectWriterCreator.createObjectWriter(type);
        }

        if (!canGenerate(type)) {
            return ObjectWriterCreator.createObjectWriter(type);
        }

        try {
            return (ObjectWriter<T>) generateWriter(type);
        } catch (Throwable e) {
            // Fallback to reflection
            return ObjectWriterCreator.createObjectWriter(type);
        }
    }

    private static boolean canGenerate(Class<?> type) {
        if (type.isInterface() || type.isArray() || type.isEnum()
                || type.isPrimitive() || Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        // Needs to be accessible for getfield
        return Modifier.isPublic(type.getModifiers());
    }

    private static ObjectWriter<?> generateWriter(Class<?> beanType) {
        // 1. Collect field info (same logic as ObjectWriterCreator)
        List<FieldWriterInfo> fields = collectFields(beanType);
        if (fields.isEmpty()) {
            return ObjectWriterCreator.createObjectWriter(beanType);
        }

        // 2. Generate bytecode
        String beanInternalName = beanType.getName().replace('.', '/');
        String className = "com.alibaba.fastjson3.writer.gen.OW_"
                + beanType.getSimpleName() + "_" + SEED.getAndIncrement();
        String classInternalName = className.replace('.', '/');

        ClassWriter cw = new ClassWriter(null);
        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
                classInternalName, "java/lang/Object", INTERFACES);

        // Generate static fields for pre-encoded name data
        for (int i = 0; i < fields.size(); i++) {
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nc" + i, "[C");  // nameChars
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nb" + i, "[B");  // nameBytes
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nl" + i, "[J");  // nameByteLongs
            cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "nn" + i, "I");   // nameBytesLen
        }

        // Generate <clinit> to initialize name data
        generateClinit(cw, classInternalName, fields);

        // Generate constructor
        generateInit(cw, classInternalName);

        // Generate write method
        generateWriteMethod(cw, classInternalName, beanType, beanInternalName, fields);

        // Load class
        byte[] bytecode = cw.toByteArray();
        Class<?> writerClass = CLASS_LOADER.loadClass(className, bytecode, 0, bytecode.length);

        try {
            return (ObjectWriter<?>) writerClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new JSONException("Failed to instantiate generated writer for " + beanType.getName(), e);
        }
    }

    // ==================== Bytecode generation methods ====================

    private static void generateClinit(ClassWriter cw, String classInternalName, List<FieldWriterInfo> fields) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", 32);

        for (int i = 0; i < fields.size(); i++) {
            FieldWriterInfo fi = fields.get(i);
            String encodedName = "\"" + fi.jsonName + "\":";

            // nameChars: char[]
            pushString(mw, encodedName);
            mw.invokevirtual("java/lang/String", "toCharArray", "()[C");
            mw.putstatic(classInternalName, "nc" + i, "[C");

            // nameBytes: byte[] (UTF-8)
            pushString(mw, encodedName);
            mw.getstatic("java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
            mw.invokevirtual("java/lang/String", "getBytes",
                    "(Ljava/nio/charset/Charset;)[B");
            mw.putstatic(classInternalName, "nb" + i, "[B");

            // nameByteLongs: call FieldWriter.encodeByteLongs(nameBytes)
            mw.getstatic(classInternalName, "nb" + i, "[B");
            mw.invokestatic("com/alibaba/fastjson3/writer/FieldWriter",
                    "encodeByteLongs", "([B)[J");
            mw.putstatic(classInternalName, "nl" + i, "[J");

            // nameBytesLen: nameBytes.length
            mw.getstatic(classInternalName, "nb" + i, "[B");
            mw.arraylength();
            mw.putstatic(classInternalName, "nn" + i, "I");
        }

        mw.return_();
        mw.visitMaxs(3, 0);
    }

    private static void generateInit(ClassWriter cw, String classInternalName) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", 16);
        mw.aload(0);
        mw.invokespecial("java/lang/Object", "<init>", "()V");
        mw.return_();
        mw.visitMaxs(1, 1);
    }

    private static void generateWriteMethod(
            ClassWriter cw,
            String classInternalName,
            Class<?> beanType,
            String beanInternalName,
            List<FieldWriterInfo> fields
    ) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "write", METHOD_DESC_WRITE, 64);
        // Locals: 0=this, 1=generator, 2=object, 3=fieldName, 4=fieldType, 5-6=features(long)
        // 7=bean (cast result)

        // Cast object to bean type
        mw.aload(2);
        mw.checkcast(beanInternalName);
        mw.astore(7);

        // generator.startObject()
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "startObject", "()V");

        // Write each field
        for (int i = 0; i < fields.size(); i++) {
            FieldWriterInfo fi = fields.get(i);
            generateWriteField(mw, classInternalName, beanInternalName, fi, i);
        }

        // generator.endObject()
        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "endObject", "()V");

        mw.return_();
        mw.visitMaxs(10, 8);
    }

    private static void generateWriteField(
            MethodWriter mw,
            String classInternalName,
            String beanInternalName,
            FieldWriterInfo fi,
            int fieldIndex
    ) {
        String namePrefix = "" + fieldIndex;

        switch (fi.typeTag) {
            case FieldWriter.TYPE_INT -> generateWriteInt(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_LONG -> generateWriteLong(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_DOUBLE -> generateWriteDouble(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_FLOAT -> generateWriteFloat(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_BOOL -> generateWriteBool(mw, classInternalName, beanInternalName, fi, namePrefix);
            case FieldWriter.TYPE_STRING -> generateWriteString(mw, classInternalName, beanInternalName, fi, namePrefix);
            default -> generateWriteGeneric(mw, classInternalName, beanInternalName, fi, namePrefix);
        }
    }

    private static void generateWriteInt(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        // generator.writeNameInt32(nl, nn, nb, nc, bean.field)
        mw.aload(1); // generator
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(7); // bean

        if (fi.field != null && fi.fieldClass == int.class) {
            mw.getfield(beanInternalName, fi.field.getName(), "I");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Integer.class) {
                mw.invokevirtual("java/lang/Integer", "intValue", "()I");
            }
        } else if (fi.field != null) {
            // Boxed type field (e.g., Integer): getfield returns object, need unbox
            mw.getfield(beanInternalName, fi.field.getName(), getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Integer.class) {
                mw.invokevirtual("java/lang/Integer", "intValue", "()I");
            }
        } else {
            throw new JSONException("no field or getter for int property: " + fi.jsonName);
        }

        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameInt32",
                "([JI[B[CI)V");
    }

    private static void generateWriteLong(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        // generator.writePreEncodedNameLongs(...) + generator.writeInt64(bean.field)
        mw.aload(1);
        loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                "([JI[C[B)V");

        mw.aload(1); // generator
        mw.aload(7); // bean
        if (fi.field != null && fi.fieldClass == long.class) {
            mw.getfield(beanInternalName, fi.field.getName(), "J");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Long.class) {
                mw.invokevirtual("java/lang/Long", "longValue", "()J");
            }
        } else if (fi.field != null) {
            mw.getfield(beanInternalName, fi.field.getName(), getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Long.class) {
                mw.invokevirtual("java/lang/Long", "longValue", "()J");
            }
        } else {
            throw new JSONException("no field or getter for long property: " + fi.jsonName);
        }
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeInt64", "(J)V");
    }

    private static void generateWriteDouble(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        mw.aload(1);
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(7);

        if (fi.field != null && fi.fieldClass == double.class) {
            mw.getfield(beanInternalName, fi.field.getName(), "D");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Double.class) {
                mw.invokevirtual("java/lang/Double", "doubleValue", "()D");
            }
        } else if (fi.field != null) {
            mw.getfield(beanInternalName, fi.field.getName(), getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Double.class) {
                mw.invokevirtual("java/lang/Double", "doubleValue", "()D");
            }
        } else {
            throw new JSONException("no field or getter for double property: " + fi.jsonName);
        }

        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameDouble",
                "([JI[B[CD)V");
    }

    private static void generateWriteFloat(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        mw.aload(1);
        loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                "([JI[C[B)V");

        mw.aload(1);
        mw.aload(7);
        if (fi.field != null && fi.fieldClass == float.class) {
            mw.getfield(beanInternalName, fi.field.getName(), "F");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Float.class) {
                mw.invokevirtual("java/lang/Float", "floatValue", "()F");
            }
        } else if (fi.field != null) {
            mw.getfield(beanInternalName, fi.field.getName(), getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Float.class) {
                mw.invokevirtual("java/lang/Float", "floatValue", "()F");
            }
        } else {
            throw new JSONException("no field or getter for float property: " + fi.jsonName);
        }
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeFloat", "(F)V");
    }

    private static void generateWriteBool(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        mw.aload(1);
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(7);

        if (fi.field != null && fi.fieldClass == boolean.class) {
            mw.getfield(beanInternalName, fi.field.getName(), "Z");
        } else if (fi.getter != null) {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Boolean.class) {
                mw.invokevirtual("java/lang/Boolean", "booleanValue", "()Z");
            }
        } else if (fi.field != null) {
            mw.getfield(beanInternalName, fi.field.getName(), getDescriptor(fi.fieldClass));
            if (fi.fieldClass == Boolean.class) {
                mw.invokevirtual("java/lang/Boolean", "booleanValue", "()Z");
            }
        } else {
            throw new JSONException("no field or getter for boolean property: " + fi.jsonName);
        }

        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameBool",
                "([JI[B[CZ)V");
    }

    private static void generateWriteString(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        // String value = bean.field;
        mw.aload(7);
        if (fi.field != null) {
            mw.getfield(beanInternalName, fi.field.getName(),
                    "Ljava/lang/String;");
        } else {
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()Ljava/lang/String;");
        }
        mw.astore(8); // local 8 = string value

        // if (value == null) { /* skip or writeNull */ } else { writeNameString }
        Label notNull = new Label();
        mw.aload(8);
        mw.ifnonnull(notNull);
        // null case: skip (default behavior: omit nulls)
        Label end = new Label();
        mw.goto_(end);

        mw.visitLabel(notNull);
        mw.aload(1); // generator
        loadNameFields(mw, classInternalName, namePrefix);
        mw.aload(8); // value
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeNameString",
                "([JI[B[CLjava/lang/String;)V");

        mw.visitLabel(end);
    }

    private static void generateWriteGeneric(
            MethodWriter mw, String classInternalName, String beanInternalName,
            FieldWriterInfo fi, String namePrefix
    ) {
        // Object value = bean.getField() or bean.field
        mw.aload(7);
        if (fi.getter != null) {
            String retDesc = getDescriptor(fi.fieldClass);
            mw.invokevirtual(beanInternalName, fi.getter.getName(),
                    "()" + retDesc);
            // Box if primitive
            if (fi.fieldClass.isPrimitive()) {
                boxPrimitive(mw, fi.fieldClass);
            }
        } else {
            mw.getfield(beanInternalName, fi.field.getName(),
                    getDescriptor(fi.fieldClass));
            if (fi.fieldClass.isPrimitive()) {
                boxPrimitive(mw, fi.fieldClass);
            }
        }
        mw.astore(8);

        // if (value == null) skip
        Label notNull = new Label();
        mw.aload(8);
        mw.ifnonnull(notNull);
        Label end = new Label();
        mw.goto_(end);

        mw.visitLabel(notNull);
        // generator.writePreEncodedNameLongs(...)
        mw.aload(1);
        loadNameFieldsForPreEncoded(mw, classInternalName, namePrefix);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writePreEncodedNameLongs",
                "([JI[C[B)V");

        // generator.writeAny(value)
        mw.aload(1);
        mw.aload(8);
        mw.invokevirtual(TYPE_JSON_GENERATOR, "writeAny",
                "(Ljava/lang/Object;)V");

        mw.visitLabel(end);
    }

    // ==================== Helper methods ====================

    /**
     * Load name fields in the order expected by writeNameXxx: nl, nn, nb, nc
     */
    private static void loadNameFields(MethodWriter mw, String classInternalName, String namePrefix) {
        mw.getstatic(classInternalName, "nl" + namePrefix, "[J");
        mw.getstatic(classInternalName, "nn" + namePrefix, "I");
        mw.getstatic(classInternalName, "nb" + namePrefix, "[B");
        mw.getstatic(classInternalName, "nc" + namePrefix, "[C");
    }

    /**
     * Load name fields in the order expected by writePreEncodedNameLongs: nl, nn, nc, nb
     */
    private static void loadNameFieldsForPreEncoded(MethodWriter mw, String classInternalName, String namePrefix) {
        mw.getstatic(classInternalName, "nl" + namePrefix, "[J");
        mw.getstatic(classInternalName, "nn" + namePrefix, "I");
        mw.getstatic(classInternalName, "nc" + namePrefix, "[C");
        mw.getstatic(classInternalName, "nb" + namePrefix, "[B");
    }

    private static void pushString(MethodWriter mw, String value) {
        mw.visitLdcInsn(value);
    }

    private static String getDescriptor(Class<?> type) {
        if (type == void.class) {
            return "V";
        }
        if (type == boolean.class) {
            return "Z";
        }
        if (type == byte.class) {
            return "B";
        }
        if (type == char.class) {
            return "C";
        }
        if (type == short.class) {
            return "S";
        }
        if (type == int.class) {
            return "I";
        }
        if (type == long.class) {
            return "J";
        }
        if (type == float.class) {
            return "F";
        }
        if (type == double.class) {
            return "D";
        }
        if (type.isArray()) {
            return "[" + getDescriptor(type.getComponentType());
        }
        return "L" + type.getName().replace('.', '/') + ";";
    }

    private static void boxPrimitive(MethodWriter mw, Class<?> type) {
        if (type == int.class) {
            mw.invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        } else if (type == long.class) {
            mw.invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        } else if (type == double.class) {
            mw.invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        } else if (type == float.class) {
            mw.invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        } else if (type == boolean.class) {
            mw.invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        } else if (type == byte.class) {
            mw.invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        } else if (type == short.class) {
            mw.invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        } else if (type == char.class) {
            mw.invokestatic("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        }
    }

    // ==================== Field collection (reuses ObjectWriterCreator logic) ====================

    private static List<FieldWriterInfo> collectFields(Class<?> beanType) {
        JSONType jsonType = beanType.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null && jsonType.includes().length > 0
                ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null && jsonType.ignores().length > 0
                ? Set.of(jsonType.ignores()) : Set.of();
        boolean alphabetic = jsonType == null || jsonType.alphabetic();

        Map<String, FieldWriterInfo> writerMap = new LinkedHashMap<>();

        // 1. Getter methods
        for (Method method : beanType.getMethods()) {
            if (method.getDeclaringClass() == Object.class
                    || method.getParameterCount() != 0
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            String propertyName = extractPropertyName(method.getName(), method.getReturnType());
            if (propertyName == null) {
                continue;
            }

            JSONField jsonField = method.getAnnotation(JSONField.class);
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            // Check backing field annotation
            if (jsonField == null) {
                Field f = findDeclaredField(beanType, propertyName);
                if (f != null) {
                    jsonField = f.getAnnotation(JSONField.class);
                    if (jsonField != null && !jsonField.serialize()) {
                        continue;
                    }
                }
            }

            String jsonName = resolveJsonName(propertyName, jsonField, naming);
            int ordinal = jsonField != null ? jsonField.ordinal() : 0;

            // Prefer backing field for direct getfield access
            Field backingField = findDeclaredField(beanType, propertyName);
            if (backingField != null && Modifier.isPublic(backingField.getModifiers())) {
                writerMap.put(propertyName, new FieldWriterInfo(
                        jsonName, ordinal, method.getReturnType(), backingField, null));
            } else {
                writerMap.put(propertyName, new FieldWriterInfo(
                        jsonName, ordinal, method.getReturnType(), null, method));
            }
        }

        // 2. Public fields (fallback)
        for (Field field : beanType.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            String propertyName = field.getName();
            if (writerMap.containsKey(propertyName)) {
                continue;
            }

            JSONField jsonField = field.getAnnotation(JSONField.class);
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            String jsonName = resolveJsonName(propertyName, jsonField, naming);
            int ordinal = jsonField != null ? jsonField.ordinal() : 0;

            writerMap.put(propertyName, new FieldWriterInfo(
                    jsonName, ordinal, field.getType(), field, null));
        }

        // 3. Filter
        if (!includes.isEmpty()) {
            writerMap.keySet().retainAll(includes);
        }
        if (!ignores.isEmpty()) {
            writerMap.keySet().removeAll(ignores);
        }

        // 4. Sort
        List<FieldWriterInfo> result = new ArrayList<>(writerMap.values());
        if (alphabetic) {
            Collections.sort(result);
        }

        return result;
    }

    private static String extractPropertyName(String methodName, Class<?> returnType) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2
                && (returnType == boolean.class || returnType == Boolean.class)) {
            return decapitalize(methodName.substring(2));
        }
        return null;
    }

    private static String decapitalize(String name) {
        if (name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static String resolveJsonName(String propertyName, JSONField jsonField, NamingStrategy naming) {
        if (jsonField != null && !jsonField.name().isEmpty()) {
            return jsonField.name();
        }
        return ObjectWriterCreator.applyNamingStrategy(propertyName, naming);
    }

    private static Field findDeclaredField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    // ==================== Field info holder ====================

    static final class FieldWriterInfo implements Comparable<FieldWriterInfo> {
        final String jsonName;
        final int ordinal;
        final Class<?> fieldClass;
        final Field field;
        final Method getter;
        final int typeTag;

        FieldWriterInfo(String jsonName, int ordinal, Class<?> fieldClass, Field field, Method getter) {
            this.jsonName = jsonName;
            this.ordinal = ordinal;
            this.fieldClass = fieldClass;
            this.field = field;
            this.getter = getter;
            this.typeTag = resolveTypeTag(fieldClass);
        }

        private static int resolveTypeTag(Class<?> type) {
            if (type == String.class) {
                return FieldWriter.TYPE_STRING;
            }
            if (type == int.class || type == Integer.class) {
                return FieldWriter.TYPE_INT;
            }
            if (type == long.class || type == Long.class) {
                return FieldWriter.TYPE_LONG;
            }
            if (type == double.class || type == Double.class) {
                return FieldWriter.TYPE_DOUBLE;
            }
            if (type == float.class || type == Float.class) {
                return FieldWriter.TYPE_FLOAT;
            }
            if (type == boolean.class || type == Boolean.class) {
                return FieldWriter.TYPE_BOOL;
            }
            return FieldWriter.TYPE_GENERIC;
        }

        @Override
        public int compareTo(FieldWriterInfo other) {
            int cmp = Integer.compare(this.ordinal, other.ordinal);
            if (cmp != 0) {
                return cmp;
            }
            return this.jsonName.compareTo(other.jsonName);
        }
    }
}
