package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.internal.asm.*;
import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.Serializable;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

import static com.alibaba.fastjson2.internal.asm.ASMUtils.*;
import static com.alibaba.fastjson2.internal.asm.Opcodes.ALOAD;
import static com.alibaba.fastjson2.internal.asm.Opcodes.PUTFIELD;
import static com.alibaba.fastjson2.reader.ObjectReader.HASH_TYPE;
import static com.alibaba.fastjson2.util.JDKUtils.*;

public class ObjectReaderCreatorASM
        extends ObjectReaderCreator {
    // GraalVM not support
    // Android not support
    public static final ObjectReaderCreatorASM INSTANCE = new ObjectReaderCreatorASM(DynamicClassLoader.getInstance());

    protected static final AtomicLong seed = new AtomicLong();

    protected final DynamicClassLoader classLoader;

    static final String METHOD_DESC_GET_ITEM_OBJECT_READER = "(" + DESC_JSON_READER + ")" + DESC_OBJECT_READER;
    static final String METHOD_DESC_GET_OBJECT_READER_1 = "(" + DESC_JSON_READER + ")" + DESC_OBJECT_READER;
    static final String METHOD_DESC_INIT = "(Ljava/lang/Class;" + DESC_SUPPLIER + DESC_FIELD_READER_ARRAY + ")V";
    static final String METHOD_DESC_ADAPTER_INIT = "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;J" + DESC_JSONSCHEMA + DESC_SUPPLIER + "Ljava/util/function/Function;" + DESC_FIELD_READER_ARRAY + ")V";
    static final String METHOD_DESC_READ_OBJECT = "(" + DESC_JSON_READER + "Ljava/lang/reflect/Type;Ljava/lang/Object;J)Ljava/lang/Object;";
    static final String METHOD_DESC_GET_FIELD_READER = "(J)" + DESC_FIELD_READER;
    static final String METHOD_DESC_READ_FIELD_VALUE = "(" + DESC_JSON_READER + "Ljava/lang/Object;)V";
    static final String METHOD_DESC_ADD_RESOLVE_TASK = "(" + DESC_JSON_READER + "Ljava/lang/Object;Ljava/lang/String;)V";
    static final String METHOD_DESC_ADD_RESOLVE_TASK_2 = "(" + DESC_JSON_READER + "Ljava/util/List;ILjava/lang/String;)V";
    static final String METHOD_DESC_CHECK_ARRAY_AUTO_TYPE = "(" + DESC_JSON_READER + ")" + DESC_OBJECT_READER;
    static final String METHOD_DESC_PROCESS_EXTRA = "(" + DESC_JSON_READER + "Ljava/lang/Object;)V";

    static final String METHOD_DESC_JSON_READER_CHECK_ARRAY_AUTO_TYPE = "(" + DESC_JSON_READER + "Ljava/lang/Class;J)" + DESC_OBJECT_READER;

    static final int THIS = 0;

    static final String packageName;
    static final Map<Class, FieldReaderInfo> infos = new HashMap<>();

    static {
        Package pkg = ObjectReaderCreatorASM.class.getPackage();
        packageName = pkg != null ? pkg.getName() : "";

        infos.put(boolean.class, new FieldReaderInfo(ASMUtils.type(ObjBoolConsumer.class), "(Ljava/lang/Object;Z)V", "(Z)V", Opcodes.ILOAD, "readFieldBoolValue", "()Z", Opcodes.ISTORE));
        infos.put(char.class, new FieldReaderInfo(ASMUtils.type(ObjCharConsumer.class), "(Ljava/lang/Object;C)V", "(C)V", Opcodes.ILOAD, "readInt32Value", "()C", Opcodes.ISTORE));
        infos.put(byte.class, new FieldReaderInfo(ASMUtils.type(ObjByteConsumer.class), "(Ljava/lang/Object;B)V", "(B)V", Opcodes.ILOAD, "readInt32Value", "()B", Opcodes.ISTORE));
        infos.put(short.class, new FieldReaderInfo(ASMUtils.type(ObjShortConsumer.class), "(Ljava/lang/Object;S)V", "(S)V", Opcodes.ILOAD, "readInt32Value", "()S", Opcodes.ISTORE));
        infos.put(int.class, new FieldReaderInfo(ASMUtils.type(ObjIntConsumer.class), "(Ljava/lang/Object;I)V", "(I)V", Opcodes.ILOAD, "readInt32Value", "()I", Opcodes.ISTORE));
        infos.put(long.class, new FieldReaderInfo(ASMUtils.type(ObjLongConsumer.class), "(Ljava/lang/Object;J)V", "(J)V", Opcodes.LLOAD, "readInt64Value", "()V", Opcodes.LSTORE));
        infos.put(float.class, new FieldReaderInfo(ASMUtils.type(ObjFloatConsumer.class), "(Ljava/lang/Object;F)V", "(F)V", Opcodes.FLOAD, "readFieldFloatValue", "()F", Opcodes.FSTORE));
        infos.put(double.class, new FieldReaderInfo(ASMUtils.type(ObjDoubleConsumer.class), "(Ljava/lang/Object;D)V", "(D)V", Opcodes.DLOAD, "readFloatDoubleValue", "()D", Opcodes.DSTORE));

        infos.put(String.class, new FieldReaderInfo(ASMUtils.type(BiConsumer.class), "(Ljava/lang/Object;Ljava/lang/Object;)V", "(Ljava/lang/String;)V", Opcodes.ALOAD, "readString", "()Ljava/lang/String;", Opcodes.ASTORE));
        infos.put(Integer.class, new FieldReaderInfo(ASMUtils.type(BiConsumer.class), "(Ljava/lang/Object;Ljava/lang/Integer;)V", "(Ljava/lang/Integer;)V", Opcodes.ALOAD, "readInt32", "()Ljava/lang/Integer;", Opcodes.ASTORE));
    }

    static final String[] fieldItemObjectReader = new String[1024];

    static String fieldReader(int i) {
        switch (i) {
            case 0:
                return "fieldReader0";
            case 1:
                return "fieldReader1";
            case 2:
                return "fieldReader2";
            case 3:
                return "fieldReader3";
            case 4:
                return "fieldReader4";
            case 5:
                return "fieldReader5";
            case 6:
                return "fieldReader6";
            case 7:
                return "fieldReader7";
            case 8:
                return "fieldReader8";
            case 9:
                return "fieldReader9";
            case 10:
                return "fieldReader10";
            case 11:
                return "fieldReader11";
            case 12:
                return "fieldReader12";
            case 13:
                return "fieldReader13";
            case 14:
                return "fieldReader14";
            case 15:
                return "fieldReader15";
            default:
                String base = "fieldReader";
                int size = IOUtils.stringSize(i);
                char[] chars = new char[base.length() + size];
                base.getChars(0, base.length(), chars, 0);
                IOUtils.getChars(i, chars.length, chars);
                return new String(chars);
        }
    }

    static String fieldObjectReader(int i) {
        switch (i) {
            case 0:
                return "objectReader0";
            case 1:
                return "objectReader1";
            case 2:
                return "objectReader2";
            case 3:
                return "objectReader3";
            case 4:
                return "objectReader4";
            case 5:
                return "objectReader5";
            case 6:
                return "objectReader6";
            case 7:
                return "objectReader7";
            case 8:
                return "objectReader8";
            case 9:
                return "objectReader9";
            case 10:
                return "objectReader10";
            case 11:
                return "objectReader11";
            case 12:
                return "objectReader12";
            case 13:
                return "objectReader13";
            case 14:
                return "objectReader14";
            case 15:
                return "objectReader15";
            default:
                String base = "objectReader";
                int size = IOUtils.stringSize(i);
                char[] chars = new char[base.length() + size];
                base.getChars(0, base.length(), chars, 0);
                IOUtils.getChars(i, chars.length, chars);
                return new String(chars);
        }
    }

    static String fieldItemObjectReader(int i) {
        String fieldName = fieldItemObjectReader[i];

        if (fieldName != null) {
            return fieldName;
        }

        String base = "itemReader";
        int size = IOUtils.stringSize(i);
        char[] chars = new char[base.length() + size];
        base.getChars(0, base.length(), chars, 0);
        IOUtils.getChars(i, chars.length, chars);
        fieldItemObjectReader[i] = fieldName = new String(chars);
        return fieldName;
    }

    @Override
    public <T> FieldReader<T> createFieldReader(
            Class objectClass,
            Type objectType,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            String schema,
            Type fieldType,
            Class fieldClass,
            Field field,
            ObjectReader initReader
    ) {
        return super.createFieldReader(
                objectClass,
                objectType,
                fieldName,
                ordinal,
                features,
                format,
                locale,
                defaultValue,
                schema,
                fieldType,
                fieldClass,
                field,
                initReader
        );
    }

    private static class FieldReaderInfo {
        final String interfaceDesc;
        final String acceptDesc;
        final String setterDesc;
        final int loadCode;
        final String readMethodName;
        final String readMethodDesc;
        final int storeCode;

        FieldReaderInfo(
                String interfaceDesc,
                String acceptDesc,
                String setterDesc,
                int loadCode,
                String readMethodName,
                String readMethodDesc,
                int storeCode) {
            this.interfaceDesc = interfaceDesc;
            this.acceptDesc = acceptDesc;
            this.setterDesc = setterDesc;
            this.loadCode = loadCode;
            this.readMethodName = readMethodName;
            this.readMethodDesc = readMethodDesc;
            this.storeCode = storeCode;
        }
    }

    public ObjectReaderCreatorASM(ClassLoader classLoader) {
        this.classLoader = classLoader instanceof DynamicClassLoader
                ? (DynamicClassLoader) classLoader
                : new DynamicClassLoader(classLoader);
    }

    @Override
    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            Type objectType,
            boolean fieldBased,
            ObjectReaderProvider provider
    ) {
        boolean externalClass = classLoader.isExternalClass(objectClass);
        int objectClassModifiers = objectClass.getModifiers();

        if (Modifier.isAbstract(objectClassModifiers) || Modifier.isInterface(objectClassModifiers)) {
            return super.createObjectReader(objectClass, objectType, fieldBased, provider);
        }

        if (externalClass) {
            if (!UNSAFE_SUPPORT) {
                return super.createObjectReader(objectClass, objectType, fieldBased, provider);
            }
        }

        BeanInfo beanInfo = new BeanInfo();
        provider.getBeanInfo(beanInfo, objectClass);
        if (externalClass || !Modifier.isPublic(objectClassModifiers)) {
            beanInfo.readerFeatures |= FieldInfo.JIT;
        }

        if (beanInfo.deserializer != null && ObjectReader.class.isAssignableFrom(beanInfo.deserializer)) {
            try {
                Constructor constructor = beanInfo.deserializer.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ObjectReader<T>) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new JSONException("create deserializer error", e);
            }
        }

        ObjectReader annotatedObjectReader = getAnnotatedObjectReader(provider, objectClass, beanInfo);
        if (annotatedObjectReader != null) {
            return annotatedObjectReader;
        }

        if (fieldBased && objectClass.isInterface()) {
            fieldBased = false;
        }

        if (Enum.class.isAssignableFrom(objectClass) && (beanInfo.createMethod == null || beanInfo.createMethod.getParameterCount() == 1)) {
            return createEnumReader(objectClass, beanInfo.createMethod, provider);
        }

        if (beanInfo.creatorConstructor != null || beanInfo.createMethod != null) {
            return createObjectReaderWithCreator(objectClass, objectType, provider, beanInfo);
        }

        if (beanInfo.builder != null) {
            return createObjectReaderWithBuilder(objectClass, objectType, provider, beanInfo);
        }

        if (Throwable.class.isAssignableFrom(objectClass) || BeanUtils.isExtendedMap(objectClass)) {
            return super.createObjectReader(objectClass, objectType, fieldBased, provider);
        }

        FieldReader[] fieldReaderArray = createFieldReaders(objectClass, objectType, beanInfo, fieldBased, provider);
        boolean match = true;

        if (!fieldBased) {
            if (JVM_VERSION >= 9 && objectClass == StackTraceElement.class) {
                try {
                    Constructor<StackTraceElement> constructor = StackTraceElement.class.getConstructor(String.class, String.class, String.class, String.class, String.class, String.class, int.class);
                    return createObjectReaderNoneDefaultConstructor(constructor, "", "classLoaderName", "moduleName", "moduleVersion", "declaringClass", "methodName", "fileName", "lineNumber");
                } catch (NoSuchMethodException | SecurityException ignored) {
                }
            }

            for (FieldReader fieldReader : fieldReaderArray) {
                Method method = fieldReader.method;
                if (fieldReader.isReadOnly()
                        || fieldReader.isUnwrapped()
                ) {
                    match = false;
                    break;
                }

                if ((fieldReader.features & FieldInfo.READ_USING_MASK) != 0) {
                    match = false;
                    break;
                }
            }
        }

        if (beanInfo.autoTypeBeforeHandler != null) {
            match = false;
        }

        if (match) {
            for (FieldReader fieldReader : fieldReaderArray) {
                if (fieldReader.defaultValue != null || fieldReader.schema != null) {
                    match = false;
                    break;
                }

                Class fieldClass = fieldReader.fieldClass;
                if (!Modifier.isPublic(fieldClass.getModifiers())) {
                    match = false;
                    break;
                }
            }
        }

        if (match && beanInfo.schema != null && !beanInfo.schema.isEmpty()) {
            match = false;
        }

        if (!match) {
            return super.createObjectReader(objectClass, objectType, fieldBased, provider);
        }

        Constructor defaultConstructor = null;
        if (!Modifier.isInterface(objectClassModifiers) && !Modifier.isAbstract(objectClassModifiers)) {
            Constructor constructor = BeanUtils.getDefaultConstructor(objectClass, true);
            if (constructor != null) {
                defaultConstructor = constructor;
                try {
                    constructor.setAccessible(true);
                } catch (SecurityException ignored) {
                    // ignored
                }
            }
        }

        if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
            return createObjectReaderSeeAlso(
                    objectClass,
                    beanInfo.typeKey,
                    beanInfo.seeAlso,
                    beanInfo.seeAlsoNames,
                    beanInfo.seeAlsoDefault,
                    fieldReaderArray
            );
        }

        if (!fieldBased) {
            if (defaultConstructor == null) {
                return super.createObjectReader(objectClass, objectType, false, provider);
            }
        }

        return jitObjectReader(
                objectClass,
                objectType,
                fieldBased,
                externalClass,
                objectClassModifiers,
                beanInfo,
                fieldReaderArray,
                defaultConstructor
        );
    }

    private <T> ObjectReaderBean jitObjectReader(
            Class<T> objectClass,
            Type objectType,
            boolean fieldBased,
            boolean externalClass,
            int objectClassModifiers,
            BeanInfo beanInfo,
            FieldReader[] fieldReaderArray,
            Constructor defaultConstructor
    ) {
        ClassWriter cw = new ClassWriter(
                (e) -> objectClass.getName().equals(e) ? objectClass : null
        );

        ObjectWriteContext context = new ObjectWriteContext(objectClass, cw, externalClass, fieldReaderArray);

        String className = "ORG_" + seed.incrementAndGet() + "_" + fieldReaderArray.length + "_" + objectClass.getSimpleName();
        String classNameType;
        String classNameFull;

        Package pkg = ObjectReaderCreatorASM.class.getPackage();
        if (pkg != null) {
            String packageName = pkg.getName();
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
        } else {
            classNameType = className;
            classNameFull = className;
        }

        final boolean generatedFields = fieldReaderArray.length < 128;

        String objectReaderSuper;
        switch (fieldReaderArray.length) {
            case 1:
                objectReaderSuper = TYPE_OBJECT_READER_1;
                break;
            case 2:
                objectReaderSuper = TYPE_OBJECT_READER_2;
                break;
            case 3:
                objectReaderSuper = TYPE_OBJECT_READER_3;
                break;
            case 4:
                objectReaderSuper = TYPE_OBJECT_READER_4;
                break;
            case 5:
                objectReaderSuper = TYPE_OBJECT_READER_5;
                break;
            case 6:
                objectReaderSuper = TYPE_OBJECT_READER_6;
                break;
            case 7:
                objectReaderSuper = TYPE_OBJECT_READER_7;
                break;
            case 8:
                objectReaderSuper = TYPE_OBJECT_READER_8;
                break;
            case 9:
                objectReaderSuper = TYPE_OBJECT_READER_9;
                break;
            case 10:
                objectReaderSuper = TYPE_OBJECT_READER_10;
                break;
            case 11:
                objectReaderSuper = TYPE_OBJECT_READER_11;
                break;
            case 12:
                objectReaderSuper = TYPE_OBJECT_READER_12;
                break;
            default:
                objectReaderSuper = TYPE_OBJECT_READER_ADAPTER;
                break;
        }

        if (generatedFields) {
            genFields(fieldReaderArray, cw, objectReaderSuper);
        }

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, classNameType, objectReaderSuper, new String[]{});

        {
            final int CLASS = 1, SUPPLIER = 2, FIELD_READER_ARRAY = 3;

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    METHOD_DESC_INIT,
                    fieldReaderArray.length <= 12 ? 32 : 128);
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, CLASS);
            if (beanInfo.typeKey != null) {
                mw.visitLdcInsn(beanInfo.typeKey);
            } else {
                mw.visitInsn(Opcodes.ACONST_NULL);
            }
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitLdcInsn(beanInfo.readerFeatures);
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitVarInsn(Opcodes.ALOAD, SUPPLIER);
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_READER_ARRAY);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, objectReaderSuper, "<init>", METHOD_DESC_ADAPTER_INIT, false);

            genInitFields(fieldReaderArray, classNameType, generatedFields, THIS, FIELD_READER_ARRAY, mw, objectReaderSuper);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        String TYPE_OBJECT = ASMUtils.type(objectClass);

        {
            String methodName = fieldBased && defaultConstructor == null ? "createInstance0" : "createInstance";

            if (fieldBased && (defaultConstructor == null || !Modifier.isPublic(defaultConstructor.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))) {
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(J)Ljava/lang/Object;",
                        32
                );
                mw.visitFieldInsn(Opcodes.GETSTATIC, TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                mw.visitVarInsn(Opcodes.ALOAD, 0);
                mw.visitFieldInsn(Opcodes.GETFIELD, TYPE_OBJECT_READER_ADAPTER, "objectClass", "Ljava/lang/Class;");
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/misc/Unsafe", "allocateInstance", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
                mw.visitInsn(Opcodes.ARETURN);
                mw.visitMaxs(3, 3);
            } else if (defaultConstructor != null && Modifier.isPublic(defaultConstructor.getModifiers()) && Modifier.isPublic(objectClass.getModifiers())) {
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(J)Ljava/lang/Object;",
                        32
                );
                newObject(mw, TYPE_OBJECT, defaultConstructor);
                mw.visitInsn(Opcodes.ARETURN);
                mw.visitMaxs(3, 3);
            }
        }

        Supplier<T> supplier = null;
        if (defaultConstructor != null) {
            boolean publicObject = Modifier.isPublic(objectClassModifiers) && !classLoader.isExternalClass(objectClass);
            boolean jit = !publicObject || !Modifier.isPublic(defaultConstructor.getModifiers());
            supplier = createSupplier(defaultConstructor, jit);
        }

        if (generatedFields) {
            long readerFeatures = beanInfo.readerFeatures;
            if (fieldBased) {
                readerFeatures |= JSONReader.Feature.FieldBased.mask;
            }

            ObjectReaderAdapter objectReaderAdapter = new ObjectReaderAdapter(objectClass, beanInfo.typeKey, beanInfo.typeName, readerFeatures, null, supplier, null, fieldReaderArray);

            genMethodReadJSONBObject(context, defaultConstructor, readerFeatures, TYPE_OBJECT, fieldReaderArray, cw, classNameType, objectReaderAdapter);
            genMethodReadJSONBObjectArrayMapping(context, defaultConstructor, readerFeatures, TYPE_OBJECT, fieldReaderArray, cw, classNameType, objectReaderAdapter);

            genMethodReadObject(context, defaultConstructor, readerFeatures, TYPE_OBJECT, fieldReaderArray, cw, classNameType, objectReaderAdapter);

            if (objectReaderSuper == TYPE_OBJECT_READER_ADAPTER
                    || objectReaderSuper == TYPE_OBJECT_READER_1
                    || objectReaderSuper == TYPE_OBJECT_READER_2
                    || objectReaderSuper == TYPE_OBJECT_READER_3
                    || objectReaderSuper == TYPE_OBJECT_READER_4
                    || objectReaderSuper == TYPE_OBJECT_READER_5
                    || objectReaderSuper == TYPE_OBJECT_READER_6
                    || objectReaderSuper == TYPE_OBJECT_READER_7
                    || objectReaderSuper == TYPE_OBJECT_READER_8
                    || objectReaderSuper == TYPE_OBJECT_READER_9
                    || objectReaderSuper == TYPE_OBJECT_READER_10
                    || objectReaderSuper == TYPE_OBJECT_READER_11
                    || objectReaderSuper == TYPE_OBJECT_READER_12
            ) {
                genMethodGetFieldReader(fieldReaderArray, cw, classNameType, objectReaderAdapter);
                genMethodGetFieldReaderLCase(fieldReaderArray, cw, classNameType, objectReaderAdapter);
            }
        }

        byte[] code = cw.toByteArray();
        try {
            Class<?> readerClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);

            Constructor<?> constructor = readerClass.getConstructors()[0];
            return (ObjectReaderBean) constructor
                    .newInstance(objectClass, supplier, fieldReaderArray);
        } catch (Throwable e) {
            throw new JSONException("create objectReader error, objectType " + objectType.getTypeName(), e);
        }
    }

    private static void newObject(MethodWriter mw, String TYPE_OBJECT, Constructor defaultConstructor) {
        mw.visitTypeInsn(Opcodes.NEW, TYPE_OBJECT);
        mw.visitInsn(Opcodes.DUP);
        if (defaultConstructor.getParameterCount() == 0) {
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT, "<init>", "()V", false);
        } else {
            Class paramType = defaultConstructor.getParameterTypes()[0];
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT, "<init>", "(" + ASMUtils.desc(paramType) + ")V", false);
        }
    }

    private void genMethodGetFieldReader(
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter
    ) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "getFieldReader",
                "(J)" + DESC_FIELD_READER,
                512
        );

        final int HASH_CODE_64 = 1;
        final int HASH_CODE_32 = 3;

        Label rtnlt = new Label();
        if (fieldReaderArray.length > 6) {
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodes.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodes[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.computeIfAbsent(hashCode32, k -> new ArrayList<>());
                hashCode64List.add(hashCode64);
            }
            int[] hashCode32Keys = new int[map.size()];
            {
                int off = 0;
                for (Integer key : map.keySet()) {
                    hashCode32Keys[off++] = key;
                }
            }
            Arrays.sort(hashCode32Keys);

            // // int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
            mw.visitVarInsn(Opcodes.BIPUSH, 32);
            mw.visitInsn(Opcodes.LUSHR);
            mw.visitInsn(Opcodes.LXOR);
            mw.visitInsn(Opcodes.L2I);
            mw.visitVarInsn(Opcodes.ISTORE, HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.visitVarInsn(Opcodes.ILOAD, HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (long hashCode64 : hashCode64Array) {
                    mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
                    mw.visitLdcInsn(hashCode64);
                    mw.visitInsn(Opcodes.LCMP);
                    mw.visitJumpInsn(Opcodes.IFNE, dflt);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodes, hashCode64);
                    int index = objectReaderAdapter.mapping[m];
                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(index), DESC_FIELD_READER);
                    mw.visitJumpInsn(Opcodes.GOTO, rtnlt);
                }

                mw.visitJumpInsn(Opcodes.GOTO, dflt);
            }

            mw.visitLabel(dflt);
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();
                String fieldName = fieldReaderArray[i].fieldName;
                long hashCode64 = fieldReaderArray[i].fieldNameHash;

                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                mw.visitLabel(get_);
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.visitJumpInsn(Opcodes.GOTO, rtnlt);

                mw.visitLabel(next_);
            }
        }
        mw.visitInsn(Opcodes.ACONST_NULL);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitLabel(rtnlt);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 5);
    }

    private void genMethodGetFieldReaderLCase(
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter
    ) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "getFieldReaderLCase",
                "(J)" + DESC_FIELD_READER,
                512
        );

        final int HASH_CODE_64 = 1;
        final int HASH_CODE_32 = 3;

        Label rtnlt = new Label();
        if (fieldReaderArray.length > 6) {
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodesLCase.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodesLCase[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.computeIfAbsent(hashCode32, k -> new ArrayList<>());
                hashCode64List.add(hashCode64);
            }
            int[] hashCode32Keys = new int[map.size()];
            {
                int off = 0;
                for (Integer key : map.keySet()) {
                    hashCode32Keys[off++] = key;
                }
            }
            Arrays.sort(hashCode32Keys);

            // // int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
            mw.visitVarInsn(Opcodes.BIPUSH, 32);
            mw.visitInsn(Opcodes.LUSHR);
            mw.visitInsn(Opcodes.LXOR);
            mw.visitInsn(Opcodes.L2I);
            mw.visitVarInsn(Opcodes.ISTORE, HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.visitVarInsn(Opcodes.ILOAD, HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (long hashCode64 : hashCode64Array) {
                    mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
                    mw.visitLdcInsn(hashCode64);
                    mw.visitInsn(Opcodes.LCMP);
                    mw.visitJumpInsn(Opcodes.IFNE, dflt);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodesLCase, hashCode64);
                    int index = objectReaderAdapter.mappingLCase[m];
                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(index), DESC_FIELD_READER);
                    mw.visitJumpInsn(Opcodes.GOTO, rtnlt);
                }

                mw.visitJumpInsn(Opcodes.GOTO, dflt);
            }

            mw.visitLabel(dflt);
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();
                String fieldName = fieldReaderArray[i].fieldName;
                long hashCode64 = fieldReaderArray[i].fieldNameHashLCase;

                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                mw.visitLabel(get_);
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.visitJumpInsn(Opcodes.GOTO, rtnlt);

                mw.visitLabel(next_);
            }
        }
        mw.visitInsn(Opcodes.ACONST_NULL);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitLabel(rtnlt);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 5);
    }

    private void genInitFields(
            FieldReader[] fieldReaderArray,
            String classNameType,
            boolean generatedFields,
            int THIS,
            int FIELD_READER_ARRAY,
            MethodWriter mw,
            String objectReaderSuper
    ) {
        if (objectReaderSuper != TYPE_OBJECT_READER_ADAPTER || !generatedFields) {
            return;
        }

        for (int i = 0; i < fieldReaderArray.length; i++) {
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_READER_ARRAY);
            switch (i) {
                case 0:
                    mw.visitInsn(Opcodes.ICONST_0);
                    break;
                case 1:
                    mw.visitInsn(Opcodes.ICONST_1);
                    break;
                case 2:
                    mw.visitInsn(Opcodes.ICONST_2);
                    break;
                case 3:
                    mw.visitInsn(Opcodes.ICONST_3);
                    break;
                case 4:
                    mw.visitInsn(Opcodes.ICONST_4);
                    break;
                case 5:
                    mw.visitInsn(Opcodes.ICONST_5);
                    break;
                default:
                    if (i >= 128) {
                        mw.visitIntInsn(Opcodes.SIPUSH, i);
                    } else {
                        mw.visitIntInsn(Opcodes.BIPUSH, i);
                    }
                    break;
            }
            mw.visitInsn(Opcodes.AALOAD);
            mw.visitFieldInsn(PUTFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
        }
    }

    private void genFields(FieldReader[] fieldReaderArray, ClassWriter cw, String objectReaderSuper) {
        if (objectReaderSuper == TYPE_OBJECT_READER_ADAPTER) {
            for (int i = 0; i < fieldReaderArray.length; i++) {
                FieldWriter fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldReader(i), DESC_FIELD_READER);
            }

            for (int i = 0; i < fieldReaderArray.length; i++) {
                FieldWriter fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldObjectReader(i), DESC_OBJECT_READER);
            }
        }

        for (int i = 0; i < fieldReaderArray.length; i++) {
            Class fieldClass = fieldReaderArray[i].fieldClass;
            if (List.class.isAssignableFrom(fieldClass)) {
                FieldWriter fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldItemObjectReader(i), DESC_OBJECT_READER);
            }
        }
    }

    private <T> void genMethodReadJSONBObject(
            ObjectWriteContext context,
            Constructor defaultConstructor,
            long readerFeatures,
            String TYPE_OBJECT,
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter
    ) {
        Class objectClass = context.objectClass;
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "readJSONBObject",
                METHOD_DESC_READ_OBJECT,
                2048
        );

        final int JSON_READER = 1;
        final int FIELD_TYPE = 2;
        final int FIELD_NAME = 3;
        final int FEATURES = 4;
        final int OBJECT = 6;
        final int ENTRY_CNT = 7;
        final int I = 8;
        final int HASH_CODE64 = 9;
        final int HASH_CODE_32 = 11;
        final int ITEM_CNT = 12;
        final int J = 13;
        final int FIELD_READER = 14;
        final int AUTO_TYPE_OBJECT_READER = 15;

        genCheckAutoType(classNameType, mw, JSON_READER, FIELD_TYPE, FIELD_NAME, FEATURES, AUTO_TYPE_OBJECT_READER);

        int varIndex = 16;
        Map<Object, Integer> variants = new HashMap<>();

        {
            Label notNull_ = new Label();
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNull", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, notNull_);
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.ARETURN);
            mw.visitLabel(notNull_);
        }

        if (!Serializable.class.isAssignableFrom(objectClass)) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "objectClass", "Ljava/lang/Class;");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "errorOnNoneSerializable", "(Ljava/lang/Class;)V", false);
        }

        Label object_ = new Label();

        // if (jsonReader.isArray() && jsonReader.isSupportBeanArray()) {
        {
            Label startArray_ = new Label(), endArray_ = new Label();
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isArray", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, object_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportBeanArray", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, endArray_);

            mw.visitLabel(startArray_);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "startArray", "()I", false);
            mw.visitVarInsn(Opcodes.ISTORE, ENTRY_CNT);

            genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
            mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

            Label fieldEnd_ = new Label();
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                mw.visitVarInsn(Opcodes.ILOAD, ENTRY_CNT);
                mw.visitLdcInsn(i + 1);
                mw.visitJumpInsn(Opcodes.IF_ICMPLT, fieldEnd_);

                FieldReader fieldReader = fieldReaderArray[i];
                varIndex = genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        classNameType,
                        mw,
                        THIS,
                        JSON_READER,
                        OBJECT,
                        FEATURES,
                        varIndex,
                        variants,
                        ITEM_CNT,
                        J,
                        i,
                        true,   // JSONB
                        TYPE_OBJECT
                );
            }

            skipRest(mw, JSON_READER, fieldReaderArray.length, ENTRY_CNT, J, fieldEnd_);
            mw.visitLabel(fieldEnd_);

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitInsn(Opcodes.ARETURN);

            mw.visitLabel(endArray_);
        }

        mw.visitLabel(object_);

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfObjectStart", "()Z", false);
        mw.visitInsn(Opcodes.POP);

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        // for (int i = 0; i < entry_cnt; ++i) {
        Label for_start_i_ = new Label(), for_end_i_ = new Label(), for_inc_i_ = new Label();
        mw.visitInsn(Opcodes.ICONST_0);
        mw.visitVarInsn(Opcodes.ISTORE, I);

        mw.visitLabel(for_start_i_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfObjectEnd", "()Z", false);
        mw.visitJumpInsn(Opcodes.IFNE, for_end_i_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readFieldNameHashCode", "()J", false);
        mw.visitInsn(Opcodes.DUP2);
        mw.visitVarInsn(Opcodes.LSTORE, HASH_CODE64);
        mw.visitInsn(Opcodes.LCONST_0);
        mw.visitInsn(Opcodes.LCMP);
        mw.visitJumpInsn(Opcodes.IFEQ, for_inc_i_);

        Label endAutoType_ = new Label();
        mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "typeKeyHashCode", "J");
        mw.visitInsn(Opcodes.LCMP);
        mw.visitJumpInsn(Opcodes.IFNE, endAutoType_);

        mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
        mw.visitInsn(Opcodes.LCONST_0);
        mw.visitInsn(Opcodes.LCMP);
        mw.visitJumpInsn(Opcodes.IFEQ, endAutoType_);

        // protected T autoType(JSONReader jsonReader, int entryCnt) {
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "autoType", "(" + DESC_JSON_READER + ")Ljava/lang/Object;", false);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);
        mw.visitJumpInsn(Opcodes.GOTO, for_end_i_);

        mw.visitLabel(endAutoType_);

        // continue
        if (fieldReaderArray.length > 6) {
            // use switch
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodes.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodes[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.computeIfAbsent(hashCode32, k -> new ArrayList<>());
                hashCode64List.add(hashCode64);
            }
            int[] hashCode32Keys = new int[map.size()];
            {
                int off = 0;
                for (Integer key : map.keySet()) {
                    hashCode32Keys[off++] = key;
                }
            }
            Arrays.sort(hashCode32Keys);

            // int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
            mw.visitVarInsn(Opcodes.BIPUSH, 32);
            mw.visitInsn(Opcodes.LUSHR);
            mw.visitInsn(Opcodes.LXOR);
            mw.visitInsn(Opcodes.L2I);
            mw.visitVarInsn(Opcodes.ISTORE, HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.visitVarInsn(Opcodes.ILOAD, HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (long hashCode64 : hashCode64Array) {
                    mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                    mw.visitLdcInsn(hashCode64);
                    mw.visitInsn(Opcodes.LCMP);
                    mw.visitJumpInsn(Opcodes.IFNE, dflt);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodes, hashCode64);
                    int index = objectReaderAdapter.mapping[m];

                    FieldReader fieldReader = fieldReaderArray[index];

                    varIndex = genReadFieldValue(
                            context,
                            fieldReader,
                            fieldBased,
                            classNameType,
                            mw,
                            THIS,
                            JSON_READER,
                            OBJECT,
                            FEATURES,
                            varIndex,
                            variants,
                            ITEM_CNT,
                            J,
                            index,
                            true, // JSONB
                            TYPE_OBJECT
                    );
                    mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_);
                }

                mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_);
            }

            // switch_default
            mw.visitLabel(dflt);

            Label fieldReaderNull_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, fieldReaderNull_);
            }

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "getNameHashCodeLCase", "()J", false);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_READER, "getFieldReaderLCase", METHOD_DESC_GET_FIELD_READER, true);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, FIELD_READER);
            mw.visitJumpInsn(Opcodes.IFNULL, fieldReaderNull_);

            mw.visitVarInsn(Opcodes.ALOAD, FIELD_READER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "readFieldValueJSONB", METHOD_DESC_READ_FIELD_VALUE, false);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

            mw.visitLabel(fieldReaderNull_);
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                long hashCode64 = Fnv.hashCode64(fieldReader.fieldName);
                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                varIndex = genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        classNameType,
                        mw,
                        THIS,
                        JSON_READER,
                        OBJECT,
                        FEATURES,
                        varIndex,
                        variants,
                        ITEM_CNT,
                        J,
                        i,
                        true, // JSONB
                        TYPE_OBJECT
                );

                mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

                mw.visitLabel(next_);
            }

            Label processExtra_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, processExtra_);
            }

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "getNameHashCodeLCase", "()J", false);
            mw.visitVarInsn(Opcodes.LSTORE, HASH_CODE64);

            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                long hashCode64 = Fnv.hashCode64(fieldReader.fieldName);
                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                varIndex = genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        classNameType,
                        mw,
                        THIS,
                        JSON_READER,
                        OBJECT,
                        FEATURES,
                        varIndex,
                        variants,
                        ITEM_CNT,
                        J,
                        i,
                        false, // JSONB
                        TYPE_OBJECT
                );

                mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

                mw.visitLabel(next_);
            }
            mw.visitLabel(processExtra_);
        }
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT_READER_ADAPTER, "processExtra", METHOD_DESC_PROCESS_EXTRA, false);
        mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

        mw.visitLabel(for_inc_i_);
        mw.visitIincInsn(I, 1);
        mw.visitJumpInsn(Opcodes.GOTO, for_start_i_);

        mw.visitLabel(for_end_i_);

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 10);
    }

    private static void skipRest(
            MethodWriter mw,
            int JSON_READER,
            int start,
            int ENTRY_CNT,
            int J,
            Label label
    ) {
        Label for_start_j_ = new Label(), for_inc_j_ = new Label();

        mw.visitLdcInsn(start);
        mw.visitVarInsn(Opcodes.ISTORE, J);

        mw.visitLabel(for_start_j_);

        mw.visitVarInsn(Opcodes.ILOAD, J);
        mw.visitVarInsn(Opcodes.ILOAD, ENTRY_CNT);
        mw.visitJumpInsn(Opcodes.IF_ICMPGE, label);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "skipValue", "()V", false);

        mw.visitLabel(for_inc_j_);
        mw.visitIincInsn(J, 1);
        mw.visitJumpInsn(Opcodes.GOTO, for_start_j_);
    }

    private <T> void genMethodReadJSONBObjectArrayMapping(
            ObjectWriteContext context,
            Constructor defaultConstructor,
            long readerFeatures,
            String TYPE_OBJECT,
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter
    ) {
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "readArrayMappingJSONBObject",
                METHOD_DESC_READ_OBJECT,
                512
        );

        final int JSON_READER = 1;
        final int FIELD_TYPE = 2;
        final int FIELD_NAME = 3;
        final int FEATURES = 4;
        final int OBJECT = 6;
        final int ENTRY_CNT = 7;
        final int ITEM_CNT = 8;
        final int J = 9;
        final int AUTO_TYPE_OBJECT_READER = 10;

        genCheckAutoType(classNameType, mw, JSON_READER, FIELD_TYPE, FIELD_NAME, FEATURES, AUTO_TYPE_OBJECT_READER);

        int varIndex = 11;
        Map<Object, Integer> variants = new HashMap<>();

        {
            Label notNull_ = new Label();
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNull", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, notNull_);
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.ARETURN);
            mw.visitLabel(notNull_);
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "startArray", "()I", false);
        mw.visitVarInsn(Opcodes.ISTORE, ENTRY_CNT);

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        Label fieldEnd_ = new Label();
        for (int i = 0; i < fieldReaderArray.length; ++i) {
            mw.visitVarInsn(Opcodes.ILOAD, ENTRY_CNT);
            mw.visitLdcInsn(i + 1);
            mw.visitJumpInsn(Opcodes.IF_ICMPLT, fieldEnd_);

            FieldReader fieldReader = fieldReaderArray[i];
            varIndex = genReadFieldValue(
                    context,
                    fieldReader,
                    fieldBased,
                    classNameType,
                    mw,
                    THIS,
                    JSON_READER,
                    OBJECT,
                    FEATURES,
                    varIndex,
                    variants,
                    ITEM_CNT,
                    J,
                    i,
                    true,   // JSONB
                    TYPE_OBJECT
            );
        }

        skipRest(mw, JSON_READER, fieldReaderArray.length, ENTRY_CNT, J, fieldEnd_);
        mw.visitLabel(fieldEnd_);

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 10);
    }

    private void genCheckAutoType(
            String classNameType,
            MethodWriter mw,
            int JSON_READER,
            int FIELD_TYPE,
            int FIELD_NAME,
            int FEATURES,
            int AUTO_TYPE_OBJECT_READER
    ) {
        Label checkArrayAutoTypeNull_ = new Label();

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "objectClass", "Ljava/lang/Class;");
        mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "checkAutoType", METHOD_DESC_JSON_READER_CHECK_ARRAY_AUTO_TYPE, false);

        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, AUTO_TYPE_OBJECT_READER);
        mw.visitJumpInsn(Opcodes.IFNULL, checkArrayAutoTypeNull_);

        mw.visitVarInsn(Opcodes.ALOAD, AUTO_TYPE_OBJECT_READER);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_READER, "getObjectClass", "()Ljava/lang/Class;", true);
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "objectClass", "Ljava/lang/Class;");
        mw.visitJumpInsn(Opcodes.IF_ACMPEQ, checkArrayAutoTypeNull_);

        mw.visitVarInsn(Opcodes.ALOAD, AUTO_TYPE_OBJECT_READER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_READER, "readJSONBObject", METHOD_DESC_READ_OBJECT, true);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitLabel(checkArrayAutoTypeNull_);
    }

    private <T> void genMethodReadObject(
            ObjectWriteContext context,
            Constructor defaultConstructor,
            long readerFeatures,
            String TYPE_OBJECT,
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter
    ) {
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "readObject",
                METHOD_DESC_READ_OBJECT,
                2048
        );

        final int JSON_READER = 1;
        final int FIELD_TYPE = 2;
        final int FIELD_NAME = 3;
        final int FEATURES = 4;
        final int OBJECT = 6;
        final int I = 7;
        final int HASH_CODE64 = 8;
        final int HASH_CODE_32 = 10;
        final int ITEM_CNT = 11;
        final int J = 12;
        final int FIELD_READER = 13;

        int varIndex = 14;
        Map<Object, Integer> variants = new HashMap<>();

        // if (isJSONB()) ...
        Label json_ = new Label();
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isJSONB", "()Z", false);
        mw.visitJumpInsn(Opcodes.IFEQ, json_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "readJSONBObject", METHOD_DESC_READ_OBJECT, false);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitLabel(json_);

        // if (jsonReader.isArray() && jsonReader.isSupportBeanArray()) {
        Label object_ = new Label(), singleItemArray_ = new Label();
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isArray", "()Z", false);
        mw.visitJumpInsn(Opcodes.IFEQ, object_);

        if ((readerFeatures & JSONReader.Feature.SupportArrayToBean.mask) == 0) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportBeanArray", "(J)Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, singleItemArray_);
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitIntInsn(Opcodes.BIPUSH, '[');
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        for (int i = 0; i < fieldReaderArray.length; ++i) {
            FieldReader fieldReader = fieldReaderArray[i];

            varIndex = genReadFieldValue(
                    context,
                    fieldReader,
                    fieldBased,
                    classNameType,
                    mw,
                    THIS,
                    JSON_READER,
                    OBJECT,
                    FEATURES,
                    varIndex,
                    variants,
                    ITEM_CNT,
                    J,
                    i,
                    false, // JSONB
                    TYPE_OBJECT
            );
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitIntInsn(Opcodes.BIPUSH, ']');
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
        mw.visitInsn(Opcodes.POP); // TODO HANDLE ERROR

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitIntInsn(Opcodes.BIPUSH, ',');
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
        mw.visitInsn(Opcodes.POP);

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitLabel(singleItemArray_);
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "processObjectInputSingleItemArray", METHOD_DESC_READ_OBJECT, false);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitLabel(object_);

        Label notNull_ = new Label(), end_ = new Label();

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitIntInsn(Opcodes.BIPUSH, '{');
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
        mw.visitJumpInsn(Opcodes.IFNE, notNull_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNullOrEmptyString", "()Z", false);
        mw.visitJumpInsn(Opcodes.IFEQ, notNull_);

        mw.visitInsn(Opcodes.ACONST_NULL);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);
        mw.visitJumpInsn(Opcodes.GOTO, end_);

        mw.visitLabel(notNull_);

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        // for (int i = 0; i < entry_cnt; ++i) {
        Label for_start_i_ = new Label(), for_end_i_ = new Label(), for_inc_i_ = new Label();

        mw.visitInsn(Opcodes.ICONST_0);
        mw.visitVarInsn(Opcodes.ISTORE, I);
        mw.visitLabel(for_start_i_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitIntInsn(Opcodes.BIPUSH, '}');
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
        mw.visitJumpInsn(Opcodes.IFNE, for_end_i_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readFieldNameHashCode", "()J", false);
        mw.visitInsn(Opcodes.DUP2);
        mw.visitVarInsn(Opcodes.LSTORE, HASH_CODE64);
        mw.visitLdcInsn(-1L);
        mw.visitInsn(Opcodes.LCMP);
        mw.visitJumpInsn(Opcodes.IFEQ, for_end_i_);

        Label noneAutoType_ = new Label();

        // if (i != 0 && hash == HASH_TYPE && jsonReader.isSupportAutoType())
        mw.visitVarInsn(Opcodes.ILOAD, I);
        mw.visitJumpInsn(Opcodes.IFNE, noneAutoType_);

        mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
        mw.visitLdcInsn(HASH_TYPE);
        mw.visitInsn(Opcodes.LCMP);
        mw.visitJumpInsn(Opcodes.IFNE, noneAutoType_);

        if ((readerFeatures & JSONReader.Feature.SupportAutoType.mask) == 0) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportAutoTypeOrHandler", "(J)Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, noneAutoType_);
        }

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "objectClass", "Ljava/lang/Class;");
        mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT_READER_ADAPTER, "auoType", "(" + ASMUtils.desc(JSONReader.class) + "Ljava/lang/Class;J)Ljava/lang/Object;", false);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitLabel(noneAutoType_);

        // continue
        if (fieldReaderArray.length > 6) {
            // use switch
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodes.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodes[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.computeIfAbsent(hashCode32, k -> new ArrayList<>());
                hashCode64List.add(hashCode64);
            }
            int[] hashCode32Keys = new int[map.size()];
            {
                int off = 0;
                for (Integer key : map.keySet()) {
                    hashCode32Keys[off++] = key;
                }
            }
            Arrays.sort(hashCode32Keys);

            // int hashCode32 = (int)(hashCode64 ^ (hashCode64 >>> 32));
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
            mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
            mw.visitVarInsn(Opcodes.BIPUSH, 32);
            mw.visitInsn(Opcodes.LUSHR);
            mw.visitInsn(Opcodes.LXOR);
            mw.visitInsn(Opcodes.L2I);
            mw.visitVarInsn(Opcodes.ISTORE, HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.visitVarInsn(Opcodes.ILOAD, HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (long hashCode64 : hashCode64Array) {
                    mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                    mw.visitLdcInsn(hashCode64);
                    mw.visitInsn(Opcodes.LCMP);
                    mw.visitJumpInsn(Opcodes.IFNE, dflt);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodes, hashCode64);
                    int index = objectReaderAdapter.mapping[m];

                    FieldReader fieldReader = fieldReaderArray[index];

                    varIndex = genReadFieldValue(
                            context,
                            fieldReader,
                            fieldBased,
                            classNameType,
                            mw,
                            THIS,
                            JSON_READER,
                            OBJECT,
                            FEATURES,
                            varIndex,
                            variants,
                            ITEM_CNT,
                            J,
                            index,
                            false,
                            TYPE_OBJECT
                    );
                    mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_);
                }

                mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_);
            }

            mw.visitLabel(dflt);
            Label fieldReaderNull_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, fieldReaderNull_);
            }

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "getNameHashCodeLCase", "()J", false);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_READER, "getFieldReaderLCase", METHOD_DESC_GET_FIELD_READER, true);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, FIELD_READER);
            mw.visitJumpInsn(Opcodes.IFNULL, fieldReaderNull_);

            mw.visitVarInsn(Opcodes.ALOAD, FIELD_READER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "readFieldValue", METHOD_DESC_READ_FIELD_VALUE, false);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

            mw.visitLabel(fieldReaderNull_);
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                String fieldName = fieldReader.fieldName;
                long hashCode64 = fieldReader.fieldNameHash;

                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                mw.visitLabel(get_);
                varIndex = genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        classNameType,
                        mw,
                        THIS,
                        JSON_READER,
                        OBJECT,
                        FEATURES,
                        varIndex,
                        variants,
                        ITEM_CNT,
                        J,
                        i,
                        false,
                        TYPE_OBJECT
                );

                mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

                mw.visitLabel(next_);
            }

            Label processExtra_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, processExtra_);
            }

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "getNameHashCodeLCase", "()J", false);
            mw.visitVarInsn(Opcodes.LSTORE, HASH_CODE64);

            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                String fieldName = fieldReader.fieldName;
                long hashCode64 = fieldReader.fieldNameHash;
                long hashCode64LCase = fieldReader.fieldNameHashLCase;

                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFEQ, get_);

                if (hashCode64LCase != hashCode64) {
                    mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                    mw.visitLdcInsn(hashCode64LCase);
                    mw.visitInsn(Opcodes.LCMP);
                    mw.visitJumpInsn(Opcodes.IFNE, next_);
                } else {
                    mw.visitJumpInsn(Opcodes.GOTO, next_);
                }

                mw.visitLabel(get_);
                varIndex = genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        classNameType,
                        mw,
                        THIS,
                        JSON_READER,
                        OBJECT,
                        FEATURES,
                        varIndex,
                        variants,
                        ITEM_CNT,
                        J,
                        i,
                        false,
                        TYPE_OBJECT
                );

                mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

                mw.visitLabel(next_);
            }

            mw.visitLabel(processExtra_);
        }
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT_READER_ADAPTER, "processExtra", METHOD_DESC_PROCESS_EXTRA, false);
        mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

        mw.visitLabel(for_inc_i_);
        mw.visitIincInsn(I, 1);
        mw.visitJumpInsn(Opcodes.GOTO, for_start_i_);

        mw.visitLabel(for_end_i_);

        mw.visitLabel(end_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitIntInsn(Opcodes.BIPUSH, ',');
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
        mw.visitInsn(Opcodes.POP);

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 10);
    }

    private <T> void genCreateObject(
            MethodWriter mw,
            ObjectWriteContext context,
            String classNameType,
            String TYPE_OBJECT,
            int FEATURES,
            boolean fieldBased,
            Constructor defaultConstructor,
            Supplier creator
    ) {
        Class objectClass = context.objectClass;
        final int JSON_READER = 1;

        int objectModifiers = objectClass.getModifiers();
        boolean publicObject = Modifier.isPublic(objectModifiers) && !classLoader.isExternalClass(objectClass);

        if (defaultConstructor == null || !publicObject || !Modifier.isPublic(defaultConstructor.getModifiers())) {
            if (creator != null) {
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "creator", "Ljava/util/function/Supplier;");
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/function/Supplier", "get", "()Ljava/lang/Object;", true);
            } else {
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "features", "(J)J", false);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "createInstance", "(J)Ljava/lang/Object;", false);
            }
            if (publicObject) {
                mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
            }
        } else {
            newObject(mw, TYPE_OBJECT, defaultConstructor);
        }

        if (context.hasStringField) {
            Label endInitStringAsEmpty_ = new Label(), addResolveTask_ = new Label();

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isInitStringFieldAsEmpty", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, endInitStringAsEmpty_);

            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitInsn(Opcodes.SWAP);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "initStringFieldAsEmpty", "(Ljava/lang/Object;)V", false);
            mw.visitLabel(endInitStringAsEmpty_);
        }
    }

    private <T> int genReadFieldValue(
            ObjectWriteContext context,
            FieldReader fieldReader,
            boolean fieldBased,
            String classNameType,
            MethodWriter mw,
            int THIS,
            int JSON_READER,
            int OBJECT,
            int FEATURES,
            int varIndex,
            Map<Object, Integer> variants,
            int ITEM_CNT,
            int J,
            int i,
            boolean jsonb,
            String TYPE_OBJECT
    ) {
        Class objectClass = context.objectClass;
        Class fieldClass = fieldReader.fieldClass;
        Type fieldType = fieldReader.fieldType;
        long fieldFeatures = fieldReader.features;
        String format = fieldReader.format;
        Type itemType = fieldReader.itemType;

        Field field = fieldReader.field;
        Method method = fieldReader.method;

        Label endSet_ = new Label();

        String TYPE_FIELD_CLASS = ASMUtils.type(fieldClass);
        String DESC_FIELD_CLASS = ASMUtils.desc(fieldClass);

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        int fieldModifier = 0;
        if ((fieldBased || method == null) && field != null) {
            fieldModifier = field.getModifiers();
        }

        if (fieldBased
                && Modifier.isPublic(objectClass.getModifiers())
                && Modifier.isPublic(fieldModifier)
                && !Modifier.isFinal(fieldModifier)
                && !classLoader.isExternalClass(objectClass)
        ) {
            mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
        }

        if (fieldClass == boolean.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readBoolValue", "()Z", false);
        } else if (fieldClass == byte.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readInt32Value", "()I", false);
        } else if (fieldClass == short.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readInt32Value", "()I", false);
        } else if (fieldClass == int.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readInt32Value", "()I", false);
        } else if (fieldClass == long.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readInt64Value", "()J", false);
        } else if (fieldClass == float.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readFloatValue", "()F", false);
        } else if (fieldClass == double.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readDoubleValue", "()D", false);
        } else if (fieldClass == char.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readCharValue", "()C", false);
        } else if (fieldClass == String.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            Label null_ = new Label();

            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readString", "()Ljava/lang/String;", false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitJumpInsn(Opcodes.IFNULL, null_);

            if ("trim".equals(format)) {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "trim", "()Ljava/lang/String;", false);
            }
            mw.visitLabel(null_);
        } else if (fieldClass == Integer.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readInt32", "()Ljava/lang/Integer;", false);
        } else if (fieldClass == Long.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readInt64", "()Ljava/lang/Long;", false);
        } else if (fieldClass == Float.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readFloat", "()Ljava/lang/Float;", false);
        } else if (fieldClass == Double.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readDouble", "()Ljava/lang/Double;", false);
        } else if (fieldClass == BigDecimal.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readBigDecimal", "()Ljava/math/BigDecimal;", false);
        } else if (fieldClass == BigInteger.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readBigInteger", "()Ljava/math/BigInteger;", false);
        } else if (fieldClass == UUID.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readUUID", "()Ljava/util/UUID;", false);
        } else if (fieldClass == LocalDate.class && fieldReader.format == null) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readLocalDate", "()Ljava/time/LocalDate;", false);
        } else if (fieldClass == OffsetDateTime.class && fieldReader.format == null) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readOffsetDateTime", "()Ljava/time/OffsetDateTime;", false);
        } else {
            Label endObject_ = new Label();

            Integer REFERENCE = variants.get("REFERENCE");
            if (REFERENCE == null) {
                variants.put("REFERENCE", REFERENCE = varIndex);
                varIndex++;
            }

            if (!ObjectWriterProvider.isPrimitiveOrEnum(fieldClass)) {
                Label endReference_ = new Label(), addResolveTask_ = new Label();

                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isReference", "()Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, endReference_);

                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readReference", "()Ljava/lang/String;", false);
                mw.visitInsn(Opcodes.DUP);
                mw.visitVarInsn(Opcodes.ASTORE, REFERENCE);
                mw.visitLdcInsn("..");
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, addResolveTask_);

                if (fieldClass.isAssignableFrom(objectClass)) {
                    mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
//                    mw.visitTypeInsn(CHECKCAST, TYPE_FIELD_CLASS); // cast
                    mw.visitJumpInsn(Opcodes.GOTO, endObject_);
                }

                mw.visitLabel(addResolveTask_);

                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
                mw.visitVarInsn(Opcodes.ALOAD, REFERENCE);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "addResolveTask", METHOD_DESC_ADD_RESOLVE_TASK, false);
                mw.visitInsn(Opcodes.POP);
                mw.visitJumpInsn(Opcodes.GOTO, endSet_);

                mw.visitLabel(endReference_);
            }

            if (!fieldReader.fieldClassSerializable) {
                Label endIgnoreCheck_ = new Label();

                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isIgnoreNoneSerializable", "()Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, endIgnoreCheck_);
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "skipValue", "()V", false);
                mw.visitInsn(Opcodes.POP);
                mw.visitJumpInsn(Opcodes.GOTO, endSet_);

                mw.visitLabel(endIgnoreCheck_);
            }

            boolean list = List.class.isAssignableFrom(fieldClass)
                    && !fieldClass.getName().startsWith("com.google.common.collect.Immutable");

            if (list) {
                Class itemClass = TypeUtils.getMapping(itemType);
                if (itemClass != null && Collection.class.isAssignableFrom(itemClass)) {
                    list = false;
                }
            }

            if (list) {
                if (itemType == null) {
                    itemType = Object.class;
                }

                Class itemClass = TypeUtils.getMapping(itemType);
                String ITEM_OBJECT_READER = fieldItemObjectReader(i);

                Integer LIST = variants.get(fieldClass);
                if (LIST == null) {
                    variants.put(fieldClass, LIST = varIndex);
                    varIndex++;
                }
                Integer AUTO_TYPE_OBJECT_READER = variants.get(ObjectReader.class);
                if (AUTO_TYPE_OBJECT_READER == null) {
                    variants.put(fieldClass, AUTO_TYPE_OBJECT_READER = varIndex);
                    varIndex++;
                }

                String LIST_TYPE = fieldClass.isInterface() ? "java/util/ArrayList" : TYPE_FIELD_CLASS;

                Label loadList_ = new Label(), listNotNull_ = new Label();

                boolean initCapacity = JVM_VERSION == 8 && "java/util/ArrayList".equals(LIST_TYPE);

                if (jsonb) {
                    Label checkAutoTypeNull_ = new Label();

                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "checkObjectAutoType", METHOD_DESC_CHECK_ARRAY_AUTO_TYPE, false);
                    mw.visitInsn(Opcodes.DUP);
                    mw.visitVarInsn(Opcodes.ASTORE, AUTO_TYPE_OBJECT_READER);
                    mw.visitJumpInsn(Opcodes.IFNULL, checkAutoTypeNull_);

                    mw.visitVarInsn(Opcodes.ALOAD, AUTO_TYPE_OBJECT_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                    mw.visitLdcInsn(fieldReader.fieldName);
                    mw.visitLdcInsn(fieldFeatures);
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_READER, "readJSONBObject", METHOD_DESC_READ_OBJECT, true);
                    mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_FIELD_CLASS);
                    mw.visitVarInsn(Opcodes.ASTORE, LIST);
                    mw.visitJumpInsn(Opcodes.GOTO, loadList_);

                    mw.visitLabel(checkAutoTypeNull_);

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "startArray", "()I", false);
                    mw.visitInsn(Opcodes.DUP);
                    mw.visitVarInsn(Opcodes.ISTORE, ITEM_CNT);
                    mw.visitLdcInsn(-1);
                    mw.visitJumpInsn(Opcodes.IF_ICMPNE, listNotNull_);

                    mw.visitInsn(Opcodes.ACONST_NULL);
                    mw.visitVarInsn(Opcodes.ASTORE, LIST);
                    mw.visitJumpInsn(Opcodes.GOTO, loadList_);

                    mw.visitLabel(listNotNull_);

                    mw.visitTypeInsn(Opcodes.NEW, LIST_TYPE);
                    mw.visitInsn(Opcodes.DUP);
                    if (initCapacity) {
                        mw.visitVarInsn(Opcodes.ILOAD, ITEM_CNT);
                        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "(I)V", false);
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "()V", false);
                    }
                    mw.visitVarInsn(Opcodes.ASTORE, LIST);
                } else {
                    Label match_ = new Label(), skipValue_ = new Label(), loadNull_ = new Label();

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNull", "()Z", false);
                    mw.visitJumpInsn(Opcodes.IFNE, loadNull_);

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitIntInsn(Opcodes.BIPUSH, '[');
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
                    mw.visitJumpInsn(Opcodes.IFNE, match_);

                    if (itemClass == String.class) {
                        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isString", "()Z", false);
                        mw.visitJumpInsn(Opcodes.IFEQ, skipValue_);

                        mw.visitTypeInsn(Opcodes.NEW, LIST_TYPE);
                        mw.visitInsn(Opcodes.DUP);
                        if (initCapacity) {
                            mw.visitLdcInsn(10);
                            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "(I)V", false);
                        } else {
                            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "()V", false);
                        }
                        mw.visitVarInsn(Opcodes.ASTORE, LIST);

                        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNullOrEmptyString", "()Z", false);
                        mw.visitJumpInsn(Opcodes.IFNE, loadList_);

                        mw.visitVarInsn(Opcodes.ALOAD, LIST);
                        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readString", "()Ljava/lang/String;", false);
                        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                        mw.visitInsn(Opcodes.POP);

                        mw.visitJumpInsn(Opcodes.GOTO, loadList_);
                    } else if (itemType instanceof Class) {
                        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNullOrEmptyString", "()Z", false);
                        mw.visitJumpInsn(Opcodes.IFNE, loadNull_);

                        // nextIfNullOrEmptyString
                        mw.visitTypeInsn(Opcodes.NEW, LIST_TYPE);
                        mw.visitInsn(Opcodes.DUP);
                        if (initCapacity) {
                            mw.visitLdcInsn(10);
                            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "(I)V", false);
                        } else {
                            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "()V", false);
                        }
                        mw.visitVarInsn(Opcodes.ASTORE, LIST);

                        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                        mw.visitVarInsn(Opcodes.ALOAD, LIST);
                        mw.visitLdcInsn((Class) itemType);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readArray", "(Ljava/util/List;Ljava/lang/reflect/Type;)V", false);

                        mw.visitJumpInsn(Opcodes.GOTO, loadList_);
                    }

                    mw.visitLabel(skipValue_);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "skipValue", "()V", false);

                    mw.visitLabel(loadNull_);
                    mw.visitInsn(Opcodes.ACONST_NULL);
                    mw.visitVarInsn(Opcodes.ASTORE, LIST);
                    mw.visitJumpInsn(Opcodes.GOTO, loadList_);

                    mw.visitLabel(match_);
                    mw.visitTypeInsn(Opcodes.NEW, LIST_TYPE);
                    mw.visitInsn(Opcodes.DUP);
                    if (initCapacity) {
                        mw.visitLdcInsn(10);
                        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "(I)V", false);
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, LIST_TYPE, "<init>", "()V", false);
                    }
                    mw.visitVarInsn(Opcodes.ASTORE, LIST);
                }

                Label for_start_j_ = new Label(), for_end_j_ = new Label(), for_inc_j_ = new Label();
                mw.visitInsn(Opcodes.ICONST_0);
                mw.visitVarInsn(Opcodes.ISTORE, J);

                mw.visitLabel(for_start_j_);

                if (jsonb) {
                    // j < item_cnt
                    mw.visitVarInsn(Opcodes.ILOAD, J);
                    mw.visitVarInsn(Opcodes.ILOAD, ITEM_CNT);
                    mw.visitJumpInsn(Opcodes.IF_ICMPGE, for_end_j_);
                } else {
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitIntInsn(Opcodes.BIPUSH, ']');
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
                    mw.visitJumpInsn(Opcodes.IFNE, for_end_j_);
                }

                if (itemType == String.class) {
                    mw.visitVarInsn(Opcodes.ALOAD, LIST);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readString", "()Ljava/lang/String;", false);
                } else {
                    Label notNull_ = new Label();

                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);
                    mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);

                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
