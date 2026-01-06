package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.internal.asm.*;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.*;

import java.io.Serializable;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.internal.Conf.BYTES;
import static com.alibaba.fastjson2.internal.asm.ASMUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.isFunction;
import static com.alibaba.fastjson2.writer.ObjectWriterProvider.TYPE_INT64_MASK;

public class ObjectWriterCreatorASM
        extends ObjectWriterCreator {
    // GraalVM not support
    // Android not support
    public static final ObjectWriterCreatorASM INSTANCE = new ObjectWriterCreatorASM(
            DynamicClassLoader.getInstance()
    );

    protected static final AtomicLong seed = new AtomicLong();
    protected final DynamicClassLoader classLoader;

    static final String[] INTERFACES = {TYPE_OBJECT_WRITER};

    static final String DESC_SYMBOL = desc(SymbolTable.class);

    static final String METHOD_DESC_WRITE_VALUE = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;)V";
    static final String METHOD_DESC_WRITE = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_UTF8 = "(" + DESC_JSON_WRITER_UTF8 + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_UTF16 = "(" + DESC_JSON_WRITER_UTF16 + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_JSONB = "(" + DESC_JSONB_WRITER + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_FIELD_NAME = "(" + DESC_JSON_WRITER + ")V";
    static final String METHOD_DESC_WRITE_JSONB_FIELD_NAME = "(" + DESC_JSONB_WRITER + ")V";
    static final String METHOD_DESC_WRITE_UTF8_FIELD_NAME = "(" + DESC_JSON_WRITER_UTF8 + ")V";
    static final String METHOD_DESC_WRITE_UTF16_FIELD_NAME = "(" + DESC_JSON_WRITER_UTF16 + ")V";
    static final String METHOD_DESC_WRITE_OBJECT = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_JSONB_OBJECT = "(" + DESC_JSONB_WRITER + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_J = "(" + DESC_JSON_WRITER + "J)V";
    static final String METHOD_DESC_WRITE_D = "(" + DESC_JSON_WRITER + "D)V";
    static final String METHOD_DESC_WRITE_F = "(" + DESC_JSON_WRITER + "F)V";
    static final String METHOD_DESC_WRITE_DATE_WITH_FIELD_NAME = "(" + DESC_JSON_WRITER + "ZLjava/util/Date;)V";
    static final String METHOD_DESC_WRITE_Z = "(" + DESC_JSON_WRITER + "Z)V";
    static final String METHOD_DESC_WRITE_JSONB_Z = "(" + DESC_JSONB_WRITER + "Z)V";
    static final String METHOD_DESC_WRITE_UTF8_Z = "(" + DESC_JSON_WRITER_UTF8 + "Z)V";
    static final String METHOD_DESC_WRITE_UTF16_Z = "(" + DESC_JSON_WRITER_UTF16 + "Z)V";
    static final String METHOD_DESC_WRITE_ZARRAY = "(" + DESC_JSON_WRITER + "[Z)V";
    static final String METHOD_DESC_WRITE_FARRAY = "(" + DESC_JSON_WRITER + "[F)V";
    static final String METHOD_DESC_WRITE_DARRAY = "(" + DESC_JSON_WRITER + "[D)V";
    static final String METHOD_DESC_WRITE_I = "(" + DESC_JSON_WRITER + "I)V";
    static final String METHOD_DESC_WRITE_SArray = "(" + DESC_JSON_WRITER + "[S)V";
    static final String METHOD_DESC_WRITE_BArray = "(" + DESC_JSON_WRITER + "[B)V";
    static final String METHOD_DESC_WRITE_CArray = "(" + DESC_JSON_WRITER + "[C)V";
    static final String METHOD_DESC_WRITE_ENUM = "(" + DESC_JSON_WRITER + "Ljava/lang/Enum;)V";
    static final String METHOD_DESC_WRITE_LIST = "(" + DESC_JSON_WRITER + "Ljava/util/List;)V";
    static final String METHOD_DESC_WRITE_LIST_JSONB = "(" + DESC_JSONB_WRITER + "Ljava/util/List;)V";
    static final String METHOD_DESC_WRITE_LIST_UTF8 = "(" + DESC_JSON_WRITER_UTF8 + "Ljava/util/List;)V";
    static final String METHOD_DESC_WRITE_LIST_UTF16 = "(" + DESC_JSON_WRITER_UTF16 + "Ljava/util/List;)V";
    static final String METHOD_DESC_FIELD_WRITE_OBJECT = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;)Z";
    static final String METHOD_DESC_GET_OBJECT_WRITER = "(" + DESC_JSON_WRITER + "Ljava/lang/Class;)" + DESC_OBJECT_WRITER;
    static final String METHOD_DESC_GET_ITEM_WRITER = "(" + DESC_JSON_WRITER + "Ljava/lang/reflect/Type;)" + DESC_OBJECT_WRITER;
    static final String METHOD_DESC_WRITE_TYPE_INFO = "(" + DESC_JSON_WRITER + ")Z";
    static final String METHOD_DESC_HAS_FILTER = "(" + DESC_JSON_WRITER + ")Z";
    static final String METHOD_DESC_SET_PATH2 = "(" + DESC_FIELD_WRITER + "Ljava/lang/Object;)Ljava/lang/String;";
    static final String METHOD_DESC_WRITE_REFERENCE = "(Ljava/lang/String;)V";
    static final String METHOD_DESC_IO_WRITE_REFERENCE = "([BILjava/lang/String;" + DESC_JSON_WRITER + ")I";
    static final String METHOD_DESC_WRITE_CLASS_INFO = "(" + DESC_JSON_WRITER + ")V";
    static final String METHOD_DESC_WRITE_FIELD_NAME_JSONB = "([BI" + DESC_JSONB_WRITER + ")I";
    static final String METHOD_DESC_WRITE_NAME_SYMBOL = "(" + DESC_SYMBOL + ")I";
    static final String METHOD_DESC_WRITE_LIST_VALUE_JSONB = "(" + DESC_JSON_WRITER + "Ljava/util/List;)V";

    static final int THIS = 0;
    static final int JSON_WRITER = 1;
    static final String NOT_WRITE_DEFAULT_VALUE = "WRITE_DEFAULT_VALUE";
    static final String WRITE_NULLS = "WRITE_NULLS";
    static final String CONTEXT_FEATURES = "CONTEXT_FEATURES";
    static final String NAME_DIRECT = "NAME_DIRECT";

    enum JSONWriterType {
        JSONB,
        JSON_UTF8,
        JSON_UTF16;

        public String writeMethodName() {
            switch (this) {
                case JSONB:
                    return "writeJSONB";
                case JSON_UTF8:
                    return "writeUTF8";
                default:
                    return "writeUTF16";
            }
        }

        public String writeFieldName() {
            switch (this) {
                case JSONB:
                    return "writeFieldNameJSONB";
                case JSON_UTF8:
                    return "writeFieldNameUTF8";
                default:
                    return "writeFieldNameUTF16";
            }
        }

        public String writeFieldNameDesc() {
            switch (this) {
                case JSONB:
                    return METHOD_DESC_WRITE_JSONB_FIELD_NAME;
                case JSON_UTF8:
                    return METHOD_DESC_WRITE_UTF8_FIELD_NAME;
                default:
                    return METHOD_DESC_WRITE_UTF16_FIELD_NAME;
            }
        }

        public String writeMethodDesc() {
            switch (this) {
                case JSONB:
                    return METHOD_DESC_WRITE_JSONB;
                case JSON_UTF8:
                    return METHOD_DESC_WRITE_UTF8;
                default:
                    return METHOD_DESC_WRITE_UTF16;
            }
        }

        public String writeListValueMethodName() {
            switch (this) {
                case JSONB:
                    return "writeListValueJSONB";
                case JSON_UTF8:
                    return "writeListValueUTF8";
                default:
                    return "writeListValueUTF16";
            }
        }

        public String writeListValueMethodDesc() {
            switch (this) {
                case JSONB:
                    return METHOD_DESC_WRITE_LIST_JSONB;
                case JSON_UTF8:
                    return METHOD_DESC_WRITE_LIST_UTF8;
                default:
                    return METHOD_DESC_WRITE_LIST_UTF16;
            }
        }

        public String writeBoolMethodName() {
            switch (this) {
                case JSONB:
                    return "writeBoolJSONB";
                case JSON_UTF8:
                    return "writeBoolUTF8";
                default:
                    return "writeBoolUTF16";
            }
        }

        public String writeBoolMethodDesc() {
            switch (this) {
                case JSONB:
                    return METHOD_DESC_WRITE_JSONB_Z;
                case JSON_UTF8:
                    return METHOD_DESC_WRITE_UTF8_Z;
                default:
                    return METHOD_DESC_WRITE_UTF16_Z;
            }
        }

    }

    static String fieldWriter(int i) {
        switch (i) {
            case 0:
                return "fieldWriter0";
            case 1:
                return "fieldWriter1";
            case 2:
                return "fieldWriter2";
            case 3:
                return "fieldWriter3";
            case 4:
                return "fieldWriter4";
            case 5:
                return "fieldWriter5";
            case 6:
                return "fieldWriter6";
            case 7:
                return "fieldWriter7";
            case 8:
                return "fieldWriter8";
            case 9:
                return "fieldWriter9";
            case 10:
                return "fieldWriter10";
            case 11:
                return "fieldWriter11";
            case 12:
                return "fieldWriter12";
            case 13:
                return "fieldWriter13";
            case 14:
                return "fieldWriter14";
            case 15:
                return "fieldWriter15";
            default:
                String base = "fieldWriter";
                int size = IOUtils.stringSize(i);
                char[] chars = new char[base.length() + size];
                base.getChars(0, base.length(), chars, 0);
                IOUtils.getChars(i, chars.length, chars);
                return new String(chars);
        }
    }

    public ObjectWriterCreatorASM() {
        this.classLoader = new DynamicClassLoader();
    }

    public ObjectWriterCreatorASM(ClassLoader classLoader) {
        this.classLoader = classLoader instanceof DynamicClassLoader
                ? (DynamicClassLoader) classLoader
                : new DynamicClassLoader(classLoader);
    }

    @Override
    public ObjectWriter createObjectWriter(
            List<FieldWriter> fieldWriters
    ) {
        boolean allFunction = true;
        for (int i = 0; i < fieldWriters.size(); i++) {
            if (fieldWriters.get(i).getFunction() == null) {
                allFunction = false;
                break;
            }
        }

        if (!allFunction) {
            return super.createObjectWriter(fieldWriters);
        }

        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        BeanInfo beanInfo = provider.createBeanInfo();
        return jitWriter(null, provider, beanInfo, fieldWriters, 0);
    }

    @Override
    public ObjectWriter createObjectWriter(
            Class objectClass,
            long features,
            ObjectWriterProvider provider
    ) {
        int modifiers = objectClass.getModifiers();
        boolean externalClass = classLoader.isExternalClass(objectClass);
        boolean publicClass = Modifier.isPublic(modifiers);

        BeanInfo beanInfo = provider.createBeanInfo();
        provider.getBeanInfo(beanInfo, objectClass);

        if (beanInfo.serializer != null && ObjectWriter.class.isAssignableFrom(beanInfo.serializer)) {
            try {
                Constructor constructor = beanInfo.serializer.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ObjectWriter) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new JSONException("create serializer error", e);
            }
        }

        long beanFeatures = beanInfo.writerFeatures;
        if (beanInfo.seeAlso != null) {
            beanFeatures &= ~JSONWriter.Feature.WriteClassName.mask;
        }

        boolean record = BeanUtils.isRecord(objectClass);
        long writerFieldFeatures = features | beanFeatures | (record ? FieldInfo.RECORD : 0);
        final boolean fieldBased = ((writerFieldFeatures & JSONWriter.Feature.FieldBased.mask) != 0 && !objectClass.isInterface())
                || !beanInfo.alphabetic;

        if (Throwable.class.isAssignableFrom(objectClass)
                || BeanUtils.isExtendedMap(objectClass)
                || beanInfo.rootName != null
        ) {
            return super.createObjectWriter(objectClass, features, provider);
        }

        List<FieldWriter> fieldWriters;
        Map<String, FieldWriter> fieldWriterMap = new LinkedHashMap<>();
        if (!fieldBased || record) {
            List<FieldWriter> fieldWriterList = new ArrayList<>();
            boolean fieldWritersCreated = false;
            for (ObjectWriterModule module : provider.modules) {
                if (module.createFieldWriters(this, objectClass, fieldWriterList)) {
                    fieldWritersCreated = true;
                    break;
                }
            }

            if (fieldWritersCreated) {
                for (FieldWriter fieldWriter : fieldWriterList) {
                    Method method = fieldWriter.method;
                    if (method == null) {
                        return super.createObjectWriter(objectClass, writerFieldFeatures, provider);
                    }
                    fieldWriterMap.putIfAbsent(fieldWriter.fieldName, fieldWriter);
                }
            } else {
                final FieldInfo fieldInfo = new FieldInfo();

                if (!record) {
                    BeanUtils.declaredFields(objectClass, field -> {
                        fieldInfo.init();
                        fieldInfo.ignore = fieldInfo.isPrivate = (field.getModifiers() & Modifier.PUBLIC) == 0;

                        FieldWriter fieldWriter = createFieldWriter(objectClass, writerFieldFeatures, provider, beanInfo, fieldInfo, field);
                        if (fieldWriter != null) {
                            if (fieldInfo.writeUsing != null && fieldWriter instanceof FieldWriterObject) {
                                ((FieldWriterObject) fieldWriter).writeUsing = true;
                            }
                            FieldWriter origin = fieldWriterMap.putIfAbsent(fieldWriter.fieldName, fieldWriter);
                            if (origin != null) {
                                int cmp = origin.compareTo(fieldWriter);
                                if (cmp > 0) {
                                    fieldWriterMap.put(fieldWriter.fieldName, fieldWriter);
                                }
                            }
                        }
                    });
                }

                Class mixIn = provider.getMixIn(objectClass);
                BeanUtils.getters(objectClass, mixIn, beanInfo.kotlin, method -> {
                    fieldInfo.init();
                    fieldInfo.features |= writerFieldFeatures;
                    fieldInfo.format = beanInfo.format;

                    provider.getFieldInfo(beanInfo, fieldInfo, objectClass, method);
                    if (fieldInfo.ignore) {
                        return;
                    }

                    String fieldName = getFieldName(objectClass, provider, beanInfo, record, fieldInfo, method);

                    if (beanInfo.orders != null) {
                        boolean match = false;
                        for (int i = 0; i < beanInfo.orders.length; i++) {
                            if (fieldName.equals(beanInfo.orders[i])) {
                                fieldInfo.ordinal = i;
                                match = true;
                            }
                        }
                        if (!match) {
                            if (fieldInfo.ordinal == 0) {
                                fieldInfo.ordinal = beanInfo.orders.length;
                            }
                        }
                    }

                    if (beanInfo.includes != null && beanInfo.includes.length > 0) {
                        boolean match = false;
                        for (String include : beanInfo.includes) {
                            if (include.equals(fieldName)) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            return;
                        }
                    }

                    // skip typeKey field
                    if ((beanInfo.writerFeatures & WriteClassName.mask) != 0
                            && fieldName.equals(beanInfo.typeKey)) {
                        return;
                    }

                    Class<?> returnType = method.getReturnType();
                    // skip function
                    if (isFunction(returnType) || returnType == Void.TYPE) {
                        return;
                    }

                    method.setAccessible(true);

                    ObjectWriter writeUsingWriter = null;
                    if (fieldInfo.writeUsing != null) {
                        try {
                            Constructor<?> constructor = fieldInfo.writeUsing.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            writeUsingWriter = (ObjectWriter) constructor.newInstance();
                        } catch (Exception e) {
                            throw new JSONException("create writeUsing Writer error, method " + method.getName()
                                    + ", serializer "
                                    + fieldInfo.writeUsing.getName(), e
                            );
                        }
                    }

                    if (writeUsingWriter == null && fieldInfo.fieldClassMixIn) {
                        writeUsingWriter = ObjectWriterBaseModule.VoidObjectWriter.INSTANCE;
                    }

                    FieldWriter fieldWriter = null;
                    boolean jit = (fieldInfo.features & FieldInfo.JIT) != 0;
                    if (jit) {
                        try {
                            fieldWriter = createFieldWriterLambda(
                                    provider,
                                    objectClass,
                                    fieldName,
                                    fieldInfo.ordinal,
                                    fieldInfo.features,
                                    fieldInfo.format,
                                    fieldInfo.label,
                                    method,
                                    writeUsingWriter,
                                    fieldInfo.contentAs
                            );
                        } catch (Throwable e) {
                            jitErrorCount.incrementAndGet();
                            jitErrorLast = e;
                        }
                    }
                    if (fieldWriter == null) {
                        fieldWriter = createFieldWriter(
                                provider,
                                objectClass,
                                fieldName,
                                fieldInfo.ordinal,
                                fieldInfo.features,
                                fieldInfo.format,
                                fieldInfo.locale,
                                fieldInfo.label,
                                method,
                                writeUsingWriter,
                                fieldInfo.contentAs
                        );
                    }

                    if (fieldInfo.writeUsing != null && fieldWriter instanceof FieldWriterObject) {
                        ((FieldWriterObject) fieldWriter).writeUsing = true;
                    }
                    FieldWriter origin = fieldWriterMap.putIfAbsent(fieldName, fieldWriter);

                    if (origin != null && origin.compareTo(fieldWriter) > 0) {
                        fieldWriterMap.put(fieldName, fieldWriter);
                    }

                    // the sameFieldName means only differ in first character that one is upper case the other is lower case
                    if (origin == null) {
                        String sameFieldName = null;
                        char firstChar = fieldName.charAt(0);
                        if (firstChar >= 'A' && firstChar <= 'Z') {
                            sameFieldName = (char) (firstChar + 32) + fieldName.substring(1);
                        } else if (firstChar >= 'a' && firstChar <= 'z') {
                            sameFieldName = (char) (firstChar - 32) + fieldName.substring(1);
                        }
                        if (sameFieldName != null) {
                            FieldWriter sameNameFieldWriter = fieldWriterMap.get(sameFieldName);
                            if (sameNameFieldWriter != null
                                    && (sameNameFieldWriter.method == null || sameNameFieldWriter.method.equals(method))) {
                                fieldWriterMap.remove(sameFieldName);
                            }
                        }
                    }
                });
            }
        } else {
            final FieldInfo fieldInfo = new FieldInfo();
            BeanUtils.declaredFields(objectClass, field -> {
                fieldInfo.init();
                FieldWriter fieldWriter = createFieldWriter(objectClass, writerFieldFeatures, provider, beanInfo, fieldInfo, field);
                if (fieldWriter != null) {
                    if (fieldInfo.writeUsing != null && fieldWriter instanceof FieldWriterObject) {
                        ((FieldWriterObject) fieldWriter).writeUsing = true;
                    }
                    fieldWriterMap.put(fieldWriter.fieldName, fieldWriter);
                }
            });
        }
        fieldWriters = new ArrayList<>(fieldWriterMap.values());

        handleIgnores(beanInfo, fieldWriters);
        if (beanInfo.alphabetic) {
            try {
                Collections.sort(fieldWriters);
            } catch (Exception e) {
                StringBuilder msg = new StringBuilder("fieldWriters sort error, objectClass ")
                        .append(objectClass.getName())
                        .append(", fields ");

                JSONArray array = new JSONArray();
                for (FieldWriter fieldWriter : fieldWriters) {
                    array.add(
                            JSONObject.of(
                                    "name", fieldWriter.fieldName,
                                    "type", fieldWriter.fieldClass,
                                    "ordinal", fieldWriter.ordinal,
                                    "field", fieldWriter.field,
                                    "method", fieldWriter.method
                            )
                    );
                }
                msg.append(array);
                throw new JSONException(msg.toString(), e);
            }
        }

        boolean match = fieldWriters.size() < 100 && !Throwable.class.isAssignableFrom(objectClass);

        if (!publicClass || externalClass) {
            for (FieldWriter fieldWriter : fieldWriters) {
                if (fieldWriter.method != null) {
                    match = false;
                    break;
                }
            }
        }

        for (FieldWriter fieldWriter : fieldWriters) {
            if (fieldWriter.getInitWriter() != null
                    || (fieldWriter.features & FieldInfo.VALUE_MASK) != 0
                    || (fieldWriter.features & FieldInfo.RAW_VALUE_MASK) != 0
            ) {
                match = false;
                break;
            }
        }

        if (objectClass.getSuperclass() == Object.class) {
            String simpleName = objectClass.getSimpleName();
            if (simpleName.indexOf('$') != -1 && simpleName.contains("$$")) {
                match = false;
            }
        }
        if (fieldWriters.size() > 64) {
            match = false;
        }

        long writerFeatures = features | beanInfo.writerFeatures;
        if (!match) {
            return super.createObjectWriter(objectClass, features, provider);
        }

        setDefaultValue(fieldWriters, objectClass);

        return jitWriter(objectClass, provider, beanInfo, fieldWriters, writerFeatures);
    }

    private ObjectWriterAdapter jitWriter(
            Class objectClass,
            ObjectWriterProvider provider,
            BeanInfo beanInfo,
            List<FieldWriter> fieldWriters,
            long writerFeatures
    ) {
        List<FieldWriterGroup> fieldWriterGroups = buildGroups(beanInfo.writerFeatures, fieldWriters);

        ClassWriter cw = new ClassWriter(null);

        String className = "OWG_" + seed.incrementAndGet() + "_" + fieldWriters.size() + (objectClass == null ? "" : ("_" + objectClass.getSimpleName()));
        String classNameType;
        String classNameFull;

        Package pkg = ObjectWriterCreatorASM.class.getPackage();
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

        String objectWriterSupper;
        switch (fieldWriters.size()) {
            case 1:
                objectWriterSupper = TYPE_OBJECT_WRITER_1;
                break;
            case 2:
                objectWriterSupper = TYPE_OBJECT_WRITER_2;
                break;
            case 3:
                objectWriterSupper = TYPE_OBJECT_WRITER_3;
                break;
            case 4:
                objectWriterSupper = TYPE_OBJECT_WRITER_4;
                break;
            case 5:
                objectWriterSupper = TYPE_OBJECT_WRITER_5;
                break;
            case 6:
                objectWriterSupper = TYPE_OBJECT_WRITER_6;
                break;
            case 7:
                objectWriterSupper = TYPE_OBJECT_WRITER_7;
                break;
            case 8:
                objectWriterSupper = TYPE_OBJECT_WRITER_8;
                break;
            case 9:
                objectWriterSupper = TYPE_OBJECT_WRITER_9;
                break;
            case 10:
                objectWriterSupper = TYPE_OBJECT_WRITER_10;
                break;
            case 11:
                objectWriterSupper = TYPE_OBJECT_WRITER_11;
                break;
            case 12:
                objectWriterSupper = TYPE_OBJECT_WRITER_12;
                break;
            default:
                objectWriterSupper = TYPE_OBJECT_WRITER_ADAPTER;
                break;
        }

        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                classNameType,
                objectWriterSupper,
                INTERFACES
        );

        // // define fieldWriter
        genFields(fieldWriters, cw, objectWriterSupper);

        // init fieldWriter
        genMethodInit(fieldWriters, cw, classNameType, objectWriterSupper);
