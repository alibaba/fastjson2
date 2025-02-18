package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.internal.asm.*;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.*;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.Serializable;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

import static com.alibaba.fastjson2.internal.CodeGenUtils.fieldReader;
import static com.alibaba.fastjson2.internal.asm.ASMUtils.*;
import static com.alibaba.fastjson2.internal.asm.Opcodes.*;
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
    static final String GET_FIELD_READER_UL = "(J" + DESC_JSON_READER + "J)" + DESC_FIELD_READER;
    static final String READ_FIELD_READER_UL = "(J" + DESC_JSON_READER + "JLjava/lang/Object;)V";
    static final String METHOD_DESC_ADD_RESOLVE_TASK = "(" + DESC_JSON_READER + "Ljava/lang/Object;Ljava/lang/String;)V";
    static final String METHOD_DESC_ADD_RESOLVE_TASK_2 = "(" + DESC_JSON_READER + "Ljava/util/List;ILjava/lang/String;)V";
    static final String METHOD_DESC_CHECK_ARRAY_AUTO_TYPE = "(" + DESC_JSON_READER + ")" + DESC_OBJECT_READER;
    static final String METHOD_DESC_PROCESS_EXTRA = "(" + DESC_JSON_READER + "Ljava/lang/Object;J)V";

    static final String METHOD_DESC_JSON_READER_CHECK_ARRAY_AUTO_TYPE = "(" + DESC_JSON_READER + "J)" + DESC_OBJECT_READER;
    static final String METHOD_DESC_READ_ARRAY_MAPPING_JSONB_OBJECT0 = "(" + DESC_JSON_READER + "Ljava/lang/Object;I)V";

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
        boolean externalClass = objectClass != null && classLoader.isExternalClass(objectClass);
        int objectClassModifiers = objectClass.getModifiers();

        if (Modifier.isAbstract(objectClassModifiers) || Modifier.isInterface(objectClassModifiers)) {
            return super.createObjectReader(objectClass, objectType, fieldBased, provider);
        }

        BeanInfo beanInfo = new BeanInfo(provider);
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

        if (fieldBased && (objectClass.isInterface() || BeanUtils.isRecord(objectClass))) {
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

        if (objectClass == Class.class) {
            return ObjectReaderImplClass.INSTANCE;
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
                if (fieldClass != null && !Modifier.isPublic(fieldClass.getModifiers())) {
                    match = false;
                    break;
                }

                if (fieldReader instanceof FieldReaderMapField
                        && ((FieldReaderMapField<?>) fieldReader).arrayToMapKey != null) {
                    match = false;
                    break;
                }

                if (fieldReader instanceof FieldReaderMapMethod
                        && ((FieldReaderMapMethod<?>) fieldReader).arrayToMapKey != null) {
                    match = false;
                    break;
                }
            }
        }

        if (match
                && (beanInfo.rootName != null
                || (beanInfo.schema != null && !beanInfo.schema.isEmpty()))) {
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
                null,
                fieldReaderArray,
                defaultConstructor
        );
    }

    @Override
    public <T> ObjectReader<T> createObjectReader(
            Class<T> objectClass,
            String typeKey,
            String rootName,
            long features,
            JSONSchema schema,
            Supplier<T> defaultCreator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        if (objectClass == null && defaultCreator != null && buildFunction == null) {
            boolean allFunction = true;
            for (int i = 0; i < fieldReaders.length; i++) {
                FieldReader fieldReader = fieldReaders[i];
                if (fieldReader.getFunction() == null) {
                    allFunction = false;
                    break;
                }
            }

            if (allFunction) {
                BeanInfo beanInfo = new BeanInfo(JSONFactory.getDefaultObjectReaderProvider());
                return jitObjectReader(
                        objectClass,
                        objectClass,
                        false,
                        false,
                        0,
                        beanInfo,
                        defaultCreator,
                        fieldReaders,
                        null
                );
            }
        }

        return super.createObjectReader(
                objectClass,
                typeKey,
                rootName,
                features,
                schema,
                defaultCreator,
                buildFunction,
                fieldReaders
        );
    }

    private <T> ObjectReaderBean jitObjectReader(
            Class<T> objectClass,
            Type objectType,
            boolean fieldBased,
            boolean externalClass,
            int objectClassModifiers,
            BeanInfo beanInfo,
            Supplier<T> defaultCreator,
            FieldReader[] fieldReaderArray,
            Constructor defaultConstructor
    ) {
        ClassWriter cw = new ClassWriter(
                (e) -> objectClass.getName().equals(e) ? objectClass : null
        );

        ObjectWriteContext context = new ObjectWriteContext(beanInfo, objectClass, cw, externalClass, fieldReaderArray);

        String className = "ORG_" + seed.incrementAndGet() + "_" + fieldReaderArray.length + (objectClass == null ? "" : "_" + objectClass.getSimpleName());
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
            mw.aload(THIS);
            mw.aload(CLASS);
            if (beanInfo.typeKey != null) {
                mw.visitLdcInsn(beanInfo.typeKey);
            } else {
                mw.aconst_null();
            }
            mw.aconst_null();
            mw.visitLdcInsn(beanInfo.readerFeatures);
            mw.aconst_null();
            mw.aload(SUPPLIER);
            mw.aconst_null();
            mw.aload(FIELD_READER_ARRAY);
            mw.invokespecial(objectReaderSuper, "<init>", METHOD_DESC_ADAPTER_INIT);

            genInitFields(fieldReaderArray, classNameType, generatedFields, THIS, FIELD_READER_ARRAY, mw, objectReaderSuper);

            mw.return_();
            mw.visitMaxs(3, 3);
        }

        String TYPE_OBJECT = objectClass == null ? ASMUtils.TYPE_OBJECT : ASMUtils.type(objectClass);

        {
            String methodName = fieldBased && defaultConstructor == null ? "createInstance0" : "createInstance";

            if (fieldBased && (defaultConstructor == null || !Modifier.isPublic(defaultConstructor.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))) {
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(J)Ljava/lang/Object;",
                        32
                );
                mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                mw.aload(0);
                mw.getfield(TYPE_OBJECT_READER_ADAPTER, "objectClass", "Ljava/lang/Class;");
                mw.invokevirtual("sun/misc/Unsafe", "allocateInstance", "(Ljava/lang/Class;)Ljava/lang/Object;");
                mw.areturn();
                mw.visitMaxs(3, 3);
            } else if (defaultConstructor != null && Modifier.isPublic(defaultConstructor.getModifiers()) && Modifier.isPublic(objectClass.getModifiers())) {
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        methodName,
                        "(J)Ljava/lang/Object;",
                        32
                );
                newObject(mw, TYPE_OBJECT, defaultConstructor);
                mw.areturn();
                mw.visitMaxs(3, 3);
            }
        }

        Supplier<T> supplier;
        if (defaultConstructor != null) {
            boolean publicObject = Modifier.isPublic(objectClassModifiers) && !classLoader.isExternalClass(objectClass);
            boolean jit = !publicObject || !Modifier.isPublic(defaultConstructor.getModifiers());
            supplier = createSupplier(defaultConstructor, jit);
        } else {
            supplier = defaultCreator;
        }

        if (generatedFields) {
            long readerFeatures = beanInfo.readerFeatures;
            if (fieldBased) {
                readerFeatures |= JSONReader.Feature.FieldBased.mask;
            }

            boolean disableArrayMapping = context.disableSupportArrayMapping();
            boolean disableJSONB = context.disableJSONB();

            ObjectReaderAdapter objectReaderAdapter = new ObjectReaderAdapter(objectClass, beanInfo.typeKey, beanInfo.typeName, readerFeatures, null, supplier, null, fieldReaderArray);

            if (!disableJSONB) {
                genMethodReadJSONBObject(context, defaultConstructor, readerFeatures, TYPE_OBJECT, fieldReaderArray, cw, classNameType, objectReaderAdapter);
                if (!disableArrayMapping) {
                    genMethodReadJSONBObjectArrayMapping(context, defaultConstructor, readerFeatures, TYPE_OBJECT, fieldReaderArray, cw, classNameType, objectReaderAdapter);
                }
            }

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
            throw new JSONException(
                    "create objectReader error"
                            + (objectType == null ? "" : ", objectType " + objectType.getTypeName()),
                    e
            );
        }
    }

    private static void newObject(MethodWriter mw, String TYPE_OBJECT, Constructor defaultConstructor) {
        mw.new_(TYPE_OBJECT);
        mw.dup();
        if (defaultConstructor.getParameterCount() == 0) {
            mw.invokespecial(TYPE_OBJECT, "<init>", "()V");
        } else {
            Class paramType = defaultConstructor.getParameterTypes()[0];
            mw.aconst_null();
            mw.invokespecial(TYPE_OBJECT, "<init>", "(" + ASMUtils.desc(paramType) + ")V");
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
            mw.lload(HASH_CODE_64);
            mw.lload(HASH_CODE_64);
            mw.bipush(32);
            mw.lushr();
            mw.lxor();
            mw.l2i();
            mw.istore(HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.iload(HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (int j = 0, size = hashCode64Array.size(); j < size; j++) {
                    long hashCode64 = hashCode64Array.get(j);

                    Label next = size > 1 ? new Label() : dflt;
                    mw.lload(HASH_CODE_64);
                    mw.visitLdcInsn(hashCode64);
                    mw.lcmp();
                    mw.ifne(next);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodes, hashCode64);
                    int index = objectReaderAdapter.mapping[m];
                    mw.aload(THIS);
                    mw.getfield(classNameType, fieldReader(index), DESC_FIELD_READER);
                    mw.goto_(rtnlt);

                    if (next != dflt) {
                        mw.visitLabel(next);
                    }
                }

                mw.goto_(dflt);
            }

            mw.visitLabel(dflt);
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();
                String fieldName = fieldReaderArray[i].fieldName;
                long hashCode64 = fieldReaderArray[i].fieldNameHash;

                mw.lload(HASH_CODE_64);
                mw.visitLdcInsn(hashCode64);
                mw.lcmp();
                mw.ifne(next_);

                mw.visitLabel(get_);
                mw.aload(THIS);
                mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.goto_(rtnlt);

                mw.visitLabel(next_);
            }
        }
        mw.aconst_null();
        mw.areturn();

        mw.visitLabel(rtnlt);
        mw.areturn();

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
            mw.lload(HASH_CODE_64);
            mw.lload(HASH_CODE_64);
            mw.bipush(32);
            mw.lushr();
            mw.lxor();
            mw.l2i();
            mw.istore(HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.iload(HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (long hashCode64 : hashCode64Array) {
                    mw.lload(HASH_CODE_64);
                    mw.visitLdcInsn(hashCode64);
                    mw.lcmp();
                    mw.ifne(dflt);

                    int m = Arrays.binarySearch(objectReaderAdapter.hashCodesLCase, hashCode64);
                    int index = objectReaderAdapter.mappingLCase[m];
                    mw.aload(THIS);
                    mw.getfield(classNameType, fieldReader(index), DESC_FIELD_READER);
                    mw.goto_(rtnlt);
                }

                mw.goto_(dflt);
            }

            mw.visitLabel(dflt);
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();
                String fieldName = fieldReaderArray[i].fieldName;
                long hashCode64 = fieldReaderArray[i].fieldNameHashLCase;

                mw.lload(HASH_CODE_64);
                mw.visitLdcInsn(hashCode64);
                mw.lcmp();
                mw.ifne(next_);

                mw.visitLabel(get_);
                mw.aload(THIS);
                mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.goto_(rtnlt);

                mw.visitLabel(next_);
            }
        }
        mw.aconst_null();
        mw.areturn();

        mw.visitLabel(rtnlt);
        mw.areturn();

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
            mw.aload(THIS);
            mw.aload(FIELD_READER_ARRAY);
            switch (i) {
                case 0:
                    mw.iconst_0();
                    break;
                case 1:
                    mw.iconst_1();
                    break;
                case 2:
                    mw.iconst_2();
                    break;
                case 3:
                    mw.iconst_3();
                    break;
                case 4:
                    mw.iconst_4();
                    break;
                case 5:
                    mw.iconst_5();
                    break;
                default:
                    if (i >= 128) {
                        mw.sipush(i);
                    } else {
                        mw.bipush(i);
                    }
                    break;
            }
            mw.aaload();
            mw.putfield(classNameType, fieldReader(i), DESC_FIELD_READER);
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

        boolean disableArrayMapping = context.disableSupportArrayMapping();
        boolean disableAutoType = context.disableAutoType();

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

        if (!disableAutoType) {
            genCheckAutoType(classNameType, mw, JSON_READER, FIELD_TYPE, FIELD_NAME, FEATURES, AUTO_TYPE_OBJECT_READER);
        }

        int varIndex = 16;
        Map<Object, Integer> variants = new HashMap<>();

        {
            Label notNull_ = new Label();
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfNull", "()Z");
            mw.ifeq(notNull_);
            mw.aconst_null();
            mw.areturn();
            mw.visitLabel(notNull_);
        }

        if (objectClass != null && !Serializable.class.isAssignableFrom(objectClass)) {
            mw.aload(JSON_READER);
            mw.aload(THIS);
            mw.getfield(classNameType, "objectClass", "Ljava/lang/Class;");
            mw.invokevirtual(TYPE_JSON_READER, "errorOnNoneSerializable", "(Ljava/lang/Class;)V");
        }

        if (!disableArrayMapping) {
            Label object_ = new Label();

            // if (jsonReader.isArray() && jsonReader.isSupportBeanArray()) {
            {
                Label startArray_ = new Label(), endArray_ = new Label();
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isArray", "()Z");
                mw.ifeq(object_);

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isSupportBeanArray", "()Z");
                mw.ifeq(endArray_);

                genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
                mw.astore(OBJECT);

                Label fieldEnd_ = new Label(), entryCountMatch_ = new Label();

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "startArray", "()I");
                mw.dup();
                mw.istore(ENTRY_CNT);
                mw.visitLdcInsn(fieldReaderArray.length);
                mw.if_icmpne(entryCountMatch_);

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
                            true,   // JSONB
                            true, // arrayMapping
                            TYPE_OBJECT
                    );
                }

                mw.goto_(fieldEnd_);

                mw.visitLabel(entryCountMatch_);
                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.aload(OBJECT);
                mw.iload(ENTRY_CNT);
                mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "readArrayMappingJSONBObject0", METHOD_DESC_READ_ARRAY_MAPPING_JSONB_OBJECT0);

                mw.visitLabel(fieldEnd_);

                mw.aload(OBJECT);
                mw.areturn();

                mw.visitLabel(endArray_);
            }

            mw.visitLabel(object_);
        }

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.astore(OBJECT);

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectStart", "()Z");
        mw.pop();

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.astore(OBJECT);

        // for (int i = 0; i < entry_cnt; ++i) {
        Label for_start_i_ = new Label(), for_end_i_ = new Label(), for_inc_i_ = new Label();
        if (!disableAutoType) {
            mw.iconst_0();
            mw.istore(I);
        }

        mw.visitLabel(for_start_i_);

        Label hashCode64Start = new Label();

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectEnd", "()Z");
        mw.ifne(for_end_i_);

        if (context.fieldNameLengthMin >= 2 && context.fieldNameLengthMax <= 43) {
            varIndex = genRead243(
                    context,
                    TYPE_OBJECT,
                    fieldReaderArray,
                    classNameType,
                    fieldBased,
                    mw,
                    JSON_READER,
                    FEATURES,
                    OBJECT,
                    ITEM_CNT,
                    J,
                    varIndex,
                    variants,
                    for_inc_i_,
                    hashCode64Start,
                    true
            );
        }

        mw.visitLabel(hashCode64Start);

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "readFieldNameHashCode", "()J");
        mw.dup2();
        mw.lstore(HASH_CODE64);
        mw.lconst_0();
        mw.lcmp();
        mw.ifeq(for_inc_i_);

        if (!disableAutoType) {
            Label endAutoType_ = new Label();
            mw.lload(HASH_CODE64);
            mw.aload(THIS);
            mw.getfield(classNameType, "typeKeyHashCode", "J");
            mw.lcmp();
            mw.ifne(endAutoType_);

            mw.lload(HASH_CODE64);
            mw.lconst_0();
            mw.lcmp();
            mw.ifeq(endAutoType_);

            // protected T autoType(JSONReader jsonReader, int entryCnt) {
            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.invokevirtual(classNameType, "autoType", "(" + DESC_JSON_READER + ")Ljava/lang/Object;");
            mw.astore(OBJECT);
            mw.goto_(for_end_i_);

            mw.visitLabel(endAutoType_);
        }

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
            mw.lload(HASH_CODE64);
            mw.lload(HASH_CODE64);
            mw.bipush(32);
            mw.lushr();
            mw.lxor();
            mw.l2i();
            mw.istore(HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.iload(HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (int j = 0, size = hashCode64Array.size(); j < size; j++) {
                    long hashCode64 = hashCode64Array.get(j);

                    Label next = size > 1 ? new Label() : dflt;

                    mw.lload(HASH_CODE64);
                    mw.visitLdcInsn(hashCode64);
                    mw.lcmp();
                    mw.ifne(next);

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
                            false, // arrayMapping
                            TYPE_OBJECT
                    );
                    mw.goto_(for_inc_i_);

                    if (next != dflt) {
                        mw.visitLabel(next);
                    }
                }

                mw.goto_(for_inc_i_);
            }

            // switch_default
            mw.visitLabel(dflt);

            Label fieldReaderNull_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.aload(JSON_READER);
                mw.lload(FEATURES);
                mw.invokevirtual(TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z");
                mw.ifeq(fieldReaderNull_);
            }

            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "getNameHashCodeLCase", "()J");
            mw.invokeinterface(TYPE_OBJECT_READER, "getFieldReaderLCase", METHOD_DESC_GET_FIELD_READER);
            mw.dup();
            mw.astore(FIELD_READER);
            mw.ifnull(fieldReaderNull_);

            mw.aload(FIELD_READER);
            mw.aload(JSON_READER);
            mw.aload(OBJECT);
            mw.invokevirtual(TYPE_FIELD_READE, "readFieldValueJSONB", METHOD_DESC_READ_FIELD_VALUE);
            mw.goto_(for_inc_i_); // continue

            mw.visitLabel(fieldReaderNull_);
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                long hashCode64 = Fnv.hashCode64(fieldReader.fieldName);
                mw.lload(HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.lcmp();
                mw.ifne(next_);

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
                        false, // arrayMapping
                        TYPE_OBJECT
                );

                mw.goto_(for_inc_i_); // continue

                mw.visitLabel(next_);
            }

            Label processExtra_ = new Label();

            if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                mw.aload(JSON_READER);
                mw.lload(FEATURES);
                mw.invokevirtual(TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z");
                mw.ifeq(processExtra_);
            }

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "getNameHashCodeLCase", "()J");
            mw.lstore(HASH_CODE64);

            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                long hashCode64 = Fnv.hashCode64(fieldReader.fieldName);
                mw.lload(HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.lcmp();
                mw.ifne(next_);

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
                        false, // arrayMapping
                        TYPE_OBJECT
                );

                mw.goto_(for_inc_i_); // continue

                mw.visitLabel(next_);
            }
            mw.visitLabel(processExtra_);
        }

        mw.aload(THIS);
        mw.aload(JSON_READER);
        mw.aload(OBJECT);
        mw.lload(FEATURES);
        mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "processExtra", METHOD_DESC_PROCESS_EXTRA);
        mw.goto_(for_inc_i_); // continue

        mw.visitLabel(for_inc_i_);
        if (!disableAutoType) {
            mw.visitIincInsn(I, 1);
        }
        mw.goto_(for_start_i_);

        mw.visitLabel(for_end_i_);

        mw.aload(OBJECT);
        mw.areturn();

        mw.visitMaxs(5, 10);
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

        if (!context.disableAutoType()) {
            genCheckAutoType(classNameType, mw, JSON_READER, FIELD_TYPE, FIELD_NAME, FEATURES, AUTO_TYPE_OBJECT_READER);
        }

        int varIndex = 11;
        Map<Object, Integer> variants = new HashMap<>();

        {
            Label notNull_ = new Label();
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfNull", "()Z");
            mw.ifeq(notNull_);
            mw.aconst_null();
            mw.areturn();
            mw.visitLabel(notNull_);
        }

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.astore(OBJECT);

        Label fieldEnd_ = new Label(), entryCountMatch_ = new Label();

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "startArray", "()I");
        mw.dup();
        mw.istore(ENTRY_CNT);
        mw.visitLdcInsn(fieldReaderArray.length);
        mw.if_icmpne(entryCountMatch_);

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
                    true,   // JSONB
                    true, // arrayMapping
                    TYPE_OBJECT
            );
        }
        mw.goto_(fieldEnd_);

        mw.visitLabel(entryCountMatch_);
        mw.aload(THIS);
        mw.aload(JSON_READER);
        mw.aload(OBJECT);
        mw.iload(ENTRY_CNT);
        mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "readArrayMappingJSONBObject0", METHOD_DESC_READ_ARRAY_MAPPING_JSONB_OBJECT0);
        mw.visitLabel(fieldEnd_);

        mw.aload(OBJECT);
        mw.areturn();

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

        mw.aload(THIS);
        mw.aload(JSON_READER);
        mw.lload(FEATURES);
        mw.invokevirtual(classNameType, "checkAutoType", METHOD_DESC_JSON_READER_CHECK_ARRAY_AUTO_TYPE);

        mw.dup();
        mw.astore(AUTO_TYPE_OBJECT_READER);
        mw.ifnull(checkArrayAutoTypeNull_);

        mw.aload(AUTO_TYPE_OBJECT_READER);
        mw.aload(JSON_READER);
        mw.aload(FIELD_TYPE);
        mw.aload(FIELD_NAME);
        mw.lload(FEATURES);
        mw.invokeinterface(TYPE_OBJECT_READER, "readJSONBObject", METHOD_DESC_READ_OBJECT);
        mw.areturn();

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

        boolean disableArrayMapping = context.disableSupportArrayMapping();
        boolean disableAutoType = context.disableAutoType();
        boolean disableJSONB = context.disableJSONB();
        boolean disableSmartMatch = context.disableSmartMatch();

        if (!disableJSONB) {
            Label json_ = new Label();
            mw.aload(JSON_READER);
            mw.aload(JSON_READER);
            mw.getfield(TYPE_JSON_READER, "jsonb", "Z");
            mw.ifeq(json_);

            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.aload(FIELD_TYPE);
            mw.aload(FIELD_NAME);
            mw.lload(FEATURES);
            mw.invokevirtual(classNameType, "readJSONBObject", METHOD_DESC_READ_OBJECT);
            mw.areturn();

            mw.visitLabel(json_);
        }

        if (!disableSmartMatch || !disableArrayMapping) {
            Label object_ = new Label();
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "isArray", "()Z");
            mw.ifeq(object_);

            if (!disableArrayMapping) {
                Label singleItemArray_ = new Label();

                if ((readerFeatures & JSONReader.Feature.SupportArrayToBean.mask) == 0) {
                    mw.aload(JSON_READER);
                    mw.lload(FEATURES);
                    mw.invokevirtual(TYPE_JSON_READER, "isSupportBeanArray", "(J)Z");
                    mw.ifeq(singleItemArray_);
                }

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "nextIfArrayStart", "()Z");

                genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
                mw.astore(OBJECT);

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
                            true, // arrayMapping
                            TYPE_OBJECT
                    );
                }

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "nextIfArrayEnd", "()Z");
                mw.pop(); // TODO HANDLE ERROR

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "nextIfComma", "()Z");
                mw.pop();

                mw.aload(OBJECT);
                mw.areturn();

                mw.visitLabel(singleItemArray_);
            }

            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.aload(FIELD_TYPE);
            mw.aload(FIELD_NAME);
            mw.lload(FEATURES);
            mw.invokevirtual(classNameType, "processObjectInputSingleItemArray", METHOD_DESC_READ_OBJECT);
            mw.areturn();

            mw.visitLabel(object_);
        }

        Label notNull_ = new Label(), end_ = new Label();

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectStart", "()Z");
        mw.ifne(notNull_);

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfNullOrEmptyString", "()Z");
        mw.ifeq(notNull_);

        mw.aconst_null();
        mw.astore(OBJECT);
        mw.goto_(end_);

        mw.visitLabel(notNull_);

        genCreateObject(mw, context, classNameType, TYPE_OBJECT, FEATURES, fieldBased, defaultConstructor, objectReaderAdapter.creator);
        mw.astore(OBJECT);

        // for (int i = 0; i < entry_cnt; ++i) {
        Label for_start_i_ = new Label(), for_end_i_ = new Label(), for_inc_i_ = new Label();

        if (!disableAutoType) {
            mw.iconst_0();
            mw.istore(I);
        }
        mw.visitLabel(for_start_i_);

        Label hashCode64Start = new Label(), hashCode64End = new Label();

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectEnd", "()Z");
        mw.ifne(for_end_i_);

        boolean switchGen = false;
        if (context.fieldNameLengthMin >= 5 && context.fieldNameLengthMax <= 7) {
            varIndex = genRead57(
                    context,
                    TYPE_OBJECT,
                    fieldReaderArray,
                    classNameType,
                    fieldBased,
                    mw,
                    JSON_READER,
                    FEATURES,
                    OBJECT,
                    ITEM_CNT,
                    J,
                    varIndex,
                    variants,
                    for_inc_i_,
                    hashCode64Start
            );
            switchGen = true;
        } else if (context.fieldNameLengthMin >= 2 && context.fieldNameLengthMax <= 43) {
            varIndex = genRead243(
                    context,
                    TYPE_OBJECT,
                    fieldReaderArray,
                    classNameType,
                    fieldBased,
                    mw,
                    JSON_READER,
                    FEATURES,
                    OBJECT,
                    ITEM_CNT,
                    J,
                    varIndex,
                    variants,
                    for_inc_i_,
                    hashCode64Start,
                    false
            );
            switchGen = true;
        }

        mw.visitLabel(hashCode64Start);

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "readFieldNameHashCode", "()J");
        mw.dup2();
        mw.lstore(HASH_CODE64);
        mw.visitLdcInsn(-1L);
        mw.lcmp();
        mw.ifeq(for_end_i_);

        mw.visitLabel(hashCode64End);

        if (!disableAutoType) {
            Label noneAutoType_ = new Label();

            // if (i != 0 && hash == HASH_TYPE && jsonReader.isSupportAutoType())
            mw.iload(I);
            mw.ifne(noneAutoType_);

            mw.lload(HASH_CODE64);
            mw.visitLdcInsn(HASH_TYPE);
            mw.lcmp();
            mw.ifne(noneAutoType_);

            if ((readerFeatures & JSONReader.Feature.SupportAutoType.mask) == 0) {
                mw.aload(JSON_READER);
                mw.lload(FEATURES);
                mw.invokevirtual(TYPE_JSON_READER, "isSupportAutoTypeOrHandler", "(J)Z");
                mw.ifeq(noneAutoType_);
            }

            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.aload(THIS);
            mw.getfield(classNameType, "objectClass", "Ljava/lang/Class;");
            mw.lload(FEATURES);
            mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "autoType", "(" + ASMUtils.desc(JSONReader.class) + "Ljava/lang/Class;J)Ljava/lang/Object;");
            mw.areturn();

            mw.visitLabel(noneAutoType_);
        }

        // continue
        if (switchGen) {
            mw.aload(THIS);
            mw.lload(HASH_CODE64);
            mw.aload(JSON_READER);
            mw.lload(FEATURES);
            mw.aload(OBJECT);
            mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "readFieldValue", READ_FIELD_READER_UL);
            mw.goto_(for_inc_i_); // continue
        } else if (fieldReaderArray.length > 6) {
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
            mw.lload(HASH_CODE64);
            mw.lload(HASH_CODE64);
            mw.bipush(32);
            mw.lushr();
            mw.lxor();
            mw.l2i();
            mw.istore(HASH_CODE_32);

            Label dflt = new Label();
            Label[] labels = new Label[hashCode32Keys.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
            }

            mw.iload(HASH_CODE_32);
            mw.visitLookupSwitchInsn(dflt, hashCode32Keys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int hashCode32 = hashCode32Keys[i];
                List<Long> hashCode64Array = map.get(hashCode32);
                for (int j = 0, size = hashCode64Array.size(); j < size; j++) {
                    long hashCode64 = hashCode64Array.get(j);

                    Label next = size > 1 ? new Label() : dflt;
                    mw.lload(HASH_CODE64);
                    mw.visitLdcInsn(hashCode64);
                    mw.lcmp();
                    mw.ifne(next);

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
                            false, // arrayMapping
                            TYPE_OBJECT
                    );
                    mw.goto_(for_inc_i_);

                    if (next != dflt) {
                        mw.visitLabel(next);
                    }
                }

                mw.goto_(for_inc_i_);
            }

            mw.visitLabel(dflt);

            if (!disableSmartMatch) {
                Label fieldReaderNull_ = new Label();
                if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                    mw.aload(JSON_READER);
                    mw.lload(FEATURES);
                    mw.invokevirtual(TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z");
                    mw.ifeq(fieldReaderNull_);
                }

                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "getNameHashCodeLCase", "()J");
                mw.invokeinterface(TYPE_OBJECT_READER, "getFieldReaderLCase", METHOD_DESC_GET_FIELD_READER);
                mw.dup();
                mw.astore(FIELD_READER);
                mw.ifnull(fieldReaderNull_);

                mw.aload(FIELD_READER);
                mw.aload(JSON_READER);
                mw.aload(OBJECT);
                mw.invokevirtual(TYPE_FIELD_READE, "readFieldValue", METHOD_DESC_READ_FIELD_VALUE);
                mw.goto_(for_inc_i_); // continue

                mw.visitLabel(fieldReaderNull_);
            }
        } else {
            for (int i = 0; i < fieldReaderArray.length; ++i) {
                Label next_ = new Label(), get_ = new Label();

                // if (hashCode64 == <nameHashCode>) {
                FieldReader fieldReader = fieldReaderArray[i];

                String fieldName = fieldReader.fieldName;
                long hashCode64 = fieldReader.fieldNameHash;

                mw.lload(HASH_CODE64);
                mw.visitLdcInsn(hashCode64);
                mw.lcmp();
                mw.ifne(next_);

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
                        false, // arrayMapping
                        TYPE_OBJECT
                );

                mw.goto_(for_inc_i_); // continue

                mw.visitLabel(next_);
            }

            Label processExtra_ = new Label();

            if (!disableSmartMatch) {
                if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                    mw.aload(JSON_READER);
                    mw.lload(FEATURES);
                    mw.invokevirtual(TYPE_JSON_READER, "isSupportSmartMatch", "(J)Z");
                    mw.ifeq(processExtra_);
                }

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "getNameHashCodeLCase", "()J");
                mw.lstore(HASH_CODE64);

                for (int i = 0; i < fieldReaderArray.length; ++i) {
                    Label next_ = new Label(), get_ = new Label();

                    // if (hashCode64 == <nameHashCode>) {
                    FieldReader fieldReader = fieldReaderArray[i];

                    String fieldName = fieldReader.fieldName;
                    long hashCode64 = fieldReader.fieldNameHash;
                    long hashCode64LCase = fieldReader.fieldNameHashLCase;

                    mw.lload(HASH_CODE64);
                    mw.visitLdcInsn(hashCode64);
                    mw.lcmp();
                    mw.ifeq(get_);

                    if (hashCode64LCase != hashCode64) {
                        mw.lload(HASH_CODE64);
                        mw.visitLdcInsn(hashCode64LCase);
                        mw.lcmp();
                        mw.ifne(next_);
                    } else {
                        mw.goto_(next_);
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
                            false, // arrayMapping
                            TYPE_OBJECT
                    );

                    mw.goto_(for_inc_i_); // continue

                    mw.visitLabel(next_);
                }
            }

            mw.visitLabel(processExtra_);
        }
        if (!switchGen) {
            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.aload(OBJECT);
            mw.lload(FEATURES);
            mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "processExtra", METHOD_DESC_PROCESS_EXTRA);
            mw.goto_(for_inc_i_); // continue
        }

        mw.visitLabel(for_inc_i_);
        if (!disableAutoType) {
            mw.visitIincInsn(I, 1);
        }
        mw.goto_(for_start_i_);

        mw.visitLabel(for_end_i_);

        mw.visitLabel(end_);

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfComma", "()Z");
        mw.pop();

        mw.aload(OBJECT);
        mw.areturn();

        mw.visitMaxs(5, 10);
    }

    private int genRead243(
            ObjectWriteContext context,
            String TYPE_OBJECT,
            FieldReader[] fieldReaderArray,
            String classNameType,
            boolean fieldBased,
            MethodWriter mw,
            int JSON_READER,
            int FEATURES,
            int OBJECT,
            int ITEM_CNT,
            int J,
            int varIndex,
            Map<Object, Integer> variants,
            Label for_inc_i_,
            Label hashCode64Start,
            boolean jsonb
    ) {
        IdentityHashMap<FieldReader, Integer> readerIndexMap = new IdentityHashMap<>();
        Map<Integer, List<FieldReader>> name0Map = new TreeMap<>();
        for (int i = 0; i < fieldReaderArray.length; ++i) {
            FieldReader fieldReader = fieldReaderArray[i];
            readerIndexMap.put(fieldReader, i);

            byte[] name0Bytes = new byte[4];
            if (jsonb) {
                byte[] fieldNameJSONB = JSONB.toBytes(fieldReader.fieldName);
                System.arraycopy(fieldNameJSONB, 0, name0Bytes, 0, Math.min(4, fieldNameJSONB.length));
            } else {
                byte[] fieldName = fieldReader.fieldName.getBytes(StandardCharsets.UTF_8);
                name0Bytes[0] = '"';
                if (fieldName.length == 2) {
                    System.arraycopy(fieldName, 0, name0Bytes, 1, 2);
                    name0Bytes[3] = '"';
                } else {
                    System.arraycopy(fieldName, 0, name0Bytes, 1, 3);
                }
            }

            int name0 = UNSAFE.getInt(name0Bytes, ARRAY_BYTE_BASE_OFFSET);

            List<FieldReader> fieldReaders = name0Map.get(name0);
            if (fieldReaders == null) {
                fieldReaders = new ArrayList<>();
                name0Map.put(name0, fieldReaders);
            }
            fieldReaders.add(fieldReader);
        }

        Label dflt = new Label();
        int[] switchKeys = new int[name0Map.size()];
        Label[] labels = new Label[name0Map.size()];
        {
            Iterator it = name0Map.keySet().iterator();
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
                switchKeys[i] = (Integer) it.next();
            }
        }

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "getRawInt", "()I");
        mw.visitLookupSwitchInsn(dflt, switchKeys, labels);

        for (int i = 0; i < labels.length; i++) {
            mw.visitLabel(labels[i]);

            int name0 = switchKeys[i];
            List<FieldReader> fieldReaders = name0Map.get(name0);
            for (int j = 0; j < fieldReaders.size(); j++) {
                Label nextJ = null;
                if (j + 1 != fieldReaders.size()) {
                    nextJ = new Label();
                }

                FieldReader fieldReader = fieldReaders.get(j);
                int fieldReaderIndex = readerIndexMap.get(fieldReader);
                byte[] fieldName = fieldReader.fieldName.getBytes(StandardCharsets.UTF_8);
                int fieldNameLength = fieldName.length;
                switch (fieldNameLength) {
                    case 2:
                        mw.aload(JSON_READER);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match2", "()Z");
                        break;
                    case 3:
                        mw.aload(JSON_READER);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match3", "()Z");
                        break;
                    case 4: {
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(fieldName[3]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match4", "(B)Z");
                        break;
                    }
                    case 5: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name1 &= 0xFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match5", "(I)Z");
                        break;
                    }
                    case 6: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = fieldName[5];
                        bytes4[3] = '"';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name1 &= 0xFFFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match6", "(I)Z");
                        break;
                    }
                    case 7: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match7", "(I)Z");
                        break;
                    }
                    case 8: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(fieldName[7]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match8", "(IB)Z");
                        break;
                    }
                    case 9: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name1 &= 0xFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match9", "(J)Z");
                        break;
                    }
                    case 10: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name1 &= 0xFFFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match10", "(J)Z");
                        break;
                    }
                    case 11: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match11", "(J)Z");
                        break;
                    }
                    case 12: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(fieldName[11]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match12", "(JB)Z");
                        break;
                    }
                    case 13: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[11];
                        bytes4[1] = fieldName[12];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name2 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name2 &= 0xFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match13", "(JI)Z");
                        break;
                    }
                    case 14: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[11];
                        bytes4[1] = fieldName[12];
                        bytes4[2] = fieldName[13];
                        bytes4[3] = '"';
                        int name2 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name2 &= 0xFFFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match14", "(JI)Z");
                        break;
                    }
                    case 15: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match15", "(JI)Z");
                        break;
                    }
                    case 16: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(fieldName[15]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match16", "(JIB)Z");
                        break;
                    }
                    case 17: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);

                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name2 &= 0xFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match17", "(JJ)Z");
                        break;
                    }
                    case 18: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);

                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name2 &= 0xFFFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match18", "(JJ)Z");
                        break;
                    }
                    case 19: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match19", "(JJ)Z");
                        break;
                    }
                    case 20: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(fieldName[19]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match20", "(JJB)Z");
                        break;
                    }
                    case 21: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[19];
                        bytes4[1] = fieldName[20];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name3 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name3 &= 0xFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match21", "(JJI)Z");
                        break;
                    }
                    case 22: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[19];
                        bytes4[1] = fieldName[20];
                        bytes4[2] = fieldName[21];
                        bytes4[3] = '"';
                        int name3 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name3 &= 0xFFFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match22", "(JJI)Z");
                        break;
                    }
                    case 23: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match23", "(JJI)Z");
                        break;
                    }
                    case 24: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(fieldName[23]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match24", "(JJIB)Z");
                        break;
                    }
                    case 25: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);

                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 19, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name3 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name3 &= 0xFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match25", "(JJJ)Z");
                        break;
                    }
                    case 26: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);

                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 19, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name3 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name3 &= 0xFFFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match26", "(JJJ)Z");
                        break;
                    }
                    case 27: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match27", "(JJJ)Z");
                        break;
                    }
                    case 28: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(fieldName[27]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match28", "(JJJB)Z");
                        break;
                    }
                    case 29: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[27];
                        bytes4[1] = fieldName[28];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name4 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name4 &= 0xFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match29", "(JJJI)Z");
                        break;
                    }
                    case 30: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[27];
                        bytes4[1] = fieldName[28];
                        bytes4[2] = fieldName[29];
                        bytes4[3] = '"';
                        int name4 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name4 &= 0xFFFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match30", "(JJJI)Z");
                        break;
                    }
                    case 31: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match31", "(JJJI)Z");
                        break;
                    }
                    case 32: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(fieldName[31]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match32", "(JJJIB)Z");
                        break;
                    }
                    case 33: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 27, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name4 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name4 &= 0xFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match33", "(JJJJ)Z");
                        break;
                    }
                    case 34: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 27, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name4 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name4 &= 0xFFFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match34", "(JJJJ)Z");
                        break;
                    }
                    case 35: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);

                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match35", "(JJJJ)Z");
                        break;
                    }
                    case 36: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);

                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(fieldName[35]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match36", "(JJJJB)Z");
                        break;
                    }
                    case 37: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[35];
                        bytes4[1] = fieldName[36];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name5 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name5 &= 0xFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(name5);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match37", "(JJJJI)Z");
                        break;
                    }
                    case 38: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[35];
                        bytes4[1] = fieldName[36];
                        bytes4[2] = fieldName[37];
                        bytes4[3] = '"';
                        int name5 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name5 &= 0xFFFFFF;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(name5);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match38", "(JJJJI)Z");
                        break;
                    }
                    case 39: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);

                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(name5);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match39", "(JJJJI)Z");
                        break;
                    }
                    case 40: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);

                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(name5);
                        mw.visitLdcInsn(fieldName[39]);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match40", "(JJJJIB)Z");
                        break;
                    }
                    case 41: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);

                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 35, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name5 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name5 &= 0xFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(name5);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match41", "(JJJJJ)Z");
                        break;
                    }
                    case 42: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);

                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 35, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name5 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        if (jsonb) {
                            name5 &= 0xFFFFFFFFFFFFFFL;
                        }
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(name5);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match42", "(JJJJJ)Z");
                        break;
                    }
                    case 43: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        long name5 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.visitLdcInsn(name2);
                        mw.visitLdcInsn(name3);
                        mw.visitLdcInsn(name4);
                        mw.visitLdcInsn(name5);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match43", "(JJJJJ)Z");
                        break;
                    }
                    default:
                        throw new IllegalStateException("fieldNameLength " + fieldNameLength);
                }

                mw.ifeq(nextJ != null ? nextJ : hashCode64Start);
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
                        fieldReaderIndex,
                        jsonb,
                        false, // arrayMapping
                        TYPE_OBJECT
                );

                mw.goto_(for_inc_i_);

                if (nextJ != null) {
                    mw.visitLabel(nextJ);
                }
            }

            mw.goto_(dflt);
        }

        mw.visitLabel(dflt);

        return varIndex;
    }

    private int genRead57(
            ObjectWriteContext context,
            String TYPE_OBJECT,
            FieldReader[] fieldReaderArray,
            String classNameType,
            boolean fieldBased,
            MethodWriter mw,
            int JSON_READER,
            int FEATURES,
            int OBJECT,
            int ITEM_CNT,
            int J,
            int varIndex,
            Map<Object, Integer> variants,
            Label for_inc_i_,
            Label hashCode64Start
    ) {
        Integer RAW_LONG = variants.get("RAW_LONG");
        if (RAW_LONG == null) {
            variants.put("RAW_LONG", RAW_LONG = varIndex);
            varIndex += 2;
        }

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "getRawLong", "()J");
        mw.dup2();
        mw.lstore(RAW_LONG);
        mw.lconst_0();
        mw.lcmp();
        mw.ifeq(hashCode64Start);

        for (int i = 0; i < fieldReaderArray.length; ++i) {
            Label next_ = new Label();
            FieldReader fieldReader = fieldReaderArray[i];
            byte[] fieldName = fieldReader.fieldName.getBytes(StandardCharsets.UTF_8);
            int fieldNameLength = fieldName.length;
            byte[] bytes8 = new byte[8];
            String nextMethodName;
            switch (fieldNameLength) {
                case 5:
                    bytes8[0] = '"';
                    System.arraycopy(fieldName, 0, bytes8, 1, 5);
                    bytes8[6] = '"';
                    bytes8[7] = ':';
                    nextMethodName = "nextIfName8Match0";
                    break;
                case 6:
                    bytes8[0] = '"';
                    System.arraycopy(fieldName, 0, bytes8, 1, 6);
                    bytes8[7] = '"';
                    nextMethodName = "nextIfName8Match1";
                    break;
                case 7:
                    bytes8[0] = '"';
                    System.arraycopy(fieldName, 0, bytes8, 1, 7);
                    nextMethodName = "nextIfName8Match2";
                    break;
                default:
                    throw new IllegalStateException("length " + fieldNameLength);
            }
            long rawLong = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
            mw.lload(RAW_LONG);
            mw.visitLdcInsn(rawLong);
            mw.lcmp();
            mw.ifne(next_);

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, nextMethodName, "()Z");
            mw.ifeq(hashCode64Start);

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
                    false, // arrayMapping
                    TYPE_OBJECT
            );

            mw.goto_(for_inc_i_); // continue

            mw.visitLabel(next_);
        }
        return varIndex;
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

        int objectModifiers = objectClass == null ? Modifier.PUBLIC : objectClass.getModifiers();
        boolean publicObject = Modifier.isPublic(objectModifiers) && (objectClass == null || !classLoader.isExternalClass(objectClass));

        if (defaultConstructor == null || !publicObject || !Modifier.isPublic(defaultConstructor.getModifiers())) {
            if (creator != null) {
                mw.aload(THIS);
                mw.getfield(classNameType, "creator", "Ljava/util/function/Supplier;");
                mw.invokeinterface("java/util/function/Supplier", "get", "()Ljava/lang/Object;");
            } else {
                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.lload(FEATURES);
                mw.invokevirtual(TYPE_JSON_READER, "features", "(J)J");
                mw.invokevirtual(classNameType, "createInstance", "(J)Ljava/lang/Object;");
            }
            if (publicObject) {
                mw.checkcast(TYPE_OBJECT);
            }
        } else {
            newObject(mw, TYPE_OBJECT, defaultConstructor);
        }

        if (context.hasStringField) {
            Label endInitStringAsEmpty_ = new Label(), addResolveTask_ = new Label();

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "isInitStringFieldAsEmpty", "()Z");
            mw.ifeq(endInitStringAsEmpty_);

            mw.dup();
            mw.aload(THIS);
            mw.swap();
            mw.invokevirtual(classNameType, "initStringFieldAsEmpty", "(Ljava/lang/Object;)V");
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
            boolean arrayMapping,
            String TYPE_OBJECT
    ) {
        Class objectClass = context.objectClass;
        Class fieldClass = fieldReader.fieldClass;
        Type fieldType = fieldReader.fieldType;
        long fieldFeatures = fieldReader.features;
        String format = fieldReader.format;
        Type itemType = fieldReader.itemType;

        if ((fieldFeatures & JSONReader.Feature.NullOnError.mask) != 0) {
            mw.aload(THIS);
            mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
            mw.aload(JSON_READER);
            mw.aload(OBJECT);
            mw.invokevirtual(TYPE_FIELD_READE, "readFieldValue", METHOD_DESC_READ_FIELD_VALUE);
            return varIndex;
        }

        Field field = fieldReader.field;
        Method method = fieldReader.method;

        Label endSet_ = new Label();

        String TYPE_FIELD_CLASS = ASMUtils.type(fieldClass);
        String DESC_FIELD_CLASS = ASMUtils.desc(fieldClass);

        mw.aload(OBJECT);
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
            mw.checkcast(TYPE_OBJECT);
        }

        if (fieldClass == boolean.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readBoolValue", "()Z");
        } else if (fieldClass == byte.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt32Value", "()I");
        } else if (fieldClass == short.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt32Value", "()I");
        } else if (fieldClass == int.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt32Value", "()I");
        } else if (fieldClass == long.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt64Value", "()J");
        } else if (fieldClass == float.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readFloatValue", "()F");
        } else if (fieldClass == double.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readDoubleValue", "()D");
        } else if (fieldClass == char.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readCharValue", "()C");
        } else if (fieldClass == String.class) {
            mw.aload(JSON_READER);
            Label null_ = new Label();

            mw.invokevirtual(TYPE_JSON_READER, "readString", "()Ljava/lang/String;");
            mw.dup();
            mw.ifnull(null_);

            if ("trim".equals(format)) {
                mw.invokevirtual("java/lang/String", "trim", "()Ljava/lang/String;");
            } else if ("upper".equals(format)) {
                mw.invokevirtual("java/lang/String", "toUpperCase", "()Ljava/lang/String;");
            }
            mw.visitLabel(null_);
        } else if (fieldClass == Byte.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt8", "()Ljava/lang/Byte;");
        } else if (fieldClass == Short.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt16", "()Ljava/lang/Short;");
        } else if (fieldClass == Integer.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt32", "()Ljava/lang/Integer;");
        } else if (fieldClass == Long.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt64", "()Ljava/lang/Long;");
        } else if (fieldClass == Float.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readFloat", "()Ljava/lang/Float;");
        } else if (fieldClass == Double.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readDouble", "()Ljava/lang/Double;");
        } else if (fieldClass == BigDecimal.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readBigDecimal", "()Ljava/math/BigDecimal;");
        } else if (fieldClass == BigInteger.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readBigInteger", "()Ljava/math/BigInteger;");
        } else if (fieldClass == Number.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readNumber", "()Ljava/lang/Number;");
        } else if (fieldClass == UUID.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readUUID", "()Ljava/util/UUID;");
        } else if (fieldClass == LocalDate.class && fieldReader.format == null) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readLocalDate", "()Ljava/time/LocalDate;");
        } else if (fieldClass == OffsetDateTime.class && fieldReader.format == null) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readOffsetDateTime", "()Ljava/time/OffsetDateTime;");
        } else if (fieldClass == Date.class && fieldReader.format == null) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readDate", "()Ljava/util/Date;");
        } else if (fieldClass == Calendar.class && fieldReader.format == null) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readCalendar", "()Ljava/util/Calendar;");
        } else {
            Label endObject_ = new Label();

            boolean disableReferenceDetect = context.disableReferenceDetect();
            Integer REFERENCE = variants.get("REFERENCE");
            if (REFERENCE == null && !disableReferenceDetect) {
                variants.put("REFERENCE", REFERENCE = varIndex);
                varIndex++;
            }

            if ((!disableReferenceDetect) && (!ObjectWriterProvider.isPrimitiveOrEnum(fieldClass))) {
                Label endReference_ = new Label(), addResolveTask_ = new Label();

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isReference", "()Z");
                mw.ifeq(endReference_);

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "readReference", "()Ljava/lang/String;");
                if (context.objectClass == null || fieldClass.isAssignableFrom(context.objectClass)) {
                    mw.dup();
                    mw.astore(REFERENCE);
                    mw.visitLdcInsn("..");
                    mw.invokevirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                    mw.ifeq(addResolveTask_);

                    if (objectClass != null && fieldClass.isAssignableFrom(objectClass)) {
                        mw.aload(OBJECT);
//                    mw.visitTypeInsn(CHECKCAST, TYPE_FIELD_CLASS); // cast
                        mw.goto_(endObject_);
                    }

                    mw.visitLabel(addResolveTask_);
                } else {
                    mw.astore(REFERENCE);
                }

                mw.aload(THIS);
                mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.aload(JSON_READER);
                mw.aload(OBJECT);
                mw.aload(REFERENCE);
                mw.invokevirtual(TYPE_FIELD_READE, "addResolveTask", METHOD_DESC_ADD_RESOLVE_TASK);
                mw.pop();
                mw.goto_(endSet_);

                mw.visitLabel(endReference_);
            }

            if (!fieldReader.fieldClassSerializable) {
                Label endIgnoreCheck_ = new Label();

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isIgnoreNoneSerializable", "()Z");
                mw.ifeq(endIgnoreCheck_);
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "skipValue", "()V");
                mw.pop();
                mw.goto_(endSet_);

                mw.visitLabel(endIgnoreCheck_);
            }

            boolean list = List.class.isAssignableFrom(fieldClass)
                    && fieldReader.getInitReader() == null
                    && !fieldClass.getName().startsWith("com.google.common.collect.Immutable");

            if (list) {
                Class itemClass = TypeUtils.getMapping(itemType);
                if (itemClass != null
                        && (Collection.class.isAssignableFrom(itemClass) || !Modifier.isPublic(itemClass.getModifiers()))
                ) {
                    list = false;
                }
            }

            if (list && !fieldClass.isInterface() && !BeanUtils.hasPublicDefaultConstructor(fieldClass)) {
                list = false;
            }

            if (list) {
                varIndex = genReadFieldValueList(
                        fieldReader,
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
                        jsonb,
                        arrayMapping,
                        objectClass,
                        fieldClass,
                        fieldType,
                        fieldFeatures,
                        itemType,
                        TYPE_FIELD_CLASS,
                        context
                );
            } else {
                final String FIELD_OBJECT_READER = fieldObjectReader(i);

                Label valueNotNull_ = new Label();

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "nextIfNull", "()Z");
                mw.ifeq(valueNotNull_);

                if (fieldClass == Optional.class) {
                    mw.invokestatic("java/util/Optional", "empty", "()Ljava/util/Optional;");
                } else if (fieldClass == OptionalInt.class) {
                    mw.invokestatic("java/util/OptionalInt", "empty", "()Ljava/util/OptionalInt;");
                } else if (fieldClass == OptionalLong.class) {
                    mw.invokestatic("java/util/OptionalLong", "empty", "()Ljava/util/OptionalLong;");
                } else if (fieldClass == OptionalDouble.class) {
                    mw.invokestatic("java/util/OptionalDouble", "empty", "()Ljava/util/OptionalDouble;");
                } else {
                    mw.aconst_null();
                }
                mw.goto_(endObject_);

                mw.visitLabel(valueNotNull_);

                if (fieldClass == String[].class) {
                    mw.aload(JSON_READER);
                    mw.invokevirtual(TYPE_JSON_READER, "readStringArray", "()[Ljava/lang/String;");
                } else if (fieldClass == int[].class) {
                    mw.aload(JSON_READER);
                    mw.invokevirtual(TYPE_JSON_READER, "readInt32ValueArray", "()[I");
                } else if (fieldClass == long[].class) {
                    mw.aload(JSON_READER);
                    mw.invokevirtual(TYPE_JSON_READER, "readInt64ValueArray", "()[J");
                } else {
                    if (Enum.class.isAssignableFrom(fieldClass) & !jsonb) {
                        genReadEnumValueRaw(
                                fieldReader,
                                classNameType,
                                mw,
                                THIS,
                                JSON_READER,
                                i,
                                jsonb,
                                fieldType,
                                fieldClass,
                                fieldFeatures,
                                FIELD_OBJECT_READER
                        );
                    } else {
                        genReadObject(
                                fieldReader,
                                classNameType,
                                mw,
                                THIS,
                                JSON_READER,
                                i,
                                jsonb,
                                fieldType,
                                fieldFeatures,
                                FIELD_OBJECT_READER
                        );
                    }

                    if (method != null
                            || ((objectClass == null || Modifier.isPublic(objectClass.getModifiers()))
                            && Modifier.isPublic(fieldModifier)
                            && !Modifier.isFinal(fieldModifier)
                            && !classLoader.isExternalClass(objectClass))
                    ) {
                        mw.checkcast(TYPE_FIELD_CLASS); // cast
                    }

                    if (fieldReader.noneStaticMemberClass) {
                        try {
                            Field this0 = fieldClass.getDeclaredField("this$0");
                            long fieldOffset = UNSAFE.objectFieldOffset(this0);

                            Label notNull_ = new Label();
                            mw.dup();
                            mw.ifnull(notNull_);
                            mw.dup();
                            mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                            mw.swap();
                            mw.visitLdcInsn(fieldOffset);
                            mw.aload(OBJECT);
                            mw.invokevirtual("sun/misc/Unsafe", "putObject", "(Ljava/lang/Object;JLjava/lang/Object;)V");
                            mw.visitLabel(notNull_);
                        } catch (NoSuchFieldException e) {
                            // ignored
                        }
                    }
                }
            }

            mw.visitLabel(endObject_);

            if (!jsonb) {
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "nextIfComma", "()Z");
                mw.pop();
            }
        }

        if (field != null) {
            String fieldClassName = fieldClass.getName();
            boolean setDirect = (objectClass.getModifiers() & Modifier.PUBLIC) != 0
                    && (fieldModifier & Modifier.PUBLIC) != 0
                    && (fieldModifier & Modifier.FINAL) == 0
                    && (ObjectWriterProvider.isPrimitiveOrEnum(fieldClass) || fieldClassName.startsWith("java.") || fieldClass.getClassLoader() == ObjectReaderProvider.FASTJSON2_CLASS_LOADER)
                    && !classLoader.isExternalClass(objectClass)
                    && field.getDeclaringClass() == objectClass;
            if (setDirect) {
                mw.putfield(TYPE_OBJECT, field.getName(), DESC_FIELD_CLASS);
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
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == long.class) {
                    methodName = "putLong";
                    methodDes = "(Ljava/lang/Object;JJ)V";
                    mw.lstore(FIELD_VALUE);
                    LOAD = Opcodes.LLOAD;
                } else if (fieldClass == float.class) {
                    methodName = "putFloat";
                    methodDes = "(Ljava/lang/Object;JF)V";
                    mw.fstore(FIELD_VALUE);
                    LOAD = Opcodes.FLOAD;
                } else if (fieldClass == double.class) {
                    methodName = "putDouble";
                    methodDes = "(Ljava/lang/Object;JD)V";
                    mw.dstore(FIELD_VALUE);
                    LOAD = Opcodes.DLOAD;
                } else if (fieldClass == char.class) {
                    methodName = "putChar";
                    methodDes = "(Ljava/lang/Object;JC)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == byte.class) {
                    methodName = "putByte";
                    methodDes = "(Ljava/lang/Object;JB)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == short.class) {
                    methodName = "putShort";
                    methodDes = "(Ljava/lang/Object;JS)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == boolean.class) {
                    methodName = "putBoolean";
                    methodDes = "(Ljava/lang/Object;JZ)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else {
                    methodName = "putObject";
                    methodDes = "(Ljava/lang/Object;JLjava/lang/Object;)V";
                    mw.astore(FIELD_VALUE);
                    LOAD = Opcodes.ALOAD;
                }

                mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                mw.swap();

                mw.visitLdcInsn(
                        UNSAFE.objectFieldOffset(field));
                mw.visitVarInsn(LOAD, FIELD_VALUE);
                mw.invokevirtual("sun/misc/Unsafe", methodName, methodDes);
            }
        } else {
            boolean invokeFieldReaderAccept = context.externalClass || method == null || !context.publicClass;

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
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == byte.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;B)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == short.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;S)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == int.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;I)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == long.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;J)V";
                    mw.lstore(FIELD_VALUE);
                    LOAD = Opcodes.LLOAD;
                } else if (fieldClass == char.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;C)V";
                    mw.istore(FIELD_VALUE);
                    LOAD = Opcodes.ILOAD;
                } else if (fieldClass == float.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;F)V";
                    mw.fstore(FIELD_VALUE);
                    LOAD = Opcodes.FLOAD;
                } else if (fieldClass == double.class) {
                    acceptMethodDesc = "(Ljava/lang/Object;D)V";
                    mw.dstore(FIELD_VALUE);
                    LOAD = Opcodes.DLOAD;
                } else {
                    acceptMethodDesc = "(Ljava/lang/Object;Ljava/lang/Object;)V";
                    mw.astore(FIELD_VALUE);
                    LOAD = Opcodes.ALOAD;
                }

                mw.aload(THIS);
                mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
                BiConsumer function = fieldReader.getFunction();
                if (function instanceof FieldBiConsumer) {
                    FieldBiConsumer fieldBiConsumer = (FieldBiConsumer) function;
                    mw.invokevirtual(TYPE_FIELD_READE, "getFunction", "()Ljava/util/function/BiConsumer;");
                    mw.checkcast(type(FieldBiConsumer.class));
                    mw.getfield(type(FieldBiConsumer.class), "consumer", desc(FieldConsumer.class));
                    mw.swap();
                    mw.visitLdcInsn(fieldBiConsumer.fieldIndex);
                    mw.visitVarInsn(LOAD, FIELD_VALUE);
                    mw.invokeinterface(type(FieldConsumer.class), "accept", "(Ljava/lang/Object;ILjava/lang/Object;)V");
                } else {
                    mw.swap();
                    mw.visitVarInsn(LOAD, FIELD_VALUE);
                    mw.invokevirtual(TYPE_FIELD_READE, "accept", acceptMethodDesc);
                }
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
                mw.invokevirtual(TYPE_OBJECT, methodName, methodDesc);
                if (returnType != void.class) {
                    mw.pop();
                }
            }
            // TODO BUILD METHOD
        }

        mw.visitLabel(endSet_);

        return varIndex;
    }

    private void genReadObject(
            FieldReader fieldReader,
            String classNameType,
            MethodWriter mw,
            int THIS,
            int JSON_READER,
            int i,
            boolean jsonb,
            Type fieldType,
            long fieldFeatures,
            String FIELD_OBJECT_READER
    ) {
        // object.<setMethod>(this.objectReader_<i>.readObject(jsonReader))
        Label notNull_ = new Label();

        mw.aload(THIS);
        mw.getfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);
        mw.ifnonnull(notNull_);

        mw.aload(THIS);
        mw.aload(THIS);
        mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_FIELD_READE, "getObjectReader", METHOD_DESC_GET_OBJECT_READER_1);
        mw.putfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);

        mw.visitLabel(notNull_);
        mw.aload(THIS);
        mw.getfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);

        mw.aload(JSON_READER);
        gwGetFieldType(classNameType, mw, THIS, i, fieldType);
        mw.visitLdcInsn(fieldReader.fieldName);
        mw.visitLdcInsn(fieldFeatures);
        mw.invokeinterface(
                TYPE_OBJECT_READER,
                jsonb ? "readJSONBObject" : "readObject",
                METHOD_DESC_READ_OBJECT);
    }

    private void genReadEnumValueRaw(
            FieldReader fieldReader,
            String classNameType,
            MethodWriter mw,
            int THIS,
            int JSON_READER,
            int fieldIndex,
            boolean jsonb,
            Type fieldType,
            Class fieldClass,
            long fieldFeatures,
            String FIELD_OBJECT_READER
    ) {
        Object[] enums = fieldClass.getEnumConstants();

        Map<Integer, List<Enum>> name0Map = new TreeMap<>();
        int nameLengthMin = 0, nameLengthMax = 0;
        if (enums != null) {
            for (int i = 0; i < enums.length; i++) {
                Enum e = (Enum) enums[i];
                byte[] enumName = e.name().getBytes(StandardCharsets.UTF_8);
                int nameLength = enumName.length;
                if (i == 0) {
                    nameLengthMin = nameLength;
                    nameLengthMax = nameLength;
                } else {
                    nameLengthMin = Math.min(nameLength, nameLengthMin);
                    nameLengthMax = Math.max(nameLength, nameLengthMax);
                }

                byte[] name0Bytes = new byte[4];
                name0Bytes[0] = '"';
                if (enumName.length == 2) {
                    System.arraycopy(enumName, 0, name0Bytes, 1, 2);
                    name0Bytes[3] = '"';
                } else if (enumName.length >= 3) {
                    System.arraycopy(enumName, 0, name0Bytes, 1, 3);
                }
                int name0 = UNSAFE.getInt(name0Bytes, ARRAY_BYTE_BASE_OFFSET);

                List<Enum> enumList = name0Map.get(name0);
                if (enumList == null) {
                    enumList = new ArrayList<>();
                    name0Map.put(name0, enumList);
                }
                enumList.add(e);
            }
        }

        Label dflt = new Label(), enumEnd = new Label();

        Label notNull_ = new Label();

        mw.aload(THIS);
        mw.getfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);
        mw.ifnonnull(notNull_);

        mw.aload(THIS);
        mw.aload(THIS);
        mw.getfield(classNameType, fieldReader(fieldIndex), DESC_FIELD_READER);
        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_FIELD_READE, "getObjectReader", METHOD_DESC_GET_OBJECT_READER_1);
        mw.putfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);

        mw.visitLabel(notNull_);
        mw.aload(THIS);
        mw.getfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);
        mw.instanceOf(type(ObjectReaderImplEnum.class));
        mw.ifeq(dflt);

        if (nameLengthMin >= 2 && nameLengthMax <= 11) {
            int[] switchKeys = new int[name0Map.size()];
            Label[] labels = new Label[name0Map.size()];
            {
                Iterator it = name0Map.keySet().iterator();
                for (int j = 0; j < labels.length; j++) {
                    labels[j] = new Label();
                    switchKeys[j] = (Integer) it.next();
                }
            }

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "getRawInt", "()I");
            mw.visitLookupSwitchInsn(dflt, switchKeys, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);

                int name0 = switchKeys[i];
                List<Enum> enumList = name0Map.get(name0);
                for (int j = 0; j < enumList.size(); j++) {
                    Label nextJ = null;
                    if (j > 0) {
                        nextJ = new Label();
                    }

                    Enum e = enumList.get(j);
                    byte[] enumName = e.name().getBytes(StandardCharsets.UTF_8);
                    int fieldNameLength = enumName.length;
                    switch (fieldNameLength) {
                        case 2:
                            mw.aload(JSON_READER);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match2", "()Z");
                            break;
                        case 3:
                            mw.aload(JSON_READER);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match3", "()Z");
                            break;
                        case 4: {
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(enumName[3]);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match4", "(B)Z");
                            break;
                        }
                        case 5: {
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(enumName[3]);
                            mw.visitLdcInsn(enumName[4]);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match5", "(BB)Z");
                            break;
                        }
                        case 6: {
                            byte[] bytes4 = new byte[4];
                            bytes4[0] = enumName[3];
                            bytes4[1] = enumName[4];
                            bytes4[2] = enumName[5];
                            bytes4[3] = '"';
                            int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(name1);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match6", "(I)Z");
                            break;
                        }
                        case 7: {
                            int name1 = UNSAFE.getInt(enumName, ARRAY_BYTE_BASE_OFFSET + 3);
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(name1);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match7", "(I)Z");
                            break;
                        }
                        case 8: {
                            int name1 = UNSAFE.getInt(enumName, ARRAY_BYTE_BASE_OFFSET + 3);
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(name1);
                            mw.visitLdcInsn(enumName[7]);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match8", "(IB)Z");
                            break;
                        }
                        case 9: {
                            int name1 = UNSAFE.getInt(enumName, ARRAY_BYTE_BASE_OFFSET + 3);
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(name1);
                            mw.visitLdcInsn(enumName[7]);
                            mw.visitLdcInsn(enumName[8]);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match9", "(IBB)Z");
                            break;
                        }
                        case 10: {
                            byte[] bytes8 = new byte[8];
                            System.arraycopy(enumName, 3, bytes8, 0, 7);
                            bytes8[7] = '"';
                            long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(name1);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match10", "(J)Z");
                            break;
                        }
                        case 11: {
                            byte[] bytes8 = new byte[8];
                            System.arraycopy(enumName, 3, bytes8, 0, 8);
                            long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                            mw.aload(JSON_READER);
                            mw.visitLdcInsn(name1);
                            mw.invokevirtual(TYPE_JSON_READER, "nextIfValue4Match11", "(J)Z");
                            break;
                        }
                        default:
                            throw new IllegalStateException("fieldNameLength " + fieldNameLength);
                    }

                    mw.ifeq(nextJ != null ? nextJ : dflt);

                    mw.aload(THIS);
                    mw.getfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);
                    mw.checkcast(type(ObjectReaderImplEnum.class));
                    mw.visitLdcInsn(e.ordinal());
                    mw.invokevirtual(type(ObjectReaderImplEnum.class), "getEnumByOrdinal", "(I)Ljava/lang/Enum;");
                    mw.goto_(enumEnd);

                    if (nextJ != null) {
                        mw.visitLabel(nextJ);
                    }
                }

                mw.goto_(dflt);
            }
        }

        mw.visitLabel(dflt);

        mw.aload(THIS);
        mw.getfield(classNameType, FIELD_OBJECT_READER, DESC_OBJECT_READER);
        mw.aload(JSON_READER);
        gwGetFieldType(classNameType, mw, THIS, fieldIndex, fieldType);
        mw.visitLdcInsn(fieldReader.fieldName);
        mw.visitLdcInsn(fieldFeatures);
        mw.invokeinterface(
                TYPE_OBJECT_READER,
                jsonb ? "readJSONBObject" : "readObject",
                METHOD_DESC_READ_OBJECT);

        mw.visitLabel(enumEnd);
    }

    private int genReadFieldValueList(
            FieldReader fieldReader,
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
            boolean arrayMapping,
            Class objectClass,
            Class fieldClass,
            Type fieldType,
            long fieldFeatures,
            Type itemType,
            String TYPE_FIELD_CLASS,
            ObjectWriteContext context
    ) {
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

        Label loadList_ = new Label(), listNotNull_ = new Label(), listInitEnd_ = new Label();

        boolean initCapacity = JVM_VERSION == 8 && "java/util/ArrayList".equals(LIST_TYPE);

        if (jsonb) {
            if (!context.disableAutoType()) {
                Label checkAutoTypeNull_ = new Label();

                mw.aload(THIS);
                mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_FIELD_READE, "checkObjectAutoType", METHOD_DESC_CHECK_ARRAY_AUTO_TYPE);
                mw.dup();
                mw.astore(AUTO_TYPE_OBJECT_READER);
                mw.ifnull(checkAutoTypeNull_);

                mw.aload(AUTO_TYPE_OBJECT_READER);
                mw.aload(JSON_READER);
                gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                mw.visitLdcInsn(fieldReader.fieldName);
                mw.visitLdcInsn(fieldFeatures);
                mw.invokeinterface(TYPE_OBJECT_READER, "readJSONBObject", METHOD_DESC_READ_OBJECT);
                mw.checkcast(TYPE_FIELD_CLASS);
                mw.astore(LIST);
                mw.goto_(loadList_);

                mw.visitLabel(checkAutoTypeNull_);
            }

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "startArray", "()I");
            mw.dup();
            mw.istore(ITEM_CNT);
            mw.visitLdcInsn(-1);
            mw.if_icmpne(listNotNull_);

            mw.aconst_null();
            mw.astore(LIST);
            mw.goto_(loadList_);

            mw.visitLabel(listNotNull_);

            if (fieldReader.method == null && fieldReader.field != null) {
                long fieldOffset = UNSAFE.objectFieldOffset(fieldReader.field);
                mw.getstatic(TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
                mw.aload(OBJECT);
                mw.visitLdcInsn(fieldOffset);
                mw.invokevirtual("sun/misc/Unsafe", "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;");
                mw.dup();
                mw.checkcast(TYPE_FIELD_CLASS);
                mw.astore(LIST);
                Label listNull_ = new Label();
                mw.ifnull(listNull_);

                mw.aload(LIST);
                mw.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");
                mw.getstatic("java/util/Collections", "EMPTY_LIST", "Ljava/util/List;");
                mw.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");
                mw.if_acmpne(listInitEnd_);
                mw.visitLabel(listNull_);
            }

            mw.new_(LIST_TYPE);
            mw.dup();
            if (initCapacity) {
                mw.iload(ITEM_CNT);
                mw.invokespecial(LIST_TYPE, "<init>", "(I)V");
            } else {
                mw.invokespecial(LIST_TYPE, "<init>", "()V");
            }
            mw.astore(LIST);
            mw.visitLabel(listInitEnd_);
        } else {
            Label match_ = new Label(), skipValue_ = new Label(), loadNull_ = new Label();

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfNull", "()Z");
            mw.ifne(loadNull_);

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfArrayStart", "()Z");
            mw.ifne(match_);

            if (itemClass == String.class) {
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isString", "()Z");
                mw.ifeq(skipValue_);

                mw.new_(LIST_TYPE);
                mw.dup();
                if (initCapacity) {
                    mw.visitLdcInsn(10);
                    mw.invokespecial(LIST_TYPE, "<init>", "(I)V");
                } else {
                    mw.invokespecial(LIST_TYPE, "<init>", "()V");
                }
                mw.astore(LIST);

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "nextIfNullOrEmptyString", "()Z");
                mw.ifne(loadList_);

                mw.aload(LIST);
                mw.aload(JSON_READER);
                if (itemClass == String.class) {
                    mw.invokevirtual(TYPE_JSON_READER, "readString", "()Ljava/lang/String;");
                }
                mw.invokeinterface("java/util/List", "add", "(Ljava/lang/Object;)Z");
                mw.pop();

                mw.goto_(loadList_);
            } else if (itemType instanceof Class) {
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "nextIfNullOrEmptyString", "()Z");
                mw.ifne(loadNull_);

                // nextIfNullOrEmptyString
                mw.new_(LIST_TYPE);
                mw.dup();
                if (initCapacity) {
                    mw.visitLdcInsn(10);
                    mw.invokespecial(LIST_TYPE, "<init>", "(I)V");
                } else {
                    mw.invokespecial(LIST_TYPE, "<init>", "()V");
                }
                mw.astore(LIST);

                mw.aload(JSON_READER);
                mw.aload(LIST);
                mw.visitLdcInsn((Class) itemType);
                mw.invokevirtual(TYPE_JSON_READER, "readArray", "(Ljava/util/List;Ljava/lang/reflect/Type;)V");

                mw.goto_(loadList_);
            }

            mw.visitLabel(skipValue_);
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "skipValue", "()V");

            mw.visitLabel(loadNull_);
            mw.aconst_null();
            mw.astore(LIST);
            mw.goto_(loadList_);

            mw.visitLabel(match_);
            mw.new_(LIST_TYPE);
            mw.dup();
            if (initCapacity) {
                mw.visitLdcInsn(10);
                mw.invokespecial(LIST_TYPE, "<init>", "(I)V");
            } else {
                mw.invokespecial(LIST_TYPE, "<init>", "()V");
            }
            mw.astore(LIST);
        }

        Label for_start_j_ = new Label(), for_end_j_ = new Label(), for_inc_j_ = new Label();
        mw.iconst_0();
        mw.istore(J);

        mw.visitLabel(for_start_j_);

        if (jsonb) {
            // j < item_cnt
            mw.iload(J);
            mw.iload(ITEM_CNT);
            mw.if_icmpge(for_end_j_);
        } else {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfArrayEnd", "()Z");
            mw.ifne(for_end_j_);
        }

        if (itemType == String.class) {
            mw.aload(LIST);
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readString", "()Ljava/lang/String;");
        } else if (itemType == Integer.class) {
            mw.aload(LIST);
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt32", "()Ljava/lang/Integer;");
        } else if (itemType == Long.class) {
            mw.aload(LIST);
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readInt64", "()Ljava/lang/Long;");
        } else {
            Label notNull_ = new Label();

            mw.aload(THIS);
            mw.getfield(classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);
            mw.ifnonnull(notNull_);

            mw.aload(THIS);
            mw.aload(THIS);
            mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_FIELD_READE, "getItemObjectReader", METHOD_DESC_GET_ITEM_OBJECT_READER);

            mw.putfield(classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);

            mw.visitLabel(notNull_);

            if (!context.disableReferenceDetect()) {
                mw.aload(JSON_READER);
                mw.aload(LIST);
                mw.iload(J);
                mw.invokevirtual(TYPE_JSON_READER, "readReference", "(Ljava/util/List;I)Z");
                mw.ifne(for_inc_j_);
            }
            mw.aload(LIST);

            Label readObject_ = new Label(), readObjectEnd_ = new Label();
            if (arrayMapping) {
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isArray", "()Z");
                mw.ifeq(readObject_);

                mw.aload(THIS);
                mw.getfield(classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);

                mw.aload(JSON_READER);
                gwGetFieldType(classNameType, mw, THIS, i, fieldType);
                mw.visitLdcInsn(fieldReader.fieldName);
                mw.lload(FEATURES);
                mw.invokeinterface(
                        TYPE_OBJECT_READER,
                        jsonb ? "readArrayMappingJSONBObject" : "readArrayMappingObject",
                        METHOD_DESC_READ_OBJECT);

                mw.goto_(readObjectEnd_);

                mw.visitLabel(readObject_);
            }

            mw.aload(THIS);
            mw.getfield(classNameType, ITEM_OBJECT_READER, DESC_OBJECT_READER);

            mw.aload(JSON_READER);
            gwGetFieldType(classNameType, mw, THIS, i, fieldType);
            mw.visitLdcInsn(fieldReader.fieldName);
            mw.lload(FEATURES);
            mw.invokeinterface(
                    TYPE_OBJECT_READER,
                    jsonb ? "readJSONBObject" : "readObject",
                    METHOD_DESC_READ_OBJECT);

            if (arrayMapping) {
                mw.visitLabel(readObjectEnd_);
            }
        }
        mw.invokeinterface("java/util/List", "add", "(Ljava/lang/Object;)Z");
        mw.pop();

        if (!jsonb) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfComma", "()Z");
            mw.pop();
        }

        mw.visitLabel(for_inc_j_);
        mw.visitIincInsn(J, 1);
        mw.goto_(for_start_j_);

        mw.visitLabel(for_end_j_);

        if (!jsonb) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfComma", "()Z");
            mw.pop();
        }

        mw.visitLabel(loadList_);
        mw.aload(LIST);
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

        mw.aload(THIS);
        mw.getfield(classNameType, fieldReader(i), DESC_FIELD_READER);
        mw.getfield(TYPE_FIELD_READE, "fieldType", "Ljava/lang/reflect/Type;");
    }

    static class ObjectWriteContext {
        final BeanInfo beanInfo;
        final Class objectClass;
        final ClassWriter cw;
        final boolean publicClass;
        final boolean externalClass;
        final FieldReader[] fieldReaders;
        final boolean hasStringField;
        final int fieldNameLengthMin;
        final int fieldNameLengthMax;

        public ObjectWriteContext(
                BeanInfo beanInfo,
                Class objectClass,
                ClassWriter cw,
                boolean externalClass,
                FieldReader[] fieldReaders
        ) {
            this.beanInfo = beanInfo;
            this.objectClass = objectClass;
            this.cw = cw;
            this.publicClass = objectClass == null || Modifier.isPublic(objectClass.getModifiers());
            this.externalClass = externalClass;
            this.fieldReaders = fieldReaders;

            int fieldNameLengthMin = 0, fieldNameLengthMax = 0;
            boolean hasStringField = false;
            for (int i = 0; i < fieldReaders.length; i++) {
                FieldReader fieldReader = fieldReaders[i];
                if (fieldReader.fieldClass == String.class) {
                    hasStringField = true;
                }

                byte[] nameUTF8 = fieldReader.fieldName.getBytes(StandardCharsets.UTF_8);
                int fieldNameLength = nameUTF8.length;
                for (byte ch : nameUTF8) {
                    if (ch <= 0) {
                        fieldNameLength = -1;
                        break;
                    }
                }

                if (i == 0) {
                    fieldNameLengthMin = fieldNameLength;
                    fieldNameLengthMax = fieldNameLength;
                } else {
                    fieldNameLengthMin = Math.min(fieldNameLength, fieldNameLengthMin);
                    fieldNameLengthMax = Math.max(fieldNameLength, fieldNameLengthMax);
                }
            }
            this.hasStringField = hasStringField;
            this.fieldNameLengthMin = fieldNameLengthMin;
            this.fieldNameLengthMax = fieldNameLengthMax;
        }

        public boolean disableSupportArrayMapping() {
            return (beanInfo.readerFeatures & FieldInfo.DISABLE_ARRAY_MAPPING) != 0;
        }

        public boolean disableReferenceDetect() {
            return (beanInfo.readerFeatures & FieldInfo.DISABLE_REFERENCE_DETECT) != 0;
        }

        public boolean disableAutoType() {
            return (beanInfo.readerFeatures & FieldInfo.DISABLE_AUTO_TYPE) != 0;
        }

        public boolean disableJSONB() {
            return (beanInfo.readerFeatures & FieldInfo.DISABLE_JSONB) != 0;
        }

        public boolean disableSmartMatch() {
            return (beanInfo.readerFeatures & FieldInfo.DISABLE_SMART_MATCH) != 0;
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
            mw.aload(THIS);
            mw.invokespecial("java/lang/Object", "<init>", "()V");

            mw.aload(THIS);
            mw.aload(CONSUMER);
            mw.putfield(classNameType, "consumer", "Ljava/util/function/Consumer;");

            mw.return_();
            mw.visitMaxs(3, 3);
        }

        {
            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "beforeRow",
                    "(I)V",
                    32
            );

            mw.aload(THIS);
            newObject(mw, TYPE_OBJECT, defaultConstructor);
            mw.putfield(classNameType, "object", DESC_OBJECT);

            mw.return_();
            mw.visitMaxs(3, 3);
        }

        {
            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "afterRow",
                    "(I)V",
                    32
            );

            mw.aload(THIS);
            mw.getfield(classNameType, "consumer", "Ljava/util/function/Consumer;");
            mw.aload(THIS);
            mw.getfield(classNameType, "object", DESC_OBJECT);
            mw.invokeinterface("java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V");

            mw.aload(THIS);
            mw.aconst_null();
            mw.putfield(classNameType, "object", DESC_OBJECT);

            mw.return_();
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

            mw.iload(LEN);
            mw.ifne(L0_);
            mw.return_();

            mw.visitLabel(L0_);
            mw.iload(COLUMN);
            mw.ifge(L1_);
            mw.return_();

            mw.visitLabel(L1_);
            mw.iload(COLUMN);
            mw.visitLdcInsn(fieldReaderArray.length);
            mw.if_icmple(switch_);
            mw.return_();

            mw.visitLabel(switch_);

            Label dflt = new Label();
            Label[] labels = new Label[fieldReaderArray.length];
            int[] columns = new int[fieldReaderArray.length];
            for (int i = 0; i < columns.length; i++) {
                columns[i] = i;
                labels[i] = new Label();
            }

            mw.iload(COLUMN);
            mw.visitLookupSwitchInsn(dflt, columns, labels);

            for (int i = 0; i < labels.length; i++) {
                mw.visitLabel(labels[i]);
                FieldReader fieldReader = fieldReaderArray[i];
                Field field = fieldReader.field;
                Class fieldClass = fieldReader.fieldClass;
                Type fieldType = fieldReader.fieldType;

                mw.aload(THIS);
                mw.getfield(classNameType, "object", DESC_OBJECT);

                String DESC_FIELD_CLASS, DESC_METHOD;
                if (fieldType == Integer.class
                        || fieldType == int.class
                        || fieldType == Short.class
                        || fieldType == short.class
                        || fieldType == Byte.class
                        || fieldType == byte.class
                ) {
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    mw.invokestatic(TYPE_TYPE_UTILS, "parseInt", bytes ? "([BII)I" : "([CII)I");

                    if (fieldType == short.class) {
                        DESC_FIELD_CLASS = "S";
                        DESC_METHOD = "(S)V";
                    } else if (fieldType == Short.class) {
                        mw.invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                        DESC_FIELD_CLASS = "Ljava/lang/Short;";
                        DESC_METHOD = "(Ljava/lang/Short;)V";
                    } else if (fieldType == byte.class) {
                        DESC_FIELD_CLASS = "B";
                        DESC_METHOD = "(B)V";
                    } else if (fieldType == Byte.class) {
                        mw.invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                        DESC_FIELD_CLASS = "Ljava/lang/Byte;";
                        DESC_METHOD = "(Ljava/lang/Byte;)V";
                    } else if (fieldType == int.class) {
                        DESC_FIELD_CLASS = "I";
                        DESC_METHOD = "(I)V";
                    } else {
                        mw.invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                        DESC_FIELD_CLASS = "Ljava/lang/Integer;";
                        DESC_METHOD = "(Ljava/lang/Integer;)V";
                    }
                } else if (fieldType == Long.class || fieldType == long.class) {
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    mw.invokestatic(TYPE_TYPE_UTILS, "parseLong", bytes ? "([BII)J" : "([CII)J");
                    if (fieldType == long.class) {
                        DESC_FIELD_CLASS = "J";
                        DESC_METHOD = "(J)V";
                    } else {
                        mw.invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                        DESC_FIELD_CLASS = "Ljava/lang/Long;";
                        DESC_METHOD = "(Ljava/lang/Long;)V";
                    }
                } else if (fieldType == Float.class || fieldType == float.class) {
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    mw.invokestatic(TYPE_TYPE_UTILS, "parseFloat", bytes ? "([BII)F" : "([CII)F");

                    if (fieldType == float.class) {
                        DESC_FIELD_CLASS = "F";
                        DESC_METHOD = "(F)V";
                    } else {
                        mw.invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                        DESC_FIELD_CLASS = "Ljava/lang/Float;";
                        DESC_METHOD = "(Ljava/lang/Float;)V";
                    }
                } else if (fieldType == Double.class || fieldType == double.class) {
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    mw.invokestatic(TYPE_TYPE_UTILS, "parseDouble", bytes ? "([BII)D" : "([CII)D");

                    if (fieldType == double.class) {
                        DESC_FIELD_CLASS = "D";
                        DESC_METHOD = "(D)V";
                    } else {
                        mw.invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                        DESC_FIELD_CLASS = "Ljava/lang/Double;";
                        DESC_METHOD = "(Ljava/lang/Double;)V";
                    }
                } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    mw.invokestatic(TYPE_TYPE_UTILS, "parseBoolean", bytes ? "([BII)Ljava/lang/Boolean;" : "([CII)Ljava/lang/Boolean;");

                    if (fieldType == boolean.class) {
                        mw.invokevirtual("java/lang/Boolean", "booleanValue", "()Z");
                        DESC_FIELD_CLASS = "Z";
                        DESC_METHOD = "(Z)V";
                    } else {
                        DESC_FIELD_CLASS = "Ljava/lang/Boolean;";
                        DESC_METHOD = "(Ljava/lang/Boolean;)V";
                    }
                } else if (fieldType == Date.class) {
                    mw.new_("java/util/Date");

                    // long millis = DateUtils.parseMillis(bytes, off, len, charset);
                    mw.dup();
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    if (bytes) {
                        mw.aload(CHARSET);
                        mw.invokestatic(TYPE_DATE_UTILS, "parseMillis", "([BIILjava/nio/charset/Charset;)J");
                    } else {
                        mw.invokestatic(TYPE_DATE_UTILS, "parseMillis", "([CII)J");
                    }
                    mw.invokespecial("java/util/Date", "<init>", "(J)V");

                    DESC_FIELD_CLASS = "Ljava/util/Date;";
                    DESC_METHOD = "(Ljava/util/Date;)V";
                } else if (fieldType == BigDecimal.class) {
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    mw.invokestatic(TYPE_TYPE_UTILS, "parseBigDecimal", bytes ? "([BII)Ljava/math/BigDecimal;" : "([CII)Ljava/math/BigDecimal;");

                    DESC_FIELD_CLASS = "Ljava/math/BigDecimal;";
                    DESC_METHOD = "(Ljava/math/BigDecimal;)V";
                } else {
                    mw.new_("java/lang/String");
                    mw.dup();
                    mw.aload(BYTES);
                    mw.iload(OFF);
                    mw.iload(LEN);
                    if (bytes) {
                        mw.aload(CHARSET);
                        mw.invokespecial("java/lang/String", "<init>", "([BIILjava/nio/charset/Charset;)V");
                    } else {
                        mw.invokespecial("java/lang/String", "<init>", "([CII)V");
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
                        mw.invokestatic(TYPE_TYPE_UTILS, "cast", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
                        mw.checkcast(ASMUtils.type(fieldClass));
                    }
                }

                if (fieldReader.method != null) {
                    if (fieldReader.method.getReturnType() != void.class) {
                        return null;
                    }

                    mw.invokevirtual(TYPE_OBJECT, fieldReader.method.getName(), DESC_METHOD);
                } else if (field != null) {
                    mw.putfield(TYPE_OBJECT, field.getName(), DESC_FIELD_CLASS);
                } else {
                    return null;
                }
                mw.goto_(dflt);
            }

            mw.visitLabel(dflt);

            mw.return_();
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