//                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "getItemObjectReader", METHOD_DESC_GET_ITEM_OBJECT_READER, false);
//
//                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL,  TYPE_FIELD_READE, "getItemType", "()Ljava/lang/reflect/Type;", false);
//                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL,  TYPE_JSON_READER, "getObjectReader", METHOD_DESC_GET_OBJECT_READER, false);
                    mw.visitFieldInsn(PUTFIELD, classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);

                    mw.visitLabel(notNull_);

                    Label endReference_ = new Label(), addResolveTask_ = new Label();

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isReference", "()Z", false);
                    mw.visitJumpInsn(Opcodes.IFEQ, endReference_);

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readReference", "()Ljava/lang/String;", false);
                    mw.visitInsn(Opcodes.DUP);
                    mw.visitVarInsn(Opcodes.ASTORE, REFERENCE);
                    mw.visitLdcInsn("..");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                    mw.visitJumpInsn(Opcodes.IFEQ, addResolveTask_);

                    if (fieldClass.isAssignableFrom(objectClass)) {
                        mw.visitVarInsn(Opcodes.ALOAD, LIST);
                        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
                        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                        mw.visitInsn(Opcodes.POP);
                        mw.visitJumpInsn(Opcodes.GOTO, for_inc_j_);
                    }

                    mw.visitLabel(addResolveTask_);

                    mw.visitVarInsn(Opcodes.ALOAD, LIST);
                    mw.visitInsn(Opcodes.ACONST_NULL);
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                    mw.visitInsn(Opcodes.POP);

                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, LIST);
                    mw.visitVarInsn(Opcodes.ILOAD, J);
                    mw.visitVarInsn(Opcodes.ALOAD, REFERENCE);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "addResolveTask", METHOD_DESC_ADD_RESOLVE_TASK_2, false);
                    mw.visitJumpInsn(Opcodes.GOTO, for_inc_j_);

                    mw.visitLabel(endReference_);

                    mw.visitVarInsn(Opcodes.ALOAD, LIST);
                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                    mw.visitLdcInsn(fieldReader.fieldName);
                    mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                            TYPE_OBJECT_READER,
                            jsonb ? "readJSONBObject" : "readObject",
                            METHOD_DESC_READ_OBJECT,
                            true);
                }
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                mw.visitInsn(Opcodes.POP);

                if (!jsonb) {
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitIntInsn(Opcodes.BIPUSH, ',');
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
                    mw.visitInsn(Opcodes.POP);
                }

                mw.visitLabel(for_inc_j_);
                mw.visitIincInsn(J, 1);
                mw.visitJumpInsn(Opcodes.GOTO, for_start_j_);

                mw.visitLabel(for_end_j_);

                if (!jsonb) {
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitIntInsn(Opcodes.BIPUSH, ',');
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
                    mw.visitInsn(Opcodes.POP);
                }

                mw.visitLabel(loadList_);
                mw.visitVarInsn(Opcodes.ALOAD, LIST);
            } else {
                final String FIELD_OBJECT_READER = fieldObjectReader(i);

                Label valueNotNull_ = new Label();

                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNull", "()Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, valueNotNull_);

                if (fieldClass == Optional.class) {
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Optional", "empty", "()Ljava/util/Optional;", false);
                } else if (fieldClass == OptionalInt.class) {
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/OptionalInt", "empty", "()Ljava/util/OptionalInt;", false);
                } else if (fieldClass == OptionalLong.class) {
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/OptionalLong", "empty", "()Ljava/util/OptionalLong;", false);
                } else if (fieldClass == OptionalDouble.class) {
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/OptionalDouble", "empty", "()Ljava/util/OptionalDouble;", false);
                } else {
                    mw.visitInsn(Opcodes.ACONST_NULL);
                }
                mw.visitJumpInsn(Opcodes.GOTO, endObject_);

                mw.visitLabel(valueNotNull_);

                if (fieldClass == String[].class) {
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readStringArray", "()[Ljava/lang/String;", false);
                } else if (fieldClass == long[].class) {
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readInt64ValueArray", "()[J", false);
                } else {
                    // object.<setMethod>(this.objectReader_<i>.readObject(jsonReader))
                    Label notNull_ = new Label();

                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);
                    mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);

                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "getObjectReader", METHOD_DESC_GET_OBJECT_READER_1, false);
                    mw.visitFieldInsn(PUTFIELD, classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);

                    mw.visitLabel(notNull_);
                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                    mw.visitLdcInsn(fieldReader.fieldName);
                    mw.visitLdcInsn(fieldFeatures);
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                            TYPE_OBJECT_READER,
                            jsonb ? "readJSONBObject" : "readObject",
                            METHOD_DESC_READ_OBJECT,
                            true);

                    if (method != null || (Modifier.isPublic(objectClass.getModifiers())
                            && Modifier.isPublic(fieldModifier)
                            && !Modifier.isFinal(fieldModifier)
                            && !classLoader.isExternalClass(objectClass))
                    ) {
                        mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_FIELD_CLASS); // cast
                    }

                    if (fieldReader.noneStaticMemberClass) {
                        try {
                            Field this0 = fieldClass.getDeclaredField("this$0");
                            long fieldOffset = UnsafeUtils.objectFieldOffset(this0);

                            mw.visitInsn(Opcodes.DUP);
                            mw.visitFieldInsn(Opcodes.GETSTATIC, TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                            mw.visitInsn(Opcodes.SWAP);
                            mw.visitLdcInsn(fieldOffset);
                            mw.visitVarInsn(ALOAD, OBJECT);
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/misc/Unsafe", "putObject", "(Ljava/lang/Object;JLjava/lang/Object;)V", false);
                        } catch (NoSuchFieldException e) {
                            // ignored
                        }
                    }
                }
            }

            mw.visitLabel(endObject_);

            if (!jsonb) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitIntInsn(Opcodes.BIPUSH, ',');
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfMatch", "(C)Z", false);
                mw.visitInsn(Opcodes.POP);
            }
        }

        if (method != null) {
            boolean invokeFieldReaderAccept = !context.publicClass || context.externalClass;

            if (invokeFieldReaderAccept) {
                Integer FIELD_VALUE = variants.get(fieldClass);
                if (FIELD_VALUE == null) {
                    variants.put(fieldClass, FIELD_VALUE = varIndex);
                    if (fieldClass == long.class || fieldClass == double.class) {
                        varIndex += 2;
                    } else {
                        varIndex++;
                    }
                }

                String acceptMethodDesc;
                int LOAD;
                if (fieldClass == boolean.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;Z)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == byte.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;B)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == short.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;S)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == int.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;I)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == long.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;J)V";
                    mw.visitVarInsn(Opcodes.LSTORE, FIELD_VALUE);
                    LOAD = Opcodes.LLOAD;
                } else if (fieldClass == char.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;C)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == float.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;F)V";
                    mw.visitVarInsn(Opcodes.FSTORE, FIELD_VALUE);
                    LOAD = Opcodes.FLOAD;
                } else if (fieldClass == double.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;D)V";
                    mw.visitVarInsn(Opcodes.DSTORE, FIELD_VALUE);
                    LOAD = Opcodes.DLOAD;
                } else {
                    acceptMethodDesc = "(Ljava/lang/Object;Ljava/lang/Object;)V";
                    mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);
                    LOAD = Opcodes.ALOAD;
                }

                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.visitInsn(Opcodes.SWAP);
                mw.visitVarInsn(LOAD, FIELD_VALUE);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_FIELD_READE, "accept", acceptMethodDesc, false);
            } else {
                Class<?> returnType = method.getReturnType();
                String methodName = method.getName();

                String methodDesc = null;
                if (returnType == Void.TYPE) {
                    if (fieldClass == boolean.class) {
                        methodDesc = "(Z)V";
                    } else if (fieldClass == byte.class) {
                        methodDesc = "(B)V";
                    } else if (fieldClass == short.class) {
                        methodDesc = "(S)V";
                    } else if (fieldClass == int.class) {
                        methodDesc = "(I)V";
                    } else if (fieldClass == long.class) {
                        methodDesc = "(J)V";
                    } else if (fieldClass == char.class) {
                        methodDesc = "(C)V";
                    } else if (fieldClass == float.class) {
                        methodDesc = "(F)V";
                    } else if (fieldClass == double.class) {
                        methodDesc = "(D)V";
                    } else if (fieldClass == Boolean.class) {
                        methodDesc = "(Ljava/lang/Boolean;)V";
                    } else if (fieldClass == Integer.class) {
                        methodDesc = "(Ljava/lang/Integer;)V";
                    } else if (fieldClass == Long.class) {
                        methodDesc = "(Ljava/lang/Long;)V";
                    } else if (fieldClass == Float.class) {
                        methodDesc = "(Ljava/lang/Float;)V";
                    } else if (fieldClass == Double.class) {
                        methodDesc = "(Ljava/lang/Double;)V";
                    } else if (fieldClass == BigDecimal.class) {
                        methodDesc = "(Ljava/math/BigDecimal;)V";
                    } else if (fieldClass == String.class) {
                        methodDesc = "(Ljava/lang/String;)V";
                    } else if (fieldClass == UUID.class) {
                        methodDesc = "(Ljava/util/UUID;)V";
                    } else if (fieldClass == List.class) {
                        methodDesc = "(Ljava/util/List;)V";
                    } else if (fieldClass == Map.class) {
                        methodDesc = "(Ljava/util/Map;)V";
                    }
                }

                if (methodDesc == null) {
                    methodDesc = "(" + DESC_FIELD_CLASS + ")" + ASMUtils.desc(returnType);
                }
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT, methodName, methodDesc, false);
                if (returnType != void.class) {
                    mw.visitInsn(Opcodes.POP);
                }
            }
            // TODO BUILD METHOD
        } else if (field != null) {
            String fieldClassName = fieldClass.getName();
            boolean setDirect = (objectClass.getModifiers() & Modifier.PUBLIC) != 0
                    && (fieldModifier & Modifier.PUBLIC) != 0
                    && (fieldModifier & Modifier.FINAL) == 0
                    && (ObjectWriterProvider.isPrimitiveOrEnum(fieldClass) || fieldClassName.startsWith("java.") || fieldClass.getClassLoader() == ObjectReaderProvider.FASTJSON2_CLASS_LOADER)
                    && !classLoader.isExternalClass(objectClass)
                    && field.getDeclaringClass() == objectClass;
            if (setDirect) {
                mw.visitFieldInsn(PUTFIELD, TYPE_OBJECT, field.getName(), DESC_FIELD_CLASS);
            } else {
                Integer FIELD_VALUE = variants.get(fieldClass);
                if (FIELD_VALUE == null) {
                    variants.put(fieldClass, FIELD_VALUE = varIndex);
                    if (fieldClass == long.class || fieldClass == double.class) {
                        varIndex += 2;
                    } else {
                        varIndex++;
                    }
                }

                String methodName, methodDes;
                int LOAD;
                if (fieldClass == int.class) {
                    methodName = "putInt";
                    methodDes = "(Ljava/lang/Object;JI)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == long.class) {
                    methodName = "putLong";
                    methodDes = "(Ljava/lang/Object;JJ)V";
                    mw.visitVarInsn(Opcodes.LSTORE, FIELD_VALUE);
                    LOAD = Opcodes.LLOAD;
                } else if (fieldClass == float.class) {
                    methodName = "putFloat";
                    methodDes = "(Ljava/lang/Object;JF)V";
                    mw.visitVarInsn(Opcodes.FSTORE, FIELD_VALUE);
                    LOAD = Opcodes.FLOAD;
                } else if (fieldClass == double.class) {
                    methodName = "putDouble";
                    methodDes = "(Ljava/lang/Object;JD)V";
                    mw.visitVarInsn(Opcodes.DSTORE, FIELD_VALUE);
                    LOAD = Opcodes.DLOAD;
                } else if (fieldClass == char.class) {
                    methodName = "putChar";
                    methodDes = "(Ljava/lang/Object;JC)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == byte.class) {
                    methodName = "putByte";
                    methodDes = "(Ljava/lang/Object;JB)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == short.class) {
                    methodName = "putShort";
                    methodDes = "(Ljava/lang/Object;JS)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == boolean.class) {
                    methodName = "putBoolean";
                    methodDes = "(Ljava/lang/Object;JZ)V";
                    mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else {
                    methodName = "putObject";
                    methodDes = "(Ljava/lang/Object;JLjava/lang/Object;)V";
                    mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);
                    LOAD = Opcodes.ALOAD;
                }

                mw.visitFieldInsn(Opcodes.GETSTATIC, TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                mw.visitInsn(Opcodes.SWAP);

                mw.visitLdcInsn(
                        UnsafeUtils.objectFieldOffset(field));
                mw.visitVarInsn(LOAD, FIELD_VALUE);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/misc/Unsafe", methodName, methodDes, false);
            }
        } else {
            throw new JSONException("TODO"); // TODO Lambda Getter
        }

        mw.visitLabel(endSet_);

        return varIndex;
    }

    private void gwGetFieldType(String classNameType, MethodWriter mw, int THIS, int i, Type fieldType) {
        if (fieldType instanceof Class) {
            Class fieldClass = (Class) fieldType;
            String fieldClassName = fieldClass.getName();
            boolean publicClass = Modifier.isPublic(fieldClass.getModifiers());
            boolean internalClass = fieldClassName.startsWith("java.")
                    || fieldClass == JSONArray.class
                    || fieldClass == JSONObject.class;
            if (publicClass && internalClass) {
                mw.visitLdcInsn((Class) fieldType);
                return;
            }
        }

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
        mw.visitFieldInsn(Opcodes.GETFIELD, TYPE_FIELD_READE, "fieldType", "Ljava/lang/reflect/Type;");
    }

    static class ObjectWriteContext {
        final Class objectClass;
        final ClassWriter cw;
        final boolean publicClass;
        final boolean externalClass;
        final FieldReader[] fieldReaders;
        final boolean hasStringField;

        public ObjectWriteContext(
                Class objectClass,
                ClassWriter cw,
                boolean externalClass,
                FieldReader[] fieldReaders
        ) {
            this.objectClass = objectClass;
            this.cw = cw;
            this.publicClass = Modifier.isPublic(objectClass.getModifiers());
            this.externalClass = externalClass;
            this.fieldReaders = fieldReaders;

            boolean hasStringField = false;
            for (FieldReader fieldReader : fieldReaders) {
                if (fieldReader.fieldClass == String.class) {
                    hasStringField = true;
                    break;
                }
            }
            this.hasStringField = hasStringField;
        }
    }

    public Function<Consumer, ByteArrayValueConsumer> createByteArrayValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return createValueConsumer0(objectClass, fieldReaderArray, true);
    }

    public Function<Consumer, CharArrayValueConsumer> createCharArrayValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return createValueConsumer0(objectClass, fieldReaderArray, false);
    }

    private Function createValueConsumer0(
            Class objectClass,
            FieldReader[] fieldReaderArray,
            boolean bytes
    ) {
        Constructor defaultConstructor = BeanUtils.getDefaultConstructor(objectClass, false);
        if (defaultConstructor == null || !Modifier.isPublic(objectClass.getModifiers())) {
            return null;
        }

        ClassWriter cw = new ClassWriter(
                (e) -> objectClass.getName().equals(e) ? objectClass : null
        );

        String className = (bytes ? "VBACG_" : "VCACG_")
                + seed.incrementAndGet()
                + "_" + fieldReaderArray.length
                + "_" + objectClass.getSimpleName();
        String classNameType;
        String classNameFull;

        Package pkg = ObjectReaderCreatorASM.class.getPackage();
        if (pkg != null) {
            String packageName = pkg.getName();
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
        } else {
            classNameType = className;
            classNameFull = className;
        }

        String TYPE_OBJECT = ASMUtils.type(objectClass);
        String DESC_OBJECT = ASMUtils.desc(objectClass);

        cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "consumer", "Ljava/util/function/Consumer;");
        cw.visitField(Opcodes.ACC_PUBLIC, "object", DESC_OBJECT);

        cw.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                classNameType,
                "java/lang/Object",
                new String[]{
                        bytes ? TYPE_BYTE_ARRAY_VALUE_CONSUMER : TYPE_CHAR_ARRAY_VALUE_CONSUMER
                }
        );

        {
            final int CONSUMER = 1;

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "(Ljava/util/function/Consumer;)V",
                    32
            );
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, CONSUMER);
            mw.visitFieldInsn(PUTFIELD, classNameType, "consumer", "Ljava/util/function/Consumer;");

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        {
            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "beforeRow",
                    "(I)V",
                    32
            );

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            newObject(mw, TYPE_OBJECT, defaultConstructor);
            mw.visitFieldInsn(PUTFIELD, classNameType, "object", DESC_OBJECT);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        {
            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "afterRow",
                    "(I)V",
                    32
            );

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "consumer", "Ljava/util/function/Consumer;");
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "object", DESC_OBJECT);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", true);

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitFieldInsn(PUTFIELD, classNameType, "object", DESC_OBJECT);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        {
            final int ROW = 1, COLUMN = 2, BYTES = 3, OFF = 4, LEN = 5, CHARSET = 6;

            String methodDesc;
            if (bytes) {
                methodDesc = "(II[BIILjava/nio/charset/Charset;)V";
            } else {
                methodDesc = "(II[CII)V";
            }

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "accept",
                    methodDesc,
                    32
            );

            Label switch_ = new Label(), L0_ = new Label(), L1_ = new Label();

            mw.visitVarInsn(Opcodes.ILOAD, LEN);
            mw.visitJumpInsn(Opcodes.IFNE, L0_);
            mw.visitInsn(Opcodes.RETURN);

            mw.visitLabel(L0_);
            mw.visitVarInsn(Opcodes.ILOAD, COLUMN);
            mw.visitJumpInsn(Opcodes.IFGE, L1_);
            mw.visitInsn(Opcodes.RETURN);

            mw.visitLabel(L1_);
            mw.visitVarInsn(Opcodes.ILOAD, COLUMN);
            mw.visitLdcInsn(fieldReaderArray.length);
            mw.visitJumpInsn(Opcodes.IF_ICMPLE, switch_);
            mw.visitInsn(Opcodes.RETURN);

            mw.visitLabel(switch_);

            Label dflt = new Label();
            Label[] labels = new Label[fieldReaderArray.length];
            int[] columns = new int[fieldReaderArray.length];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = i;
                labels[i] = new Label();
            }

            mw.visitVarInsn(Opcodes.ILOAD, COLUMN);
            mw.visitLookupSwitchInsn(dflt, columns, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);
                FieldReader fieldReader = fieldReaderArray[i];
                Field field = fieldReader.field;
                Class fieldClass = fieldReader.fieldClass;
                Type fieldType = fieldReader.fieldType;

                mw.visitVarInsn(ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, "object", DESC_OBJECT);

                String DESC_FIELD_CLASS, DESC_METHOD;
                if (fieldType == Integer.class
                        || fieldType == int.class
                        || fieldType == Short.class
                        || fieldType == short.class
                        || fieldType == Byte.class
                        || fieldType == byte.class
                ) {
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_TYPE_UTILS, "parseInt", bytes ? "([BII)I" : "([CII)I", false);

                    if (fieldType == short.class) {
                        DESC_FIELD_CLASS = "S";
                        DESC_METHOD = "(S)V";
                    } else if (fieldType == Short.class) {
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                        DESC_FIELD_CLASS = "Ljava/lang/Short;";
                        DESC_METHOD = "(Ljava/lang/Short;)V";
                    } else if (fieldType == byte.class) {
                        DESC_FIELD_CLASS = "B";
                        DESC_METHOD = "(B)V";
                    } else if (fieldType == Byte.class) {
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                        DESC_FIELD_CLASS = "Ljava/lang/Byte;";
                        DESC_METHOD = "(Ljava/lang/Byte;)V";
                    } else if (fieldType == int.class) {
                        DESC_FIELD_CLASS = "I";
                        DESC_METHOD = "(I)V";
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                        DESC_FIELD_CLASS = "Ljava/lang/Integer;";
                        DESC_METHOD = "(Ljava/lang/Integer;)V";
                    }
                } else if (fieldType == Long.class || fieldType == long.class) {
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_TYPE_UTILS, "parseLong", bytes ? "([BII)J" : "([CII)J", false);
                    if (fieldType == long.class) {
                        DESC_FIELD_CLASS = "J";
                        DESC_METHOD = "(J)V";
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                        DESC_FIELD_CLASS = "Ljava/lang/Long;";
                        DESC_METHOD = "(Ljava/lang/Long;)V";
                    }
                } else if (fieldType == Float.class || fieldType == float.class) {
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_TYPE_UTILS, "parseFloat", bytes ? "([BII)F" : "([CII)F", false);

                    if (fieldType == float.class) {
                        DESC_FIELD_CLASS = "F";
                        DESC_METHOD = "(F)V";
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                        DESC_FIELD_CLASS = "Ljava/lang/Float;";
                        DESC_METHOD = "(Ljava/lang/Float;)V";
                    }
                } else if (fieldType == Double.class || fieldType == double.class) {
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_TYPE_UTILS, "parseDouble", bytes ? "([BII)D" : "([CII)D", false);

                    if (fieldType == double.class) {
                        DESC_FIELD_CLASS = "D";
                        DESC_METHOD = "(D)V";
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                        DESC_FIELD_CLASS = "Ljava/lang/Double;";
                        DESC_METHOD = "(Ljava/lang/Double;)V";
                    }
                } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_TYPE_UTILS, "parseBoolean", bytes ? "([BII)Ljava/lang/Boolean;" : "([CII)Ljava/lang/Boolean;", false);

                    if (fieldType == boolean.class) {
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                        DESC_FIELD_CLASS = "Z";
                        DESC_METHOD = "(Z)V";
                    } else {
                        DESC_FIELD_CLASS = "Ljava/lang/Boolean;";
                        DESC_METHOD = "(Ljava/lang/Boolean;)V";
                    }
                } else if (fieldType == Date.class) {
                    mw.visitTypeInsn(Opcodes.NEW, "java/util/Date");

                    // long millis = DateUtils.parseMillis(bytes, off, len, charset);
                    mw.visitInsn(Opcodes.DUP);
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    if (bytes) {
                        mw.visitVarInsn(ALOAD, CHARSET);
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_DATE_UTILS, "parseMillis", "([BIILjava/nio/charset/Charset;)J", false);
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_DATE_UTILS, "parseMillis", "([CII)J", false);
                    }
                    mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Date", "<init>", "(J)V", false);

                    DESC_FIELD_CLASS = "Ljava/util/Date;";
                    DESC_METHOD = "(Ljava/util/Date;)V";
                } else if (fieldType == BigDecimal.class) {
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_TYPE_UTILS, "parseBigDecimal", bytes ? "([BII)Ljava/math/BigDecimal;" : "([CII)Ljava/math/BigDecimal;", false);

                    DESC_FIELD_CLASS = "Ljava/math/BigDecimal;";
                    DESC_METHOD = "(Ljava/math/BigDecimal;)V";
                } else {
                    mw.visitTypeInsn(Opcodes.NEW, "java/lang/String");
                    mw.visitInsn(Opcodes.DUP);
                    mw.visitVarInsn(ALOAD, BYTES);
                    mw.visitVarInsn(Opcodes.ILOAD, OFF);
                    mw.visitVarInsn(Opcodes.ILOAD, LEN);
                    if (bytes) {
                        mw.visitVarInsn(ALOAD, CHARSET);
                        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BIILjava/nio/charset/Charset;)V", false);
                    } else {
                        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([CII)V", false);
                    }

                    if (fieldType == String.class) {
                        DESC_FIELD_CLASS = "Ljava/lang/String;";
                        DESC_METHOD = "(Ljava/lang/String;)V";
                    } else {
                        DESC_FIELD_CLASS = ASMUtils.desc(fieldClass);
                        if (fieldClass == char.class) {
                            DESC_METHOD = "(C)V";
                        } else {
                            DESC_METHOD = "(" + DESC_FIELD_CLASS + ")V";
                        }

                        mw.visitLdcInsn(fieldClass);
                        mw.visitMethodInsn(Opcodes.INVOKESTATIC, TYPE_TYPE_UTILS, "cast", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;", false);
                        mw.visitTypeInsn(Opcodes.CHECKCAST, ASMUtils.type(fieldClass));
                    }
                }

                if (fieldReader.method != null) {
                    if (fieldReader.method.getReturnType() != void.class) {
                        return null;
                    }

                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT, fieldReader.method.getName(), DESC_METHOD, false);
                } else if (field != null) {
                    mw.visitFieldInsn(PUTFIELD, TYPE_OBJECT, field.getName(), DESC_FIELD_CLASS);
                } else {
                    return null;
                }
                mw.visitJumpInsn(Opcodes.GOTO, dflt);
            }

            mw.visitLabel(dflt);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        byte[] code = cw.toByteArray();

        try {
            Class<?> consumerClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);
            Constructor<?> constructor = consumerClass.getConstructor(Consumer.class);
            return (c) -> {
                try {
                    return constructor.newInstance(c);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException("create ByteArrayValueConsumer error", e);
                }
            };
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