//
//        if (objectWriterSupper == TYPE_OBJECT_WRITER_ADAPTER) {
//            genGetFieldReader(
//                    fieldWriters,
//                    cw,
//                    classNameType,
//                    new ObjectWriterAdapter(objectClass, null, null, features, fieldWriters)
//            );
//        }

        boolean disableJSONB = (writerFeatures & FieldInfo.DISABLE_JSONB) != 0;
        boolean disableArrayMapping = (writerFeatures & FieldInfo.DISABLE_ARRAY_MAPPING) != 0;

        if (!disableJSONB) {
            genMethodWriteJSONB(provider, objectClass, fieldWriterGroups, fieldWriters, cw, classNameType, writerFeatures);
        }

        genMethodWrite(provider, objectClass, fieldWriters, cw, classNameType, writerFeatures, JSONWriterType.JSON_UTF8);
        genMethodWrite(provider, objectClass, fieldWriters, cw, classNameType, writerFeatures, JSONWriterType.JSON_UTF16);

        genMethodWriteArrayMappingJSONB(provider, objectClass, writerFeatures, fieldWriterGroups, fieldWriters, cw, classNameType, writerFeatures);
        // TODO : writeArrayMapping
//        genMethodWriteArrayMapping(provider, "writeArrayMapping", objectClass, writerFeatures, fieldWriters, cw, classNameType, JSONWriterType.JSON_UTF8);
//        genMethodWriteArrayMapping(provider, "writeArrayMapping", objectClass, writerFeatures, fieldWriters, cw, classNameType, JSONWriterType.JSON_UTF16);

        byte[] code = cw.toByteArray();

        Class<?> deserClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);

        try {
            Constructor<?> constructor = deserClass.getConstructor(Class.class, String.class, String.class, long.class, List.class);
            ObjectWriterAdapter objectWriter = (ObjectWriterAdapter) constructor.newInstance(
                    objectClass,
                    beanInfo.typeKey,
                    beanInfo.typeName,
                    writerFeatures,
                    fieldWriters
            );
            if (beanInfo.serializeFilters != null) {
                configSerializeFilters(beanInfo, objectWriter);
            }
            return objectWriter;
        } catch (Throwable e) {
            throw new JSONException("create objectWriter error, objectType " + objectClass, e);
        }
    }

    private void genMethodWrite(
            ObjectWriterProvider provider,
            Class objectType,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            long objectFeatures,
            JSONWriterType type
    ) {
        boolean disableJSONB = (objectFeatures & FieldInfo.DISABLE_JSONB) != 0;
        boolean disableArrayMapping = (objectFeatures & FieldInfo.DISABLE_ARRAY_MAPPING) != 0;
        boolean disableAutoType = (objectFeatures & FieldInfo.DISABLE_AUTO_TYPE) != 0;

        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                type.writeMethodName(),
                type.writeMethodDesc(),
                fieldWriters.size() < 6 ? 512 : 1024
        );

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;
        final int COMMA = 7;

        Label notSuper_ = new Label();

        MethodWriterContext mwc = new MethodWriterContext(provider, objectType, objectFeatures, classNameType, mw, 8, type);
        mwc.genVariantsMethodBefore(false);

        mwc.genIsEnabled(JSONWriter.Feature.IgnoreErrorGetter.mask | UnquoteFieldName.mask, notSuper_);

        mw.aload(THIS);
        mw.aload(JSON_WRITER);
        mw.aload(OBJECT);
        mw.aload(FIELD_NAME);
        mw.aload(FIELD_TYPE);
        mw.lload(FIELD_FEATURES);
        mw.invokespecial(TYPE_OBJECT_WRITER_ADAPTER, type.writeMethodName(), METHOD_DESC_WRITE_OBJECT);
        mw.return_();

        mw.visitLabel(notSuper_);

        if (!disableArrayMapping) {
            Label checkFilter_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.BeanToArray.mask, checkFilter_);

            mw.aload(THIS);
            mw.aload(JSON_WRITER);
            mw.aload(OBJECT);
            mw.aload(FIELD_NAME);
            mw.aload(FIELD_TYPE);
            mw.lload(FIELD_FEATURES);
            mw.invokevirtual(classNameType, "writeArrayMapping", METHOD_DESC_WRITE_OBJECT);
            mw.return_();

            mw.visitLabel(checkFilter_);
        }

        Label object_ = new Label();
        hashFilter(mw, fieldWriters, object_);

        mw.aload(THIS);
        mw.aload(JSON_WRITER);
        mw.aload(OBJECT);
        mw.aload(FIELD_NAME);
        mw.aload(FIELD_TYPE);
        mw.lload(FIELD_FEATURES);
        mw.invokevirtual(classNameType, "writeWithFilter", METHOD_DESC_WRITE_OBJECT);
        mw.return_();

        mw.visitLabel(object_);

        Label LReturn = new Label();
        if (objectType == null || !java.io.Serializable.class.isAssignableFrom(objectType)) {
            Label endIgnoreNoneSerializable_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.IgnoreNoneSerializable.mask, endIgnoreNoneSerializable_);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
            mw.goto_(LReturn);

            mw.visitLabel(endIgnoreNoneSerializable_);

            Label endErrorOnNoneSerializable_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ErrorOnNoneSerializable.mask, endErrorOnNoneSerializable_);
            mw.aload(THIS);
            mw.invokevirtual(mwc.classNameType, "errorOnNoneSerializable", "()V");
            mw.goto_(LReturn);

            mw.visitLabel(endErrorOnNoneSerializable_);
        }

        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "startObject", "()V");

        if (!disableAutoType) {
            mw.iconst_1();
            mw.istore(COMMA); // comma = false

            Label writeFields_ = new Label();
            isWriteTypeInfo(objectFeatures, mw, OBJECT, FIELD_TYPE, FIELD_FEATURES, writeFields_);

            mw.aload(THIS);
            mw.aload(JSON_WRITER);
            mw.invokeinterface(TYPE_OBJECT_WRITER, "writeTypeInfo", METHOD_DESC_WRITE_TYPE_INFO);
            mw.iconst_1();
            mw.ixor();
            mw.istore(COMMA);

            mw.visitLabel(writeFields_);
        }

        for (int i = 0; i < fieldWriters.size(); i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            gwFieldValue(mwc, fieldWriter, OBJECT, i);
        }

        mw.aload(1);
        mw.invokevirtual(TYPE_JSON_WRITER, "endObject", "()V");

        mw.visitLabel(LReturn);
        mw.return_();
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
    }

    private static void isWriteTypeInfo(
            long objectFeatures,
            MethodWriter mw,
            int OBJECT,
            int FIELD_TYPE,
            int FEILD_FEATURE,
            Label notWriteType
    ) {
        if ((objectFeatures & JSONWriter.Feature.WriteClassName.mask) == 0 || (objectFeatures & NotWriteRootClassName.mask) != 0) {
            mw.aload(OBJECT);
            mw.ifnull(notWriteType);

            mw.aload(OBJECT);
            mw.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mw.aload(FIELD_TYPE);
            mw.if_acmpeq(notWriteType);

            mw.aload(JSON_WRITER);
            mw.aload(OBJECT);
            mw.aload(FIELD_TYPE);
            mw.lload(FEILD_FEATURE);
            mw.invokevirtual(TYPE_JSON_WRITER, "isWriteTypeInfo", "(Ljava/lang/Object;Ljava/lang/reflect/Type;J)Z");
            mw.ifeq(notWriteType);
        }
    }

    private void genMethodWriteJSONB(
            ObjectWriterProvider provider,
            Class objectType,
            List<FieldWriterGroup> fieldWriterGroups,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            long objectFeatures
    ) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "writeJSONB",
                METHOD_DESC_WRITE_JSONB,
                fieldWriters.size() < 6 ? 512 : 1024
        );

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;

        MethodWriterContext mwc = new MethodWriterContext(provider, objectType, objectFeatures, classNameType, mw, 7, JSONWriterType.JSONB);
        mwc.genVariantsMethodBefore(true);

        Label return_ = new Label();
        if (objectType == null || !java.io.Serializable.class.isAssignableFrom(objectType)) {
            Label endIgnoreNoneSerializable_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.IgnoreNoneSerializable.mask, endIgnoreNoneSerializable_);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
            mw.goto_(return_);

            mw.visitLabel(endIgnoreNoneSerializable_);

            Label endErrorOnNoneSerializable_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ErrorOnNoneSerializable.mask, endErrorOnNoneSerializable_);
            mw.aload(THIS);
            mw.invokevirtual(mwc.classNameType, "errorOnNoneSerializable", "()V");
            mw.goto_(return_);

            mw.visitLabel(endErrorOnNoneSerializable_);
        }

        if ((objectFeatures & FieldInfo.DISABLE_AUTO_TYPE) == 0) {
            Label notWriteType = new Label();
            isWriteTypeInfo(objectFeatures, mw, OBJECT, FIELD_TYPE, FIELD_FEATURES, notWriteType);

            mw.aload(THIS);
            mw.aload(JSON_WRITER);
            mw.invokevirtual(classNameType, "writeClassInfo", METHOD_DESC_WRITE_CLASS_INFO);

            mw.visitLabel(notWriteType);
        }

        Integer symbolTable = null;

        for (FieldWriterGroup group : fieldWriterGroups) {
            if (group.direct) {
                final int OFFSET = mwc.var("offset");
                final int BYTES = mwc.var("bytes");
                final int FEATURES = mwc.var2(CONTEXT_FEATURES);

                mw.aload(JSON_WRITER);
                mw.invokevirtual(TYPE_JSON_WRITER, "getOffset", "()I");
                mw.istore(OFFSET);

                if (symbolTable == null) {
                    symbolTable = mwc.var("symbolTable");
                    mw.aload(JSON_WRITER);
                    mw.getfield(TYPE_JSON_WRITER, "symbolTable", DESC_SYMBOL);
                    mw.astore(symbolTable);
                }

                int minCapacity = (group.start ? 0 : 1) + (group.end ? 0 : 1);
                for (FieldWriterRecord item : group.fieldWriters) {
                    minCapacity += item.fieldWriter.nameJSONB.length;

                    FieldWriter fieldWriter = item.fieldWriter;
                    Class fieldClass = fieldWriter.fieldClass;
                    if (isFieldVarIndex(mwc, fieldWriter)) {
                        // save field values to local variants
                        sotreFieldValueToLocalVar(mwc, item.ordinal, fieldWriter, OBJECT, mw);
                    } else {
                        minCapacity += fieldCapacity(fieldClass);
                    }
                }

                mw.aload(JSON_WRITER);
                mw.iload(OFFSET);
                mw.visitLdcInsn(minCapacity);
                mw.iadd();
                fieldValueCapacity(objectFeatures, group.fieldWriters, mwc, mw, FEATURES);
                mw.invokevirtual(TYPE_JSON_WRITER, "ensureCapacity", "(I)Ljava/lang/Object;");
                mw.checkcast("[B");
                mw.astore(BYTES);

                if (group.start) {
                    // bytes[off++] = BC_OBJECT;
                    gwWriteByte(mw, BYTES, OFFSET, BC_OBJECT);
                    mw.visitIincInsn(OFFSET, 1);
                }
                for (FieldWriterRecord item : group.fieldWriters) {
                    writeFieldValueDirectJSONB(
                            objectFeatures,
                            classNameType,
                            mwc,
                            item.fieldWriter,
                            item.ordinal,
                            mw,
                            BYTES,
                            OFFSET,
                            OBJECT,
                            FEATURES,
                            symbolTable,
                            true
                    );
                }

                if (group.end) {
                    gwWriteByte(mw, BYTES, OFFSET, BC_OBJECT_END);
                }

                mw.aload(JSON_WRITER);
                mw.iload(OFFSET);
                if (group.end) {
                    mw.iconst_1();
                    mw.iadd();
                }
                mw.invokevirtual(TYPE_JSON_WRITER, "setOffset", "(I)V");
            } else {
                if (group.start) {
                    mw.aload(JSON_WRITER);
                    mw.invokevirtual(TYPE_JSON_WRITER, "startObject", "()V");
                }
                for (FieldWriterRecord item : group.fieldWriters) {
                    gwFieldValueJSONB(mwc, item.fieldWriter, OBJECT, item.ordinal);
                }
                if (group.end) {
                    mw.aload(JSON_WRITER);
                    mw.invokevirtual(TYPE_JSON_WRITER, "endObject", "()V");
                }
            }
        }

        mw.visitLabel(return_);
        mw.return_();
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
    }

    private static void gwFieldNameDirectJSONB(
            String classNameType,
            final FieldWriter fieldWriter,
            final int ordinal,
            MethodWriterContext mwc,
            int BYTES,
            int OFFSET
    ) {
        Label L0 = new Label(), L_NAME_END = new Label();
        MethodWriter mw = mwc.mw;
        mwc.genIsDisabled(WriteNameAsSymbol.mask, L0);

        int SYMBOL_TABLE = mwc.var("symbolTable");

        {
            Label L1 = new Label();
            mw.aload(SYMBOL_TABLE);
            mw.ifnull(L1);

            /*
             * int symbol = fieldWriter.writeFieldNameSymbol(symbolTable);
             * if (symbol != -1) {
             *     offset = jsonWriter.writeSymbol(bytes, offset, -symbol);
             *     goto L_NAME_END;
             * }
             */
            int SYMBOL = mwc.var("symbol");
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(ordinal), DESC_FIELD_WRITER);
            mw.aload(SYMBOL_TABLE);
            mw.invokevirtual(TYPE_FIELD_WRITER, "writeFieldNameSymbol", METHOD_DESC_WRITE_NAME_SYMBOL);
            mw.istore(SYMBOL);

            mw.iload(SYMBOL);
            mw.visitLdcInsn(-1);
            mw.if_icmpeq(L1);

            mw.aload(BYTES);
            mw.iload(OFFSET);
            mw.iload(SYMBOL);
            mw.ineg();
            mw.invokestatic(TYPE_JSONB_IO, "writeSymbol", "([BII)I", true);
            mw.istore(OFFSET);
            mw.goto_(L_NAME_END);

            mw.visitLabel(L1);
        }

        byte[] name = fieldWriter.nameJSONB;
        int i = 0;
        for (; i + 8 <= name.length; i += 8) {
            gwWriteLong(mw, BYTES, OFFSET, name, i);
        }
        if (i + 4 <= name.length) {
            gwWriteInt(mw, BYTES, OFFSET, name, i);
            i += 4;
        }
        if (i + 2 <= name.length) {
            gwWriteShort(mw, BYTES, OFFSET, name, i);
            i += 2;
        }
        if (i + 1 <= name.length) {
            gwWriteByte(mw, BYTES, OFFSET, name, i);
        }
        mw.visitIincInsn(OFFSET, name.length);
        mw.goto_(L_NAME_END);

        mw.visitLabel(L0);
        /*
         * offset = fieldWriterN.writeFieldNameJSONB(bytes, offset, jsonWriter);
         */
        mw.aload(THIS);
        mw.getfield(classNameType, fieldWriter(ordinal), DESC_FIELD_WRITER);
        mw.aload(BYTES);
        mw.iload(OFFSET);
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_FIELD_WRITER, "writeFieldNameJSONB", METHOD_DESC_WRITE_FIELD_NAME_JSONB);
        mw.istore(OFFSET);

        mw.visitLabel(L_NAME_END);
    }

    private static void gwWriteByte(MethodWriter mw, int BYTES, int OFFSET, byte value) {
        mw.aload(BYTES);
        mw.iload(OFFSET);
        mw.iconst_n(value);
        mw.bastore();
    }

    private static void gwWriteByte(MethodWriter mw, int BYTES, int OFFSET, byte[] name, int offset) {
        mw.aload(BYTES);
        mw.iload(OFFSET);
        if (offset != 0) {
            mw.iconst_n(offset);
            mw.iadd();
        }
        mw.iconst_n(name[offset]);
        mw.bastore();
    }

    private static void gwWriteShort(MethodWriter mw, int bytesSlot, int OFFSET, byte[] name, int offset) {
        short nameInt = BYTES.getShortUnaligned(name, offset);
        mw.getstatic(TYPE_CONF, "BYTES", DESC_BYTE_ARRAY);
        mw.aload(bytesSlot);
        mw.iload(OFFSET);
        if (offset != 0) {
            mw.iconst_n(offset);
            mw.iadd();
        }
        mw.visitLdcInsn(nameInt);
        mw.invokevirtual(TYPE_BYTE_ARRAY, "putShortUnaligned", "([BIS)V");
    }

    private static void gwWriteInt(MethodWriter mw, int bytesSlot, int OFFSET, byte[] name, int offset) {
        int nameInt = BYTES.getIntUnaligned(name, offset);
        mw.getstatic(TYPE_CONF, "BYTES", DESC_BYTE_ARRAY);
        mw.aload(bytesSlot);
        mw.iload(OFFSET);
        if (offset != 0) {
            mw.iconst_n(offset);
            mw.iadd();
        }
        mw.visitLdcInsn(nameInt);
        mw.invokevirtual(TYPE_BYTE_ARRAY, "putIntUnaligned", "([BII)V");
    }

    private static void gwWriteLong(MethodWriter mw, int bytesSlot, int OFFSET, byte[] name, int offset) {
        long nameInt = IOUtils.getLongUnaligned(name, offset);
        mw.getstatic(TYPE_CONF, "BYTES", DESC_BYTE_ARRAY);
        mw.aload(bytesSlot);
        mw.iload(OFFSET);
        if (offset != 0) {
            mw.iconst_n(offset);
            mw.iadd();
        }
        mw.visitLdcInsn(nameInt);
        mw.invokevirtual(TYPE_BYTE_ARRAY, "putLongUnaligned", "([BIJ)V");
    }

    private void writeFieldValueDirectJSONB(
            long objectFeatures,
            String classNameType,
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int i,
            MethodWriter mw,
            int BYTES,
            int OFFSET,
            int OBJECT,
            int FEATURES,
            int SYMBOL_TABLE,
            boolean writeFieldName
    ) {
        Class fieldClass = fieldWriter.fieldClass;
        boolean field_var_index = isFieldVarIndex(mwc, fieldWriter);

        Integer FIELD_VALUE = null;
        Label endFieldValue_ = null;

        if (!fieldClass.isPrimitive() || writeFieldName) {
            endFieldValue_ = new Label();

            if (field_var_index) {
                FIELD_VALUE = mwc.var("field_" + i);
            } else {
                FIELD_VALUE = mwc.var(fieldClass);
                genGetObject(mwc, fieldWriter, i, OBJECT);
                mw.storeLocal(fieldClass, FIELD_VALUE);
            }
        }

        boolean pop = false;
        if ((Collection.class.isAssignableFrom(fieldClass) || fieldClass.isArray())
                && !mwc.disableReferenceDetect()
        ) {
            int REF_PATH = mwc.var("REF_PATH");
            if (endFieldValue_ == null) {
                endFieldValue_ = new Label();
            }
            Label endDetect_ = new Label();
            pop = true;

            /*
             * if (fieldValue == null) {
             *     goto endDetect_;
             * }
             *
             *  if (jsonWriter.isEnabled(JSONWriter.Feature.ReferenceDetection)) {
             *     goto endDetect_;
             * }
             */
            mw.aload(FIELD_VALUE);
            mw.ifnull(endDetect_);
            mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, endDetect_);

            mw.aload(JSON_WRITER);
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "setPath0", METHOD_DESC_SET_PATH2);
            mw.dup();
            mw.astore(REF_PATH);
            mw.ifnull(endDetect_);

            if (writeFieldName) {
                gwFieldNameDirectJSONB(classNameType, fieldWriter, i, mwc, BYTES, OFFSET);
            }

            /*
             * offset = JSONB.IO.writeReference(bytes, offset, refPath, jsonWriter);
             */
            mw.aload(BYTES);
            mw.iload(OFFSET);
            mw.aload(REF_PATH);
            mw.aload(JSON_WRITER);
            mw.invokestatic(TYPE_JSONB_IO, "writeReference", METHOD_DESC_IO_WRITE_REFERENCE, true);
            mw.istore(OFFSET);

            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
            mw.goto_(endFieldValue_);

            mw.visitLabel(endDetect_);
        }

        if (writeFieldName) {
            if (fieldWriter.defaultValue == null) {
                if (!fieldClass.isPrimitive()) {
                    Label L_NOT_NULL = new Label();
                    mw.iload(mwc.var(WRITE_NULLS));
                    mw.ifne(L_NOT_NULL);

                    mw.aload(FIELD_VALUE);
                    mw.ifnull(endFieldValue_);

                    mw.visitLabel(L_NOT_NULL);
                } else {
                    int WRITE_DEFAULT_VALUE = mwc.var(NOT_WRITE_DEFAULT_VALUE);
                    Label L_NOT_DEFAULT_VALUE = new Label();
                    if (fieldClass == byte.class || fieldClass == short.class || fieldClass == int.class || fieldClass == boolean.class) {
                        mw.iload(FIELD_VALUE);
                        mw.ifne(L_NOT_DEFAULT_VALUE);

                        mw.iload(WRITE_DEFAULT_VALUE);
                        mw.ifne(endFieldValue_);

                        mw.visitLabel(L_NOT_DEFAULT_VALUE);
                    } else if (fieldClass == long.class) {
                        mw.lload(FIELD_VALUE);
                        mw.lconst_0();
                        mw.lcmp();
                        mw.ifne(L_NOT_DEFAULT_VALUE);

                        mw.iload(WRITE_DEFAULT_VALUE);
                        mw.ifne(endFieldValue_);

                        mw.visitLabel(L_NOT_DEFAULT_VALUE);
                    }
                }
            }

            gwFieldNameDirectJSONB(classNameType, fieldWriter, i, mwc, BYTES, OFFSET);
        }

        if (Collection.class.isAssignableFrom(fieldClass)) {
            /*
             * offset = JSONB.IO.checkAndWriteTypeName(bytes, offset, object, fieldClass, jsonWriter);
             */
            mw.aload(BYTES);
            mw.iload(OFFSET);
            mw.aload(FIELD_VALUE);
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.getfield(TYPE_FIELD_WRITER, "fieldClass", "Ljava/lang/Class;");
            mw.aload(JSON_WRITER);
            String methodDesc = "([BILjava/lang/Object;Ljava/lang/Class;" + DESC_JSON_WRITER + ")I";
            mw.invokestatic(TYPE_JSONB_IO, "checkAndWriteTypeName", methodDesc, true);
            mw.istore(OFFSET);
        }

        if (fieldWriter instanceof FieldWriterEnum) {
            /*
             * if (fieldValue != null && symbolTable != null) {
             *     offset = fieldWriterN.writeEnumValueJSONB(bytes, off, fieldValue, symbolTable, features);
             *     goto endFieldValue_;
             * }
             */
            Label L0 = new Label();
            mw.aload(FIELD_VALUE);
            mw.ifnull(L0);
            mw.aload(SYMBOL_TABLE);
            mw.ifnull(L0);

            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(BYTES);
            mw.iload(OFFSET);
            mw.aload(FIELD_VALUE);
            mw.aload(SYMBOL_TABLE);
            mw.lload(FEATURES);
            String methodDesc = "([BILjava/lang/Enum;" + DESC_SYMBOL + "J)I";
            mw.invokevirtual(TYPE_FIELD_WRITER, "writeEnumValueJSONB", methodDesc);
            mw.istore(OFFSET);
            mw.goto_(endFieldValue_);
            mw.visitLabel(L0);
        }

        mw.aload(BYTES);
        mw.iload(OFFSET);
        if (FIELD_VALUE != null) {
            mw.loadLocal(fieldClass, FIELD_VALUE);
        } else {
            genGetObject(mwc, fieldWriter, i, OBJECT);
        }

        String methodName;
        String methodDesc;
        if (fieldClass == boolean.class) {
            methodName = "writeBoolean";
            methodDesc = "([BIZ)I";
        } else if (fieldClass == byte.class) {
            methodName = "writeInt8";
            methodDesc = "([BIB)I";
        } else if (fieldClass == short.class) {
            methodName = "writeInt16";
            methodDesc = "([BIS)I";
        } else if (fieldClass == int.class) {
            methodName = "writeInt32";
            methodDesc = "([BII)I";
        } else if (fieldClass == long.class) {
            methodName = "writeInt64";
            methodDesc = "([BIJ)I";
        } else if (fieldClass == float.class) {
            methodName = "writeFloat";
            methodDesc = "([BIF)I";
        } else if (fieldClass == double.class) {
            methodName = "writeDouble";
            methodDesc = "([BID)I";
        } else if (fieldClass == Boolean.class) {
            methodName = "writeBoolean";
            methodDesc = "([BILjava/lang/Boolean;)I";
        } else if (fieldClass == Byte.class) {
            methodName = "writeInt8";
            methodDesc = "([BILjava/lang/Byte;J)I";
        } else if (fieldClass == Short.class) {
            methodName = "writeInt16";
            methodDesc = "([BILjava/lang/Short;J)I";
        } else if (fieldClass == Integer.class) {
            methodName = "writeInt32";
            methodDesc = "([BILjava/lang/Integer;J)I";
        } else if (fieldClass == Long.class) {
            methodName = "writeInt64";
            methodDesc = "([BILjava/lang/Long;J)I";
        } else if (fieldClass == Float.class) {
            methodName = "writeFloat";
            methodDesc = "([BILjava/lang/Float;J)I";
        } else if (fieldClass == Double.class) {
            methodName = "writeDouble";
            methodDesc = "([BILjava/lang/Double;J)I";
        } else if (fieldClass == String.class) {
            methodName = "writeString";
            methodDesc = "([BILjava/lang/String;)I";
        } else if (fieldWriter instanceof FieldWriterEnum) {
            methodName = "writeEnum";
            methodDesc = "([BILjava/lang/Enum;J)I";
        } else if (fieldClass == UUID.class) {
            methodName = "writeUUID";
            methodDesc = "([BILjava/util/UUID;)I";
        } else if (fieldClass == LocalDate.class) {
            methodName = "writeLocalDate";
            methodDesc = "([BILjava/time/LocalDate;)I";
        } else if (fieldClass == LocalTime.class) {
            methodName = "writeLocalTime";
            methodDesc = "([BILjava/time/LocalTime;)I";
        } else if (fieldClass == LocalDateTime.class) {
            methodName = "writeLocalDateTime";
            methodDesc = "([BILjava/time/LocalDateTime;)I";
        } else if (fieldClass == OffsetDateTime.class) {
            methodName = "writeOffsetDateTime";
            methodDesc = "([BILjava/time/OffsetDateTime;)I";
        } else if (fieldClass == OffsetTime.class) {
            methodName = "writeOffsetTime";
            methodDesc = "([BILjava/time/OffsetTime;)I";
        } else if (fieldClass == Instant.class) {
            methodName = "writeInstant";
            methodDesc = "([BILjava/time/Instant;)I";
        } else if (fieldClass == String[].class) {
            methodName = "writeString";
            methodDesc = "([BI[Ljava/lang/String;J)I";
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            Class<?> itemClass = fieldWriter.getItemClass();
            if (itemClass == String.class) {
                methodName = "writeString";
                methodDesc = "([BILjava/util/Collection;J)I";
            } else if (itemClass == Long.class) {
                methodName = "writeInt64";
                methodDesc = "([BILjava/util/Collection;J)I";
            } else {
                throw new JSONException("assert error " + fieldClass.getName());
            }
        } else {
            throw new JSONException("assert error " + fieldClass.getName());
        }

        boolean needFeatures = fieldClass == Float.class
                || fieldClass == Double.class
                || fieldClass == Byte.class
                || fieldClass == Short.class
                || fieldClass == Integer.class
                || fieldClass == Long.class
                || fieldClass == String[].class
                || Collection.class.isAssignableFrom(fieldClass)
                || fieldWriter instanceof FieldWriterEnum;
        if (needFeatures) {
            mw.lload(FEATURES);
            long fieldFeatures = objectFeatures | fieldWriter.features;
            if (fieldFeatures != 0) {
                mw.visitLdcInsn(fieldFeatures);
                mw.lor();
            }
        }

        mw.invokestatic(TYPE_JSONB_IO, methodName, methodDesc, true);

        mw.istore(OFFSET);

        if (endFieldValue_ != null) {
            if (pop) {
                mw.aload(JSON_WRITER);
                mw.aload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
            }
            mw.visitLabel(endFieldValue_);
        }
    }

    private void sotreFieldValueToLocalVar(
            MethodWriterContext mwc,
            int i,
            FieldWriter fieldWriter,
            int OBJECT,
            MethodWriter mw
    ) {
        int FIELD_VALUE = mwc.var("field_" + i);
        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.astore(FIELD_VALUE);
    }

    private static boolean isFieldVarIndex(MethodWriterContext mwc, FieldWriter fieldWriter) {
        Class fieldClass = fieldWriter.fieldClass;
        return fieldClass == String.class
                || Collection.class.isAssignableFrom(fieldClass)
                || fieldClass == String[].class
                || fieldWriter instanceof FieldWriterEnum
                || Collection.class.isAssignableFrom(fieldClass) && !mwc.disableReferenceDetect();
    }

    /**
     * 调用之前当前需要有个int类型变量
     */
    private static void fieldValueCapacity(
            long objectFeatures,
            List<FieldWriterRecord> fieldWriters,
            MethodWriterContext mwc,
            MethodWriter mw,
            int FEATURES
    ) {
        /*
         * minCapacity += stringSize(field_string_N);
         */
        for (FieldWriterRecord item : fieldWriters) {
            FieldWriter fieldWriter = item.fieldWriter;
            if (fieldWriter.fieldClass == String.class) {
                int FIELD_VALUE = mwc.var("field_" + item.ordinal);
                mw.aload(FIELD_VALUE);
                mw.invokestatic(TYPE_JSONB_IO, "stringCapacity", "(Ljava/lang/String;)I", true);
                mw.iadd();
            } else if (fieldWriter.fieldClass == String[].class) {
                int FIELD_VALUE = mwc.var("field_" + item.ordinal);
                mw.aload(FIELD_VALUE);
                mw.invokestatic(TYPE_JSONB_IO, "stringCapacity", "([Ljava/lang/String;)I", true);
                mw.iadd();
            } else if (fieldWriter instanceof FieldWriterEnum) {
                int FIELD_VALUE = mwc.var("field_" + item.ordinal);
                mw.aload(FIELD_VALUE);
                mw.lload(FEATURES);
                long fieldFeatures = objectFeatures | fieldWriter.features;
                if (fieldFeatures != 0) {
                    mw.visitLdcInsn(fieldFeatures);
                    mw.lor();
                }
                mw.invokestatic(TYPE_JSONB_IO, "enumCapacity", "(Ljava/lang/Enum;J)I", true);
                mw.iadd();
            } else if (Collection.class.isAssignableFrom(fieldWriter.fieldClass)) {
                int FIELD_VALUE = mwc.var("field_" + item.ordinal);
                mw.aload(FIELD_VALUE);
                Class<?> itemClass = fieldWriter.getItemClass();
                if (itemClass == String.class) {
                    mw.invokestatic(TYPE_JSONB_IO, "stringCapacity", "(Ljava/util/Collection;)I", true);
                    mw.iadd();
                } else if (itemClass == Long.class) {
                    mw.invokestatic(TYPE_JSONB_IO, "int64Capacity", "(Ljava/util/Collection;)I", true);
                    mw.iadd();
                } else {
                    throw new JSONException("assert error itemClass " + itemClass.getName());
                }
            }
        }
    }

    private void genMethodWriteArrayMappingJSONB(
            ObjectWriterProvider provider,
            Class objectType,
            long objectFeatures,
            List<FieldWriterGroup> fieldWriterGroups,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            long features
    ) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "writeArrayMappingJSONB",
                METHOD_DESC_WRITE,
                512
        );
        MethodWriterContext mwc = new MethodWriterContext(provider, objectType, objectFeatures, classNameType, mw, 7, JSONWriterType.JSONB);

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;
        final int OFFSET = mwc.var("offset");
        final int BYTES = mwc.var("bytes");
        final int FEATURES = mwc.var2(CONTEXT_FEATURES);

        if ((features & FieldInfo.DISABLE_AUTO_TYPE) == 0) {
            Label notWriteType = new Label();
            isWriteTypeInfo(objectFeatures, mw, OBJECT, FIELD_TYPE, FIELD_FEATURES, notWriteType);

            mw.aload(THIS);
            mw.aload(JSON_WRITER);
            mw.invokevirtual(classNameType, "writeClassInfo", METHOD_DESC_WRITE_CLASS_INFO);

            mw.visitLabel(notWriteType);
        }

        int size = fieldWriters.size();
        mwc.genVariantsMethodBefore(true);

        for (FieldWriterGroup group : fieldWriterGroups) {
            if (group.direct) {
                /*
                 * int offset = jsonWriter.getOffset();
                 */
                mw.aload(JSON_WRITER);
                mw.invokevirtual(TYPE_JSON_WRITER, "getOffset", "()I");
                mw.istore(OFFSET);

                int minCapacity = 6;
                for (FieldWriterRecord item : group.fieldWriters) {
                    FieldWriter fieldWriter = item.fieldWriter;
                    Class fieldClass = fieldWriter.fieldClass;
                    if (isFieldVarIndex(mwc, fieldWriter)) {
                        // save field values to local variants
                        sotreFieldValueToLocalVar(mwc, item.ordinal, fieldWriter, OBJECT, mw);
                    } else {
                        minCapacity += fieldCapacity(fieldClass);
                    }
                }

                /*
                 * byte[] bytes = (byte[]) jsonWriter.ensureCapacity(offset + size)
                 */
                mw.aload(JSON_WRITER);
                mw.iload(OFFSET);
                mw.visitLdcInsn(minCapacity);
                mw.iadd();

                fieldValueCapacity(objectFeatures, group.fieldWriters, mwc, mw, FEATURES);

                mw.invokevirtual(TYPE_JSON_WRITER, "ensureCapacity", "(I)Ljava/lang/Object;");
                mw.checkcast("[B");
                mw.astore(BYTES);

                if (group.start) {
                    mw.aload(BYTES);
                    mw.iload(OFFSET);
                    mw.visitLdcInsn(fieldWriters.size());
                    mw.invokestatic(TYPE_JSONB_IO, "startArray", "([BII)I", true);
                    mw.istore(OFFSET);
                }

                int symbolTable = mwc.var("symbolTable");
                mw.aload(JSON_WRITER);
                mw.getfield(TYPE_JSON_WRITER, "symbolTable", DESC_SYMBOL);
                mw.astore(symbolTable);

                for (FieldWriterRecord item : group.fieldWriters) {
                    FieldWriter fieldWriter = item.fieldWriter;
                    writeFieldValueDirectJSONB(objectFeatures, classNameType, mwc, fieldWriter, item.ordinal, mw, BYTES, OFFSET, OBJECT, FEATURES, symbolTable, false);
                }

                mw.aload(JSON_WRITER);
                mw.iload(OFFSET);
                mw.invokevirtual(TYPE_JSON_WRITER, "setOffset", "(I)V");
            } else {
                if (group.start) {
                    mw.aload(JSON_WRITER);
                    if (size <= 15) {
                        mw.invokevirtual(TYPE_JSON_WRITER, "startArray" + size, "()V");
                    } else {
                        mw.iconst_n(size);
                        mw.invokevirtual(TYPE_JSON_WRITER, "startArray", "(I)V");
                    }
                }
                for (FieldWriterRecord fieldWriter : group.fieldWriters) {
                    gwValueJSONB(mwc, fieldWriter.fieldWriter, OBJECT, fieldWriter.ordinal);
                }
            }
        }

        mw.return_();
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
    }

    private int fieldCapacity(Class<?> fieldClass) {
        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return 1;
        } else if (fieldClass == byte.class || fieldClass == Byte.class) {
            return 2;
        } else if (fieldClass == short.class || fieldClass == Short.class) {
            return 3;
        } else if (fieldClass == int.class || fieldClass == Integer.class
                || fieldClass == float.class || fieldClass == Float.class
                || fieldClass == LocalDate.class
        ) {
            return 5;
        } else if (fieldClass == long.class || fieldClass == Long.class
                || fieldClass == double.class || fieldClass == Double.class
                || fieldClass == LocalTime.class
        ) {
            return 9;
        } else if (fieldClass == LocalDateTime.class) {
            return 13;
        } else if (fieldClass == Instant.class) {
            return 15;
        } else if (fieldClass == UUID.class) {
            return 18;
        } else if (fieldClass == OffsetDateTime.class || fieldClass == OffsetTime.class) {
            return 21;
        } else {
            throw new JSONException("assert error " + fieldClass.getName());
        }
    }

    private void gwValueJSONB(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        long features = fieldWriter.features | mwc.objectFeatures;
        Class<?> fieldClass = fieldWriter.fieldClass;

        boolean beanToArray = (features & JSONWriter.Feature.BeanToArray.mask) != 0;
        boolean userDefineWriter = false;
        if ((fieldClass == long.class || fieldClass == Long.class || fieldClass == long[].class)
                && (mwc.provider.userDefineMask & TYPE_INT64_MASK) != 0) {
            userDefineWriter = mwc.provider.getObjectWriter(Long.class) != ObjectWriterImplInt64.INSTANCE;
        }

        if (fieldClass == boolean.class
                || fieldClass == boolean[].class
                || fieldClass == char.class
                || fieldClass == char[].class
                || fieldClass == byte.class
                || fieldClass == byte[].class
                || fieldClass == short.class
                || fieldClass == short[].class
                || fieldClass == int.class
                || fieldClass == int[].class
                || fieldClass == long.class
                || (fieldClass == long[].class && !userDefineWriter)
                || fieldClass == float.class
                || fieldClass == float[].class
                || fieldClass == double.class
                || fieldClass == double[].class
                || fieldClass == String.class
                || fieldClass == Integer.class
                || fieldClass == Long.class
                || fieldClass == BigDecimal.class
                || fieldClass.isEnum()
        ) {
            gwValue(mwc, fieldWriter, OBJECT, i, null);
        } else if (fieldClass == Date.class) {
            gwDate(mwc, fieldWriter, OBJECT, i);
        } else if (fieldWriter instanceof FieldWriterList) {
            gwListJSONB(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass.isArray()) {
            gwObjectA(mwc, fieldWriter, OBJECT, i);
        } else {
            gwObjectJSONB(fieldWriter, OBJECT, mwc, i, beanToArray);
        }
    }

    private void gwObjectJSONB(
            FieldWriter fieldWriter,
            int OBJECT,
            MethodWriterContext mwc,
            int i,
            boolean beanToArray
    ) {
        Class<?> fieldClass = fieldWriter.fieldClass;
        String fieldName = fieldWriter.fieldName;

        String classNameType = mwc.classNameType;
        MethodWriter mw = mwc.mw;
        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        mw.ifnonnull(notNull_);
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        boolean refDetection = (!mwc.disableSupportArrayMapping()) && !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
        if (refDetection) {
            int REF_PATH = mwc.var("REF_PATH");
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, endDetect_);

            if (mwc.objectClass != null && fieldClass.isAssignableFrom(mwc.objectClass)) {
                mw.aload(OBJECT);
                mw.aload(FIELD_VALUE);
                mw.if_acmpne(refSetPath_);

                mw.aload(JSON_WRITER);
                mw.visitLdcInsn("..");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V");

                mw.goto_(endIfNull_);

                mw.visitLabel(refSetPath_);
            }

            mw.aload(JSON_WRITER);
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "setPath0", METHOD_DESC_SET_PATH2);
            mw.dup();
            mw.astore(REF_PATH);
            mw.ifnull(endDetect_);

            mw.aload(JSON_WRITER);
            mw.aload(REF_PATH);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE);

            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
            mw.goto_(endIfNull_);

            mw.visitLabel(endDetect_);
        }

        // fw.getObjectWriter(w, value.getClass());
        mw.aload(THIS);
        mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.aload(JSON_WRITER);
        mw.aload(FIELD_VALUE);
        mw.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");
        mw.invokevirtual(
                TYPE_FIELD_WRITER,
                "getObjectWriter",
                METHOD_DESC_GET_OBJECT_WRITER);

        // objectWriter.write(jw, ctx, value);
        mw.aload(JSON_WRITER);
        mw.aload(FIELD_VALUE);
        mw.visitLdcInsn(fieldName);
        mwc.loadFieldType(i, fieldWriter.fieldType);
        mw.visitLdcInsn(fieldWriter.features);
        mw.invokeinterface(
                TYPE_OBJECT_WRITER,
                beanToArray ? "writeJSONB" : "writeArrayMappingJSONB",
                METHOD_DESC_WRITE_OBJECT);

        if (refDetection) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwListJSONB(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        boolean disableReferenceDetect = mwc.disableReferenceDetect();

        Type fieldType = fieldWriter.fieldType;
        Class<?> fieldClass = fieldWriter.fieldClass;

        String classNameType = mwc.classNameType;
        MethodWriter mw = mwc.mw;
        int LIST = mwc.var(fieldClass);
        int REF_PATH = mwc.var("REF_PATH");

        boolean listSimple = false;
        Type itemType;
        Class itemClass = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
                itemClass = TypeUtils.getClass(itemType);
                listSimple = (itemType == String.class || itemType == Integer.class || itemType == Long.class);
            }
        }

        Label endIfListNull_ = new Label(), listNotNull_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(LIST);
        mw.ifnonnull(listNotNull_);

        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
        mw.goto_(endIfListNull_);

        mw.visitLabel(listNotNull_);

        if (!disableReferenceDetect) {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, endDetect_);

            if (fieldClass.isAssignableFrom(mwc.objectClass)) {
                mw.aload(OBJECT);
                mw.aload(LIST);
                mw.if_acmpne(refSetPath_);

                mw.aload(JSON_WRITER);
                mw.visitLdcInsn("..");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V");

                mw.goto_(endIfListNull_);

                mw.visitLabel(refSetPath_);
            }

            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(LIST);
            mw.lload(mwc.var(CONTEXT_FEATURES));
            mw.invokevirtual(TYPE_FIELD_WRITER, "isRefDetect", "(Ljava/lang/Object;J)Z");
            mw.ifeq(endDetect_);

            mw.aload(JSON_WRITER);
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(LIST);
            mw.invokevirtual(TYPE_JSON_WRITER, "setPath0", METHOD_DESC_SET_PATH2);
            mw.dup();
            mw.astore(REF_PATH);
            mw.ifnull(endDetect_);

            mw.aload(JSON_WRITER);
            mw.aload(REF_PATH);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE);

            mw.aload(JSON_WRITER);
            mw.aload(LIST);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
            mw.goto_(endIfListNull_);

            mw.visitLabel(endDetect_);
        }

        if (listSimple) {
            gwListSimpleType(mwc, i, mw, fieldClass, itemClass, LIST);
        } else {
            int PREVIOUS_CLASS = mwc.var("ITEM_CLASS");
            int ITEM_OBJECT_WRITER = mwc.var("ITEM_OBJECT_WRITER");
            mw.aconst_null();
            mw.dup();
            mw.astore(PREVIOUS_CLASS);
            mw.astore(ITEM_OBJECT_WRITER);

            // TODO writeTypeInfo
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);
            mw.aload(LIST);
            mw.invokevirtual(
                    TYPE_FIELD_WRITER,
                    "writeListValueJSONB",
                    METHOD_DESC_WRITE_LIST);
        }

        if (!disableReferenceDetect) {
            mw.aload(JSON_WRITER);
            mw.aload(LIST);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
        }

        mw.visitLabel(endIfListNull_);
    }

    private void gwDate(MethodWriterContext mwc, FieldWriter fieldWriter, int OBJECT, int i) {
        MethodWriter mw = mwc.mw;
        mw.aload(THIS);
        mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.aload(JSON_WRITER);
        mw.iconst_0();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.invokevirtual(TYPE_FIELD_WRITER, "writeDate", METHOD_DESC_WRITE_DATE_WITH_FIELD_NAME);
    }

    private void gwValue(MethodWriterContext mwc, FieldWriter fieldWriter, int OBJECT, int i, Integer LOCAL_FIELD_VALUE) {
        MethodWriter mw = mwc.mw;
        Class fieldClass = fieldWriter.fieldClass;

        if (fieldClass == String.class) {
            genGetObject(mwc, fieldWriter, i, OBJECT);
            mw.checkcast("java/lang/String");
            int FIELD_VALUE = mwc.var("FIELD_VALUE_" + fieldWriter.fieldClass.getName());
            mw.astore(FIELD_VALUE);

            gwString(mwc, false, true, FIELD_VALUE);
            return;
        }

        mw.aload(JSON_WRITER);
        if (LOCAL_FIELD_VALUE != null) {
            mw.loadLocal(fieldClass, LOCAL_FIELD_VALUE);
        } else {
            genGetObject(mwc, fieldWriter, i, OBJECT);
        }

        if (fieldWriter.decimalFormat != null) {
            if (fieldClass == double.class) {
                mw.aload(THIS);
                mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                mw.getfield(TYPE_FIELD_WRITER, "decimalFormat", "Ljava/text/DecimalFormat;");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeDouble", "(DLjava/text/DecimalFormat;)V");
            } else if (fieldClass == float.class) {
                mw.aload(THIS);
                mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                mw.getfield(TYPE_FIELD_WRITER, "decimalFormat", "Ljava/text/DecimalFormat;");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeFloat", "(FLjava/text/DecimalFormat;)V");
            } else if (fieldClass == BigDecimal.class) {
                mw.visitLdcInsn(fieldWriter.features);
                mw.aload(THIS);
                mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                mw.getfield(TYPE_FIELD_WRITER, "decimalFormat", "Ljava/text/DecimalFormat;");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeDecimal", "(Ljava/math/BigDecimal;JLjava/text/DecimalFormat;)V");
            } else {
                throw new UnsupportedOperationException();
            }
            return;
        }

        boolean writeAsString = (fieldWriter.features & WriteNonStringValueAsString.mask) != 0;

        if (fieldClass == int.class && !writeAsString) {
            String format = fieldWriter.format;
            if ("string".equals(format)) {
                mw.invokestatic("java/lang/Integer", "toString", "(I)Ljava/lang/String;");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeString", "(Ljava/lang/String;)V");
            } else if (format != null) {
                mw.visitLdcInsn(format);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeInt32", "(ILjava/lang/String;)V");
            } else {
                mw.invokevirtual(TYPE_JSON_WRITER, "writeInt32", "(I)V");
            }
            return;
        }

        String methodName, methodDesc;
        if (fieldClass == boolean.class) {
            methodName = "writeBool";
            methodDesc = "(Z)V";
        } else if (fieldClass == char.class) {
            methodName = "writeChar";
            methodDesc = "(C)V";
        } else if (fieldClass == byte.class) {
            methodName = writeAsString ? "writeString" : "writeInt8";
            methodDesc = "(B)V";
        } else if (fieldClass == short.class) {
            methodName = writeAsString ? "writeString" : "writeInt16";
            methodDesc = "(S)V";
        } else if (fieldClass == int.class) {
            methodName = writeAsString ? "writeString" : "writeInt32";
            methodDesc = "(I)V";
        } else if (fieldClass == Integer.class) {
            methodName = "writeInt32";
            methodDesc = "(Ljava/lang/Integer;)V";
        } else if (fieldClass == long.class) {
            methodName = writeAsString ? "writeString" : "writeInt64";
            methodDesc = "(J)V";
        } else if (fieldClass == Long.class) {
            methodName = "writeInt64";
            methodDesc = "(Ljava/lang/Long;)V";
        } else if (fieldClass == float.class) {
            methodName = writeAsString ? "writeString" : "writeFloat";
            methodDesc = "(F)V";
        } else if (fieldClass == double.class) {
            methodName = writeAsString ? "writeString" : "writeDouble";
            methodDesc = "(D)V";
        } else if (fieldClass == boolean[].class) {
            methodName = "writeBool";
            methodDesc = "([Z)V";
        } else if (fieldClass == char[].class) {
            methodName = "writeString";
            methodDesc = "([C)V";
        } else if (fieldClass == byte[].class) {
            methodName = "writeBinary";
            methodDesc = "([B)V";
        } else if (fieldClass == short[].class) {
            methodName = "writeInt16";
            methodDesc = "([S)V";
        } else if (fieldClass == int[].class) {
            methodName = "writeInt32";
            methodDesc = "([I)V";
        } else if (fieldClass == long[].class && mwc.provider.getObjectWriter(Long.class) == ObjectWriterImplInt64.INSTANCE) {
            methodName = "writeInt64";
            methodDesc = "([J)V";
        } else if (fieldClass == float[].class) {
            methodName = "writeFloat";
            methodDesc = "([F)V";
        } else if (fieldClass == double[].class) {
            methodName = "writeDouble";
            methodDesc = "([D)V";
        } else if (fieldClass == BigDecimal.class) {
            methodName = "writeDecimal";
            methodDesc = "(Ljava/math/BigDecimal;JLjava/text/DecimalFormat;)V";
            mw.visitLdcInsn(fieldWriter.features);
            mw.aconst_null();
        } else if (Enum.class.isAssignableFrom(fieldClass)) {
            methodName = "writeEnum";
            methodDesc = "(Ljava/lang/Enum;)V";
//        } else if (fieldClass == String.class) {
//            methodName = "writeString";
//            methodDesc = "(Ljava/lang/String;)V";
        } else {
            throw new UnsupportedOperationException();
        }

        mw.invokevirtual(TYPE_JSON_WRITER, methodName, methodDesc);
    }

    private void gwObjectA(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;
        if (fieldWriter.fieldClass == String[].class) {
            mw.aload(JSON_WRITER);
            genGetObject(mwc, fieldWriter, i, OBJECT);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeString", "([Ljava/lang/String;)V");
        } else {
            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);
            mw.aload(OBJECT);
            mw.invokevirtual(TYPE_FIELD_WRITER, "writeValue", METHOD_DESC_WRITE_VALUE);
        }
    }

    private void genMethodWriteArrayMapping(
            ObjectWriterProvider provider,
            String methodName,
            Class objectType,
            long objectFeatures,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            JSONWriterType type
    ) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                methodName,
                METHOD_DESC_WRITE,
                512
        );

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;

        Label jsonb_ = new Label();

        mw.aload(JSON_WRITER);
        mw.getfield(TYPE_JSON_WRITER, "jsonb", "Z");
        mw.ifeq(jsonb_);

        mw.aload(THIS);
        mw.aload(JSON_WRITER);
        mw.aload(OBJECT);
        mw.aload(FIELD_NAME);
        mw.aload(FIELD_TYPE);
        mw.lload(FIELD_FEATURES);
        mw.invokevirtual(classNameType, "writeArrayMappingJSONB", METHOD_DESC_WRITE_OBJECT);
        mw.return_();

        mw.visitLabel(jsonb_);

        Label object_ = new Label();
        hashFilter(mw, fieldWriters, object_);

        mw.aload(THIS);
        mw.aload(JSON_WRITER);
        mw.aload(OBJECT);
        mw.aload(FIELD_NAME);
        mw.aload(FIELD_TYPE);
        mw.lload(FIELD_FEATURES);
        mw.invokespecial(TYPE_OBJECT_WRITER_ADAPTER, methodName, METHOD_DESC_WRITE);
        mw.return_();

        mw.visitLabel(object_);

        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "startArray", "()V");

        MethodWriterContext mwc = new MethodWriterContext(provider, objectType, objectFeatures, classNameType, mw, 7, type);

        for (int i = 0; i < fieldWriters.size(); i++) {
            if (i != 0) {
                mw.aload(JSON_WRITER);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeComma", "()V");
            }

            gwFieldValueArrayMapping(
                    fieldWriters.get(i),
                    mwc,
                    OBJECT,
                    i
            );
        }

        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "endArray", "()V");

        mw.return_();
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
    }

    private static void hashFilter(MethodWriter mw, List<FieldWriter> fieldWriters, Label object_) {
        boolean containsNoneFieldGetter = false;
        for (FieldWriter fieldWriter : fieldWriters) {
            if (fieldWriter.method != null && (fieldWriter.features & FieldInfo.FIELD_MASK) == 0) {
                containsNoneFieldGetter = true;
                break;
            }
        }

        mw.aload(THIS);
        mw.aload(JSON_WRITER);
        mw.invokevirtual(
                TYPE_OBJECT_WRITER_ADAPTER,
                containsNoneFieldGetter ? "hasFilter" : "hasFilter0",
                METHOD_DESC_HAS_FILTER);
        mw.ifeq(object_);
    }

    private void gwFieldValueArrayMapping(
            FieldWriter fieldWriter,
            MethodWriterContext mwc,
            int OBJECT,
            int i
    ) {
        Class objectType = mwc.objectClass;
        Class<?> fieldClass = fieldWriter.fieldClass;

        final String TYPE_OBJECT = objectType == null ? "java/lang/Object" : ASMUtils.type(objectType);

        boolean userDefineWriter = false;
        if ((fieldClass == long.class || fieldClass == Long.class || fieldClass == long[].class)
                && (mwc.provider.userDefineMask & TYPE_INT64_MASK) != 0) {
            userDefineWriter = mwc.provider.getObjectWriter(Long.class) != ObjectWriterImplInt64.INSTANCE;
        }

        if (fieldClass == boolean.class
                || fieldClass == boolean[].class
                || fieldClass == char.class
                || fieldClass == char[].class
                || fieldClass == byte.class
                || fieldClass == byte[].class
                || fieldClass == short.class
                || fieldClass == short[].class
                || fieldClass == int.class
                || fieldClass == int[].class
                || fieldClass == long.class
                || (fieldClass == long[].class && !userDefineWriter)
                || fieldClass == float.class
                || fieldClass == float[].class
                || fieldClass == double.class
                || fieldClass == double[].class
                || fieldClass == String.class
                || fieldClass == Integer.class
                || fieldClass == Long.class
                || fieldClass == BigDecimal.class
                || fieldClass.isEnum()
        ) {
            gwValue(mwc, fieldWriter, OBJECT, i, null);
        } else if (fieldClass == Date.class) {
            gwDate(mwc, fieldWriter, OBJECT, i);
        } else if (fieldWriter instanceof FieldWriterList) {
            gwList(mwc, OBJECT, i, fieldWriter);
        } else {
            gwObject(mwc, OBJECT, i, fieldWriter, TYPE_OBJECT);
        }
    }

    private void gwObject(
            MethodWriterContext mwc,
            int OBJECT,
            int i,
            FieldWriter fieldWriter,
            String TYPE_OBJECT
    ) {
        Class<?> fieldClass = fieldWriter.fieldClass;
        String fieldName = fieldWriter.fieldName;

        MethodWriter mw = mwc.mw;
        int FIELD_VALUE = mwc.var(fieldClass);
        int REF_PATH = mwc.var("REF_PATH");

        Label endIfNull_ = new Label(), notNull_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        mw.ifnonnull(notNull_);
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        if (fieldClass == Double.class || fieldClass == Float.class || fieldClass == BigDecimal.class) {
            mw.aload(JSON_WRITER);
            if (fieldWriter.decimalFormat != null) {
                mw.aload(FIELD_VALUE);
                if (fieldClass == Double.class) {
                    mw.invokevirtual("java/lang/Double", "doubleValue", "()D");
                    mw.aload(THIS);
                    mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                    mw.getfield(TYPE_FIELD_WRITER, "decimalFormat", "Ljava/text/DecimalFormat;");
                    mw.invokevirtual(TYPE_JSON_WRITER, "writeDouble", "(DLjava/text/DecimalFormat;)V");
                } else if (fieldClass == Float.class) {
                    mw.invokevirtual("java/lang/Float", "floatValue", "()F");
                    mw.aload(THIS);
                    mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                    mw.getfield(TYPE_FIELD_WRITER, "decimalFormat", "Ljava/text/DecimalFormat;");
                    mw.invokevirtual(TYPE_JSON_WRITER, "writeFloat", "(FLjava/text/DecimalFormat;)V");
                } else {
                    long features = fieldWriter.features;
                    mw.visitLdcInsn(features);
                    mw.aload(THIS);
                    mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                    mw.getfield(TYPE_FIELD_WRITER, "decimalFormat", "Ljava/text/DecimalFormat;");
                    mw.invokevirtual(TYPE_JSON_WRITER, "writeDecimal", "(Ljava/math/BigDecimal;JLjava/text/DecimalFormat;)V");
                }
            } else {
                mw.aload(FIELD_VALUE);
                if (fieldClass == Double.class) {
                    mw.invokevirtual("java/lang/Double", "doubleValue", "()D");
                    mw.invokevirtual(TYPE_JSON_WRITER, "writeDouble", "(D)V");
                } else if (fieldClass == Float.class) {
                    mw.invokevirtual("java/lang/Float", "floatValue", "()F");
                    mw.invokevirtual(TYPE_JSON_WRITER, "writeFloat", "(F)V");
                } else {
                    long features = fieldWriter.features;
                    mw.visitLdcInsn(features);
                    mw.aconst_null();
                    mw.invokevirtual(TYPE_JSON_WRITER, "writeDecimal", "(Ljava/math/BigDecimal;JLjava/text/DecimalFormat;)V");
                }
            }
        } else {
            boolean refDetection = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
            if (refDetection) {
                Label endDetect_ = new Label(), refSetPath_ = new Label();

                mw.aload(JSON_WRITER);
                mw.invokevirtual(TYPE_JSON_WRITER, "isRefDetect", "()Z");
                mw.ifeq(endDetect_);

                if (mwc.objectClass != null && fieldClass.isAssignableFrom(mwc.objectClass)) {
                    mw.aload(OBJECT);
                    mw.aload(FIELD_VALUE);
                    mw.if_acmpne(refSetPath_);

                    mw.aload(JSON_WRITER);
                    mw.visitLdcInsn("..");
                    mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V");

                    mw.goto_(endIfNull_);

                    mw.visitLabel(refSetPath_);
                }

                mw.aload(JSON_WRITER);
                mw.aload(THIS);
                mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                mw.aload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "setPath0", METHOD_DESC_SET_PATH2);
                mw.dup();
                mw.astore(REF_PATH);
                mw.ifnull(endDetect_);

                mw.aload(JSON_WRITER);
                mw.aload(REF_PATH);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE);

                mw.aload(JSON_WRITER);
                mw.aload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
                mw.goto_(endIfNull_);

                mw.visitLabel(endDetect_);
            }

            if (fieldClass == String[].class) {
                mw.aload(JSON_WRITER);
                mw.aload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeString", "([Ljava/lang/String;)V");
            } else {
                // fw.getObjectWriter(w, value.getClass());
                mw.aload(THIS);
                mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                mw.aload(JSON_WRITER);
                mw.aload(FIELD_VALUE);
                mw.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");
                mw.invokevirtual(
                        TYPE_FIELD_WRITER,
                        "getObjectWriter",
                        METHOD_DESC_GET_OBJECT_WRITER);

                // objectWriter.write(jw, ctx, value);
                mw.aload(JSON_WRITER);
                mw.aload(FIELD_VALUE);
                mw.visitLdcInsn(fieldWriter.fieldName);
                mwc.loadFieldType(i, fieldWriter.fieldType);
                mw.visitLdcInsn(fieldWriter.features);
                mw.invokeinterface(TYPE_OBJECT_WRITER, "write", METHOD_DESC_WRITE_OBJECT);
            }

            if (refDetection) {
                mw.aload(JSON_WRITER);
                mw.aload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
            }
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwList(MethodWriterContext mwc, int OBJECT, int i, FieldWriter fieldWriter) {
        Type fieldType = fieldWriter.fieldType;
        Class<?> fieldClass = fieldWriter.fieldClass;
        int LIST = mwc.var(fieldClass);
        MethodWriter mw = mwc.mw;

        boolean listSimple = false;
        Type itemType;
        Class itemClass = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
                itemClass = TypeUtils.getMapping(itemType);
                listSimple = (itemType == String.class || itemType == Integer.class || itemType == Long.class);
            }
        }

        Label L_END_IF_LIST_NULL = new Label(), L_LIST_NOT_NULL = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(LIST);
        mw.ifnonnull(L_LIST_NOT_NULL);

        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
        mw.goto_(L_END_IF_LIST_NULL);

        mw.visitLabel(L_LIST_NOT_NULL);

        if (listSimple) {
            genGetObject(mwc, fieldWriter, i, OBJECT);
            mw.astore(LIST);
            gwListSimpleType(mwc, i, mw, fieldClass, itemClass, LIST);
        } else {
            int LIST_SIZE = mwc.var("LIST_SIZE");
            int J = mwc.var("J");

            int ITEM_CLASS = mwc.var(Class.class);
            int PREVIOUS_CLASS = mwc.var("PREVIOUS_CLASS");
            int ITEM_OBJECT_WRITER = mwc.var("ITEM_OBJECT_WRITER");

            mw.aconst_null();
            mw.dup();
            mw.astore(PREVIOUS_CLASS);
            mw.astore(ITEM_OBJECT_WRITER);

            // for(int j = 0；
            mw.aload(LIST);
            mw.invokeinterface("java/util/List", "size", "()I");
            mw.istore(LIST_SIZE);

            // startArray(int size)
            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "startArray", "()V");

            Label for_start_j_ = new Label(), for_end_j_ = new Label(), for_inc_j_ = new Label(), notFirst_ = new Label();
            mw.iconst_0();
            mw.istore(J);

            mw.visitLabel(for_start_j_);
            mw.iload(J);
            mw.iload(LIST_SIZE);
            mw.if_icmpge(for_end_j_);

            mw.iload(J);
            mw.ifeq(notFirst_);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeComma", "()V");
            mw.visitLabel(notFirst_);

            int ITEM = mwc.var(itemClass);
            Label L_NOT_NULL = new Label(), L_CLASS_EQ = new Label();

            mw.aload(LIST);
            mw.iload(J);
            mw.invokeinterface("java/util/List", "get", "(I)Ljava/lang/Object;");
            mw.dup();
            mw.astore(ITEM);

            // if(item == null)
            mw.ifnonnull(L_NOT_NULL);
            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
            mw.goto_(for_inc_j_);

            mw.visitLabel(L_NOT_NULL);

            mw.aload(ITEM);
            mw.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mw.dup();
            mw.astore(ITEM_CLASS);

            // if (itemClass == previousClass) {
            mw.aload(PREVIOUS_CLASS);
            mw.if_acmpeq(L_CLASS_EQ);

            // previousObjectWriter = fw_i.getItemWriter(jsonWriter.getContext(), itemClass);
            mw.aload(0);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);
            mw.aload(ITEM_CLASS);

            mw.invokevirtual(TYPE_FIELD_WRITER,
                    "getItemWriter",
                    METHOD_DESC_GET_ITEM_WRITER);
            mw.astore(ITEM_OBJECT_WRITER);

            mw.aload(ITEM_CLASS);
            mw.astore(PREVIOUS_CLASS);

            mw.visitLabel(L_CLASS_EQ);
            mw.aload(ITEM_OBJECT_WRITER);
            mw.aload(JSON_WRITER);
            mw.aload(ITEM);
            mw.iload(J);
            mw.invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            mwc.loadFieldType(i, fieldType);
            mw.visitLdcInsn(fieldWriter.features);
            mw.invokeinterface(TYPE_OBJECT_WRITER, "write", METHOD_DESC_WRITE_OBJECT);

            mw.visitLabel(for_inc_j_);
            mw.visitIincInsn(J, 1);
            mw.goto_(for_start_j_);

            mw.visitLabel(for_end_j_);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "endArray", "()V");
        }

        mw.visitLabel(L_END_IF_LIST_NULL);
    }

    private void gwFieldValue(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        Class<?> fieldClass = fieldWriter.fieldClass;

        if (fieldClass == boolean.class) {
            gwFieldValueBooleanV(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == boolean[].class
                || fieldClass == byte[].class
                || fieldClass == char[].class
                || fieldClass == short[].class
                || fieldClass == float[].class
                || fieldClass == double[].class
        ) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == char.class
                || fieldClass == byte.class
                || fieldClass == int.class
                || fieldClass == short.class
                || fieldClass == float.class
        ) {
            gwFieldValueInt32V(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == int[].class) {
            gwFieldValueIntVA(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == long.class
                || fieldClass == double.class) {
            gwFieldValueInt64V(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == long[].class
                && mwc.provider.getObjectWriter(Long.class) == ObjectWriterImplInt64.INSTANCE
        ) {
            gwFieldValueInt64VA(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == Integer.class) {
            gwInt32(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Long.class) {
            gwInt64(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Float.class) {
            gwFloat(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Double.class) {
            gwDouble(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == String.class) {
            gwFieldValueString(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass.isEnum()
                && BeanUtils.getEnumValueField(fieldClass, mwc.provider) == null
                && !(fieldWriter instanceof FieldWriterObject)
        ) {
            gwFieldValueEnum(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Date.class) {
            gwFieldValueDate(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == List.class) {
            gwFieldValueList(mwc, fieldWriter, OBJECT, i);
        } else {
            gwFieldValueObject(mwc, fieldWriter, OBJECT, i, false);
        }
    }

    private void gwFieldValueEnum(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        Class<?> fieldClass = fieldWriter.fieldClass;

        MethodWriter mw = mwc.mw;

        int FIELD_VALUE = mwc.var(fieldClass);

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        Label null_ = new Label(), notNull_ = new Label();
        mw.ifnull(null_);

        // void writeEnum(JSONWriter jw, Enum e)
        mw.aload(0);
        mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.aload(JSON_WRITER);
        mw.aload(FIELD_VALUE);

        mw.invokevirtual(TYPE_FIELD_WRITER, "writeEnum", METHOD_DESC_WRITE_ENUM);
        mw.goto_(notNull_);

        mw.visitLabel(null_);

        // if (!jw.isWriteNulls())
        mw.iload(mwc.var(WRITE_NULLS));
        mw.ifeq(notNull_);

        // writeFieldName(w);
        gwFieldName(mwc, fieldWriter, i);

        // jw.writeNull
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");

        mw.visitLabel(notNull_);
    }

    private void gwFieldValueObject(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean jsonb
    ) {
        Class<?> fieldClass = fieldWriter.fieldClass;
        Type fieldType = fieldWriter.fieldType;
        String fieldName = fieldWriter.fieldName;

        boolean disableReferenceDetect = mwc.disableReferenceDetect();

        boolean refDetection = (!disableReferenceDetect) && !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
        int FIELD_VALUE = mwc.var(fieldClass);

        Integer REF_PATH = null;
        if (refDetection) {
            REF_PATH = mwc.var("REF_PATH");
        }

        long features = fieldWriter.features | mwc.objectFeatures;
        MethodWriter mw = mwc.mw;

        Label null_ = new Label(), notNull_ = new Label();

        if (fieldWriter.unwrapped() || (fieldWriter.features & WriteNonStringValueAsString.mask) != 0) {
            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);
            mw.aload(OBJECT);
            mw.invokevirtual(TYPE_FIELD_WRITER,
                    "write", METHOD_DESC_FIELD_WRITE_OBJECT);
            mw.pop();
            mw.goto_(notNull_);
        }

        if (fieldWriter.backReference) {
            mw.aload(JSON_WRITER);
            mw.aload(OBJECT);
            mw.invokevirtual(TYPE_JSON_WRITER, "containsReference", "(Ljava/lang/Object;)Z");
            mw.ifne(notNull_);
        }

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);
        mw.ifnull(null_);

        if (Map.class.isAssignableFrom(fieldClass)) {
            Label ignoreEmptyEnd_ = null;
            if ((fieldWriter.features & IgnoreEmpty.mask) == 0) {
                ignoreEmptyEnd_ = new Label();
                mwc.genIsEnabled(IgnoreEmpty.mask, ignoreEmptyEnd_);
            }

            mw.aload(FIELD_VALUE);
            mw.invokeinterface("java/util/Map", "isEmpty", "()Z");
            mw.ifne(notNull_);

            if (ignoreEmptyEnd_ != null) {
                mw.visitLabel(ignoreEmptyEnd_);
            }
        }

        if (!Serializable.class.isAssignableFrom(fieldClass) && fieldClass != List.class) {
            mw.aload(JSON_WRITER);
            if (!fieldWriter.isFieldClassSerializable()) {
                mw.invokevirtual(TYPE_JSON_WRITER, "isIgnoreNoneSerializable", "()Z");
            } else {
                mw.aload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "isIgnoreNoneSerializable", "(Ljava/lang/Object;)Z");
            }
            mw.ifne(notNull_);
        }

        if (refDetection) {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            int REF_DETECT = mwc.var("REF_DETECT");

            if (fieldClass == Object.class) {
                mw.aload(JSON_WRITER);
                mw.aload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "isRefDetect", "(Ljava/lang/Object;)Z");
            } else {
                mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, null);
            }
            mw.dup();
            mw.istore(REF_DETECT);
            mw.ifeq(endDetect_);

            if (mwc.objectClass != null && fieldClass.isAssignableFrom(mwc.objectClass)) {
                mw.aload(OBJECT);
                mw.aload(FIELD_VALUE);
                mw.if_acmpne(refSetPath_);

                gwFieldName(mwc, fieldWriter, i);

                mw.aload(JSON_WRITER);
                mw.visitLdcInsn("..");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V");

                mw.goto_(notNull_);

                mw.visitLabel(refSetPath_);
            }

            mw.aload(JSON_WRITER);
            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "setPath0", METHOD_DESC_SET_PATH2);
            mw.dup();
            mw.astore(REF_PATH);
            mw.ifnull(endDetect_);

            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);
            mw.aload(REF_PATH);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE);

            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
            mw.goto_(notNull_);

            mw.visitLabel(endDetect_);

            if ("this$0".equals(fieldName) || "this$1".equals(fieldName) || "this$2".equals(fieldName)) {
                mw.iload(REF_DETECT);
                mw.ifeq(null_);
            }
        }

        if (Object[].class.isAssignableFrom(fieldClass)) {
            Label notWriteEmptyArrayEnd_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.NotWriteEmptyArray.mask, notWriteEmptyArrayEnd_);

            mw.aload(FIELD_VALUE);
            mw.checkcast("[Ljava/lang/Object;");
            mw.arraylength();
            mw.ifne(notWriteEmptyArrayEnd_);

            mw.goto_(notNull_);

            mw.visitLabel(notWriteEmptyArrayEnd_);
        } else if (Collection.class.isAssignableFrom(fieldClass)) {
            Label notWriteEmptyArrayEnd_ = new Label();
            if ((features & NotWriteEmptyArray.mask) == 0) {
                mwc.genIsEnabled(JSONWriter.Feature.NotWriteEmptyArray.mask, notWriteEmptyArrayEnd_);
            }

            mw.aload(FIELD_VALUE);
            mw.checkcast("java/util/Collection");
            mw.invokeinterface("java/util/Collection", "isEmpty", "()Z");
            mw.ifeq(notWriteEmptyArrayEnd_);

            mw.goto_(notNull_);

            mw.visitLabel(notWriteEmptyArrayEnd_);
        }

        // writeFieldName(w);
        gwFieldName(mwc, fieldWriter, i);

        Class itemClass = fieldWriter.getItemClass();
        if (fieldClass == BigDecimal.class) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.visitLdcInsn(features);
            if (fieldWriter.decimalFormat != null) {
                mw.aload(THIS);
                mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
                mw.getfield(TYPE_FIELD_WRITER, "decimalFormat", "Ljava/text/DecimalFormat;");
            } else {
                mw.aconst_null();
            }
            mw.invokevirtual(TYPE_JSON_WRITER, "writeDecimal", "(Ljava/math/BigDecimal;JLjava/text/DecimalFormat;)V");
        } else if (fieldClass == BigInteger.class) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            if (features == 0) {
                mw.invokevirtual(TYPE_JSON_WRITER, "writeBigInt", "(Ljava/math/BigInteger;)V");
            } else {
                mw.visitLdcInsn(features);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeBigInt", "(Ljava/math/BigInteger;J)V");
            }
        } else if (fieldClass == UUID.class) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeUUID", "(Ljava/util/UUID;)V");
        } else if (fieldClass == LocalDate.class
                && fieldWriter.format == null
                && mwc.provider.getObjectWriter(LocalDate.class) == ObjectWriterImplLocalDate.INSTANCE
        ) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeLocalDate", "(Ljava/time/LocalDate;)V");
        } else if (fieldClass == OffsetDateTime.class
                && fieldWriter.format == null
                && mwc.provider.getObjectWriter(OffsetDateTime.class) == ObjectWriterImplOffsetDateTime.INSTANCE
        ) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeOffsetDateTime", "(Ljava/time/OffsetDateTime;)V");
        } else if (fieldClass == String[].class) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeString", "([Ljava/lang/String;)V");
        } else if (fieldClass == List.class && (itemClass == String.class || itemClass == Integer.class || itemClass == Long.class)) {
            gwListSimpleType(mwc, i, mw, fieldClass, itemClass, FIELD_VALUE);
        } else {
            // fw.getObjectWriter(w, value.getClass());
            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Object", "getClass", "()Ljava/lang/Class;");

            mw.invokevirtual(TYPE_FIELD_WRITER,
                    "getObjectWriter", METHOD_DESC_GET_OBJECT_WRITER);

            // objectWriter.write(jw, ctx, value);
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.visitLdcInsn(fieldName);
            mwc.loadFieldType(i, fieldType);
            mw.visitLdcInsn(features);

            String writeMethod;
            if (jsonb) {
                writeMethod = (features & JSONWriter.Feature.BeanToArray.mask) != 0 ? "writeArrayMappingJSONB" : "writeJSONB";
            } else {
                writeMethod = (features & JSONWriter.Feature.BeanToArray.mask) != 0 ? "writeArrayMapping" : "write";
            }
            mw.invokeinterface(
                    TYPE_OBJECT_WRITER,
                    writeMethod,
                    jsonb ? METHOD_DESC_WRITE_JSONB_OBJECT : METHOD_DESC_WRITE_OBJECT);
        }

        if (refDetection) {
            int REF_DETECT = mwc.var("REF_DETECT");

            Label endDetect_ = new Label();

            mw.iload(REF_DETECT);
            mw.ifeq(endDetect_);

            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");

            mw.visitLabel(endDetect_);
        }

        mw.goto_(notNull_);

        mw.visitLabel(null_);

        // if (!jw.isWriteNulls())
        if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
            long nullFeatures = JSONWriter.Feature.WriteNulls.mask;
            if (fieldClass == AtomicLongArray.class
                    || fieldClass == AtomicIntegerArray.class
                    || Collection.class.isAssignableFrom(fieldClass)
                    || fieldClass.isArray()) {
                nullFeatures |= WriteNullListAsEmpty.mask;
                nullFeatures |= NullAsDefaultValue.mask;
            } else if (Number.class.isAssignableFrom(fieldClass)) {
                nullFeatures |= WriteNullNumberAsZero.mask;
                nullFeatures |= NullAsDefaultValue.mask;
            } else if (fieldClass == Boolean.class) {
                nullFeatures |= WriteNullBooleanAsFalse.mask;
                nullFeatures |= NullAsDefaultValue.mask;
            } else if (fieldClass == String.class) {
                nullFeatures |= WriteNullStringAsEmpty.mask;
                nullFeatures |= NullAsDefaultValue.mask;
            } else {
                nullFeatures |= NullAsDefaultValue.mask;
            }
            mwc.genIsEnabled(fieldWriter.features, nullFeatures, notNull_);
