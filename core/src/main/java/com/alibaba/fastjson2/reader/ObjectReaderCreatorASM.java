package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.internal.asm.*;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.Serializable;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

import static com.alibaba.fastjson2.internal.asm.Opcodes.PUTFIELD;
import static com.alibaba.fastjson2.reader.ObjectReader.HASH_TYPE;

public class ObjectReaderCreatorASM
        extends ObjectReaderCreator {
    // GraalVM not support
    // Android not support
    public static ObjectReaderCreatorASM INSTANCE = new ObjectReaderCreatorASM();

    protected static final AtomicLong seed = new AtomicLong();

    protected final DynamicClassLoader classLoader;

    static final String TYPE_UNSAFE_UTILS = ASMUtils.type(UnsafeUtils.class);

    static final String TYPE_JSON_READER = ASMUtils.type(JSONReader.class);
    static final String TYPE_OBJECT_READER_ADAPTER = ASMUtils.type(ObjectReaderAdapter.class);
    static final String TYPE_OBJECT_READER = ASMUtils.type(ObjectReader.class);
    static final String TYPE_FIELD_READE = ASMUtils.type(FieldReader.class);

    static final String DESC_JSON_READER = ASMUtils.desc(JSONReader.class);
    static final String DESC_FIELD_READER = ASMUtils.desc(FieldReader.class);
    static final String DESC_OBJECT_READER = ASMUtils.desc(ObjectReader.class);
    static final String DESC_SUPPLIER = ASMUtils.desc(Supplier.class);
    static final String DESC_JSONSCHEMA = ASMUtils.desc(JSONSchema.class);
    static final String DESC_FIELD_READER_ARRAY = ASMUtils.desc(FieldReader[].class);
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

    static final String METHOD_DESC_JSON_READER_CHECK_ARRAY_AUTO_TYPE = "(" + DESC_JSON_READER + "Ljava/lang/Class;J)" + DESC_OBJECT_READER;

    static final int THIS = 0;

    static final String packageName;
    static Map<Class, FieldReaderInfo> infos = new HashMap<>();

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

    static String[] fieldReaderCache = new String[1024];
    static String[] fieldObjectReaderCache = new String[1024];
    static String[] fieldItemObjectReader = new String[1024];

    static String fieldReader(int i) {
        String fieldName = fieldReaderCache[i];

        if (fieldName != null) {
            return fieldName;
        }

        String base = "fr";
        int size = IOUtils.stringSize(i);
        char[] chars = new char[base.length() + size];
        base.getChars(0, base.length(), chars, 0);
        IOUtils.getChars(i, chars.length, chars);
        fieldReaderCache[i] = fieldName = new String(chars);
        return fieldName;
    }

    static String fieldObjectReader(int i) {
        String fieldName = fieldObjectReaderCache[i];

        if (fieldName != null) {
            return fieldName;
        }

        String base = "objectReader_";
        int size = IOUtils.stringSize(i);
        char[] chars = new char[base.length() + size];
        base.getChars(0, base.length(), chars, 0);
        IOUtils.getChars(i, chars.length, chars);
        fieldObjectReaderCache[i] = fieldName = new String(chars);
        return fieldName;
    }

    static String fieldItemObjectReader(int i) {
        String fieldName = fieldItemObjectReader[i];

        if (fieldName != null) {
            return fieldName;
        }

        String base = "itemObjectReader_";
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
        return super.createFieldReader(objectClass, objectType, fieldName, ordinal, features, format, locale, defaultValue, schema, fieldType, fieldClass, field, initReader);
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

    public ObjectReaderCreatorASM() {
        this.classLoader = new DynamicClassLoader();
    }

    public ObjectReaderCreatorASM(ClassLoader classLoader) {
        this.classLoader = new DynamicClassLoader(classLoader);
    }

    @Override
    public <T> ObjectReader<T> createObjectReader(Class<T> objectClass, Type objectType, boolean fieldBased, List<ObjectReaderModule> modules) {
        boolean externalClass = classLoader.isExternalClass(objectClass);
        int objectClassModifiers = objectClass.getModifiers();
        boolean publicClass = Modifier.isPublic(objectClassModifiers);

        if (Modifier.isAbstract(objectClassModifiers) || Modifier.isInterface(objectClassModifiers)) {
            return super.createObjectReader(objectClass, objectType, fieldBased, modules);
        }

        if (externalClass || !publicClass) {
            if (!fieldBased || !JDKUtils.UNSAFE_SUPPORT) {
                return super.createObjectReader(objectClass, objectType, fieldBased, modules);
            }
        }

        BeanInfo beanInfo = new BeanInfo();
        for (ObjectReaderModule module : modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getBeanInfo(beanInfo, objectClass);
            }
        }

        if (beanInfo.deserializer != null && ObjectReader.class.isAssignableFrom(beanInfo.deserializer)) {
            try {
                return (ObjectReader<T>) beanInfo.deserializer.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create deserializer error", e);
            }
        }

        if (fieldBased && (objectClass.isInterface() || objectClass.isInterface())) {
            fieldBased = false;
        }

        if (Enum.class.isAssignableFrom(objectClass)) {
            return createEnumReader(objectClass, beanInfo.createMethod, modules);
        }

        if (beanInfo.creatorConstructor != null || beanInfo.createMethod != null) {
            return createObjectReaderWithCreator(objectClass, modules, beanInfo);
        }

        if (beanInfo.builder != null) {
            return createObjectReaderWithBuilder(objectClass, modules, beanInfo);
        }

        if ((!fieldBased) && Throwable.class.isAssignableFrom(objectClass)) {
            return super.createObjectReader(objectClass, objectType, fieldBased, modules);
        }

        if (fieldBased && JDKUtils.JVM_VERSION >= 11 && Throwable.class.isAssignableFrom(objectClass)) {
            return super.createObjectReader(objectClass, objectType, fieldBased, modules);
        }

        FieldReader[] fieldReaderArray = createFieldReaders(objectClass, objectType, beanInfo, fieldBased, modules);
        Arrays.sort(fieldReaderArray);

        boolean match = true;

        if (!fieldBased) {
            if (JDKUtils.JVM_VERSION >= 9 && objectClass == StackTraceElement.class) {
                try {
                    Constructor<StackTraceElement> constructor = StackTraceElement.class.getConstructor(String.class, String.class, String.class, String.class, String.class, String.class, int.class);
                    return createObjectReaderNoneDefaultConstrutor(constructor, "", "classLoaderName", "moduleName", "moduleVersion", "declaringClass", "methodName", "fileName", "lineNumber");
                } catch (NoSuchMethodException | SecurityException ignored) {
                }
            }

            for (FieldReader fieldReader : fieldReaderArray) {
                Method method = fieldReader.getMethod();
                if (method != null && method.getReturnType() != void.class) {
                    match = false;
                    break;
                }

                if (fieldReader instanceof FieldReaderReadOnly
                        || fieldReader.isUnwrapped()
                ) {
                    match = false;
                    break;
                }

                if ((fieldReader.getFeatures() & FieldInfo.READ_USING_MASK) != 0) {
                    match = false;
                    break;
                }
            }
        }

        for (FieldReader fieldReader : fieldReaderArray) {
            if (fieldReader.getDefaultValue() != null || fieldReader.getSchema() != null) {
                match = false;
                break;
            }

            Class fieldClass = fieldReader.getFieldClass();
            if (!Modifier.isPublic(fieldClass.getModifiers())) {
                match = false;
                break;
            }
        }

        if (match && beanInfo.schema != null && !beanInfo.schema.isEmpty()) {
            match = false;
        }

        if (!match) {
            return super.createObjectReader(objectClass, objectType, fieldBased, modules);
        }

        Supplier<T> supplier = createInstanceSupplier(objectClass);

        if (beanInfo.seeAlso != null && beanInfo.seeAlso.length != 0) {
            return createObjectReaderSeeAlso(objectClass, supplier, beanInfo.typeKey, beanInfo.seeAlso, beanInfo.seeAlsoNames, fieldReaderArray);
        }

        Constructor defaultConstructor = null;
        try {
            Constructor constructor = objectClass.getDeclaredConstructor();
            int modifiers = constructor.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                supplier = null;
            } else {
                defaultConstructor = constructor;
            }
        } catch (NoSuchMethodException ignored) {
            supplier = null;
        }

        if (!fieldBased) {
            if (supplier == null) {
                return super.createObjectReader(objectClass, objectType, fieldBased, modules);
            }
        }

        ClassWriter cw = new ClassWriter();

        String className = "ObjectReader_" + seed.incrementAndGet();
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

        if (generatedFields) {
            genFields(fieldReaderArray, cw);
        }

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, classNameType, TYPE_OBJECT_READER_ADAPTER, new String[]{});

        {
            final int CLASS = 1, SUPPLIER = 2, FIELD_READER_ARRAY = 3;

            MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                    METHOD_DESC_INIT);
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
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT_READER_ADAPTER, "<init>", METHOD_DESC_ADAPTER_INIT, false);

            genInitFields(fieldReaderArray, classNameType, generatedFields, THIS, FIELD_READER_ARRAY, mw);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
            mw.visitEnd();
        }

        String TYPE_OBJECT = ASMUtils.type(objectClass);

        {
            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    fieldBased && defaultConstructor == null ? "createInstance0" : "createInstance",
                    "(J)Ljava/lang/Object;"
            );

            if (fieldBased && (defaultConstructor == null || !Modifier.isPublic(defaultConstructor.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))) {
                mw.visitFieldInsn(Opcodes.GETSTATIC, TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                mw.visitVarInsn(Opcodes.ALOAD, 0);
                mw.visitFieldInsn(Opcodes.GETFIELD, TYPE_OBJECT_READER_ADAPTER, "objectClass", "Ljava/lang/Class;");
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/misc/Unsafe", "allocateInstance", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
            } else {
                mw.visitTypeInsn(Opcodes.NEW, TYPE_OBJECT);
                mw.visitInsn(Opcodes.DUP);
                mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT, "<init>", "()V", false);
            }

            mw.visitInsn(Opcodes.ARETURN);
            mw.visitMaxs(3, 3);
            mw.visitEnd();
        }

        if (generatedFields) {
            long readerFeatures = beanInfo.readerFeatures;
            if (fieldBased) {
                readerFeatures |= JSONReader.Feature.FieldBased.mask;
            }

            ObjectReaderAdapter objectReaderAdapter = new ObjectReaderAdapter(objectClass, beanInfo.typeKey, beanInfo.typeName, readerFeatures, null, supplier, null, fieldReaderArray);

            genMethodReadJSONBObject(objectClass, readerFeatures, TYPE_OBJECT, fieldReaderArray, cw, classNameType, objectReaderAdapter);
            genMethodReadJSONBObjectArrayMapping(objectClass, readerFeatures, TYPE_OBJECT, fieldReaderArray, cw, classNameType, objectReaderAdapter);

            genMethodReadObject(objectClass, readerFeatures, TYPE_OBJECT, supplier, fieldReaderArray, cw, classNameType, objectReaderAdapter);

            genMethodGetFieldReader(fieldReaderArray, cw, classNameType, objectReaderAdapter);
            genMethodGetFieldReaderLCase(fieldReaderArray, cw, classNameType, objectReaderAdapter);
        }

        byte[] code = cw.toByteArray();

        Class<?> readerClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);

        try {
            Constructor<?> constructor = readerClass.getConstructors()[0];
            return (ObjectReader) constructor
                    .newInstance(objectClass, null, fieldReaderArray);
        } catch (Throwable e) {
            throw new JSONException("create objectReader error, objectType " + objectType.getTypeName(), e);
        }
    }

    private void genMethodGetFieldReader(
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "getFieldReader",
                "(J)" + DESC_FIELD_READER
        );

        final int HASH_CODE_64 = 1;
        final int HASH_CODE_32 = 3;

        Label rtnlt = new Label();
        if (fieldReaderArray.length <= 6) {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();
                String fieldName = fieldReaderArray[i].getFieldName();
                long hashCode64 = Fnv.hashCode64(fieldName);

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

            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.ARETURN);
        } else {
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodes.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodes[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.get(hashCode32);
                if (hashCode64List == null) {
                    hashCode64List = new ArrayList<>();
                    map.put(hashCode32, hashCode64List);
                }
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
                for (int j = 0; j < hashCode64Array.size(); ++j) {
                    long hashCode64 = hashCode64Array.get(j);
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
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.ARETURN);
        }

        mw.visitLabel(rtnlt);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 5);
        mw.visitEnd();
    }

    private void genMethodGetFieldReaderLCase(
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "getFieldReaderLCase",
                "(J)" + DESC_FIELD_READER
        );

        final int HASH_CODE_64 = 1;
        final int HASH_CODE_32 = 3;

        Label rtnlt = new Label();
        if (fieldReaderArray.length <= 6) {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();
                String fieldName = fieldReaderArray[i].getFieldName();
                long hashCode64 = Fnv.hashCode64LCase(fieldName);

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

            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.ARETURN);
        } else {
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodesLCase.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodesLCase[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.get(hashCode32);
                if (hashCode64List == null) {
                    hashCode64List = new ArrayList<>();
                    map.put(hashCode32, hashCode64List);
                }
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
                for (int j = 0; j < hashCode64Array.size(); ++j) {
                    long hashCode64 = hashCode64Array.get(j);
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
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.ARETURN);
        }

        mw.visitLabel(rtnlt);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 5);
        mw.visitEnd();
    }

    private void genInitFields(
            FieldReader[] fieldReaderArray,
            String classNameType,
            boolean generatedFields,
            int THIS,
            int FIELD_READER_ARRAY,
            MethodWriter mw) {
        if (!generatedFields) {
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

    private void genFields(FieldReader[] fieldReaderArray, ClassWriter cw) {
        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldWriter fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldReader(i), DESC_FIELD_READER);
            fv.visitEnd();
        }

        for (int i = 0; i < fieldReaderArray.length; i++) {
            FieldWriter fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldObjectReader(i), DESC_OBJECT_READER);
            fv.visitEnd();
        }

        for (int i = 0; i < fieldReaderArray.length; i++) {
            Class fieldClass = fieldReaderArray[i].getFieldClass();
            if (List.class.isAssignableFrom(fieldClass)) {
                FieldWriter fv = cw.visitField(Opcodes.ACC_PUBLIC, fieldItemObjectReader(i), DESC_OBJECT_READER);
                fv.visitEnd();
            }
        }
    }

    private <T> void genMethodReadJSONBObject(
            Class<T> objectType,
            long readerFeatures,
            String TYPE_OBJECT,
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter
    ) {
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "readJSONBObject",
                METHOD_DESC_READ_OBJECT
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

        if (!Serializable.class.isAssignableFrom(objectType)) {
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

            genCreateObject(mw, classNameType, TYPE_OBJECT, FEATURES, fieldBased);
            mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

            for (int i = 0; i < fieldReaderArray.length; ++i) {
                FieldReader fieldReader = fieldReaderArray[i];
                varIndex = genReadFieldValue(objectType,
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

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitInsn(Opcodes.ARETURN);

            mw.visitLabel(endArray_);
        }

        mw.visitLabel(object_);

        genCreateObject(mw, classNameType, TYPE_OBJECT, FEATURES, fieldBased);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfObjectStart", "()Z", false);
        mw.visitInsn(Opcodes.POP);

        genCreateObject(mw, classNameType, TYPE_OBJECT, FEATURES, fieldBased);
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

        if (fieldReaderArray.length <= 6) {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                long hashCode64 = Fnv.hashCode64(fieldReader.getFieldName());
                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                varIndex = genReadFieldValue(objectType,
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

            Label noneSmartMatch_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, noneSmartMatch_);
            }

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "getNameHashCodeLCase", "()J", false);
            mw.visitVarInsn(Opcodes.LSTORE, HASH_CODE64);

            mw.visitLabel(noneSmartMatch_);

            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                long hashCode64 = Fnv.hashCode64(fieldReader.getFieldName());
                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                varIndex = genReadFieldValue(objectType,
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

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "skipValue", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue
        } else {
            // use switch
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodes.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodes[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.get(hashCode32);
                if (hashCode64List == null) {
                    hashCode64List = new ArrayList<>();
                    map.put(hashCode32, hashCode64List);
                }
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
                for (int j = 0; j < hashCode64Array.size(); ++j) {
                    long hashCode64 = hashCode64Array.get(j);
                    mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                    mw.visitLdcInsn(hashCode64);
                    mw.visitInsn(Opcodes.LCMP);
                    mw.visitJumpInsn(Opcodes.IFNE, dflt);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodes, hashCode64);
                    int index = objectReaderAdapter.mapping[m];

                    FieldReader fieldReader = fieldReaderArray[index];

                    varIndex = genReadFieldValue(objectType,
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
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "readFieldValueJSONB", METHOD_DESC_READ_FIELD_VALUE, true);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

            mw.visitLabel(fieldReaderNull_);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "skipValue", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue
        }

        mw.visitLabel(for_inc_i_);
        mw.visitIincInsn(I, 1);
        mw.visitJumpInsn(Opcodes.GOTO, for_start_i_);

        mw.visitLabel(for_end_i_);

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 10);
        mw.visitEnd();
    }

    private <T> void genMethodReadJSONBObjectArrayMapping(
            Class<T> objectType,
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
                METHOD_DESC_READ_OBJECT
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

        genCreateObject(mw, classNameType, TYPE_OBJECT, FEATURES, fieldBased);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        for (int i = 0; i < fieldReaderArray.length; ++i) {
            FieldReader fieldReader = fieldReaderArray[i];
            varIndex = genReadFieldValue(objectType,
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

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitInsn(Opcodes.ARETURN);

        mw.visitMaxs(5, 10);
        mw.visitEnd();
    }

    private void genCheckAutoType(
            String classNameType,
            MethodWriter mw,
            int JSON_READER,
            int FIELD_TYPE,
            int FIELD_NAME,
            int FEATURES,
            int AUTO_TYPE_OBJECT_READER) {
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
            Class<T> objectType,
            long readerFeatures,
            String TYPE_OBJECT,
            Supplier<T> supplier,
            FieldReader[] fieldReaderArray,
            ClassWriter cw,
            String classNameType,
            ObjectReaderAdapter objectReaderAdapter
    ) {
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "readObject",
                METHOD_DESC_READ_OBJECT
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

        genCreateObject(mw, classNameType, TYPE_OBJECT, FEATURES, fieldBased);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);

        for (int i = 0; i < fieldReaderArray.length; ++i) {
            FieldReader fieldReader = fieldReaderArray[i];

            varIndex = genReadFieldValue(
                    objectType,
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
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfNull", "()Z", false);
        mw.visitJumpInsn(Opcodes.IFEQ, notNull_);

        mw.visitInsn(Opcodes.ACONST_NULL);
        mw.visitVarInsn(Opcodes.ASTORE, OBJECT);
        mw.visitJumpInsn(Opcodes.GOTO, end_);

        mw.visitLabel(notNull_);

        genCreateObject(mw, classNameType, TYPE_OBJECT, FEATURES, fieldBased);
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
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportAutoType", "(J)Z", false);
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

        if (fieldReaderArray.length <= 6) {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                String fieldName = fieldReader.getFieldName();
                long hashCode64 = Fnv.hashCode64(fieldName);

                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                mw.visitLabel(get_);
                varIndex = genReadFieldValue(objectType,
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

            Label noneSmartMatch_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, noneSmartMatch_);
            }

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "getNameHashCodeLCase", "()J", false);
            mw.visitVarInsn(Opcodes.LSTORE, HASH_CODE64);

            mw.visitLabel(noneSmartMatch_);

            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                String fieldName = fieldReader.getFieldName();
                long hashCode64 = Fnv.hashCode64(fieldName);
                long hashCode64LCase = Fnv.hashCode64LCase(fieldName);

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
                varIndex = genReadFieldValue(objectType,
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

            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "skipValue", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue
        } else {
            // use switch
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < objectReaderAdapter.hashCodes.length; i++) {
                long hashCode64 = objectReaderAdapter.hashCodes[i];
                int hashCode32 = (int) (hashCode64 ^ (hashCode64 >>> 32));
                List<Long> hashCode64List = map.get(hashCode32);
                if (hashCode64List == null) {
                    hashCode64List = new ArrayList<>();
                    map.put(hashCode32, hashCode64List);
                }
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
                for (int j = 0; j < hashCode64Array.size(); ++j) {
                    long hashCode64 = hashCode64Array.get(j);
                    mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE64);
                    mw.visitLdcInsn(hashCode64);
                    mw.visitInsn(Opcodes.LCMP);
                    mw.visitJumpInsn(Opcodes.IFNE, dflt);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodes, hashCode64);
                    int index = objectReaderAdapter.mapping[m];

                    FieldReader fieldReader = fieldReaderArray[index];

                    varIndex = genReadFieldValue(objectType,
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
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "readFieldValue", METHOD_DESC_READ_FIELD_VALUE, true);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue

            mw.visitLabel(fieldReaderNull_);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "skipValue", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_i_); // continue
        }

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
        mw.visitEnd();
    }

    private <T> void genCreateObject(MethodWriter mw, String classNameType, String TYPE_OBJECT, int FEATURES, boolean fieldBased) {
        final int JSON_READER = 1;

        if (fieldBased) {
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
            mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "features", "(J)J", false);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "createInstance", "(J)Ljava/lang/Object;", false);
        } else {
            mw.visitTypeInsn(Opcodes.NEW, TYPE_OBJECT);
            mw.visitInsn(Opcodes.DUP);
            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT, "<init>", "()V", false);
        }
    }

    private <T> int genReadFieldValue(
            Class<T> objectType,
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
        Class fieldClass = fieldReader.getFieldClass();
        Type fieldType = fieldReader.getFieldType();
        long fieldFeatures = fieldReader.getFeatures();
        String format = fieldReader.getFormat();
        Type itemType = fieldReader.getItemType();

        Field field = fieldReader.getField();
        Method method = fieldReader.getMethod();

        Label endSet_ = new Label();

        String TYPE_FIELD_CLASS = ASMUtils.type(fieldClass);
        String DESC_FIELD_CLASS = ASMUtils.desc(fieldClass);

        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        int fieldModifers = fieldBased || (method == null && field != null) ? field.getModifiers() : 0;
        if (fieldBased
                && Modifier.isPublic(objectType.getModifiers())
                && Modifier.isPublic(fieldModifers)
                && !Modifier.isFinal(fieldModifers)
                && !classLoader.isExternalClass(objectType)
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

                if (fieldClass.isAssignableFrom(objectType)) {
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
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "addResolveTask", METHOD_DESC_ADD_RESOLVE_TASK, true);
                mw.visitInsn(Opcodes.POP);
                mw.visitJumpInsn(Opcodes.GOTO, endSet_);

                mw.visitLabel(endReference_);
            }

            if (!fieldReader.isFieldClassSerializable()) {
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

                boolean initCapacity = JDKUtils.JVM_VERSION == 8 && "java/util/ArrayList".equals(LIST_TYPE);

                if (jsonb) {
                    Label checkAutoTypeNull_ = new Label();

                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "checkObjectAutoType", METHOD_DESC_CHECK_ARRAY_AUTO_TYPE, true);
                    mw.visitInsn(Opcodes.DUP);
                    mw.visitVarInsn(Opcodes.ASTORE, AUTO_TYPE_OBJECT_READER);
                    mw.visitJumpInsn(Opcodes.IFNULL, checkAutoTypeNull_);

                    mw.visitVarInsn(Opcodes.ALOAD, AUTO_TYPE_OBJECT_READER);
                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                    mw.visitLdcInsn(fieldReader.getFieldName());
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

                        mw.visitVarInsn(Opcodes.ALOAD, LIST);
                        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "readString", "()Ljava/lang/String;", false);
                        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                        mw.visitInsn(Opcodes.POP);

                        mw.visitJumpInsn(Opcodes.GOTO, loadList_);
                    } else if (itemType instanceof Class) {
                        mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_READER, "nextIfEmptyString", "()Z", false);
                        mw.visitJumpInsn(Opcodes.IFNE, loadNull_);

                        // nextIfEmptyString
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
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                    mw.visitInsn(Opcodes.POP);
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
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "getItemObjectReader", METHOD_DESC_GET_ITEM_OBJECT_READER, true);
//
//                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,  TYPE_FIELD_READE, "getItemType", "()Ljava/lang/reflect/Type;", true);
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

                    if (fieldClass.isAssignableFrom(objectType)) {
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
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "addResolveTask", METHOD_DESC_ADD_RESOLVE_TASK_2, true);
                    mw.visitJumpInsn(Opcodes.GOTO, for_inc_j_);

                    mw.visitLabel(endReference_);

                    mw.visitVarInsn(Opcodes.ALOAD, LIST);
                    mw.visitVarInsn(Opcodes.ALOAD, THIS);
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);

                    mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                    gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                    mw.visitLdcInsn(fieldReader.getFieldName());
                    mw.visitVarInsn(Opcodes.LLOAD, FEATURES);
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                            TYPE_OBJECT_READER,
                            jsonb ? "readJSONBObject" : "readObject",
                            METHOD_DESC_READ_OBJECT,
                            true);
                    mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
                    mw.visitInsn(Opcodes.POP);
                }

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

                // object.<setMethod>(this.objectReader_<i>.readObject(jsonReader))
                Label notNull_ = new Label();

                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);
                mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);

                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "getObjectReader", METHOD_DESC_GET_OBJECT_READER_1, true);
                mw.visitFieldInsn(PUTFIELD, classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);

                mw.visitLabel(notNull_);
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);

                mw.visitVarInsn(Opcodes.ALOAD, JSON_READER);
                gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                mw.visitLdcInsn(fieldReader.getFieldName());
                mw.visitLdcInsn(fieldFeatures);
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                        TYPE_OBJECT_READER,
                        jsonb ? "readJSONBObject" : "readObject",
                        METHOD_DESC_READ_OBJECT,
                        true);

                if (method != null || (Modifier.isPublic(objectType.getModifiers())
                        && Modifier.isPublic(fieldModifers)
                        && !Modifier.isFinal(fieldModifers)
                        && !classLoader.isExternalClass(objectType))
                ) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_FIELD_CLASS); // cast
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
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    TYPE_OBJECT,
                    method.getName(),
                    "(" + DESC_FIELD_CLASS + ")V", false);
            // TODO BUILD METHOD
        } else if (field != null) {
            if (Modifier.isPublic(objectType.getModifiers())
                    && Modifier.isPublic(fieldModifers)
                    && !Modifier.isFinal(fieldModifers)
                    && ObjectWriterProvider.isPrimitiveOrEnum(fieldClass)
                    && !classLoader.isExternalClass(objectType)
                    && field.getDeclaringClass() == objectType
            ) {
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
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_READE, "getFieldType", "()Ljava/lang/reflect/Type;", true);
    }

    void genIsEnabled(MethodWriter mw, int var, long features, Label elseLabel) {
        mw.visitVarInsn(Opcodes.LLOAD, var);
        mw.visitLdcInsn(features);
        mw.visitInsn(Opcodes.LAND);
        mw.visitInsn(Opcodes.LCONST_0);
        mw.visitInsn(Opcodes.LCMP);
        mw.visitJumpInsn(Opcodes.IFEQ, elseLabel);
    }
}
