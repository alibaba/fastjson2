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
import static com.alibaba.fastjson2.reader.ObjectReader.HASH_TYPE;
import static com.alibaba.fastjson2.reader.ObjectReaderCreatorASM.MethodWriterContext.*;
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
    static final String READ_FIELD_READER_UL = "(J" + DESC_JSON_READER + "JLjava/lang/Object;)V";
    static final String METHOD_DESC_ADD_RESOLVE_TASK = "(" + DESC_JSON_READER + "Ljava/lang/Object;Ljava/lang/String;)V";
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
        boolean match = fieldReaderArray.length <= 96;

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
    protected <T> ObjectReaderNoneDefaultConstructor createNoneDefaultConstructorObjectReader(
            Class objectClass,
            BeanInfo beanInfo,
            Function<Map<Long, Object>, T> constructorFunction,
            List<Constructor> alternateConstructors,
            String[] parameterNames,
            FieldReader[] paramFieldReaders,
            FieldReader[] fieldReaderArray
    ) {
        ObjectReaderNoneDefaultConstructor objectReaderAdapter = new ObjectReaderNoneDefaultConstructor(
                objectClass,
                beanInfo.typeKey,
                beanInfo.typeName,
                beanInfo.readerFeatures,
                constructorFunction,
                alternateConstructors,
                parameterNames,
                paramFieldReaders,
                fieldReaderArray,
                beanInfo.seeAlso,
                beanInfo.seeAlsoNames
        );

        boolean match = true;

        if (beanInfo.autoTypeBeforeHandler != null
                || fieldReaderArray.length != 0
                || (!(constructorFunction instanceof ConstructorFunction) && (!(constructorFunction instanceof FactoryFunction)))
                || (alternateConstructors != null && !alternateConstructors.isEmpty())
                || classLoader.isExternalClass(objectClass)
                || (beanInfo.readerFeatures & JSONReader.Feature.SupportAutoType.mask) != 0
                || (objectReaderAdapter.noneDefaultConstructor != null && objectReaderAdapter.noneDefaultConstructor.getParameterCount() != paramFieldReaders.length)
                || (constructorFunction instanceof FactoryFunction && ((FactoryFunction<T>) constructorFunction).paramNames.length != paramFieldReaders.length)
                || paramFieldReaders.length > 64
        ) {
            match = false;
        }

        if (match) {
            for (FieldReader fieldReader : paramFieldReaders) {
                if (fieldReader.getInitReader() != null) {
                    match = false;
                    break;
                }

                if (fieldReader.defaultValue != null || fieldReader.schema != null) {
                    match = false;
                    break;
                }

                Class fieldClass = fieldReader.fieldClass;
                if (fieldClass != null && (!Modifier.isPublic(fieldClass.getModifiers()) || classLoader.isExternalClass(fieldClass))) {
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

        if (!match) {
            return objectReaderAdapter;
        }

        boolean externalClass = objectClass != null && classLoader.isExternalClass(objectClass);
        ClassWriter cw = new ClassWriter(
                (e) -> objectClass.getName().equals(e) ? objectClass : null
        );

        beanInfo.readerFeatures |= FieldInfo.DISABLE_REFERENCE_DETECT;
        ObjectReadContext context = new ObjectReadContext(beanInfo, objectClass, cw, externalClass, paramFieldReaders, null);
        context.objectReaderAdapter = objectReaderAdapter;

        genFields(paramFieldReaders, cw, TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR);

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, context.classNameType, TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR, new String[]{});

        {
            String MD_INIT = "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/function/Function;Ljava/util/List;[Ljava/lang/String;[Lcom/alibaba/fastjson2/reader/FieldReader;[Lcom/alibaba/fastjson2/reader/FieldReader;[Ljava/lang/Class;[Ljava/lang/String;)V";

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    MD_INIT,
                    fieldReaderArray.length <= 12 ? 32 : 128);
            mw.aload(THIS);
            mw.aload(1); // CLASS
            mw.aload(2); // TYPE_KEY
            mw.aload(3); // TYPE_NAME
            mw.lload(4); // FEATURES
            mw.aload(6); // CREATOR
            mw.aload(7); // alternateConstructors
            mw.aload(8); // paramNames
            mw.aload(9); // paramFieldReaders
            mw.aload(10); // setterFieldReaders
            mw.aload(11); // seeAlso
            mw.aload(12); // seeAlsoNames
            mw.invokespecial(TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR, "<init>", MD_INIT);

            int FIELD_READER_ARRAY = 9;
            genInitFields(paramFieldReaders, context.classNameType, true, FIELD_READER_ARRAY, mw, TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR);

            mw.return_();
            mw.visitMaxs(3, 3);
        }

        genMethodReadObject(context, beanInfo.readerFeatures);

        if (!context.disableJSONB()) {
            genMethodReadJSONBObject(context, beanInfo.readerFeatures);
        }

        byte[] code = cw.toByteArray();
        try {
            Class<?> readerClass = classLoader.defineClassPublic(context.classNameFull, code, 0, code.length);

            Constructor<?> constructor = readerClass.getConstructors()[0];
            return (ObjectReaderNoneDefaultConstructor) constructor
                    .newInstance(
                            objectClass,
                            beanInfo.typeKey,
                            beanInfo.typeName,
                            beanInfo.readerFeatures,
                            constructorFunction,
                            alternateConstructors,
                            parameterNames,
                            paramFieldReaders,
                            fieldReaderArray,
                            null,
                            null
                    );
        } catch (Throwable e) {
            throw new JSONException(
                    "create objectReader error"
                            + (objectClass == null ? "" : ", objectType " + objectClass.getTypeName()), e);
        }
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

        ObjectReadContext context = new ObjectReadContext(beanInfo, objectClass, cw, externalClass, fieldReaderArray, defaultConstructor);

        final boolean generatedFields = fieldReaderArray.length <= 96;

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

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, context.classNameType, objectReaderSuper, new String[]{});

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

            genInitFields(fieldReaderArray, context.classNameType, generatedFields, FIELD_READER_ARRAY, mw, objectReaderSuper);

            mw.return_();
            mw.visitMaxs(3, 3);
        }

        {
            String methodName = fieldBased && defaultConstructor == null ? "createInstance0" : "createInstance";

            if ((externalClass && defaultConstructor != null)
                    || fieldBased && (defaultConstructor == null || !Modifier.isPublic(defaultConstructor.getModifiers()) || !Modifier.isPublic(objectClass.getModifiers()))) {
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
                newObject(mw, context.objectType, defaultConstructor);
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

            context.objectReaderAdapter = new ObjectReaderAdapter(objectClass, beanInfo.typeKey, beanInfo.typeName, readerFeatures, null, supplier, null, fieldReaderArray);

            if (!disableJSONB) {
                genMethodReadJSONBObject(context, readerFeatures);
                if (!disableArrayMapping) {
                    genMethodReadJSONBObjectArrayMapping(context, readerFeatures);
                }
            }

            genMethodReadObject(context, readerFeatures);

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
                genMethodGetFieldReader(context);
                genMethodGetFieldReaderLCase(context);
            }
        }

        byte[] code = cw.toByteArray();
        try {
            Class<?> readerClass = classLoader.defineClassPublic(context.classNameFull, code, 0, code.length);

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

    private void genMethodGetFieldReader(ObjectReadContext context) {
        ObjectReaderAdapter objectReaderAdapter = context.objectReaderAdapter;
        FieldReader[] fieldReaderArray = context.fieldReaders;
        MethodWriter mw = context.cw.visitMethod(
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
                    mw.getfield(context.classNameType, fieldReader(index), DESC_FIELD_READER);
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
                mw.getfield(context.classNameType, fieldReader(i), DESC_FIELD_READER);
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

    private void genMethodGetFieldReaderLCase(ObjectReadContext context) {
        ObjectReaderAdapter objectReaderAdapter = context.objectReaderAdapter;
        FieldReader[] fieldReaderArray = context.fieldReaders;
        MethodWriter mw = context.cw.visitMethod(
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
                    mw.getfield(context.classNameType, fieldReader(index), DESC_FIELD_READER);
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
                mw.getfield(context.classNameType, fieldReader(i), DESC_FIELD_READER);
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
            int FIELD_READER_ARRAY,
            MethodWriter mw,
            String objectReaderSuper
    ) {
        if ((objectReaderSuper != TYPE_OBJECT_READER_ADAPTER && objectReaderSuper != TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR) || !generatedFields) {
            return;
        }

        for (int i = 0; i < fieldReaderArray.length; i++) {
            mw.aload(THIS);
            mw.aload(FIELD_READER_ARRAY);
            mw.iconst_n(i);
            mw.aaload();
            mw.putfield(classNameType, fieldReader(i), DESC_FIELD_READER);
        }
    }

    private void genFields(FieldReader[] fieldReaderArray, ClassWriter cw, String objectReaderSuper) {
        if (objectReaderSuper == TYPE_OBJECT_READER_ADAPTER || objectReaderSuper == TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR) {
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
                cw.visitField(Opcodes.ACC_PUBLIC, fieldItemObjectReader(i), DESC_OBJECT_READER);
            }
        }
    }

    /**
     *  <blockquote><pre>
     *      class Bean {
     *          private String field1;
     *          private int field2;
     *      }
     *
     *      public void readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
     *          features = jsonReader.features(features);
     *          if (nextIfNull()) {
     *              return null;
     *          }
     *
     *          jsonReader.errorOnNoneSerializable(this.objectClass);
     *
     *          if (jsonReader.isArray() && jsonReader.isSupportBeanArray()) {
     *              return readArrayMappingObject(jsonReader, fieldType, fieldName, features);
     *          }
     *
     *          Bean object = new Bean();
     *
     *          jsonReader.nextIfObjectStart();
     *
     *          for (;;) {
     *              if (jsonReader.nextIfObjectEnd()) {
     *                  break;
     *              }
     *              switch(jsonReader.getRawInt()) {
     *                  case field1NameHash32:
     *                      if(jsonReader.nextIfName4Match2()) {
     *                          object.field1 = jsonReader.readString();
     *                          break;
     *                      }
     *                      goto hashCode64Start;
     *                  case field2NameHash32:
     *                      if(jsonReader.nextIfName4Match2()) {
     *                          object.field2 = jsonReader.readInt();
     *                          break;
     *                      }
     *                      goto hashCode64Start;
     *                  default:
     *                      goto hashCode64Start;
     *              }
     *
     *              hashCode64Start:
     *              long hashCode64 = readFieldNameHashCode();
     *
     *              if (this.typeKeyHashCode == hashCode64) {
     *                  object = this.autoType(jsonReader);
     *              }
     *
     *              switch(jsonReader.getFieldOrdinal(hashCode64)) {
     *                  case 0:
     *                      object.field1 = jsonReader.readString();
     *              }
     *          }
     *
     *
     *      }
     *  </pre></blockquote>
     */
    private <T> void genMethodReadJSONBObject(ObjectReadContext context, long readerFeatures) {
        String classNameType = context.classNameType;
        FieldReader[] fieldReaderArray = context.fieldReaders;
        Class objectClass = context.objectClass;
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReaderAdapter objectReaderAdapter = context.objectReaderAdapter;

        MethodWriter mw = context.cw.visitMethod(Opcodes.ACC_PUBLIC,
                "readJSONBObject",
                METHOD_DESC_READ_OBJECT,
                2048
        );

        boolean disableArrayMapping = context.disableSupportArrayMapping();
        boolean disableAutoType = context.disableAutoType();

        MethodWriterContext mwc = new MethodWriterContext(mw, 6, true);
        mw.aload(JSON_READER);
        mw.lload(FEATURES);
        mw.invokevirtual(TYPE_JSON_READER, "features", "(J)J");
        mw.lstore(FEATURES);

        final int OBJECT = mwc.var("object");
        final int I = mwc.var("I");
        final int HASH_CODE64 = mwc.var2("hashCode64");
        final int HASH_CODE_32 = mwc.var("hashCode32");
        final int FIELD_READER = mwc.var("fieldReader");

        if (!disableAutoType) {
            genCheckAutoType(classNameType, mwc);
        }

        {
            /*
             * if (jsonReader.nextIfNull()) {
             *      return null;
             * }
             */
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
            Label L0 = new Label();

            // if (jsonReader.isArray() && jsonReader.isSupportBeanArray()) {
            {
                Label startArray_ = new Label(), endArray_ = new Label();
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isArray", "()Z");
                mw.ifeq(L0);

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "isSupportBeanArray", "()Z");
                mw.ifeq(endArray_);

                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.aload(FIELD_TYPE);
                mw.aload(FIELD_NAME);
                mw.lload(FEATURES);
                mw.invokevirtual(classNameType, "readArrayMappingObject", METHOD_DESC_READ_OBJECT);
                mw.areturn();

                mw.visitLabel(endArray_);
            }

            mw.visitLabel(L0);
        }

        if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
            {
                /*
                 * if (jsonReader.hasAutoTypeBeforeHandler()
                 *      || (features & (JSONReader.Feature.SupportSmartMatch.mask | JSONReader.Feature.SupportAutoType.mask)) != 0
                 * ) {
                 *     return super.readObject(jsonReader, fieldType, fieldName, features);
                 * }
                 */
                Label L3 = new Label(), L4 = new Label();
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "hasAutoTypeBeforeHandler", "()Z");
                mw.ifne(L3);

                mw.lload(FEATURES);
                mw.visitLdcInsn(JSONReader.Feature.SupportSmartMatch.mask | JSONReader.Feature.SupportAutoType.mask);
                mw.land();
                mw.lconst_0();
                mw.lcmp();
                mw.ifeq(L4);

                mw.visitLabel(L3);
                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.aload(FIELD_TYPE);
                mw.aload(FIELD_NAME);
                mw.lload(FEATURES);
                mw.invokespecial(TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR, "readJSONBObject", METHOD_DESC_READ_OBJECT);
                mw.areturn();

                mw.visitLabel(L4);
            }

            genInitForNonDefaultConstructor(fieldReaderArray, mwc);
        } else {
            genCreateObject(mw, context, classNameType);
            mw.astore(OBJECT);
        }

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectStart", "()Z");
        mw.pop();

        // for (int i = 0; i < entry_cnt; ++i) {
        Label L_FOR_START = new Label(), L_FOR_END = new Label(), L_FOR_INC = new Label();
        if (!disableAutoType) {
            mw.iconst_0();
            mw.istore(I);
        }

        mw.visitLabel(L_FOR_START);

        Label hashCode64Start = new Label();

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectEnd", "()Z");
        mw.ifne(L_FOR_END);

        boolean switchGen = false;
        if (context.fieldNameLengthMin >= 2 && context.fieldNameLengthMax <= 43) {
            genRead243(
                    context,
                    fieldBased,
                    mwc,
                    OBJECT,
                    L_FOR_INC,
                    hashCode64Start
            );
            switchGen = true;
        }

        mw.visitLabel(hashCode64Start);

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "readFieldNameHashCode", "()J");
        mw.dup2();
        mw.lstore(HASH_CODE64);
        mw.lconst_0();
        mw.lcmp();
        mw.ifeq(L_FOR_INC);

        if (!disableAutoType && !(context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor)) {
            /*
             * if (hashCode64 == this.typeKeyHashCode() && hashCode64 != 0) {
             *      object = this.autoType(jsonReader);
             * }
             */
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
            mw.goto_(L_FOR_END);

            mw.visitLabel(endAutoType_);
        }

        if (switchGen) {
            if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
                genReadHashCode64ValueForNonDefaultConstructor(context, mwc, HASH_CODE64, fieldBased, OBJECT, L_FOR_INC);
            } else {
                /*
                 * this.readFieldValue(hashCode64, jsonReader, features, object);
                 */
                mw.aload(THIS);
                mw.lload(HASH_CODE64);
                mw.aload(JSON_READER);
                mw.lload(FEATURES);
                mw.aload(OBJECT);
                mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "readFieldValue", READ_FIELD_READER_UL);
            }
            mw.goto_(L_FOR_INC); // continue
            // continue
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

                    genReadFieldValue(
                            context,
                            fieldReader,
                            fieldBased,
                            mwc,
                            OBJECT,
                            index,
                            true // JSONB
                    );
                    mw.goto_(L_FOR_INC);

                    if (next != dflt) {
                        mw.visitLabel(next);
                    }
                }

                mw.goto_(L_FOR_INC);
            }

            // switch_default
            mw.visitLabel(dflt);

            boolean disableSmartMatch = context.disableSmartMatch();
            if (!disableSmartMatch && !(context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor)) {
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
                mw.goto_(L_FOR_INC); // continue

                mw.visitLabel(fieldReaderNull_);
            }
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

                genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        mwc,
                        OBJECT,
                        i,
                        false // arrayMapping
                );

                mw.goto_(L_FOR_INC); // continue

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

                genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        mwc,
                        OBJECT,
                        i,
                        false // arrayMapping
                );

                mw.goto_(L_FOR_INC); // continue

                mw.visitLabel(next_);
            }
            mw.visitLabel(processExtra_);
        }

        if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "skipValue", "()V");
        } else {
            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.aload(OBJECT);
            mw.lload(FEATURES);
            mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "processExtra", METHOD_DESC_PROCESS_EXTRA);
        }
        mw.goto_(L_FOR_INC); // continue

        mw.visitLabel(L_FOR_INC);
        if (!disableAutoType) {
            mw.visitIincInsn(I, 1);
        }
        mw.goto_(L_FOR_START);

        mw.visitLabel(L_FOR_END);

        if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
            createObjectForNonConstructor(context, mwc);
        } else {
            mw.aload(OBJECT);
        }
        mw.areturn();

        mw.visitMaxs(5, 10);
    }

    private void genReadHashCode64ValueForNonDefaultConstructor(
            ObjectReadContext context,
            MethodWriterContext mwc,
            int HASH_CODE64,
            boolean fieldBased,
            int OBJECT,
            Label L_FOR_INC
    ) {
        /*
         *  swith(this.getFieldOrdinal(hashCode64)) {
         *      case 0:
         *          fieldValue0 = ...;
         *          break;
         *      case 1:
         *          fieldValue0 = ...;
         *          break;
         *      default:
         *          skipValue();
         *          break;
         *  }
         *  goto
         */
        FieldReader[] fieldReaderArray = context.fieldReaders;
        MethodWriter mw = mwc.mw;
        mw.aload(THIS);
        mw.lload(HASH_CODE64);
        mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "getFieldOrdinal", "(J)I");

        Label dflt = new Label();
        Label[] labels = new Label[fieldReaderArray.length];
        int[] switchKeys = new int[fieldReaderArray.length];
        for (int i = 0; i < fieldReaderArray.length; i++) {
            labels[i] = new Label();
            switchKeys[i] = i;
        }

        mw.visitLookupSwitchInsn(dflt, switchKeys, labels);

        for (int i = 0; i < fieldReaderArray.length; i++) {
            mw.visitLabel(labels[i]);
            FieldReader fieldReader = fieldReaderArray[i];
            genReadFieldValue(
                    context,
                    fieldReader,
                    fieldBased,
                    mwc,
                    OBJECT,
                    i,
                    false
            );
            mw.goto_(L_FOR_INC);
        }

        // jsonReader.skipValue();
        mw.visitLabel(dflt);
        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "skipValue", "()V");
    }

    private <T> void genMethodReadJSONBObjectArrayMapping(ObjectReadContext context, long readerFeatures) {
        FieldReader[] fieldReaderArray = context.fieldReaders;
        String classNameType = context.classNameType;
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        MethodWriter mw = context.cw.visitMethod(Opcodes.ACC_PUBLIC,
                "readArrayMappingJSONBObject",
                METHOD_DESC_READ_OBJECT,
                512
        );

        MethodWriterContext mwc = new MethodWriterContext(mw, 6, true);
        mw.aload(JSON_READER);
        mw.lload(FEATURES);
        mw.invokevirtual(TYPE_JSON_READER, "features", "(J)J");
        mw.lstore(FEATURES);

        final int OBJECT = mwc.var("object");
        final int ENTRY_CNT = mwc.var("entryCnt");

        if (!context.disableAutoType()) {
            genCheckAutoType(classNameType, mwc);
        }

        {
            Label notNull_ = new Label();
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfNull", "()Z");
            mw.ifeq(notNull_);
            mw.aconst_null();
            mw.areturn();
            mw.visitLabel(notNull_);
        }

        genCreateObject(mw, context, classNameType);
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
            genReadFieldValue(
                    context,
                    fieldReader,
                    fieldBased,
                    mwc,
                    OBJECT,
                    i,
                    true // arrayMapping
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

    private void genCheckAutoType(String classNameType, MethodWriterContext mwc) {
        MethodWriter mw = mwc.mw;
        int AUTO_TYPE_OBJECT_READER = mwc.var("autoTypeObjectReader");
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

    private <T> void genMethodReadObject(ObjectReadContext context, long readerFeatures) {
        FieldReader[] fieldReaderArray = context.fieldReaders;
        String classNameType = context.classNameType;
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        MethodWriter mw = context.cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "readObject",
                METHOD_DESC_READ_OBJECT,
                2048
        );

        MethodWriterContext mwc = new MethodWriterContext(mw, 6, false);

        final int OBJECT = mwc.var("object");
        final int I = mwc.var("I");
        final int HASH_CODE64 = mwc.var2("hashCode64");
        final int HASH_CODE_32 = mwc.var("hashCode32");
        final int FIELD_READER = mwc.var("fieldReader");

        boolean disableArrayMapping = context.disableSupportArrayMapping();
        boolean disableAutoType = context.disableAutoType();
        boolean disableJSONB = context.disableJSONB();
        boolean disableSmartMatch = context.disableSmartMatch();

        if (!disableJSONB) {
            /*
             * if (jsonReader.json) {
             *     return readJSONBObject(jsonReader, fieldType, fieldName, features);
             * }
             */
            Label L0 = new Label();
            mw.aload(JSON_READER);
            mw.getfield(TYPE_JSON_READER, "jsonb", "Z");
            mw.ifeq(L0);

            mw.aload(THIS);
            mw.aload(JSON_READER);
            mw.aload(FIELD_TYPE);
            mw.aload(FIELD_NAME);
            mw.lload(FEATURES);
            mw.invokevirtual(classNameType, "readJSONBObject", METHOD_DESC_READ_OBJECT);
            mw.areturn();

            mw.visitLabel(L0);
        }

        /*
         * long features = jsonReader.features(features);
         */
        mw.aload(JSON_READER);
        mw.lload(FEATURES);
        mw.invokevirtual(TYPE_JSON_READER, "features", "(J)J");
        mw.lstore(FEATURES);

        if (!disableSmartMatch || !disableArrayMapping) {
            /*
             * if (jsonReader.isArray()) {
             *     if ((features & JSONReader.Feature.SupportArrayToBean.mask) != 0) {
             *         return readArrayMappingObject(jsonReader, fieldType, fieldName, features);
             *     }
             *     return processObjectInputSingleItemArray(jsonReader, fieldType, fieldName, features);
             * }
             */
            Label L1 = new Label();
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "isArray", "()Z");
            mw.ifeq(L1);

            if (!disableArrayMapping) {
                Label singleItemArray_ = new Label();

                if ((readerFeatures & JSONReader.Feature.SupportArrayToBean.mask) == 0) {
                    mw.aload(JSON_READER);
                    mw.lload(FEATURES);
                    mw.invokevirtual(TYPE_JSON_READER, "isSupportBeanArray", "(J)Z");
                    mw.ifeq(singleItemArray_);
                }

                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.aload(FIELD_TYPE);
                mw.aload(FIELD_NAME);
                mw.lload(FEATURES);
                mw.invokevirtual(classNameType, "readArrayMappingObject", METHOD_DESC_READ_OBJECT);
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

            mw.visitLabel(L1);
        }

        Label end_ = new Label();

        {
            /*
             * if (jsonReader.nextIfObjectStart()) {
             *    if (jsonReader.nextIfNull()) {
             *         return null;
             *    }
             * }
             */
            Label L2 = new Label();

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectStart", "()Z");
            mw.ifne(L2);

            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "nextIfNullOrEmptyString", "()Z");
            mw.ifeq(L2);

            mw.aconst_null();
            mw.areturn();

            mw.visitLabel(L2);
        }

        if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
            {
                /*
                 * if (jsonReader.hasAutoTypeBeforeHandler()
                 *      || (features & (JSONReader.Feature.SupportSmartMatch.mask | JSONReader.Feature.SupportAutoType.mask)) != 0
                 * ) {
                 *     return super.readObject(jsonReader, fieldType, fieldName, features);
                 * }
                 */
                Label L3 = new Label(), L4 = new Label();
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "hasAutoTypeBeforeHandler", "()Z");
                mw.ifne(L3);

                mw.lload(FEATURES);
                mw.visitLdcInsn(JSONReader.Feature.SupportSmartMatch.mask | JSONReader.Feature.SupportAutoType.mask);
                mw.land();
                mw.lconst_0();
                mw.lcmp();
                mw.ifeq(L4);

                mw.visitLabel(L3);
                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.aload(FIELD_TYPE);
                mw.aload(FIELD_NAME);
                mw.lload(FEATURES);
                mw.invokespecial(TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR, "readObject", METHOD_DESC_READ_OBJECT);
                mw.areturn();

                mw.visitLabel(L4);
            }

            genInitForNonDefaultConstructor(fieldReaderArray, mwc);
        } else {
            genCreateObject(mw, context, classNameType);
            mw.astore(OBJECT);
        }

        /*
         * for (int i = 0; i < entry_cnt; ++i) {
         *    long hashCode64 = jsonReader.readFieldNameHashCode();
         * }
         */
        Label L_FOR_START = new Label(), L_FOR_END = new Label(), L_FOR_INC = new Label();

        if (!disableAutoType) {
            mw.iconst_0();
            mw.istore(I);
        }
        mw.visitLabel(L_FOR_START);

        Label hashCode64Start = new Label();

        mw.aload(JSON_READER);
        mw.invokevirtual(TYPE_JSON_READER, "nextIfObjectEnd", "()Z");
        mw.ifne(L_FOR_END);

        boolean switchGen = false;
        if (context.fieldNameLengthMin >= 5 && context.fieldNameLengthMax <= 7) {
            genRead57(
                    context,
                    fieldBased,
                    mwc,
                    OBJECT,
                    L_FOR_INC,
                    hashCode64Start
            );
            switchGen = true;
        } else if (context.fieldNameLengthMin >= 2 && context.fieldNameLengthMax <= 43) {
            genRead243(
                    context,
                    fieldBased,
                    mwc,
                    OBJECT,
                    L_FOR_INC,
                    hashCode64Start
            );
            switchGen = true;
        }

        mw.visitLabel(hashCode64Start);

        {
            /*
             * long hashCode64 = jsonReader.readFieldNameHashCode();
             * if (hashCode64 == -1) {
             *     break;
             * }
             */
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readFieldNameHashCode", "()J");
            mw.dup2();
            mw.lstore(HASH_CODE64);

            mw.visitLdcInsn(-1L);
            mw.lcmp();
            mw.ifeq(L_FOR_END);
        }

        if (!disableAutoType && !(context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor)) {
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
            if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
                genReadHashCode64ValueForNonDefaultConstructor(context, mwc, HASH_CODE64, fieldBased, OBJECT, L_FOR_INC);
            } else {
                /*
                 * this.readFieldValue(hashCode64, jsonReader, features, object);
                 */
                mw.aload(THIS);
                mw.lload(HASH_CODE64);
                mw.aload(JSON_READER);
                mw.lload(FEATURES);
                mw.aload(OBJECT);
                mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "readFieldValue", READ_FIELD_READER_UL);
            }
            mw.goto_(L_FOR_INC); // continue
        } else if (fieldReaderArray.length > 6) {
            // use switch
            Map<Integer, List<Long>> map = new TreeMap();

            for (int i = 0; i < context.objectReaderAdapter.hashCodes.length; i++) {
                long hashCode64 = context.objectReaderAdapter.hashCodes[i];
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

                    int m = Arrays.binarySearch(context.objectReaderAdapter.hashCodes, hashCode64);
                    int index = context.objectReaderAdapter.mapping[m];

                    FieldReader fieldReader = fieldReaderArray[index];

                    genReadFieldValue(
                            context,
                            fieldReader,
                            fieldBased,
                            mwc,
                            OBJECT,
                            index,
                            false // arrayMapping
                    );
                    mw.goto_(L_FOR_INC);

                    if (next != dflt) {
                        mw.visitLabel(next);
                    }
                }

                mw.goto_(L_FOR_INC);
            }

            mw.visitLabel(dflt);

            if (!disableSmartMatch && !(context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor)) {
                Label fieldReaderNull_ = new Label();
                if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                    mw.lload(FEATURES);
                    mw.visitLdcInsn(JSONReader.Feature.SupportSmartMatch.mask);
                    mw.land();
                    mw.lconst_0();
                    mw.lcmp();
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
                mw.goto_(L_FOR_INC); // continue

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
                genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        mwc,
                        OBJECT,
                        i,
                        false // arrayMapping
                );

                mw.goto_(L_FOR_INC); // continue

                mw.visitLabel(next_);
            }

            Label processExtra_ = new Label();

            if (!disableSmartMatch) {
                if ((readerFeatures & JSONReader.Feature.SupportSmartMatch.mask) == 0) {
                    mw.lload(FEATURES);
                    mw.visitLdcInsn(JSONReader.Feature.SupportSmartMatch.mask);
                    mw.land();
                    mw.lconst_0();
                    mw.lcmp();
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
                    genReadFieldValue(
                            context,
                            fieldReader,
                            fieldBased,
                            mwc,
                            OBJECT,
                            i,
                            false // arrayMapping
                    );

                    mw.goto_(L_FOR_INC); // continue

                    mw.visitLabel(next_);
                }
            }

            mw.visitLabel(processExtra_);
        }
        if (!switchGen) {
            if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "skipValue", "()V");
            } else {
                mw.aload(THIS);
                mw.aload(JSON_READER);
                mw.aload(OBJECT);
                mw.lload(FEATURES);
                mw.invokevirtual(TYPE_OBJECT_READER_ADAPTER, "processExtra", METHOD_DESC_PROCESS_EXTRA);
            }
            mw.goto_(L_FOR_INC); // continue
        }

        mw.visitLabel(L_FOR_INC);
        if (!disableAutoType) {
            mw.visitIincInsn(I, 1);
        }
        mw.goto_(L_FOR_START);

        mw.visitLabel(L_FOR_END);

        mw.visitLabel(end_);

        if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
            createObjectForNonConstructor(context, mwc);
        } else {
            mw.aload(OBJECT);
        }
        mw.areturn();

        mw.visitMaxs(5, 10);
    }

    private void createObjectForNonConstructor(ObjectReadContext context, MethodWriterContext mwc) {
        FieldReader[] fieldReaderArray = context.fieldReaders;
        MethodWriter mw = mwc.mw;
        ObjectReaderNoneDefaultConstructor objectReaderNoneDefaultConstructor = (ObjectReaderNoneDefaultConstructor) context.objectReaderAdapter;
        boolean constructDirect = true;
        if (classLoader.isExternalClass(context.objectClass)
                || context.objectClass.getTypeParameters().length != 0
                || (objectReaderNoneDefaultConstructor.constructor != null && !Modifier.isPublic(objectReaderNoneDefaultConstructor.constructor.getModifiers()))
                || (context.objectClass != null && !Modifier.isPublic(context.objectClass.getModifiers()))
                || objectReaderNoneDefaultConstructor.factoryFunction != null
                || objectReaderNoneDefaultConstructor.noneDefaultConstructor != null && !Modifier.isPublic(objectReaderNoneDefaultConstructor.noneDefaultConstructor.getModifiers())
        ) {
            constructDirect = false;
        }

        if (constructDirect) {
            mw.new_(context.objectType);
            mw.dup();
            StringBuilder buf = new StringBuilder().append("(");
            for (FieldReader fieldReader : fieldReaderArray) {
                mw.loadLocal(fieldReader.fieldClass, mwc.var(fieldReader));
                buf.append(ASMUtils.desc(fieldReader.fieldClass));
            }
            buf.append(")V");
            mw.invokespecial(context.objectType, "<init>", buf.toString());
        } else {
            mw.aload(THIS);
            mw.iconst_n(fieldReaderArray.length);
            mw.anewArray("java/lang/Object");
            for (int i = 0; i < fieldReaderArray.length; i++) {
                FieldReader fieldReader = fieldReaderArray[i];
                mw.dup();
                mw.iconst_n(i);
                mw.loadLocal(fieldReader.fieldClass, mwc.var(fieldReader));
                if (fieldReader.fieldClass == int.class) {
                    mw.invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                } else if (fieldReader.fieldClass == long.class) {
                    mw.invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                } else if (fieldReader.fieldClass == float.class) {
                    mw.invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                } else if (fieldReader.fieldClass == double.class) {
                    mw.invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                } else if (fieldReader.fieldClass == boolean.class) {
                    mw.invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                } else if (fieldReader.fieldClass == short.class) {
                    mw.invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                } else if (fieldReader.fieldClass == byte.class) {
                    mw.invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                } else if (fieldReader.fieldClass == char.class) {
                    mw.invokestatic("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
                }
                mw.aastore();
            }
            mw.invokevirtual(TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR, "createInstance", "([Ljava/lang/Object;)Ljava/lang/Object;");
        }
    }

    private static void genInitForNonDefaultConstructor(FieldReader[] fieldReaderArray, MethodWriterContext mwc) {
        MethodWriter mw = mwc.mw;
        for (FieldReader fieldReader : fieldReaderArray) {
            Class fieldClass = fieldReader.fieldClass;
            int var = mwc.var(fieldReader);
            if (fieldClass == byte.class || fieldClass == short.class || fieldClass == int.class || fieldClass == boolean.class || fieldClass == char.class) {
                mw.iconst_0();
                mw.istore(var);
            } else if (fieldClass == long.class) {
                mw.lconst_0();
                mw.lstore(var);
            } else if (fieldClass == float.class) {
                mw.iconst_0();
                mw.i2f();
                mw.fstore(var);
            } else if (fieldClass == double.class) {
                mw.iconst_0();
                mw.i2d();
                mw.dstore(var);
            } else {
                mw.aconst_null();
                mw.astore(var);
            }
        }
    }

    private void genRead243(
            ObjectReadContext context,
            boolean fieldBased,
            MethodWriterContext mwc,
            int OBJECT,
            Label L_FOR_INC,
            Label hashCode64Start
    ) {
        String classNameType = context.classNameType;
        FieldReader[] fieldReaderArray = context.fieldReaders;
        boolean jsonb = mwc.jsonb;
        MethodWriter mw = mwc.mw;
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
                        mw.iconst_n(fieldName[3]);
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
                        mw.iconst_n(name1);
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
                        mw.iconst_n(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match6", "(I)Z");
                        break;
                    }
                    case 7: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        mw.aload(JSON_READER);
                        mw.iconst_n(name1);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match7", "(I)Z");
                        break;
                    }
                    case 8: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        mw.aload(JSON_READER);
                        mw.iconst_n(name1);
                        mw.iconst_n(fieldName[7]);
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
                        mw.iconst_n(fieldName[11]);
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
                        mw.iconst_n(name2);
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
                        mw.iconst_n(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match14", "(JI)Z");
                        break;
                    }
                    case 15: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.iconst_n(name2);
                        mw.invokevirtual(TYPE_JSON_READER, "nextIfName4Match15", "(JI)Z");
                        break;
                    }
                    case 16: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        mw.aload(JSON_READER);
                        mw.visitLdcInsn(name1);
                        mw.iconst_n(name2);
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
                        mw.iconst_n(fieldName[19]);
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
                        mw.iconst_n(name3);
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
                        mw.iconst_n(name3);
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
                        mw.iconst_n(name3);
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
                        mw.iconst_n(name3);
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
                        mw.iconst_n(name4);
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
                        mw.iconst_n(name4);
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
                        mw.iconst_n(name4);
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
                        mw.iconst_n(fieldName[31]);
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
                        mw.iconst_n(fieldName[35]);
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
                        mw.iconst_n(name5);
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
                        mw.iconst_n(name5);
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
                        mw.iconst_n(name5);
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
                        mw.iconst_n(name5);
                        mw.iconst_n(fieldName[39]);
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
                genReadFieldValue(
                        context,
                        fieldReader,
                        fieldBased,
                        mwc,
                        OBJECT,
                        fieldReaderIndex,
                        false // arrayMapping
                );

                mw.goto_(L_FOR_INC);

                if (nextJ != null) {
                    mw.visitLabel(nextJ);
                }
            }

            mw.goto_(dflt);
        }

        mw.visitLabel(dflt);
    }

    private void genRead57(
            ObjectReadContext context,
            boolean fieldBased,
            MethodWriterContext mwc,
            int OBJECT,
            Label L_FOR_INC,
            Label hashCode64Start
    ) {
        FieldReader[] fieldReaderArray = context.fieldReaders;
        /*
         * long rawLong = jsonReader.getRawLong();
         * if (rawLong == 0) {
         *     goto hashCode64Start;
         * }
         */
        int RAW_LONG = mwc.var2("RAW_LONG");
        MethodWriter mw = mwc.mw;

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

            genReadFieldValue(
                    context,
                    fieldReader,
                    fieldBased,
                    mwc,
                    OBJECT,
                    i,
                    false // arrayMapping
            );

            mw.goto_(L_FOR_INC); // continue

            mw.visitLabel(next_);
        }
    }

    private <T> void genCreateObject(
            MethodWriter mw,
            ObjectReadContext context,
            String classNameType
    ) {
        Constructor defaultConstructor = context.defaultConstructor;
        Supplier creator = context.objectReaderAdapter.creator;
        Class objectClass = context.objectClass;

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
                mw.checkcast(context.objectType);
            }
        } else {
            newObject(mw, context.objectType, context.defaultConstructor);
        }

        if (context.hasStringField) {
            /*
             * if ((features & JSONReader.Feature.InitStringFieldAsEmpty.mask) != 0) {
             *    this.initStringFieldAsEmpty(object);
             * }
             */
            Label L0 = new Label();
            mw.lload(FEATURES);
            mw.visitLdcInsn(JSONReader.Feature.InitStringFieldAsEmpty.mask);
            mw.land();
            mw.lconst_0();
            mw.lcmp();
            mw.ifeq(L0);

            mw.dup();
            mw.aload(THIS);
            mw.swap();
            mw.invokevirtual(classNameType, "initStringFieldAsEmpty", "(Ljava/lang/Object;)V");
            mw.visitLabel(L0);
        }
    }

    private <T> void genReadFieldValue(
            ObjectReadContext context,
            FieldReader fieldReader,
            boolean fieldBased,
            MethodWriterContext mwc,
            int OBJECT,
            int fieldReaderIndex,
            boolean arrayMapping
    ) {
        String classNameType = context.classNameType;
        boolean jsonb = mwc.jsonb;
        Class objectClass = context.objectClass;
        Class fieldClass = fieldReader.fieldClass;
        Type fieldType = fieldReader.fieldType;
        long fieldFeatures = fieldReader.features;
        String format = fieldReader.format;
        Type itemType = fieldReader.itemType;

        MethodWriter mw = mwc.mw;

        if ((fieldFeatures & JSONReader.Feature.NullOnError.mask) != 0) {
            mw.aload(THIS);
            mw.getfield(classNameType, fieldReader(fieldReaderIndex), DESC_FIELD_READER);
            mw.aload(JSON_READER);
            mw.aload(OBJECT);
            mw.invokevirtual(TYPE_FIELD_READE, "readFieldValue", METHOD_DESC_READ_FIELD_VALUE);
            return;
        }

        Field field = fieldReader.field;
        Method method = fieldReader.method;

        Label endSet_ = new Label();

        String TYPE_FIELD_CLASS = ASMUtils.type(fieldClass);
        String DESC_FIELD_CLASS = ASMUtils.desc(fieldClass);

        if (!(context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor)) {
            mw.aload(OBJECT);
        }
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
            mw.checkcast(context.objectType);
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
        } else if (fieldClass == Boolean.class) {
            mw.aload(JSON_READER);
            mw.invokevirtual(TYPE_JSON_READER, "readBool", "()Ljava/lang/Boolean;");
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
            Integer REFERENCE = null;
            if (!disableReferenceDetect) {
                REFERENCE = mwc.var("REFERENCE");
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
                mw.getfield(classNameType, fieldReader(fieldReaderIndex), DESC_FIELD_READER);
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

                /*
                 * if ((features & Feature.IgnoreNoneSerializable.mask) != 0) {
                 *     jsonReader.skipValue();
                 *     goto endSet_;
                 * }
                 */
                mw.lload(FEATURES);
                mw.visitLdcInsn(JSONReader.Feature.IgnoreNoneSerializable.mask);
                mw.land();
                mw.lconst_0();
                mw.lcmp();
                mw.ifeq(endIgnoreCheck_);

                mw.aload(JSON_READER);
                mw.invokevirtual(TYPE_JSON_READER, "skipValue", "()V");
                if (!(context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor)) {
                    mw.pop();
                }
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
                genReadFieldValueList(
                        fieldReader,
                        classNameType,
                        mwc,
                        OBJECT,
                        fieldReaderIndex,
                        arrayMapping,
                        objectClass,
                        fieldClass,
                        fieldType,
                        fieldFeatures,
                        itemType,
                        TYPE_FIELD_CLASS,
                        context,
                        fieldBased
                );
            } else {
                final String FIELD_OBJECT_READER = fieldObjectReader(fieldReaderIndex);

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
                                mwc,
                                fieldReaderIndex,
                                fieldType,
                                fieldClass,
                                fieldFeatures,
                                FIELD_OBJECT_READER
                        );
                    } else {
                        genReadObject(
                                fieldReader,
                                classNameType,
                                mwc,
                                fieldReaderIndex,
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
                mw.putfield(context.objectType, field.getName(), DESC_FIELD_CLASS);
            } else {
                int FIELD_VALUE = mwc.var(fieldClass);

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
        } else if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
            if (!fieldClass.isPrimitive()) {
                mw.checkcast(ASMUtils.type(fieldClass));
            }
            mw.storeLocal(fieldClass, mwc.var(fieldReader));
        } else {
            boolean invokeFieldReaderAccept = context.externalClass || method == null || !context.publicClass;

            if (invokeFieldReaderAccept) {
                int FIELD_VALUE = mwc.var(fieldClass);

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
                mw.getfield(classNameType, fieldReader(fieldReaderIndex), DESC_FIELD_READER);
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
                mw.invokevirtual(context.objectType, methodName, methodDesc);
                if (returnType != void.class) {
                    mw.pop();
                }
            }
            // TODO BUILD METHOD
        }

        mw.visitLabel(endSet_);
    }

    private void genReadObject(
            FieldReader fieldReader,
            String classNameType,
            MethodWriterContext mwc,
            int i,
            Type fieldType,
            long fieldFeatures,
            String FIELD_OBJECT_READER
    ) {
        // object.<setMethod>(this.objectReader_<i>.readObject(jsonReader))
        Label notNull_ = new Label();
        MethodWriter mw = mwc.mw;
        boolean jsonb = mwc.jsonb;

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
        gwGetFieldType(classNameType, mw, i, fieldType);
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
            MethodWriterContext mwc,
            int fieldIndex,
            Type fieldType,
            Class fieldClass,
            long fieldFeatures,
            String FIELD_OBJECT_READER
    ) {
        MethodWriter mw = mwc.mw;
        boolean jsonb = mwc.jsonb;
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
        gwGetFieldType(classNameType, mw, fieldIndex, fieldType);
        mw.visitLdcInsn(fieldReader.fieldName);
        mw.visitLdcInsn(fieldFeatures);
        mw.invokeinterface(
                TYPE_OBJECT_READER,
                jsonb ? "readJSONBObject" : "readObject",
                METHOD_DESC_READ_OBJECT);

        mw.visitLabel(enumEnd);
    }

    private void genReadFieldValueList(
            FieldReader fieldReader,
            String classNameType,
            MethodWriterContext mwc,
            int OBJECT,
            int i,
            boolean arrayMapping,
            Class objectClass,
            Class fieldClass,
            Type fieldType,
            long fieldFeatures,
            Type itemType,
            String TYPE_FIELD_CLASS,
            ObjectReadContext context,
            boolean fieldBased
    ) {
        boolean jsonb = mwc.jsonb;
        if (itemType == null) {
            itemType = Object.class;
        }

        Class itemClass = TypeUtils.getMapping(itemType);
        String ITEM_OBJECT_READER = fieldItemObjectReader(i);
        MethodWriter mw = mwc.mw;

        int LIST;
        if (context.objectReaderAdapter instanceof ObjectReaderNoneDefaultConstructor) {
            LIST = mwc.var(fieldReader);
        } else {
            LIST = mwc.var(fieldClass);
        }
        Integer AUTO_TYPE_OBJECT_READER = mwc.var(ObjectReader.class);

        String LIST_TYPE = fieldClass.isInterface() ? "java/util/ArrayList" : TYPE_FIELD_CLASS;

        Label loadList_ = new Label(), listNotNull_ = new Label(), listInitEnd_ = new Label();

        boolean initCapacity = JVM_VERSION == 8 && "java/util/ArrayList".equals(LIST_TYPE);
        int ITEM_CNT = mwc.var("ITEM_CNT");

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
                gwGetFieldType(classNameType, mw, i, fieldType);
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

        int J = mwc.var("J");
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
                gwGetFieldType(classNameType, mw, i, fieldType);
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
            gwGetFieldType(classNameType, mw, i, fieldType);
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

        mw.visitLabel(for_inc_j_);
        mw.visitIincInsn(J, 1);
        mw.goto_(for_start_j_);

        mw.visitLabel(for_end_j_);

        mw.visitLabel(loadList_);
        mw.aload(LIST);
    }

    private void gwGetFieldType(String classNameType, MethodWriter mw, int i, Type fieldType) {
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

    static class ObjectReadContext {
        final BeanInfo beanInfo;
        final Class objectClass;
        final ClassWriter cw;
        final boolean publicClass;
        final boolean externalClass;
        final FieldReader[] fieldReaders;
        final boolean hasStringField;
        final int fieldNameLengthMin;
        final int fieldNameLengthMax;
        final String classNameType;
        final String classNameFull;
        final Constructor defaultConstructor;
        ObjectReaderAdapter objectReaderAdapter;
        final String objectType;

        public ObjectReadContext(
                BeanInfo beanInfo,
                Class objectClass,
                ClassWriter cw,
                boolean externalClass,
                FieldReader[] fieldReaders,
                Constructor defaultConstructor
        ) {
            this.beanInfo = beanInfo;
            this.objectClass = objectClass;
            this.cw = cw;
            this.publicClass = objectClass == null || Modifier.isPublic(objectClass.getModifiers());
            this.externalClass = externalClass;
            this.fieldReaders = fieldReaders;
            this.defaultConstructor = defaultConstructor;
            this.objectType = objectClass == null ? ASMUtils.TYPE_OBJECT : ASMUtils.type(objectClass);

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

            String className = "ORG_" + seed.incrementAndGet() + "_" + fieldReaders.length + (objectClass == null ? "" : "_" + objectClass.getSimpleName());

            Package pkg = ObjectReaderCreatorASM.class.getPackage();
            if (pkg != null) {
                classNameFull = packageName + '.' + className;
                classNameType = classNameFull.replace('.', '/');
            } else {
                classNameType = className;
                classNameFull = className;
            }
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
            classNameFull = packageName + '.' + className;
            classNameType = classNameFull.replace('.', '/');
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

    static class MethodWriterContext {
        final MethodWriter mw;
        final Map<Object, Integer> variants = new LinkedHashMap<>();
        final boolean jsonb;
        int maxVariant;
        static final int JSON_READER = 1;
        static final int FIELD_TYPE = 2;
        static final int FIELD_NAME = 3;
        static final int FEATURES = 4;

        public MethodWriterContext(MethodWriter mw, int maxVariant, boolean jsonb) {
            this.mw = mw;
            this.maxVariant = maxVariant;
            this.jsonb = jsonb;
        }

        int var(Object key) {
            Integer var = variants.get(key);
            if (var == null) {
                var = maxVariant;
                variants.put(key, var);
                if (key == long.class || key == double.class) {
                    maxVariant += 2;
                } else {
                    maxVariant += 1;
                }
            }
            return var;
        }

        int var(FieldReader fieldReader) {
            return var("_param_" + fieldReader.fieldName, fieldReader.fieldClass);
        }

        int var(String name, Class type) {
            Integer var = variants.get(name);
            if (var == null) {
                var = maxVariant;
                variants.put(name, var);
                if (type == long.class || type == double.class) {
                    maxVariant += 2;
                } else {
                    maxVariant += 1;
                }
            }
            return var;
        }

        int var2(Object key) {
            Integer var = variants.get(key);
            if (var == null) {
                var = maxVariant;
                variants.put(key, var);
                maxVariant += 2;
            }
            return var;
        }
    }
}