//            mw.iload(mwc.var(WRITE_NULLS));
//            mw.ifeq(notNull_);
        }

        // writeFieldName(w);
        gwFieldName(mwc, fieldWriter, i);

        // jw.writeNull
        mw.aload(JSON_WRITER);
        String WRITE_NULL_METHOD;
        String WRITE_NULL_DESC = "()V";
        if (fieldClass == AtomicLongArray.class
                || fieldClass == AtomicIntegerArray.class
                || Collection.class.isAssignableFrom(fieldClass)
                || fieldClass.isArray()) {
            WRITE_NULL_METHOD = "writeArrayNull";
        } else if (fieldClass == Float.class
                || fieldClass == Double.class
                || fieldClass == BigDecimal.class) {
            WRITE_NULL_METHOD = "writeDecimalNull";
            WRITE_NULL_DESC = "(J)V";
            mw.lload(mwc.var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(fieldWriter.features);
            mw.lor();
        } else if (Number.class.isAssignableFrom(fieldClass)) {
            WRITE_NULL_METHOD = "writeNumberNull";
            WRITE_NULL_DESC = "(J)V";
            mw.lload(mwc.var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(fieldWriter.features);
            mw.lor();
        } else if (fieldClass == Boolean.class) {
            if ((fieldWriter.features & WriteNullBooleanAsFalse.mask) != 0) {
                WRITE_NULL_METHOD = "writeBool";
                WRITE_NULL_DESC = "(Z)V";
                mw.iconst_0();
            } else {
                WRITE_NULL_METHOD = "writeBooleanNull";
            }
        } else if (fieldClass == String.class
                || fieldClass == Appendable.class
                || fieldClass == StringBuffer.class
                || fieldClass == StringBuilder.class) {
            WRITE_NULL_METHOD = "writeStringNull";
        } else {
            WRITE_NULL_METHOD = "writeObjectNull";
            WRITE_NULL_DESC = "(Ljava/lang/Class;)V";
            mwc.loadFieldClass(i, fieldClass);
        }
        mw.invokevirtual(TYPE_JSON_WRITER, WRITE_NULL_METHOD, WRITE_NULL_DESC);

        mw.visitLabel(notNull_);
    }

    private void gwFieldValueList(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        boolean disableReferenceDetect = mwc.disableReferenceDetect();

        Type fieldType = fieldWriter.fieldType;
        Class<?> fieldClass = fieldWriter.fieldClass;
        MethodWriter mw = mwc.mw;

        int LIST = mwc.var(fieldClass);
        int REF_PATH = -1;

        if (!disableReferenceDetect) {
            REF_PATH = mwc.var("REF_PATH");
        }

        Class itemClass = null;
        boolean listSimple = false;

        if ((fieldWriter.features & WriteNonStringValueAsString.mask) == 0 && fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 1) {
                Type arg0 = actualTypeArguments[0];
                itemClass = TypeUtils.getClass(arg0);
                listSimple = (arg0 == String.class || arg0 == Integer.class || arg0 == Long.class);
            }
        }

        int FIELD_VALUE = mwc.var(fieldClass);

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        Label null_ = new Label(), notNull_ = new Label();
        mw.ifnull(null_);

        Label ignoreEmptyEnd_ = null;
        if ((fieldWriter.features & IgnoreEmpty.mask) == 0) {
            ignoreEmptyEnd_ = new Label();
            mwc.genIsEnabled(IgnoreEmpty.mask, ignoreEmptyEnd_);
        }

        mw.aload(FIELD_VALUE);
        mw.invokeinterface("java/util/Collection", "isEmpty", "()Z");
        mw.ifne(notNull_);

        if (ignoreEmptyEnd_ != null) {
            mw.visitLabel(ignoreEmptyEnd_);
        }

        if (!disableReferenceDetect) {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, endDetect_);

            if (fieldClass.isAssignableFrom(mwc.objectClass)) {
                mw.aload(OBJECT);
                mw.aload(LIST);
                mw.if_acmpne(refSetPath_);

                mw.aload(JSON_WRITER);
                mw.visitLdcInsn("..");
                mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V");

                mw.goto_(notNull_);

                mw.visitLabel(refSetPath_);
            }

            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(LIST);
            mw.lload(mwc.var(CONTEXT_FEATURES));
            mw.invokevirtual(TYPE_FIELD_WRITER, "isRefDetect", "(Ljava/lang/Object;J)Z");
            mw.ifeq(endDetect_);

            mw.aload(JSON_WRITER);
            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(LIST);
            mw.invokevirtual(TYPE_JSON_WRITER, "setPath0", METHOD_DESC_SET_PATH2);
            mw.dup();
            mw.astore(REF_PATH);
            mw.ifnull(endDetect_);

            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);
            mw.aload(REF_PATH);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE);

            mw.aload(JSON_WRITER);
            mw.aload(LIST);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
            mw.goto_(notNull_);

            mw.visitLabel(endDetect_);
        }

        {
            Label notWriteEmptyArrayEnd_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.NotWriteEmptyArray.mask, notWriteEmptyArrayEnd_);

            mw.aload(LIST);
            mw.invokeinterface("java/util/Collection", "isEmpty", "()Z");
            mw.ifeq(notWriteEmptyArrayEnd_);

            mw.goto_(notNull_);

            mw.visitLabel(notWriteEmptyArrayEnd_);
        }

        // listStr
        if (listSimple) {
            // void writeListStr(JSONWriter jw, List<String> list)
//            mw.aload(THIS);
//            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
//            mw.aload(JSON_WRITER);
//            mw.iconst_1();
//            mw.aload(FIELD_VALUE);
//            mw.invokevirtual(TYPE_FIELD_WRITER, "writeListStr", METHOD_DESC_WRITE_LIST);
//
            gwFieldName(mwc, fieldWriter, i);
            gwListSimpleType(mwc, i, mw, fieldClass, itemClass, FIELD_VALUE);
        } else {
            gwFieldName(mwc, fieldWriter, i);

            // void writeList(JSONWriter jw, ObjectWriterContext ctx, List list) {
            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(
                    TYPE_FIELD_WRITER,
                    mwc.type.writeListValueMethodName(),
                    mwc.type.writeListValueMethodDesc());
        }

        if (!disableReferenceDetect) {
            mw.aload(JSON_WRITER);
            mw.aload(LIST);
            mw.invokevirtual(TYPE_JSON_WRITER, "popPath0", "(Ljava/lang/Object;)V");
        }

        mw.goto_(notNull_);

        mw.visitLabel(null_);
        mwc.genIsEnabled(fieldWriter.features, WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask, notNull_);

        // writeFieldName(w);
        gwFieldName(mwc, fieldWriter, i);

        // jw.writeNull
        mw.aload(JSON_WRITER);
        mw.lload(mwc.var2(CONTEXT_FEATURES));
        mw.visitLdcInsn(fieldWriter.features);
        mw.lor();
        mw.invokevirtual(TYPE_JSON_WRITER, "writeArrayNull", "(J)V");

        mw.visitLabel(notNull_);
    }

    private void gwFieldValueJSONB(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        Class<?> fieldClass = fieldWriter.fieldClass;

        boolean writeAsString = (fieldWriter.features & WriteNonStringValueAsString.mask) != 0;

        if (fieldClass == boolean.class) {
            gwFieldValueBooleanV(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == boolean[].class
                || fieldClass == byte[].class
                || fieldClass == char[].class
                || fieldClass == short[].class
                || fieldClass == float[].class
                || fieldClass == double[].class
        ) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == char.class
                || fieldClass == byte.class
                || fieldClass == short.class
                || fieldClass == int.class
                || fieldClass == float.class
        ) {
            gwFieldValueInt32V(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == int[].class) {
            gwFieldValueIntVA(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == long.class
                || fieldClass == double.class) {
            gwFieldValueInt64V(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == long[].class
                && mwc.provider.getObjectWriter(Long.class) == ObjectWriterImplInt64.INSTANCE
        ) {
            gwFieldValueInt64VA(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == Integer.class) {
            gwInt32(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Long.class) {
            gwInt64(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == String.class) {
            gwFieldValueString(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass.isEnum()) {
            // gwFieldValueEnumJSONB(classNameType, mw, objectType, OBJECT, i, member, fieldClass, TYPE_OBJECT, true);
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Date.class) {
            gwFieldValueDate(mwc, fieldWriter, OBJECT, i);
        } else {
            gwFieldValueObject(mwc, fieldWriter, OBJECT, i, true);
        }
    }

    private void gwInt32(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
//        boolean jsonb = mwc.jsonb;
        String classNameType = mwc.classNameType;
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        mw.ifnonnull(notNull_);

        boolean writeAsString = (fieldWriter.features & WriteNonStringValueAsString.mask) != 0;

        if ((fieldWriter.features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask)) == 0) {
            mwc.genIsEnabled(
                    WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask,
                    writeNullValue_,
                    endIfNull_
            );

            mw.visitLabel(writeNullValue_);

            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeNumberNull", "()V");
        } else {
            gwFieldName(mwc, fieldWriter, i);
            mw.aload(JSON_WRITER);
            long features = fieldWriter.features;
            if ((features & (WriteNullNumberAsZero.mask | NullAsDefaultValue.mask)) != 0) {
                mw.visitLdcInsn(0);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeInt32", "(I)V");
            } else {  // (features & WriteNulls.mask) != 0
                mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
            }
        }

        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        if (writeAsString) {
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Integer", "intValue", "()I");

            mw.invokevirtual(TYPE_FIELD_WRITER, "writeInt32", METHOD_DESC_WRITE_I);
        } else {
            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Integer", "intValue", "()I");

            mw.invokevirtual(TYPE_JSON_WRITER, "writeInt32", "(I)V");
        }
        mw.visitLabel(endIfNull_);
    }

    private void gwInt64(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
//        boolean jsonb = mwc.jsonb;
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;
        String classNameType = mwc.classNameType;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);
        mw.ifnonnull(notNull_);

        if ((fieldWriter.features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask)) == 0) {
            mwc.genIsEnabled(
                    WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask,
                    writeNullValue_,
                    endIfNull_
            );

            mw.visitLabel(writeNullValue_);

            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeInt64Null", "()V");
        } else {
            gwFieldName(mwc, fieldWriter, i);
            mw.aload(JSON_WRITER);
            long features = fieldWriter.features;
            if ((features & (WriteNullNumberAsZero.mask | NullAsDefaultValue.mask)) != 0) {
                mw.lconst_0();
                mw.invokevirtual(TYPE_JSON_WRITER, "writeInt64", "(J)V");
            } else {  // (features & WriteNulls.mask) != 0
                mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
            }
        }

        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        if ((fieldWriter.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask | BrowserCompatible.mask)) == 0) {
            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Long", "longValue", "()J");

            mw.invokevirtual(TYPE_JSON_WRITER, "writeInt64", "(J)V");
        } else {
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Long", "longValue", "()J");

            mw.invokevirtual(TYPE_FIELD_WRITER, "writeInt64", METHOD_DESC_WRITE_J);
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwDouble(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;
        String classNameType = mwc.classNameType;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);
        mw.ifnonnull(notNull_);

        if ((fieldWriter.features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask)) == 0) {
            mwc.genIsEnabled(
                    WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask,
                    writeNullValue_,
                    endIfNull_
            );

            mw.visitLabel(writeNullValue_);

            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeDecimalNull", "()V");
        } else {
            gwFieldName(mwc, fieldWriter, i);
            mw.aload(JSON_WRITER);
            long features = fieldWriter.features;
            if ((features & WriteNullNumberAsZero.mask) != 0) {
                mw.visitLdcInsn(0);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeInt32", "(I)V");
            } else if ((features & NullAsDefaultValue.mask) != 0) {
                mw.visitLdcInsn(0);
                mw.i2d();
                mw.invokevirtual(TYPE_JSON_WRITER, "writeDouble", "(D)V");
            } else {  // (features & WriteNulls.mask) != 0
                mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
            }
        }

        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        if (mwc.type == JSONWriterType.JSONB && (fieldWriter.features & WriteNonStringValueAsString.mask) == 0) {
            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Double", "doubleValue", "()D");

            mw.invokevirtual(TYPE_JSON_WRITER, "writeDouble", "(D)V");
        } else {
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Double", "doubleValue", "()D");

            mw.invokevirtual(TYPE_FIELD_WRITER, "writeDouble", METHOD_DESC_WRITE_D);
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwFloat(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;
        String classNameType = mwc.classNameType;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        mw.ifnonnull(notNull_);

        if ((fieldWriter.features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask)) == 0) {
            mwc.genIsEnabled(
                    WriteNulls.mask | NullAsDefaultValue.mask | WriteNullNumberAsZero.mask,
                    writeNullValue_,
                    endIfNull_
            );

            mw.visitLabel(writeNullValue_);

            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeDecimalNull", "()V");
        } else {
            gwFieldName(mwc, fieldWriter, i);
            mw.aload(JSON_WRITER);
            long features = fieldWriter.features;
            if ((features & WriteNullNumberAsZero.mask) != 0) {
                mw.visitLdcInsn(0);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeInt32", "(I)V");
            } else if ((features & NullAsDefaultValue.mask) != 0) {
                mw.visitLdcInsn(0);
                mw.i2f();
                mw.invokevirtual(TYPE_JSON_WRITER, "writeFloat", "(F)V");
            } else {  // (features & WriteNulls.mask) != 0
                mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");
            }
        }

        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        if (mwc.type == JSONWriterType.JSONB) {
            gwFieldName(mwc, fieldWriter, i);

            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Float", "floatValue", "()F");

            mw.invokevirtual(TYPE_JSON_WRITER, "writeFloat", "(D)V");
        } else {
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.aload(JSON_WRITER);

            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/Float", "floatValue", "()F");

            mw.invokevirtual(TYPE_FIELD_WRITER, "writeFloat", METHOD_DESC_WRITE_F);
        }

        mw.visitLabel(endIfNull_);
    }

    private static void gwListSimpleType(
            MethodWriterContext mwc,
            int i,
            MethodWriter mw,
            Class<?> fieldClass,
            Class itemClass,
            int FIELD_VALUE
    ) {
        if (mwc.type == JSONWriterType.JSONB) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mwc.loadFieldClass(i, fieldClass);
            mw.invokevirtual(
                    TYPE_JSON_WRITER,
                    "checkAndWriteTypeName",
                    "(Ljava/lang/Object;Ljava/lang/Class;)V");
        }

        if (itemClass == Integer.class) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeListInt32", "(Ljava/util/List;)V");
            return;
        }

        if (itemClass == Long.class) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeListInt64", "(Ljava/util/List;)V");
            return;
        }

        if (itemClass == String.class) {
            mw.aload(JSON_WRITER);
            mw.aload(FIELD_VALUE);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeString", "(Ljava/util/List;)V");
            return;
        }

        throw new JSONException("TOOD " + itemClass.getName());
    }

    static void gwString(MethodWriterContext mwc, boolean symbol, boolean checkNull, int STR) {
        MethodWriter mw = mwc.mw;

        Label notNull_ = new Label(), endNull_ = new Label();
        if (checkNull) {
            mw.aload(STR);
            mw.ifnonnull(notNull_);
            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "writeStringNull", "()V");
            mw.goto_(endNull_);

            mw.visitLabel(notNull_);
        }

        if (JVM_VERSION == 8
                && !OPENJ9
                && !FIELD_STRING_VALUE_ERROR
                && !symbol
        ) {
            mw.aload(JSON_WRITER);
            mw.getstatic(ObjectWriterCreatorASMUtils.TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
            mw.aload(STR);
            mw.visitLdcInsn(FIELD_STRING_VALUE_OFFSET);
            mw.invokevirtual("sun/misc/Unsafe", "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;");
            mw.checkcast("[C");
            mw.invokevirtual(
                    TYPE_JSON_WRITER,
                    "writeString",
                    "([C)V");
        } else if (JVM_VERSION > 8
                && !OPENJ9
                && FIELD_STRING_CODER_OFFSET != -1
                && FIELD_STRING_VALUE_OFFSET != -1
                && !symbol
        ) {
            Label utf16_ = new Label(), end_ = new Label();
            mw.aload(JSON_WRITER);
            mw.getstatic(ObjectWriterCreatorASMUtils.TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
            mw.aload(STR);
            mw.visitLdcInsn(FIELD_STRING_VALUE_OFFSET);
            mw.invokevirtual("sun/misc/Unsafe", "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;");
            mw.checkcast("[B");

            mw.getstatic(ObjectWriterCreatorASMUtils.TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
            mw.aload(STR);
            mw.visitLdcInsn(FIELD_STRING_CODER_OFFSET);
            mw.invokevirtual("sun/misc/Unsafe", "getByte", "(Ljava/lang/Object;J)B");
            mw.ifne(utf16_);

            mw.invokevirtual(
                    TYPE_JSON_WRITER,
                    "writeStringLatin1",
                    "([B)V");
            mw.goto_(end_);
            mw.visitLabel(utf16_);
            mw.invokevirtual(
                    TYPE_JSON_WRITER,
                    "writeStringUTF16",
                    "([B)V");
            mw.visitLabel(end_);
        } else {
            mw.aload(JSON_WRITER);
            mw.aload(STR);
            mw.invokevirtual(
                    TYPE_JSON_WRITER,
                    symbol ? "writeSymbol" : "writeString",
                    "(Ljava/lang/String;)V");
        }

        if (checkNull) {
            mw.visitLabel(endNull_);
        }
    }

    private void gwFieldValueDate(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;

        Label null_ = new Label(), writeNull_ = new Label(), endIfNull_ = new Label();

        int FIELD_VALUE = mwc.var(fieldClass);

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);
        mw.ifnull(null_);

        // void writeEnum(JSONWriter jw, Enum e)
        mw.aload(THIS);
        mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.aload(JSON_WRITER);
        mw.aload(FIELD_VALUE);
        mw.invokevirtual("java/util/Date", "getTime", "()J");
        mw.invokevirtual(TYPE_FIELD_WRITER, "writeDate", METHOD_DESC_WRITE_J);
        mw.goto_(endIfNull_);

        mw.visitLabel(null_);

        // if (!jw.isWriteNulls())
        if ((fieldWriter.features & WriteNulls.mask) == 0) {
            mw.iload(mwc.var(WRITE_NULLS));
            mw.ifne(writeNull_);
            mw.goto_(endIfNull_);
        }

        mw.visitLabel(writeNull_);
        gwFieldName(mwc, fieldWriter, i);

        // jw.writeNull
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeNull", "()V");

        mw.visitLabel(endIfNull_);
    }

    private void gwFieldValueArray(MethodWriterContext mwc, FieldWriter fieldWriter, int OBJECT, int i) {
        MethodWriter mw = mwc.mw;
        Class fieldClass = fieldWriter.fieldClass;

        String methodName, methodDesc;
        if (fieldClass == char[].class) {
            methodName = "writeString";
            methodDesc = METHOD_DESC_WRITE_CArray;
        } else if (fieldClass == boolean[].class) {
            methodName = "writeBool";
            methodDesc = METHOD_DESC_WRITE_ZARRAY;
        } else if (fieldClass == byte[].class) {
            methodName = "writeBinary";
            methodDesc = METHOD_DESC_WRITE_BArray;
        } else if (fieldClass == short[].class) {
            methodName = "writeInt16";
            methodDesc = METHOD_DESC_WRITE_SArray;
        } else if (fieldClass == float[].class) {
            methodName = "writeFloat";
            methodDesc = METHOD_DESC_WRITE_FARRAY;
        } else if (fieldClass == double[].class) {
            methodName = "writeDouble";
            methodDesc = METHOD_DESC_WRITE_DARRAY;
        } else if (fieldClass.isEnum()) {
            methodName = "writeEnumJSONB";
            methodDesc = METHOD_DESC_WRITE_ENUM;
        } else {
            throw new UnsupportedOperationException();
        }

        mw.aload(THIS);
        mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.aload(JSON_WRITER);
        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.invokevirtual(TYPE_FIELD_WRITER, methodName, methodDesc);
    }

    private void gwFieldName(MethodWriterContext mwc, FieldWriter fieldWriter, int i) {
        MethodWriter mw = mwc.mw;
        String classNameType = mwc.classNameType;

        Label labelElse = new Label(), labelEnd = new Label();
        boolean writeDirect = false;
        if (mwc.type != JSONWriterType.JSONB) {
            byte[] fieldNameUTF8 = fieldWriter.fieldName.getBytes(StandardCharsets.UTF_8);

            boolean asciiName = true;
            for (int j = 0; j < fieldNameUTF8.length; j++) {
                if (fieldNameUTF8[j] < 0) {
                    asciiName = false;
                    break;
                }
            }

            int length = fieldNameUTF8.length;
            if (length >= 2 && length <= 16 && asciiName) {
                Number name1 = 0, name1SQ = 0;
                String methodName;
                String methodDesc = "(J)V";
                byte[] bytes = new byte[8];
                switch (length) {
                    case 2:
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 2);
                        bytes[3] = '"';
                        bytes[4] = ':';
                        methodName = "writeName2Raw";
                        break;
                    case 3:
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 3);
                        bytes[4] = '"';
                        bytes[5] = ':';
                        methodName = "writeName3Raw";
                        break;
                    case 4:
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 4);
                        bytes[5] = '"';
                        bytes[6] = ':';
                        methodName = "writeName4Raw";
                        break;
                    case 5:
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 5);
                        bytes[6] = '"';
                        bytes[7] = ':';
                        methodName = "writeName5Raw";
                        break;
                    case 6:
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 6);
                        bytes[7] = '"';
                        methodName = "writeName6Raw";
                        break;
                    case 7:
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodName = "writeName7Raw";
                        break;
                    case 8: {
                        bytes = fieldNameUTF8;
                        methodName = "writeName8Raw";
                        break;
                    }
                    case 9: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodDesc = "(JI)V";
                        byte[] name1Bytes = new byte[4];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = '"';
                        name1Bytes[3] = ':';
                        name1 = BYTES.getIntUnaligned(name1Bytes, 0);

                        name1Bytes[2] = '\'';
                        name1SQ = BYTES.getIntUnaligned(name1Bytes, 0);

                        methodName = "writeName9Raw";
                        break;
                    }
                    case 10: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodDesc = "(JJ)V";
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = '"';
                        name1Bytes[4] = ':';
                        name1 = BYTES.getLongUnaligned(name1Bytes, 0);

                        name1Bytes[3] = '\'';
                        name1SQ = BYTES.getLongUnaligned(name1Bytes, 0);

                        methodName = "writeName10Raw";
                        break;
                    }
                    case 11: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodDesc = "(JJ)V";
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = '"';
                        name1Bytes[5] = ':';

                        name1 = BYTES.getLongUnaligned(name1Bytes, 0);

                        name1Bytes[4] = '\'';
                        name1SQ = BYTES.getLongUnaligned(name1Bytes, 0);

                        methodName = "writeName11Raw";
                        break;
                    }
                    case 12: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodDesc = "(JJ)V";
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = fieldNameUTF8[11];
                        name1Bytes[5] = '"';
                        name1Bytes[6] = ':';

                        name1 = BYTES.getLongUnaligned(name1Bytes, 0);

                        name1Bytes[5] = '\'';
                        name1SQ = BYTES.getLongUnaligned(name1Bytes, 0);

                        methodName = "writeName12Raw";
                        break;
                    }
                    case 13: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodDesc = "(JJ)V";
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = fieldNameUTF8[11];
                        name1Bytes[5] = fieldNameUTF8[12];
                        name1Bytes[6] = '"';
                        name1Bytes[7] = ':';

                        name1 = BYTES.getLongUnaligned(name1Bytes, 0);

                        name1Bytes[6] = '\'';
                        name1SQ = BYTES.getLongUnaligned(name1Bytes, 0);

                        methodName = "writeName13Raw";
                        break;
                    }
                    case 14: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodDesc = "(JJ)V";
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = fieldNameUTF8[11];
                        name1Bytes[5] = fieldNameUTF8[12];
                        name1Bytes[6] = fieldNameUTF8[13];
                        name1Bytes[7] = '"';

                        name1 = BYTES.getLongUnaligned(name1Bytes, 0);

                        name1Bytes[7] = '\'';
                        name1SQ = BYTES.getLongUnaligned(name1Bytes, 0);

                        methodName = "writeName14Raw";
                        break;
                    }
                    case 15: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        methodDesc = "(JJ)V";
                        name1 = BYTES.getLongUnaligned(fieldNameUTF8, 7);
                        name1SQ = name1;
                        methodName = "writeName15Raw";
                        break;
                    }
                    case 16: {
                        System.arraycopy(fieldNameUTF8, 0, bytes, 0, 8);
                        methodDesc = "(JJ)V";
                        name1 = BYTES.getLongUnaligned(fieldNameUTF8, 8);
                        name1SQ = name1;
                        methodName = "writeName16Raw";
                        break;
                    }
                    default:
                        throw new IllegalStateException("length : " + length);
                }

                long nameIn64 = BYTES.getLongUnaligned(bytes, 0);
                for (int j = 0; j < bytes.length; j++) {
                    if (bytes[j] == '"') {
                        bytes[j] = '\'';
                    }
                }
                long nameIn64SQ = BYTES.getLongUnaligned(bytes, 0);

                mw.aload(JSON_WRITER);

                mwc.ldcIFEQ(NAME_DIRECT, nameIn64, nameIn64SQ);

                if ("(JI)V".equals(methodDesc) || "(JJ)V".equals(methodDesc)) {
                    mwc.ldcIFEQ(NAME_DIRECT, name1, name1SQ);
                }

                mw.invokevirtual(
                        TYPE_JSON_WRITER,
                        methodName,
                        methodDesc);

                return;
            }
        } else {
            byte[] fieldNameUTF8 = JSONB.toBytes(fieldWriter.fieldName);
            int length = fieldNameUTF8.length;
            String methodName = null;
            String methodDesc = "(J)V";
            byte[] bytes = Arrays.copyOf(fieldNameUTF8, 16);
            switch (length) {
                case 2:
                    methodName = "writeName2Raw";
                    break;
                case 3:
                    methodName = "writeName3Raw";
                    break;
                case 4:
                    methodName = "writeName4Raw";
                    break;
                case 5:
                    methodName = "writeName5Raw";
                    break;
                case 6:
                    methodName = "writeName6Raw";
                    break;
                case 7:
                    methodName = "writeName7Raw";
                    break;
                case 8:
                    methodName = "writeName8Raw";
                    break;
                case 9:
                    methodName = "writeName9Raw";
                    methodDesc = "(JI)V";
                    break;
                case 10:
                    methodName = "writeName10Raw";
                    methodDesc = "(JJ)V";
                    break;
                case 11:
                    methodName = "writeName11Raw";
                    methodDesc = "(JJ)V";
                    break;
                case 12:
                    methodName = "writeName12Raw";
                    methodDesc = "(JJ)V";
                    break;
                case 13:
                    methodName = "writeName13Raw";
                    methodDesc = "(JJ)V";
                    break;
                case 14:
                    methodName = "writeName14Raw";
                    methodDesc = "(JJ)V";
                    break;
                case 15:
                    methodName = "writeName15Raw";
                    methodDesc = "(JJ)V";
                    break;
                case 16:
                    methodName = "writeName16Raw";
                    methodDesc = "(JJ)V";
                    break;
                default:
                    break;
            }

            if (methodName != null) {
                mw.iload(mwc.var(NAME_DIRECT));
                mw.ifeq(labelElse);

                long nameIn64 = BYTES.getLongUnaligned(bytes, 0);
                mw.aload(JSON_WRITER);
                mw.visitLdcInsn(nameIn64);
                if ("(JI)V".equals(methodDesc)) {
                    int name1 = BYTES.getIntUnaligned(bytes, 8);
                    mw.visitLdcInsn(name1);
                } else if ("(JJ)V".equals(methodDesc)) {
                    long name1 = BYTES.getLongUnaligned(bytes, 8);
                    mw.visitLdcInsn(name1);
                }
                mw.invokevirtual(
                        TYPE_JSON_WRITER,
                        methodName,
                        methodDesc);
                mw.goto_(labelEnd);
                writeDirect = true;
            }
        }

        if (writeDirect) {
            mw.visitLabel(labelElse);
        }

        mw.aload(THIS);
        mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.aload(JSON_WRITER);
        mw.invokevirtual(
                TYPE_FIELD_WRITER,
                mwc.type.writeFieldName(),
                mwc.type.writeFieldNameDesc());

        if (writeDirect) {
            mw.visitLabel(labelEnd);
        }
    }

    private void gwFieldValueInt64VA(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean jsonb
    ) {
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        mw.ifnonnull(notNull_);

        mw.iload(mwc.var(WRITE_NULLS));
        mw.ifne(writeNullValue_);

        mw.goto_(endIfNull_);

        mw.visitLabel(writeNullValue_);

        gwFieldName(mwc, fieldWriter, i);

        // TODO if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsEmpty.mask)) == 0) {
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeArrayNull", "()V");

        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        gwFieldName(mwc, fieldWriter, i);

        boolean writeAsString = (fieldWriter.features & WriteNonStringValueAsString.mask) != 0;

        mw.aload(JSON_WRITER);
        mw.aload(FIELD_VALUE);
        mw.invokevirtual(TYPE_JSON_WRITER, writeAsString ? "writeString" : "writeInt64", "([J)V");

        mw.visitLabel(endIfNull_);
    }

    private void gwFieldValueInt64V(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean jsonb
    ) {
        MethodWriter mw = mwc.mw;
        String format = fieldWriter.format;
        String classNameType = mwc.classNameType;
        Class fieldClass = fieldWriter.fieldClass;

        int FIELD_VALUE = mwc.var(fieldClass);
        int WRITE_DEFAULT_VALUE = mwc.var(NOT_WRITE_DEFAULT_VALUE);
        Label notDefaultValue_ = new Label(), endWriteValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup2();
        mw.storeLocal(fieldClass, FIELD_VALUE);
        mw.cmpWithZero(fieldClass);
        mw.ifne(notDefaultValue_);

        if (fieldWriter.defaultValue == null) {
            mw.iload(WRITE_DEFAULT_VALUE);
            mw.ifeq(notDefaultValue_);
            mw.goto_(endWriteValue_);
        }
        mw.visitLabel(notDefaultValue_);

        if (fieldClass == long.class) {
            boolean writeDate = "iso8601".equals(format) || fieldWriter instanceof FieldWriterDate;
            if (writeDate || (fieldWriter.features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask | BrowserCompatible.mask)) != 0) {
                mw.aload(THIS);
                mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);

                mw.aload(JSON_WRITER);
                mw.lload(FIELD_VALUE);

                mw.invokevirtual(TYPE_FIELD_WRITER, writeDate ? "writeDate" : "writeInt64", METHOD_DESC_WRITE_J);
            } else {
                gwFieldName(mwc, fieldWriter, i);
                mw.aload(JSON_WRITER);
                mw.lload(FIELD_VALUE);
                mw.invokevirtual(TYPE_JSON_WRITER, "writeInt64", "(J)V");
            }
        } else if (fieldClass == double.class) {
            gwFieldName(mwc, fieldWriter, i);
            gwValue(mwc, fieldWriter, OBJECT, i, FIELD_VALUE);
        } else {
            throw new UnsupportedOperationException();
        }

        mw.visitLabel(endWriteValue_);
    }

    void gwFieldValueIntVA(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean jsonb
    ) {
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.astore(FIELD_VALUE);

        mw.ifnonnull(notNull_);

        mw.iload(mwc.var(WRITE_NULLS));
        mw.ifne(writeNullValue_);

        mw.goto_(endIfNull_);

        mw.visitLabel(writeNullValue_);

        gwFieldName(mwc, fieldWriter, i);

        // TODO if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsEmpty.mask)) == 0) {
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeArrayNull", "()V");

        mw.goto_(endIfNull_);

        mw.visitLabel(notNull_);

        gwFieldName(mwc, fieldWriter, i);

        boolean writeAsString = (fieldWriter.features & WriteNonStringValueAsString.mask) != 0;

        mw.aload(JSON_WRITER);
        mw.aload(FIELD_VALUE);
        mw.invokevirtual(TYPE_JSON_WRITER, writeAsString ? "writeString" : "writeInt32", "([I)V");

        mw.visitLabel(endIfNull_);
    }

    private void gwFieldValueInt32V(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean jsonb
    ) {
        MethodWriter mw = mwc.mw;
        String format = fieldWriter.format;
        String classNameType = mwc.classNameType;
        Class<?> fieldClass = fieldWriter.fieldClass;

        int FIELD_VALUE = mwc.var(fieldClass);
        int WRITE_DEFAULT_VALUE = mwc.var(NOT_WRITE_DEFAULT_VALUE);
        Label notDefaultValue_ = new Label(), endWriteValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup(fieldClass);
        mw.storeLocal(fieldClass, FIELD_VALUE);
        mw.cmpWithZero(fieldClass);
        mw.ifne(notDefaultValue_);

        if (fieldWriter.defaultValue == null) {
            mw.iload(WRITE_DEFAULT_VALUE);
            mw.ifeq(notDefaultValue_);
            mw.goto_(endWriteValue_);
        }
        mw.visitLabel(notDefaultValue_);

        gwFieldName(mwc, fieldWriter, i);

        gwValue(mwc, fieldWriter, OBJECT, i, FIELD_VALUE);

        mw.visitLabel(endWriteValue_);
    }

    private void gwFieldValueBooleanV(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean jsonb
    ) {
        MethodWriter mw = mwc.mw;
        String classNameType = mwc.classNameType;

        int FIELD_VALUE = mwc.var(boolean.class);
        int WRITE_DEFAULT_VALUE = mwc.var(NOT_WRITE_DEFAULT_VALUE);
        Label notDefaultValue_ = new Label(), endWriteValue_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);
        mw.dup();
        mw.istore(FIELD_VALUE);
        mw.ifne(notDefaultValue_);

        if (fieldWriter.defaultValue == null) {
            mw.iload(WRITE_DEFAULT_VALUE);
            mw.ifeq(notDefaultValue_);
            mw.goto_(endWriteValue_);
        }
        mw.visitLabel(notDefaultValue_);

        mw.aload(THIS);
        mw.getfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.aload(JSON_WRITER);
        mw.iload(FIELD_VALUE);
        mw.invokevirtual(TYPE_FIELD_WRITER, mwc.type.writeBoolMethodName(), mwc.type.writeBoolMethodDesc());

        mw.visitLabel(endWriteValue_);
    }

    private void gwFieldValueString(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        long features = fieldWriter.features | mwc.objectFeatures;
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.fieldClass;
        String format = fieldWriter.format;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label null_ = new Label(), endIfNull_ = new Label();

        genGetObject(mwc, fieldWriter, i, OBJECT);

        mw.dup();
        mw.astore(FIELD_VALUE);

        mw.ifnull(null_);

        if ("trim".equals(format)) {
            mw.aload(FIELD_VALUE);
            mw.invokevirtual("java/lang/String", "trim", "()Ljava/lang/String;");
            mw.astore(FIELD_VALUE);
        }

        Label ignoreEmptyEnd_ = null;
        if ((features & IgnoreEmpty.mask) == 0) {
            ignoreEmptyEnd_ = new Label();
            mwc.genIsEnabled(IgnoreEmpty.mask, ignoreEmptyEnd_);
        }

        mw.aload(FIELD_VALUE);
        mw.invokevirtual("java/lang/String", "isEmpty", "()Z");
        mw.ifne(endIfNull_);

        if (ignoreEmptyEnd_ != null) {
            mw.visitLabel(ignoreEmptyEnd_);
        }

        // void writeFieldName(JSONWriter w)
        gwFieldName(mwc, fieldWriter, i);

        final boolean symbol = mwc.type == JSONWriterType.JSONB && "symbol".equals(format);
        gwString(mwc, symbol, false, FIELD_VALUE);

        mw.goto_(endIfNull_);

        mw.visitLabel(null_);

        Label writeNullValue_ = new Label(), writeNull_ = new Label();

        final long defaultValueMask = NullAsDefaultValue.mask
                | WriteNullNumberAsZero.mask
                | WriteNullBooleanAsFalse.mask
                | WriteNullListAsEmpty.mask
                | WriteNullStringAsEmpty.mask;

        // if (!jw.isWriteNulls())
        if ((features & (JSONWriter.Feature.WriteNulls.mask | defaultValueMask)) == 0) {
            mwc.genIsEnabled(
                    WriteNulls.mask | NullAsDefaultValue.mask | WriteNullStringAsEmpty.mask,
                    writeNull_,
                    endIfNull_
            );
        }

        mw.visitLabel(writeNull_);

        if (fieldWriter.defaultValue == null) {
            mwc.genIsDisabled(NotWriteDefaultValue.mask, endIfNull_);
        }

        // writeFieldName(w);
        gwFieldName(mwc, fieldWriter, i);

        if ((features & defaultValueMask) == 0) {
            long mask = NullAsDefaultValue.mask;
            if (fieldClass == String.class) {
                mask |= WriteNullStringAsEmpty.mask;
            } else if (fieldClass == Boolean.class) {
                mask |= WriteNullBooleanAsFalse.mask;
            } else if (Number.class.isAssignableFrom(fieldClass)) {
                mask |= JSONWriter.Feature.WriteNullNumberAsZero.mask;
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                mask |= WriteNullListAsEmpty.mask;
            }

            mw.aload(JSON_WRITER);
            mw.visitLdcInsn(mask);
            mw.invokevirtual(TYPE_JSON_WRITER, "isEnabled", "(J)Z");
            mw.ifeq(writeNullValue_);
        }

        mw.aload(JSON_WRITER);
        mw.visitLdcInsn("");
        mw.invokevirtual(TYPE_JSON_WRITER, "writeString", "(Ljava/lang/String;)V");
        mw.goto_(endIfNull_);

        // jw.writeNull
        mw.visitLabel(writeNullValue_);
        mw.aload(JSON_WRITER);
        mw.invokevirtual(TYPE_JSON_WRITER, "writeStringNull", "()V");

        mw.visitLabel(endIfNull_);
    }

    private void genMethodInit(List<FieldWriter> fieldWriters,
                               ClassWriter cw,
                               String classNameType,
                               String objectWriterSupper) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V",
                64
        );
        mw.aload(THIS);
        mw.aload(1);
        mw.aload(2);
        mw.aload(3);
        mw.lload(4);
        mw.aload(6);
        mw.invokespecial(objectWriterSupper, "<init>", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V");

        if (objectWriterSupper == TYPE_OBJECT_WRITER_ADAPTER) {
            for (int i = 0; i < fieldWriters.size(); i++) {
                mw.aload(THIS);
                mw.dup();

                mw.getfield(TYPE_OBJECT_WRITER_ADAPTER, "fieldWriterArray", DESC_FIELD_WRITER_ARRAY);
                mw.iconst_n(i);
                mw.aaload(); // fieldWriterArray
                mw.checkcast(TYPE_FIELD_WRITER);
                mw.putfield(classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            }
        }

        mw.return_();
        mw.visitMaxs(7, 7);
    }

    private void genFields(List<FieldWriter> fieldWriters, ClassWriter cw, String objectWriterSupper) {
        if (objectWriterSupper != TYPE_OBJECT_WRITER_ADAPTER) {
            return;
        }

        for (int i = 0; i < fieldWriters.size(); i++) {
            cw.visitField(
                    Opcodes.ACC_PUBLIC,
                    fieldWriter(i),
                    DESC_FIELD_WRITER
            );
        }
    }

    public <T> FieldWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Field field,
            ObjectWriter initObjectWriter,
            Class<?> contentAs,
            JSONWriterType type
    ) {
        Class<?> declaringClass = field.getDeclaringClass();
        if (Throwable.class.isAssignableFrom(declaringClass)
                || declaringClass.getName().startsWith("java.lang")
        ) {
            return super.createFieldWriter(provider, fieldName, ordinal, features, format, locale, label, field, initObjectWriter, contentAs);
        }

        Class<?> fieldClass = field.getType();
        Type fieldType = field.getGenericType();

        if (initObjectWriter != null) {
            if (fieldClass == byte.class) {
                fieldType = fieldClass = Byte.class;
            } else if (fieldClass == short.class) {
                fieldType = fieldClass = Short.class;
            } else if (fieldClass == float.class) {
                fieldType = fieldClass = Float.class;
            } else if (fieldClass == double.class) {
                fieldType = fieldClass = Double.class;
            } else if (fieldClass == boolean.class) {
                fieldType = fieldClass = Boolean.class;
            }

            FieldWriterObject objImp = new FieldWriterObject(
                    fieldName,
                    ordinal,
                    features,
                    format,
                    locale,
                    label,
                    fieldType,
                    fieldClass,
                    field,
                    null
            );
            objImp.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objImp.initObjectWriter = initObjectWriter;
            }
            return objImp;
        }

        if (fieldClass == boolean.class) {
            return new FieldWriterBoolVal(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
        }

        if (fieldClass == byte.class) {
            return new FieldWriterInt8Val(fieldName, ordinal, features, format, label, field, null, null);
        }

        if (fieldClass == short.class) {
            return new FieldWriterInt16Val(fieldName, ordinal, features, format, label, field, null, null);
        }

        if (fieldClass == int.class) {
            return new FieldWriterInt32Val(fieldName, ordinal, features, format, label, field, null, null);
        }

        if (fieldClass == long.class) {
            if (format == null || format.isEmpty() || "string".equals(format)) {
                return new FieldWriterInt64Val(fieldName, ordinal, features, format, label, field, null, null);
            }
            return new FieldWriterMillis(fieldName, ordinal, features, format, label, field, null, null);
        }

        if (fieldClass == Long.class) {
            return new FieldWriterInt64(fieldName, ordinal, features, format, label, fieldClass, field, null, null);
        }

        if (fieldClass == float.class) {
            return new FieldWriterFloatValue(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
        }

        if (fieldClass == Float.class) {
            return new FieldWriterFloat(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
        }

        if (fieldClass == double.class) {
            return new FieldWriterDoubleVal(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
        }

        if (fieldClass == Double.class) {
            return new FieldWriterDouble(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
        }

        if (fieldClass == char.class) {
            return new FieldWriterCharVal(fieldName, ordinal, features, format, label, field, null, null);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldWriterBigInteger(fieldName, ordinal, features, format, locale, label, field, null, null);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimal<>(fieldName, ordinal, features, format, locale, label, field, null, null);
        }

        if (fieldClass == java.util.Date.class) {
            if (format != null) {
                format = format.trim();

                if (format.isEmpty()) {
                    format = null;
                }
            }

            return new FieldWriterDate(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
        }

        if (fieldClass == String.class) {
            return new FieldWriterString(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
        }

        if (fieldClass.isEnum()) {
            BeanInfo beanInfo = provider.createBeanInfo();
            provider.getBeanInfo(beanInfo, fieldClass);

            boolean writeEnumAsJavaBean = beanInfo.writeEnumAsJavaBean;
            if (!writeEnumAsJavaBean) {
                ObjectWriter objectWriter = provider.cache.get(fieldClass);
                if (objectWriter != null && !(objectWriter instanceof ObjectWriterImplEnum)) {
                    writeEnumAsJavaBean = true;
                }
            }

            Member enumValueField = BeanUtils.getEnumValueField(fieldClass, provider);

            if (enumValueField == null && !writeEnumAsJavaBean) {
                String[] enumAnnotationNames = BeanUtils.getEnumAnnotationNames(fieldClass);
                if (enumAnnotationNames == null) {
                    return new FieldWriterEnum(fieldName, ordinal, features, format, label, fieldType, (Class<? extends Enum>) fieldClass, field, null, null);
                }
            }
        }

        if (fieldClass == List.class || fieldClass == ArrayList.class) {
            Type itemType = null;
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            }
            return createFieldWriterList(provider, fieldName, ordinal, features, format, label, field, contentAs, itemType, fieldType, fieldClass, type);
        }

        if (Map.class.isAssignableFrom(fieldClass)) {
            return new FieldWriterMap(fieldName, ordinal, features, format, locale, label, field.getGenericType(), fieldClass, field, null, null, contentAs);
        }

        if (fieldClass.isArray()) {
            Class<?> itemClass = fieldClass.getComponentType();

            if (declaringClass == Throwable.class && "stackTrace".equals(fieldName)) {
                try {
                    Method method = Throwable.class.getMethod("getStackTrace");
                    return new FieldWriterObjectArray(fieldName, itemClass, ordinal, features, format, label, fieldType, fieldClass, field, method, null);
                } catch (NoSuchMethodException ignored) {
                }
            }
//
//            boolean base64 = fieldClass == byte[].class && "base64".equals(format);
//            if (!base64) {
//                return new FieldWriterObjectArray(fieldName, itemClass, ordinal, features, format, label, fieldType, fieldClass, field, null, null);
//            }
        }

        if (fieldClass == BigDecimal[].class) {
            return new FieldWriterObjectArray<>(fieldName, BigDecimal.class, ordinal, features, format, label, BigDecimal[].class, BigDecimal[].class, field, null, null);
        }

        if (fieldClass == Float[].class) {
            return new FieldWriterObjectArray<>(fieldName, Float.class, ordinal, features, format, label, Float[].class, Float[].class, field, null, null);
        }

        if (fieldClass == Double[].class) {
            return new FieldWriterObjectArray<>(fieldName, Float.class, ordinal, features, format, label, Double[].class, Double[].class, field, null, null);
        }

        if (isFunction(fieldClass)) {
            return null;
        }

        return new FieldWriterObject(fieldName, ordinal, features, format, locale, label, field.getGenericType(), fieldClass, field, null);
    }

    private FieldWriter createFieldWriterList(
            ObjectWriterProvider provider,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Class<?> contentAs,
            Type itemType,
            Type fieldType,
            Class<?> fieldClass,
            JSONWriterType type
    ) {
        FieldWriter fieldWriter = jitFieldWriterList(provider, fieldName, ordinal, features, format, label, field, contentAs, itemType, fieldType, fieldClass, type);
        if (fieldWriter == null) {
            fieldWriter = new FieldWriterList(
                    fieldName,
                    itemType,
                    ordinal,
                    features,
                    format,
                    label,
                    fieldType,
                    fieldClass,
                    field,
                    null,
                    contentAs
            );
        }
        return fieldWriter;
    }

    private FieldWriter jitFieldWriterList(
            ObjectWriterProvider provider,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Class<?> contentAs,
            Type itemType,
            Type fieldType,
            Class<?> fieldClass,
            JSONWriterType type
    ) {
        boolean direct = false;
        List<FieldWriter> fieldWriters = null;
        Class<?> itemClass = TypeUtils.getClass(itemType);
        if (itemClass != null && field != null && field.getDeclaringClass() != itemClass) {
            ObjectWriter fieldValueWriter = provider.getObjectWriterFromCache(itemType, itemClass, FieldBased.isEnabled(features));
            if (fieldValueWriter == null && itemClass != null) {
                fieldValueWriter = super.createObjectWriter(itemClass, features, provider);
            }
            fieldWriters = fieldValueWriter.getFieldWriters();
            List<FieldWriterGroup> groups = buildGroups(fieldValueWriter.getFeatures(), fieldWriters);
            if (groups.size() == 1 && groups.get(0).direct) {
                direct = true;
            }

            for (FieldWriter fieldWriter : fieldWriters) {
                if (fieldWriter.method == null && fieldWriter.field == null && fieldWriter.getFunction() == null) {
                    direct = false;
                    break;
                }
                Class cls = fieldWriter.fieldClass;
                if (cls != boolean.class && cls != Boolean.class
                        && cls != byte.class && cls != Byte.class
                        && cls != short.class && cls != Short.class
                        && cls != int.class && cls != Integer.class
                        && cls != long.class && cls != Long.class
                        && cls != float.class && cls != Float.class
                        && cls != double.class && cls != Double.class
                        && cls != LocalDate.class && cls != LocalTime.class && cls != LocalDateTime.class
                        && cls != OffsetDateTime.class && cls != OffsetTime.class
                        && cls != Instant.class
                        && cls != UUID.class
                ) {
                    direct = false;
                    break;
                }
            }
        }

        if (direct) {
            int capacity = 6;
            for (FieldWriter fieldWriter : fieldWriters) {
                capacity = fieldCapacity(fieldWriter.fieldClass);
            }

            ClassWriter cw = new ClassWriter(null);

            String className = "OWF_" + seed.incrementAndGet() + "_" + fieldWriters.size() + "_" + itemClass.getSimpleName();
            String classNameType;
            String classNameFull;

            Package pkg = ObjectWriterCreatorASM.class.getPackage();
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

            String supperType = type(FieldWriterList.class);
            cw.visit(Opcodes.V1_8,
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                    classNameType,
                    supperType,
                    INTERFACES
            );

            {
                String initDesc = "(Ljava/lang/String;Ljava/lang/reflect/Type;IJLjava/lang/String;Ljava/lang/String;Ljava/lang/reflect/Type;Ljava/lang/Class;Ljava/lang/reflect/Field;Ljava/lang/reflect/Method;Ljava/util/function/Function;Ljava/lang/Class;)V";
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        initDesc,
                        64
                );
                mw.aload(THIS);
                mw.aload(1);
                mw.aload(2);
                mw.iload(3);
                mw.lload(4);
                mw.aload(6);
                mw.aload(7);
                mw.aload(8);
                mw.aload(9);
                mw.aload(10);
                mw.aload(11);
                mw.aload(12);
                mw.aload(13);
                mw.invokespecial(supperType, "<init>", initDesc);

                mw.return_();
                mw.visitMaxs(12, 12);
            }

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "writeListValueJSONB",
                    METHOD_DESC_WRITE_LIST_VALUE_JSONB,
                    fieldWriters.size() < 6 ? 512 : 1024
            );
            MethodWriterContext mwc = new MethodWriterContext(provider, itemClass, features, classNameType, mw, 8, type);

            int LIST = 2;
            int OFFSET = mwc.var("offset");
            int BYTES = mwc.var("bytes");
            int FEATURES = mwc.var2(CONTEXT_FEATURES);
            int symbolTable = mwc.var("symbolTable");
            /*
             * int offset = jsonWriter.getOffset();
             */
            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "getOffset", "()I");
            mw.istore(OFFSET);

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "getFeatures", "()J");
            mw.lstore(FEATURES);

            Label L_SUPPER = new Label();
            if (!provider.isDisableReferenceDetect()) {
                /*
                 * if ((features & ReferenceDetection.mask) != 0) {
                 *  goto L_SUPPER
                 * }
                 */
                mw.lload(FEATURES);
                mw.visitLdcInsn(ReferenceDetection.mask);
                mw.land();
                mw.lconst_0();
                mw.lcmp();
                mw.ifne(L_SUPPER);
            }

            mw.aload(JSON_WRITER);
            mw.getfield(TYPE_JSON_WRITER, "symbolTable", DESC_SYMBOL);
            mw.astore(symbolTable);

            int SIZE = mwc.var("size");
            mw.aload(LIST);
            mw.invokeinterface("java/util/List", "size", "()I");
            mw.istore(SIZE);

            /*
             * byte[] bytes = (byte[]) ensureCapacity(offset + 5 + size * capacity);
             */
            mw.aload(JSON_WRITER);
            mw.iload(OFFSET);
            mw.iconst_5();
            mw.iadd();
            mw.visitLdcInsn(capacity);
            mw.iload(SIZE);
            mw.imul();
            mw.iadd();
            mw.invokevirtual(TYPE_JSON_WRITER, "ensureCapacity", "(I)Ljava/lang/Object;");
            mw.checkcast("[B");
            mw.astore(BYTES);

            /*
             * offset = JSONB.IO.startArray(bytes, offset, size);
             */
            mw.aload(BYTES);
            mw.iload(OFFSET);
            mw.iload(SIZE);
            mw.invokestatic(TYPE_JSONB_IO, "startArray", "([BII)I", true);
            mw.istore(OFFSET);

            Label L0 = new Label(), L1 = new Label(), L3 = new Label();
            int I = mwc.var("I");
            mw.iconst_0();
            mw.istore(I);

            mw.visitLabel(L0);
            mw.iload(I);
            mw.iload(SIZE);
            mw.if_icmpge(L3);

            mw.aload(BYTES);
            mw.iload(OFFSET);
            mw.visitLdcInsn(fieldWriters.size());
            mw.invokestatic(TYPE_JSONB_IO, "startArray", "([BII)I", true);
            mw.istore(OFFSET);

            int ITEM = mwc.var("ITEM");

            mw.aload(LIST);
            mw.iload(I);
            mw.invokeinterface("java/util/List", "get", "(I)Ljava/lang/Object;");
            mw.astore(ITEM);

            {
                /*
                 * if (item == null) {
                 *     bytes[off++] = BC_NULL;
                 * }
                 */
                // bytes[off] = BC_NULL
                mw.aload(ITEM);
                mw.ifnonnull(L1);
                mw.aload(BYTES);
                mw.iload(OFFSET);
                mw.bipush(BC_NULL);
                mw.bastore();
                mw.visitIincInsn(OFFSET, 1);
                mw.goto_(L0);

                mw.visitLabel(L1);
            }

            {
                mw.aload(ITEM);
                mw.invokevirtual(TYPE_OBJECT, "getClass", "()Ljava/lang/Class;");
                mw.aload(THIS);
                mw.getfield(TYPE_FIELD_WRITER, "fieldClass", "Ljava/lang/Class;");
                mw.if_acmpeq(L_SUPPER);
            }

            for (int i = 0; i < fieldWriters.size(); i++) {
                FieldWriter fieldWriter = fieldWriters.get(i);
                writeFieldValueDirectJSONB(features, classNameType, mwc, fieldWriter, i, mw, BYTES, OFFSET, ITEM, FEATURES, symbolTable, false);
            }

            mw.visitIincInsn(I, 1);
            mw.goto_(L0);

            mw.visitLabel(L3);

            mw.aload(JSON_WRITER);
            mw.iload(OFFSET);
            mw.invokevirtual(TYPE_JSON_WRITER, "setOffset", "(I)V");
            mw.return_();

            mw.visitLabel(L_SUPPER);
            mw.aload(THIS);
            mw.aload(JSON_WRITER);
            mw.aload(LIST);
            mw.invokespecial(type(FieldWriterList.class), "writeListValueJSONB", METHOD_DESC_WRITE_LIST_VALUE_JSONB);
            mw.return_();

            mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);

            byte[] code = cw.toByteArray();

            Class<?> deserClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);

            try {
                Constructor<?> constructor = deserClass.getConstructor(
                        String.class,
                        Type.class,
                        int.class,
                        long.class,
                        String.class,
                        String.class,
                        Type.class,
                        Class.class,
                        Field.class,
                        Method.class,
                        Function.class,
                        Class.class
                );
                return (FieldWriterList) constructor.newInstance(
                        fieldName,
                        itemType,
                        ordinal,
                        features,
                        format,
                        label,
                        fieldType,
                        fieldClass,
                        field,
                        null,
                        null,
                        contentAs
                );
            } catch (Throwable e) {
                throw new JSONException("create objectWriter error, objectType " + itemClass, e);
            }
        }
        return null;
    }

    void genGetObject(MethodWriterContext mwc, FieldWriter fieldWriter, int i, int OBJECT) {
        MethodWriter mw = mwc.mw;
        Class objectClass = mwc.objectClass;
        final String TYPE_OBJECT = objectClass == null ? "java/lang/Object" : ASMUtils.type(objectClass);
        Class fieldClass = fieldWriter.fieldClass;
        Member member = fieldWriter.method != null ? fieldWriter.method : fieldWriter.field;
        Function function = fieldWriter.getFunction();

        if (member == null && function != null) {
            mw.aload(THIS);
            mw.getfield(mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.invokevirtual(TYPE_FIELD_WRITER, "getFunction", "()Ljava/util/function/Function;");
            mw.aload(OBJECT);
            mw.invokeinterface(type(Function.class), "apply", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mw.checkcast(type(fieldClass));
            return;
        }

        if (member instanceof Method) {
            mw.aload(OBJECT);
            mw.checkcast(TYPE_OBJECT);
            if (objectClass.isInterface()) {
                mw.invokeinterface(TYPE_OBJECT, member.getName(), "()" + ASMUtils.desc(fieldClass));
            } else {
                mw.invokevirtual(TYPE_OBJECT, member.getName(), "()" + ASMUtils.desc(fieldClass));
            }
            return;
        }

        if (Modifier.isPublic(objectClass.getModifiers())
                && Modifier.isPublic(member.getModifiers())
                && !classLoader.isExternalClass(objectClass)
        ) {
            mw.aload(OBJECT);
            mw.checkcast(TYPE_OBJECT);
            mw.getfield(TYPE_OBJECT, member.getName(), ASMUtils.desc(fieldClass));
            return;
        }

        Field field = (Field) member;

        String methodName, methodDes, castToType = null;
        if (fieldClass == int.class) {
            methodName = "getInt";
            methodDes = "(Ljava/lang/Object;J)I";
        } else if (fieldClass == long.class) {
            methodName = "getLong";
            methodDes = "(Ljava/lang/Object;J)J";
        } else if (fieldClass == float.class) {
            methodName = "getFloat";
            methodDes = "(Ljava/lang/Object;J)F";
        } else if (fieldClass == double.class) {
            methodName = "getDouble";
            methodDes = "(Ljava/lang/Object;J)D";
        } else if (fieldClass == char.class) {
            methodName = "getChar";
            methodDes = "(Ljava/lang/Object;J)C";
        } else if (fieldClass == byte.class) {
            methodName = "getByte";
            methodDes = "(Ljava/lang/Object;J)B";
        } else if (fieldClass == short.class) {
            methodName = "getShort";
            methodDes = "(Ljava/lang/Object;J)S";
        } else if (fieldClass == boolean.class) {
            methodName = "getBoolean";
            methodDes = "(Ljava/lang/Object;J)Z";
        } else {
            methodName = "getObject";
            methodDes = "(Ljava/lang/Object;J)Ljava/lang/Object;";
            if (fieldClass.isEnum()) {
                castToType = "java/lang/Enum";
            } else if (ObjectWriterProvider.isPrimitiveOrEnum(fieldClass)) {
                castToType = ASMUtils.type(fieldClass);
            } else if (fieldClass.isArray() && ObjectWriterProvider.isPrimitiveOrEnum(fieldClass.getComponentType())) {
                castToType = ASMUtils.type(fieldClass);
            } else if (Map.class.isAssignableFrom(fieldClass)) {
                castToType = "java/util/Map";
            } else if (List.class.isAssignableFrom(fieldClass)) {
                castToType = "java/util/List";
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                castToType = "java/util/Collection";
            }
        }

        mw.getstatic(ObjectWriterCreatorASMUtils.TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.aload(OBJECT);
        mw.visitLdcInsn(
                UNSAFE.objectFieldOffset(field));
        mw.invokevirtual("sun/misc/Unsafe", methodName, methodDes);
        if (castToType != null) {
            mw.checkcast(castToType);
        }
    }

    static class MethodWriterContext {
        final ObjectWriterProvider provider;
        final Class objectClass;
        final long objectFeatures;
        final String classNameType;
        final MethodWriter mw;
        final Map<Object, Integer> variants = new LinkedHashMap<>();
        final JSONWriterType type;
        int maxVariant;

        public MethodWriterContext(
                ObjectWriterProvider provider,
                Class objectClass,
                long objectFeatures,
                String classNameType,
                MethodWriter mw,
                int maxVariant,
                JSONWriterType type
        ) {
            this.provider = provider;
            this.objectClass = objectClass;
            this.objectFeatures = objectFeatures;
            this.classNameType = classNameType;
            this.mw = mw;
            this.type = type;
            this.maxVariant = maxVariant;
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

        int var2(Object key) {
            Integer var = variants.get(key);
            if (var == null) {
                var = maxVariant;
                variants.put(key, var);
                maxVariant += 2;
            }
            return var;
        }

        void genVariantsMethodBefore(boolean jsonb) {
            Label notDefault_ = new Label(), LEnd = new Label();

            mw.aload(JSON_WRITER);
            mw.invokevirtual(TYPE_JSON_WRITER, "getFeatures", "()J");
            mw.lstore(var2(CONTEXT_FEATURES));

            if (!jsonb) {
                Label l1 = new Label(), l2 = new Label();

                mw.aload(JSON_WRITER);
                mw.getfield(TYPE_JSON_WRITER, "useSingleQuote", "Z");
                mw.ifne(l1);

                mw.lload(var2(CONTEXT_FEATURES));
                mw.visitLdcInsn(UnquoteFieldName.mask | UseSingleQuotes.mask);
                mw.land();
                mw.lconst_0();
                mw.lcmp();
                mw.ifne(l1);
                mw.iconst_1();
                mw.goto_(l2);

                mw.visitLabel(l1);
                mw.iconst_0();

                mw.visitLabel(l2);
                mw.istore(var2(NAME_DIRECT));
            } else {
                Label l1 = new Label(), l2 = new Label();

                mw.aload(JSON_WRITER);
                mw.getfield(TYPE_JSON_WRITER, "symbolTable", DESC_SYMBOL);
                mw.ifnonnull(l1);

                mw.lload(var2(CONTEXT_FEATURES));
                mw.visitLdcInsn(WriteNameAsSymbol.mask);
                mw.land();
                mw.lconst_0();
                mw.lcmp();
                mw.ifne(l1);
                mw.iconst_1();
                mw.goto_(l2);

                mw.visitLabel(l1);
                mw.iconst_0();

                mw.visitLabel(l2);
                mw.istore(var2(NAME_DIRECT));
            }
            // UTF8_DIRECT

            genIsEnabledAndAssign(NotWriteDefaultValue.mask, var(NOT_WRITE_DEFAULT_VALUE));
            mw.iload(var(NOT_WRITE_DEFAULT_VALUE));
            mw.ifeq(notDefault_);

            mw.iconst_0();
            mw.istore(var(WRITE_NULLS));
            mw.goto_(LEnd);

            mw.visitLabel(notDefault_);

            long features = WriteNulls.mask | NullAsDefaultValue.mask;
//            genIsEnabled(features);
//            mw.istore(var(WRITE_NULLS));
            genIsEnabledAndAssign(features, var(WRITE_NULLS));

            mw.visitLabel(LEnd);
        }

        void genIsEnabled(long features, Label elseLabel) {
            mw.lload(var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(features);
            mw.land();
            mw.lconst_0();
            mw.lcmp();
            if (elseLabel != null) {
                mw.ifeq(elseLabel);
            }
        }

        void genIsEnabled(long fieldFeatures, long features, Label elseLabel) {
            mw.lload(var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(fieldFeatures);
            mw.lor();
            mw.visitLdcInsn(features);
            mw.land();
            mw.lconst_0();
            mw.lcmp();
            if (elseLabel != null) {
                mw.ifeq(elseLabel);
            }
        }

        void genIsDisabled(long features, Label elseLabel) {
            mw.lload(var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(features);
            mw.land();
            mw.lconst_0();
            mw.lcmp();
            mw.ifne(elseLabel);
        }

        void genIsEnabled(long features, Label trueLabel, Label falseLabel) {
            mw.lload(var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(features);
            mw.land();
            mw.lconst_0();
            mw.lcmp();
            mw.ifeq(falseLabel);
            mw.goto_(trueLabel);
        }

        void genIsEnabledAndAssign(long features, int var) {
            mw.lload(var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(features);
            mw.land();
            mw.lconst_0();
            mw.lcmp();
            mw.istore(var);
        }

        private void loadFieldType(int fieldIndex, Type fieldType) {
            if (fieldType instanceof Class && fieldType.getTypeName().startsWith("java")) {
                mw.visitLdcInsn((Class) fieldType);
                return;
            }
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(fieldIndex), DESC_FIELD_WRITER);
            mw.getfield(TYPE_FIELD_WRITER, "fieldType", "Ljava/lang/reflect/Type;");
        }

        private void loadFieldClass(int fieldIndex, Class fieldClass) {
            if (fieldClass.getName().startsWith("java")) {
                mw.visitLdcInsn(fieldClass);
                return;
            }
            mw.aload(THIS);
            mw.getfield(classNameType, fieldWriter(fieldIndex), DESC_FIELD_WRITER);
            mw.getfield(TYPE_FIELD_WRITER, "fieldClass", "Ljava/lang/Class;");
        }

        private void ldcIFEQ(String varName, Number name1, Number name1SQ) {
            if (name1.longValue() == name1SQ.longValue()) {
                mw.visitLdcInsn(name1);
                return;
            }

            Label L1 = new Label(), L2 = new Label();
            mw.iload(var(varName));
            mw.ifeq(L1);

            mw.visitLdcInsn(name1);
            mw.goto_(L2);

            mw.visitLabel(L1);
            mw.visitLdcInsn(name1SQ);
            mw.visitLabel(L2);
        }

        public boolean disableSupportArrayMapping() {
            return (objectFeatures & FieldInfo.DISABLE_ARRAY_MAPPING) != 0;
        }

        public boolean disableReferenceDetect() {
            return (objectFeatures & FieldInfo.DISABLE_REFERENCE_DETECT) != 0;
        }

        public boolean disableSmartMatch() {
            return (objectFeatures & FieldInfo.DISABLE_ARRAY_MAPPING) != 0;
        }

        public boolean disableAutoType() {
            return (objectFeatures & FieldInfo.DISABLE_AUTO_TYPE) != 0;
        }

        public boolean disableJSONB() {
            return (objectFeatures & FieldInfo.DISABLE_JSONB) != 0;
        }
    }

    static class FieldWriterGroup {
        final boolean start;
        final boolean direct;
        boolean end;
        final List<FieldWriterRecord> fieldWriters = new ArrayList<>();

        public FieldWriterGroup(boolean start, boolean direct) {
            this.start = start;
            this.direct = direct;
        }
    }

    static final class FieldWriterRecord {
        final FieldWriter fieldWriter;
        final int ordinal;

        public FieldWriterRecord(FieldWriter fieldWriter, int ordinal) {
            this.fieldWriter = fieldWriter;
            this.ordinal = ordinal;
        }

        static List<FieldWriterRecord> of(List<FieldWriter> fieldWriters) {
            List<FieldWriterRecord> records = new ArrayList<>();
            for (int i = 0; i < fieldWriters.size(); i++) {
                records.add(new FieldWriterRecord(fieldWriters.get(i), i));
            }
            return records;
        }
    }

    @SuppressWarnings("rawtypes")
    static List<FieldWriterGroup> buildGroups(long beanFeatures, List<FieldWriter> fieldWriters) {
        List<FieldWriterGroup> groups = new ArrayList<>();

        if (fieldWriters.isEmpty()) {
            FieldWriterGroup group = new FieldWriterGroup(true, false);
            group.end = true;
            groups.add(group);
            return groups;
        }

        FieldWriterGroup group = null;
        for (int i = 0; i < fieldWriters.size(); i++) {
            FieldWriter item = fieldWriters.get(i);
            boolean direct = supportDirectWrite(beanFeatures, item);
            if (group == null || group.direct != direct) {
                group = new FieldWriterGroup(i == 0, direct);
                groups.add(group);
            }
            group.fieldWriters.add(new FieldWriterRecord(item, i));
            if (i == fieldWriters.size() - 1) {
                group.end = true;
            }
        }

        return groups;
    }

    static boolean supportDirectWrite(long beanFeatures, FieldWriter fieldWriter) {
        if (WriteNonStringValueAsString.mask == (beanFeatures & WriteNonStringValueAsString.mask)
                || fieldWriter.format != null
        ) {
            return false;
        }
        Class fieldClass = fieldWriter.fieldClass;

        if (Collection.class.isAssignableFrom(fieldClass)) {
            Class itemClass = fieldWriter.getItemClass();
            return itemClass == String.class || itemClass == Long.class;
        }

        return fieldClass == byte.class || fieldClass == Byte.class
                || fieldClass == short.class || fieldClass == Short.class
                || fieldClass == int.class || fieldClass == Integer.class
                || fieldClass == long.class || fieldClass == Long.class
                || fieldClass == float.class || fieldClass == Float.class
                || fieldClass == double.class || fieldClass == Double.class
                || fieldClass == boolean.class || fieldClass == Boolean.class
                || fieldClass == String.class || fieldClass == String[].class
                || fieldClass == UUID.class
                || fieldClass == LocalDate.class
                || fieldClass == LocalDateTime.class
                || fieldClass == LocalTime.class
                || fieldClass == OffsetDateTime.class
                || fieldClass == OffsetTime.class
                || fieldClass == Instant.class
                || fieldWriter instanceof FieldWriterEnum;
    }
}
