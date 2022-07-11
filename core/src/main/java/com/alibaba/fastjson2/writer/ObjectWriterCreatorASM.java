package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.internal.asm.*;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.*;

import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class ObjectWriterCreatorASM
        extends ObjectWriterCreator {
    // GraalVM not support
    // Android not support
    public static final ObjectWriterCreatorASM INSTANCE = new ObjectWriterCreatorASM();

    protected static final AtomicLong seed = new AtomicLong();
    protected final DynamicClassLoader classLoader;

    static final String TYPE_COLLECTION = ASMUtils.type(Collection.class);
    static final String TYPE_OBJECT_WRITER = ASMUtils.type(ObjectWriter.class);
    static final String TYPE_JSON_WRITER = ASMUtils.type(JSONWriter.class);
    static final String TYPE_JSONB = ASMUtils.type(JSONB.class);
    static final String TYPE_FIELD_WRITER = ASMUtils.type(FieldWriter.class);
    static final String TYPE_CONTEXT = ASMUtils.type(JSONWriter.Context.class);
    static final String TYPE_OBJECT_WRITER_ADAPTER = ASMUtils.type(ObjectWriterAdapter.class);

    static final String[] INTERFACES = {TYPE_OBJECT_WRITER};

    static final String DESC_OBJECT_WRITER = ASMUtils.desc(ObjectWriter.class);
    static final String DESC_JSON_WRITER = ASMUtils.desc(JSONWriter.class);
    static final String DESC_FIELD_WRITER = ASMUtils.desc(FieldWriter.class);
    static final String DESC_FIELD_WRITER_ARRAY = ASMUtils.desc(FieldWriter[].class);

    static final String METHOD_DESC_WRITE_VALUE = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;)V";
    static final String METHOD_DESC_WRITE = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_FIELD_NAME = "(" + DESC_JSON_WRITER + ")V";
    static final String METHOD_DESC_WRITE_OBJECT = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;J)V";
    static final String METHOD_DESC_WRITE_J = "(" + DESC_JSON_WRITER + "J)V";
    static final String METHOD_DESC_WRITE_DATE_WITH_FIELD_NAME = "(" + DESC_JSON_WRITER + "Z" + ASMUtils.desc(Date.class) + ")V";
    static final String METHOD_DESC_WRITE_Z = "(" + DESC_JSON_WRITER + "Z)V";
    static final String METHOD_DESC_WRITE_ZARRAY = "(" + DESC_JSON_WRITER + "[Z)V";
    static final String METHOD_DESC_WRITE_FARRAY = "(" + DESC_JSON_WRITER + "[F)V";
    static final String METHOD_DESC_WRITE_DARRAY = "(" + DESC_JSON_WRITER + "[D)V";
    static final String METHOD_DESC_WRITE_I = "(" + DESC_JSON_WRITER + "I)V";
    static final String METHOD_DESC_WRITE_SArray = "(" + DESC_JSON_WRITER + "[S)V";
    static final String METHOD_DESC_WRITE_BArray = "(" + DESC_JSON_WRITER + "[B)V";
    static final String METHOD_DESC_WRITE_CArray = "(" + DESC_JSON_WRITER + "[C)V";
    static final String METHOD_DESC_WRITE_ENUM = "(" + DESC_JSON_WRITER + "Ljava/lang/Enum;)V";
    static final String METHOD_DESC_WRITE_LIST = "(" + DESC_JSON_WRITER + "ZLjava/util/List;)V";
    static final String METHOD_DESC_FIELD_WRITE_OBJECT = "(" + DESC_JSON_WRITER + "Ljava/lang/Object;)Z";
    static final String METHOD_DESC_GET_OBJECT_WRITER = "(" + DESC_JSON_WRITER + "Ljava/lang/Class;)" + DESC_OBJECT_WRITER;
    static final String METHOD_DESC_GET_ITEM_WRITER = "(" + DESC_JSON_WRITER + "Ljava/lang/reflect/Type;)" + DESC_OBJECT_WRITER;
    static final String METHOD_DESC_WRITE_TYPE_INFO = "(" + DESC_JSON_WRITER + ")Z";
    static final String METHOD_DESC_HAS_FILTER = "(" + DESC_JSON_WRITER + ")Z";
    static final String METHOD_DESC_GET_CONTEXT = "()" + ASMUtils.desc(JSONWriter.Context.class);
    static final String METHOD_DESC_SET_PATH = "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;";
    static final String METHOD_DESC_SET_PATH_I = "(ILjava/lang/Object;)Ljava/lang/String;";
    static final String METHOD_DESC_WRITE_REFERENCE = "(Ljava/lang/String;)V";
    static final String METHOD_DESC_WRITE_CLASS_INFO = "(" + DESC_JSON_WRITER + ")V";

    static final int THIS = 0;
    static final int JSON_WRITER = 1;
    static final String NOT_WRITE_DEFAULT_VALUE = "WRITE_DEFAULT_VALUE";
    static final String WRITE_NULLS = "WRITE_NULLS";
    static final String CONTEXT_FEATURES = "CONTEXT_FEATURES";

    static String[] fieldWriterCache = new String[1024];

    static String fieldWriter(int i) {
        String fieldName = fieldWriterCache[i];

        if (fieldName != null) {
            return fieldName;
        }

        String base = "fw";
        int size = IOUtils.stringSize(i);
        char[] chars = new char[base.length() + size];
        base.getChars(0, base.length(), chars, 0);
        IOUtils.getChars(i, chars.length, chars);
        fieldWriterCache[i] = fieldName = new String(chars);
        return fieldName;
    }

    public ObjectWriterCreatorASM() {
        this.classLoader = new DynamicClassLoader();
    }

    public ObjectWriterCreatorASM(ClassLoader classLoader) {
        this.classLoader = new DynamicClassLoader(classLoader);
    }

    @Override
    public ObjectWriter createObjectWriter(
            Class objectClass,
            long features,
            List<ObjectWriterModule> modules
    ) {
        int modifiers = objectClass.getModifiers();
        boolean externalClass = classLoader.isExternalClass(objectClass);
        boolean publicClass = Modifier.isPublic(modifiers);

        if (!publicClass || externalClass) {
            if (!JDKUtils.UNSAFE_SUPPORT) {
                return super.createObjectWriter(objectClass, features, modules);
            }
        }

        BeanInfo beanInfo = new BeanInfo();
        for (ObjectWriterModule module : modules) {
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getBeanInfo(beanInfo, objectClass);
        }

        if (beanInfo.serializer != null && ObjectWriter.class.isAssignableFrom(beanInfo.serializer)) {
            try {
                return (ObjectWriter) beanInfo.serializer.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new JSONException("create serializer error", e);
            }
        }

        long writerFeatures = features | beanInfo.writerFeatures;
        boolean fieldBased = (writerFeatures & JSONWriter.Feature.FieldBased.mask) != 0;
        if (fieldBased && (objectClass.isInterface() || objectClass.isInterface())) {
            fieldBased = false;
        }

        if (fieldBased && JDKUtils.JVM_VERSION >= 11 && Throwable.class.isAssignableFrom(objectClass)) {
            return super.createObjectWriter(objectClass, features, modules);
        }

        boolean record = BeanUtils.isRecord(objectClass);

        List<FieldWriter> fieldWriters;
        if (fieldBased && !record) {
            Map<String, FieldWriter> fieldWriterMap = new TreeMap<>();
            final FieldInfo fieldInfo = new FieldInfo();
            BeanUtils.declaredFields(objectClass, field -> {
                if (Modifier.isTransient(field.getModifiers())) {
                    return;
                }

                fieldInfo.init();
                FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFeatures, modules, beanInfo, fieldInfo, field);
                if (fieldWriter != null) {
                    fieldWriterMap.put(fieldWriter.getFieldName(), fieldWriter);
                }
            });
            fieldWriters = new ArrayList<>(fieldWriterMap.values());
        } else {
            Map<String, FieldWriter> fieldWriterMap = new TreeMap<>();
            List<FieldWriter> fieldWriterList = new ArrayList<>();
            boolean fieldWritersCreated = false;
            for (ObjectWriterModule module : modules) {
                if (module.createFieldWriters(this, objectClass, fieldWriterList)) {
                    fieldWritersCreated = true;
                    break;
                }
            }

            if (fieldWritersCreated) {
                for (FieldWriter fieldWriter : fieldWriterList) {
                    Method method = fieldWriter.getMethod();
                    if (method == null) {
                        return super.createObjectWriter(objectClass, writerFeatures, modules);
                    }
                    fieldWriterMap.putIfAbsent(fieldWriter.getFieldName(), fieldWriter);
                }
            } else {
                final FieldInfo fieldInfo = new FieldInfo();

                if (!record) {
                    BeanUtils.fields(objectClass, field -> {
                        fieldInfo.init();
                        FieldWriter fieldWriter = creteFieldWriter(objectClass, writerFeatures, modules, beanInfo, fieldInfo, field);
                        if (fieldWriter != null) {
                            fieldWriterMap.putIfAbsent(fieldWriter.getFieldName(), fieldWriter);
                        }
                    });
                }

                BeanUtils.getters(objectClass, method -> {
                    fieldInfo.init();
                    fieldInfo.features |= writerFeatures;
                    for (ObjectWriterModule module : modules) {
                        ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
                        if (annotationProcessor == null) {
                            continue;
                        }
                        annotationProcessor.getFieldInfo(beanInfo, fieldInfo, objectClass, method);
                    }
                    if (fieldInfo.ignore) {
                        return;
                    }

                    String fieldName;
                    if (fieldInfo.fieldName == null || fieldInfo.fieldName.isEmpty()) {
                        if (record) {
                            fieldName = method.getName();
                        } else {
                            fieldName = BeanUtils.getterName(method.getName(), beanInfo.namingStrategy);
                        }
                    } else {
                        fieldName = fieldInfo.fieldName;
                    }

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

                    method.setAccessible(true);

                    ObjectWriter writeUsingWriter = null;
                    if (fieldInfo.writeUsing != null) {
                        try {
                            writeUsingWriter = (ObjectWriter) fieldInfo.writeUsing.newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new JSONException("create writeUsing Writer error", e);
                        }
                    }

                    if (writeUsingWriter == null && fieldInfo.fieldClassMixIn) {
                        writeUsingWriter = ObjectWriterBaseModule.VoidObjectWriter.INSTANCE;
                    }

                    FieldWriter fieldWriter = createFieldWriter(
                            objectClass,
                            fieldName,
                            fieldInfo.ordinal,
                            fieldInfo.features,
                            fieldInfo.format,
                            fieldInfo.label,
                            method,
                            writeUsingWriter
                    );

                    FieldWriter origin = fieldWriterMap.putIfAbsent(fieldName, fieldWriter);

                    if (origin != null && origin.compareTo(fieldWriter) > 0) {
                        fieldWriterMap.put(fieldName, fieldWriter);
                    }
                });
            }
            fieldWriters = new ArrayList<>(fieldWriterMap.values());
        }

        handleIgnores(beanInfo, fieldWriters);
        Collections.sort(fieldWriters);

        boolean match = true;
        if (fieldWriters.size() >= 100 || Throwable.class.isAssignableFrom(objectClass)) {
            match = false;
        }

        if (!publicClass || externalClass) {
            for (FieldWriter fieldWriter : fieldWriters) {
                if (fieldWriter.getMethod() != null) {
                    match = false;
                    break;
                }
            }
        }

        for (FieldWriter fieldWriter : fieldWriters) {
            if (fieldWriter.getInitWriter() != null
                    || (fieldWriter.getFeatures() & FieldInfo.VALUE_MASK) != 0
                    || (fieldWriter.getFeatures() & FieldInfo.RAW_VALUE_MASK) != 0
            ) {
                match = false;
                break;
            }
        }

        if (!match) {
            return super.createObjectWriter(objectClass, writerFeatures, modules);
        }

        ClassWriter cw = new ClassWriter();

        String className = "ObjectWriter_" + seed.incrementAndGet();
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

        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
                classNameType,
                TYPE_OBJECT_WRITER_ADAPTER,
                INTERFACES
        );

        // // define fieldWriter
        genFields(fieldWriters, cw);

        // init fieldWriter
        genMethodInit(fieldWriters, cw, classNameType);

        genGetFieldReader(
                fieldWriters,
                cw,
                classNameType,
                new ObjectWriterAdapter(objectClass, null, null, features, fieldWriters)
        );

        genMethodWriteJSONB(objectClass, fieldWriters, cw, classNameType, writerFeatures);

        if ((writerFeatures & JSONWriter.Feature.BeanToArray.mask) != 0) {
            genMethodWriteArrayMapping("write", objectClass, writerFeatures, fieldWriters, cw, classNameType);
        } else {
            genMethodWrite(objectClass, fieldWriters, cw, classNameType, writerFeatures);
        }

        genMethodWriteArrayMappingJSONB(objectClass, writerFeatures, fieldWriters, cw, classNameType, writerFeatures);

        genMethodWriteArrayMapping("writeArrayMapping", objectClass, writerFeatures, fieldWriters, cw, classNameType);

        byte[] code = cw.toByteArray();

        Class<?> deserClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);

        try {
            Constructor<?> constructor = deserClass.getConstructor(Class.class, String.class, String.class, long.class, List.class);
            return (ObjectWriter) constructor.newInstance(objectClass, beanInfo.typeKey, beanInfo.typeName, writerFeatures, fieldWriters);
        } catch (Throwable e) {
            throw new JSONException("create objectWriter error, objectType " + objectClass, e);
        }
    }

    private void genMethodWrite(
            Class objectType,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            long objectFeatures
    ) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "write",
                METHOD_DESC_WRITE
        );

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;
        final int COMMA = 7;

        Label json_ = new Label(), jsonb_ = new Label(), notSuper_ = new Label();

        MethodWriterContext mwc = new MethodWriterContext(objectType, objectFeatures, classNameType, mw, 8, false);
        mwc.genVariantsMethodBefore();

        mwc.genIsEnabled(JSONWriter.Feature.IgnoreErrorGetter.mask, notSuper_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.LLOAD, FIELD_FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT_WRITER_ADAPTER, "write", METHOD_DESC_WRITE_OBJECT, false);
        mw.visitInsn(Opcodes.RETURN);

        mw.visitLabel(notSuper_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isJSONB", "()Z", false);
        mw.visitJumpInsn(Opcodes.IFEQ, json_);

        mwc.genIsEnabled(JSONWriter.Feature.BeanToArray.mask, jsonb_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.LLOAD, FIELD_FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "writeArrayMappingJSONB", METHOD_DESC_WRITE_OBJECT, false);
        mw.visitInsn(Opcodes.RETURN);

        mw.visitLabel(jsonb_);
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.LLOAD, FIELD_FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "writeJSONB", METHOD_DESC_WRITE_OBJECT, false);
        mw.visitInsn(Opcodes.RETURN);

        mw.visitLabel(json_);

        Label checkFilter_ = new Label();

        mwc.genIsEnabled(JSONWriter.Feature.BeanToArray.mask, checkFilter_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.LLOAD, FIELD_FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "writeArrayMapping", METHOD_DESC_WRITE_OBJECT, false);
        mw.visitInsn(Opcodes.RETURN);

        mw.visitLabel(checkFilter_);

        Label object_ = new Label();
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_WRITER, "hasFilter", METHOD_DESC_HAS_FILTER, true);
        mw.visitJumpInsn(Opcodes.IFEQ, object_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.LLOAD, FIELD_FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "writeWithFilter", METHOD_DESC_WRITE_OBJECT, false);
        mw.visitInsn(Opcodes.RETURN);

        mw.visitLabel(object_);

        Label return_ = new Label();
        if (!java.io.Serializable.class.isAssignableFrom(objectType)) {
            Label endIgnoreNoneSerializable_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.IgnoreNoneSerializable.mask, endIgnoreNoneSerializable_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, return_);

            mw.visitLabel(endIgnoreNoneSerializable_);

            Label endErrorOnNoneSerializable_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ErrorOnNoneSerializable.mask, endErrorOnNoneSerializable_);
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, mwc.classNameType, "errorOnNoneSerializable", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, return_);

            mw.visitLabel(endErrorOnNoneSerializable_);
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "startObject", "()V", false);

        mw.visitInsn(Opcodes.ICONST_1);
        mw.visitVarInsn(Opcodes.ISTORE, COMMA); // comma = false

        Label writeFields_ = new Label();
        if ((objectFeatures & JSONWriter.Feature.WriteClassName.mask) == 0) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isWriteTypeInfo", "(Ljava/lang/Object;Ljava/lang/reflect/Type;)Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, writeFields_);
        }

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_WRITER, "writeTypeInfo", METHOD_DESC_WRITE_TYPE_INFO, true);
        mw.visitInsn(Opcodes.ICONST_1);
        mw.visitInsn(Opcodes.IXOR);
        mw.visitVarInsn(Opcodes.ISTORE, COMMA);

        mw.visitLabel(writeFields_);

        for (int i = 0; i < fieldWriters.size(); i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            gwFieldValue(mwc, fieldWriter, OBJECT, i);
        }

        mw.visitVarInsn(Opcodes.ALOAD, 1);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "endObject", "()V", false);

        mw.visitLabel(return_);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
        mw.visitEnd();
    }

    private void genMethodWriteJSONB(
            Class objectType,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            long objectFeatures
    ) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "writeJSONB", METHOD_DESC_WRITE);

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;

        MethodWriterContext mwc = new MethodWriterContext(objectType, objectFeatures, classNameType, mw, 7, true);
        mwc.genVariantsMethodBefore();

        Label return_ = new Label();
        if (!java.io.Serializable.class.isAssignableFrom(objectType)) {
            Label endIgnoreNoneSerializable_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.IgnoreNoneSerializable.mask, endIgnoreNoneSerializable_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, return_);

            mw.visitLabel(endIgnoreNoneSerializable_);

            Label endErrorOnNoneSerializable_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ErrorOnNoneSerializable.mask, endErrorOnNoneSerializable_);
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, mwc.classNameType, "errorOnNoneSerializable", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, return_);

            mw.visitLabel(endErrorOnNoneSerializable_);
        }

        int entryCnt = fieldWriters.size();

        Label notWriteType = new Label();
        if ((objectFeatures & JSONWriter.Feature.WriteClassName.mask) == 0) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isWriteTypeInfo", "(Ljava/lang/Object;Ljava/lang/reflect/Type;)Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, notWriteType);
        }

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "writeClassInfo", METHOD_DESC_WRITE_CLASS_INFO, false);

        mw.visitLabel(notWriteType);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "startObject", "()V", false);

        for (int i = 0; i < fieldWriters.size(); i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            gwFieldValueJSONB(mwc, fieldWriter, OBJECT, i);
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "endObject", "()V", false);

        mw.visitLabel(return_);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
        mw.visitEnd();
    }

    private void genMethodWriteArrayMappingJSONB(
            Class objectType,
            long objectFeatures,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            long features
    ) {
        MethodWriter mw = cw.visitMethod(
                Opcodes.ACC_PUBLIC,
                "writeArrayMappingJSONB",
                METHOD_DESC_WRITE
        );

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;

        {
            Label notWriteType = new Label();
            if ((objectFeatures & JSONWriter.Feature.WriteClassName.mask) == 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
                mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
                mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isWriteTypeInfo", "(Ljava/lang/Object;Ljava/lang/reflect/Type;)Z", false);
                mw.visitJumpInsn(Opcodes.IFEQ, notWriteType);
            }

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "writeClassInfo", METHOD_DESC_WRITE_CLASS_INFO, false);

            mw.visitLabel(notWriteType);
        }

        int size = fieldWriters.size();

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        if (size >= 128) {
            mw.visitIntInsn(Opcodes.SIPUSH, size);
        } else {
            mw.visitIntInsn(Opcodes.BIPUSH, size);
        }
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "startArray", "(I)V", false);

        MethodWriterContext mwc = new MethodWriterContext(objectType, objectFeatures, classNameType, mw, 7, true);

        mwc.genVariantsMethodBefore();

        for (int i = 0; i < size; i++) {
            gwValueJSONB(
                    mwc,
                    fieldWriters.get(i),
                    OBJECT,
                    i,
                    false
            );
        }

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
        mw.visitEnd();
    }

    private void gwValueJSONB(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean table
    ) {
        long features = fieldWriter.getFeatures() | mwc.objectFeatures;
        Class<?> fieldClass = fieldWriter.getFieldClass();

        boolean beanToArray = (features & JSONWriter.Feature.BeanToArray.mask) != 0 || table;

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
                || fieldClass == long[].class
                || fieldClass == float.class
                || fieldClass == float[].class
                || fieldClass == double.class
                || fieldClass == double[].class
                || fieldClass == String.class
                || fieldClass.isEnum()
        ) {
            gwValue(mwc, fieldWriter, OBJECT);
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
        Class<?> fieldClass = fieldWriter.getFieldClass();
        String fieldName = fieldWriter.getFieldName();

        String classNameType = mwc.classNameType;
        MethodWriter mw = mwc.mw;
        int FIELD_VALUE = mwc.var(fieldClass);
        int REF_PATH = mwc.var("REF_PATH");

        Label endIfNull_ = new Label(), notNull_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);
        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(notNull_);

        boolean refDetection = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
        if (refDetection) {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitJumpInsn(Opcodes.IF_ACMPNE, refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn("..");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V", false);

            mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

            mw.visitLabel(refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn(fieldName);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "setPath", METHOD_DESC_SET_PATH, false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, REF_PATH);
            mw.visitJumpInsn(Opcodes.IFNULL, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, REF_PATH);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE, false);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
            mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

            mw.visitLabel(endDetect_);
        }

        // fw.getObjectWriter(w, value.getClass());
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                TYPE_FIELD_WRITER,
                "getObjectWriter",
                METHOD_DESC_GET_OBJECT_WRITER,
                true);

        // objectWriter.write(jw, ctx, value);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitLdcInsn(fieldName);
        mwc.loadFieldType(i, fieldWriter.getFieldType());
        mw.visitLdcInsn(fieldWriter.getFeatures());
        mw.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                TYPE_OBJECT_WRITER,
                beanToArray ? "writeJSONB" : "writeArrayMappingJSONB",
                METHOD_DESC_WRITE_OBJECT,
                true
        );

        if (refDetection) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwListJSONB(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        Type fieldType = fieldWriter.getFieldType();
        Class<?> fieldClass = fieldWriter.getFieldClass();
        String fieldName = fieldWriter.getFieldName();

        String classNameType = mwc.classNameType;
        MethodWriter mw = mwc.mw;
        int LIST = mwc.var(fieldClass);
        int REF_PATH = mwc.var("REF_PATH");

        boolean listStr = false;
        Type itemType;
        Class itemClass = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
                itemClass = TypeUtils.getMapping(itemType);
                listStr = itemType == String.class;
            }
        }

        Label endIfListNull_ = new Label(), listNotNull_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, LIST);
        mw.visitJumpInsn(Opcodes.IFNONNULL, listNotNull_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);
        mw.visitJumpInsn(Opcodes.GOTO, endIfListNull_);

        mw.visitLabel(listNotNull_);

        {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitJumpInsn(Opcodes.IF_ACMPNE, refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn("..");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V", false);

            mw.visitJumpInsn(Opcodes.GOTO, endIfListNull_);

            mw.visitLabel(refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn(fieldName);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "setPath", METHOD_DESC_SET_PATH, false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, REF_PATH);
            mw.visitJumpInsn(Opcodes.IFNULL, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, REF_PATH);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE, false);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
            mw.visitJumpInsn(Opcodes.GOTO, endIfListNull_);

            mw.visitLabel(endDetect_);
        }

        int ITEM_CLASS = mwc.var(Class.class);
        int PREVIOUS_CLASS = mwc.var("ITEM_CLASS");
        int ITEM_OBJECT_WRITER = mwc.var("ITEM_OBJECT_WRITER");
        if (!listStr) {
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, PREVIOUS_CLASS);
            mw.visitVarInsn(Opcodes.ASTORE, ITEM_OBJECT_WRITER);
        }

        // TODO writeTypeInfo

        if (listStr) {
            // void writeListStr(JSONWriter jw, List<String> list)
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitInsn(Opcodes.ICONST_0);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeListStr", METHOD_DESC_WRITE_LIST, true);
        } else {
            // void writeList(JSONWriter jw, ObjectWriterContext ctx, List list) {
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitInsn(Opcodes.ICONST_0);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeList", METHOD_DESC_WRITE_LIST, true);
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, LIST);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);

        mw.visitLabel(endIfListNull_);
    }

    private void gwDate(MethodWriterContext mwc, FieldWriter fieldWriter, int OBJECT, int i) {
        MethodWriter mw = mwc.mw;
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitInsn(Opcodes.ICONST_0);

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeDate", METHOD_DESC_WRITE_DATE_WITH_FIELD_NAME, true);
    }

    private void gwValue(MethodWriterContext mwc, FieldWriter fieldWriter, int OBJECT) {
        MethodWriter mw = mwc.mw;
        Class fieldClass = fieldWriter.getFieldClass();

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        genGetObject(mwc, fieldWriter, OBJECT);

        String methodName, methodDesc;
        if (fieldClass == boolean.class) {
            methodName = "writeBool";
            methodDesc = "(Z)V";
        } else if (fieldClass == char.class) {
            methodName = "writeString";
            methodDesc = "(C)V";
        } else if (fieldClass == byte.class) {
            methodName = "writeInt32";
            methodDesc = "(I)V";
        } else if (fieldClass == short.class) {
            methodName = "writeInt32";
            methodDesc = "(I)V";
        } else if (fieldClass == int.class) {
            methodName = "writeInt32";
            methodDesc = "(I)V";
        } else if (fieldClass == long.class) {
            methodName = "writeInt64";
            methodDesc = "(J)V";
        } else if (fieldClass == float.class) {
            methodName = "writeFloat";
            methodDesc = "(F)V";
        } else if (fieldClass == double.class) {
            methodName = "writeDouble";
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
        } else if (fieldClass == long[].class) {
            methodName = "writeInt64";
            methodDesc = "([J)V";
        } else if (fieldClass == float[].class) {
            methodName = "writeFloat";
            methodDesc = "([F)V";
        } else if (fieldClass == double[].class) {
            methodName = "writeDouble";
            methodDesc = "([D)V";
        } else if (fieldClass == String.class) {
            methodName = "writeString";
            methodDesc = "(Ljava/lang/String;)V";
        } else if (Enum.class.isAssignableFrom(fieldClass)) {
            methodName = "writeEnum";
            methodDesc = "(Ljava/lang/Enum;)V";
        } else {
            throw new UnsupportedOperationException();
        }

        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, methodName, methodDesc, false);
    }

    private void gwObjectA(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeValue", METHOD_DESC_WRITE_VALUE, true);
    }

    private void genMethodWriteArrayMapping(
            String methodName,
            Class objectType,
            long objectFeatures,
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType
    ) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                methodName,
                METHOD_DESC_WRITE
        );

        final int OBJECT = 2;
        final int FIELD_NAME = 3;
        final int FIELD_TYPE = 4;
        final int FIELD_FEATURES = 5;

        Label jsonb_ = new Label();

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isJSONB", "()Z", false);
        mw.visitJumpInsn(Opcodes.IFEQ, jsonb_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.LLOAD, FIELD_FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, classNameType, "writeArrayMappingJSONB", METHOD_DESC_WRITE_OBJECT, false);
        mw.visitInsn(Opcodes.RETURN);

        mw.visitLabel(jsonb_);

        Label object_ = new Label();
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_WRITER, "hasFilter", METHOD_DESC_HAS_FILTER, true);
        mw.visitJumpInsn(Opcodes.IFEQ, object_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_NAME);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_TYPE);
        mw.visitVarInsn(Opcodes.LLOAD, FIELD_FEATURES);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT_WRITER_ADAPTER, methodName, METHOD_DESC_WRITE, false);
        mw.visitInsn(Opcodes.RETURN);

        mw.visitLabel(object_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "startArray", "()V", false);

        MethodWriterContext mwc = new MethodWriterContext(objectType, objectFeatures, classNameType, mw, 7, false);

        for (int i = 0; i < fieldWriters.size(); i++) {
            if (i != 0) {
                mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeComma", "()V", false);
            }

            gwFieldValueArrayMapping(
                    fieldWriters.get(i),
                    mwc,
                    OBJECT,
                    i
            );
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "endArray", "()V", false);

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(mwc.maxVariant + 1, mwc.maxVariant + 1);
        mw.visitEnd();
    }

    private void gwFieldValueArrayMapping(
            FieldWriter fieldWriter,
            MethodWriterContext mwc,
            int OBJECT,
            int i
    ) {
        Class objectType = mwc.objectClass;
        Class<?> fieldClass = fieldWriter.getFieldClass();

        final String TYPE_OBJECT = ASMUtils.type(objectType);

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
                || fieldClass == long[].class
                || fieldClass == float.class
                || fieldClass == float[].class
                || fieldClass == double.class
                || fieldClass == double[].class
                || fieldClass == String.class
                || fieldClass.isEnum()
        ) {
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == Date.class) {
            gwDate(mwc, fieldWriter, OBJECT, i);
        } else if (fieldWriter instanceof FieldWriterList) {
            gwList(mwc, OBJECT, i, fieldWriter, TYPE_OBJECT);
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
        Class<?> fieldClass = fieldWriter.getFieldClass();
        String fieldName = fieldWriter.getFieldName();

        MethodWriter mw = mwc.mw;
        int FIELD_VALUE = mwc.var(fieldClass);
        int REF_PATH = mwc.var("REF_PATH");

        Label endIfNull_ = new Label(), notNull_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);
        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(notNull_);

        boolean refDetection = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
        if (refDetection) {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isRefDetect", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitJumpInsn(Opcodes.IF_ACMPNE, refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn("..");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V", false);

            mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

            mw.visitLabel(refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn(fieldName);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "setPath", METHOD_DESC_SET_PATH, false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, REF_PATH);
            mw.visitJumpInsn(Opcodes.IFNULL, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, REF_PATH);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE, false);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
            mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

            mw.visitLabel(endDetect_);
        }

        // fw.getObjectWriter(w, value.getClass());
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                TYPE_FIELD_WRITER,
                "getObjectWriter",
                METHOD_DESC_GET_OBJECT_WRITER,
                true);

        // objectWriter.write(jw, ctx, value);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitLdcInsn(fieldWriter.getFieldName());
        mwc.loadFieldType(i, fieldWriter.getFieldType());
        mw.visitLdcInsn(fieldWriter.getFeatures());
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_WRITER, "write", METHOD_DESC_WRITE_OBJECT, true);

        if (refDetection) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwList(MethodWriterContext mwc, int OBJECT, int i, FieldWriter fieldWriter, String TYPE_OBJECT) {
        Type fieldType = fieldWriter.getFieldType();
        Class<?> fieldClass = fieldWriter.getFieldClass();
        int LIST = mwc.var(fieldClass);
        MethodWriter mw = mwc.mw;

        boolean listStr = false;
        Type itemType;
        Class itemClass = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 1) {
                itemType = actualTypeArguments[0];
                itemClass = TypeUtils.getMapping(itemType);
                listStr = itemType == String.class;
            }
        }

        Label endIfListNull_ = new Label(), listNotNull_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, LIST);
        mw.visitJumpInsn(Opcodes.IFNONNULL, listNotNull_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);
        mw.visitJumpInsn(Opcodes.GOTO, endIfListNull_);

        mw.visitLabel(listNotNull_);

        int LIST_SIZE = mwc.var("LIST_SIZE");
        int J = mwc.var("J");

        int ITEM_CLASS = mwc.var(Class.class);
        int PREVIOUS_CLASS = mwc.var("PREVIOUS_CLASS");
        int ITEM_OBJECT_WRITER = mwc.var("ITEM_OBJECT_WRITER");

        if (!listStr) {
            mw.visitInsn(Opcodes.ACONST_NULL);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, PREVIOUS_CLASS);
            mw.visitVarInsn(Opcodes.ASTORE, ITEM_OBJECT_WRITER);
        }

        // for(int j = 0；
        mw.visitVarInsn(Opcodes.ALOAD, LIST);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "size", "()I", true);
        mw.visitVarInsn(Opcodes.ISTORE, LIST_SIZE);

        // startArray(int size)
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "startArray", "()V", false);

        Label for_start_j_ = new Label(), for_end_j_ = new Label(), for_inc_j_ = new Label(), notFirst_ = new Label();
        mw.visitInsn(Opcodes.ICONST_0);
        mw.visitVarInsn(Opcodes.ISTORE, J);

        mw.visitLabel(for_start_j_);
        mw.visitVarInsn(Opcodes.ILOAD, J);
        mw.visitVarInsn(Opcodes.ILOAD, LIST_SIZE);
        mw.visitJumpInsn(Opcodes.IF_ICMPGE, for_end_j_);

        mw.visitVarInsn(Opcodes.ILOAD, J);
        mw.visitJumpInsn(Opcodes.IFEQ, notFirst_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeComma", "()V", false);
        mw.visitLabel(notFirst_);

        if (listStr) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitVarInsn(Opcodes.ILOAD, J);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
            mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String"); // cast
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeString", "(Ljava/lang/String;)V", false);
        } else {
            int ITEM = mwc.var(itemClass);
            Label notNull_ = new Label(), classEQ_ = new Label();

            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitVarInsn(Opcodes.ILOAD, J);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, ITEM);

            // if(item == null)
            mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);
            mw.visitJumpInsn(Opcodes.GOTO, for_inc_j_);

            mw.visitLabel(notNull_);

            mw.visitVarInsn(Opcodes.ALOAD, ITEM);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, ITEM_CLASS);

            // if (itemClass == previousClass) {
            mw.visitVarInsn(Opcodes.ALOAD, PREVIOUS_CLASS);
            mw.visitJumpInsn(Opcodes.IF_ACMPEQ, classEQ_);

            // previousObjectWriter = fw_i.getItemWriter(jsonWriter.getContext(), itemClass);
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, ITEM_CLASS);

            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER,
                    "getItemWriter",
                    METHOD_DESC_GET_ITEM_WRITER,
                    true
            );
            mw.visitVarInsn(Opcodes.ASTORE, ITEM_OBJECT_WRITER);

            mw.visitVarInsn(Opcodes.ALOAD, ITEM_CLASS);
            mw.visitVarInsn(Opcodes.ASTORE, PREVIOUS_CLASS);

            mw.visitLabel(classEQ_);
            mw.visitVarInsn(Opcodes.ALOAD, ITEM_OBJECT_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, ITEM);
            mw.visitVarInsn(Opcodes.ILOAD, J);
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            mwc.loadFieldType(i, fieldType);
            mw.visitLdcInsn(fieldWriter.getFeatures());
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_WRITER, "write", METHOD_DESC_WRITE_OBJECT, true);
        }

        mw.visitLabel(for_inc_j_);
        mw.visitIincInsn(J, 1);
        mw.visitJumpInsn(Opcodes.GOTO, for_start_j_);

        mw.visitLabel(for_end_j_);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "endArray", "()V", false);

        mw.visitLabel(endIfListNull_);
    }

    private void gwFieldValue(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;

        Class<?> fieldClass = fieldWriter.getFieldClass();

        if (fieldClass == boolean.class) {
            gwFieldValueBooleanV(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == boolean[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == char.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == char[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == byte.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == byte[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == short.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == short[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == int.class) {
            gwFieldValueInt32V(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == int[].class) {
            gwFieldValueIntVA(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == long.class) {
            gwFieldValueInt64V(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == long[].class) {
            gwFieldValueInt64VA(mwc, fieldWriter, OBJECT, i, false);
        } else if (fieldClass == float.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == float[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == double.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == double[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Integer.class) {
            gwInt32(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Long.class) {
            gwInt64(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == String.class) {
            gwFieldValueString(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass.isEnum()
                && BeanUtils.getEnumValueField(fieldClass) == null
                && !(fieldWriter instanceof FieldWriterObject)
        ) {
            gwFieldValueEnum(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == Date.class) {
            gwFieldValueDate(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == List.class) {
            gwFieldValueList(mwc, fieldWriter, OBJECT, i);
        } else {
            gwFieldValueObject(mwc, fieldWriter, OBJECT, i);
        }
    }

    private void gwFieldValueEnum(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        Class<?> fieldClass = fieldWriter.getFieldClass();

        MethodWriter mw = mwc.mw;

        int FIELD_VALUE = mwc.var(fieldClass);

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        Label null_ = new Label(), notNull_ = new Label();
        mw.visitJumpInsn(Opcodes.IFNULL, null_);

        // void writeEnum(JSONWriter jw, Enum e)
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);

        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeEnum", METHOD_DESC_WRITE_ENUM, true);
        mw.visitJumpInsn(Opcodes.GOTO, notNull_);

        mw.visitLabel(null_);

        // if (!jw.isWriteNulls())
        mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
        mw.visitJumpInsn(Opcodes.IFEQ, notNull_);

        // writeFieldName(w);
        gwFieldName(mwc, i);

        // jw.writeNulll
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);

        mw.visitLabel(notNull_);
    }

    private void gwFieldValueObject(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        Class<?> fieldClass = fieldWriter.getFieldClass();
        Type fieldType = fieldWriter.getFieldType();
        String fieldName = fieldWriter.getFieldName();

        boolean refDetection = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
        int FIELD_VALUE = mwc.var(fieldClass);

        Integer REF_PATH = null;
        if (refDetection) {
            REF_PATH = mwc.var("REF_PATH");
        }

        long features = fieldWriter.getFeatures() | mwc.objectFeatures;
        MethodWriter mw = mwc.mw;

        Label null_ = new Label(), notNull_ = new Label();

        if (fieldWriter.unwrapped()) {
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER,
                    "write", METHOD_DESC_FIELD_WRITE_OBJECT, true);
            mw.visitInsn(Opcodes.POP);
            mw.visitJumpInsn(Opcodes.GOTO, notNull_);
        }

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNULL, null_);

        if (!fieldWriter.isFieldClassSerializable()) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isIgnoreNoneSerializable", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFNE, notNull_);
        }

        if (refDetection) {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            int REF_DETECT = mwc.var("REF_DETECT");

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            if (fieldClass == Object.class) {
                mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isRefDetect", "(Ljava/lang/Object;)Z", false);
            } else {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isRefDetect", "()Z", false);
            }
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ISTORE, REF_DETECT);
            mw.visitJumpInsn(Opcodes.IFEQ, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitJumpInsn(Opcodes.IF_ACMPNE, refSetPath_);

            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn("..");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V", false);

            mw.visitJumpInsn(Opcodes.GOTO, notNull_);

            mw.visitLabel(refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn(fieldName);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "setPath", METHOD_DESC_SET_PATH, false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, REF_PATH);
            mw.visitJumpInsn(Opcodes.IFNULL, endDetect_);

            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, REF_PATH);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE, false);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
            mw.visitJumpInsn(Opcodes.GOTO, notNull_);

            mw.visitLabel(endDetect_);
        }

        if (Object[].class.isAssignableFrom(fieldClass)) {
            Label notWriteEmptyArrayEnd_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.NotWriteEmptyArray.mask, notWriteEmptyArrayEnd_);

            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
            mw.visitInsn(Opcodes.ARRAYLENGTH);
            mw.visitJumpInsn(Opcodes.IFNE, notWriteEmptyArrayEnd_);

            mw.visitJumpInsn(Opcodes.GOTO, notNull_);

            mw.visitLabel(notWriteEmptyArrayEnd_);
        }

        // writeFieldName(w);
        gwFieldName(mwc, i);

        if (fieldClass == BigDecimal.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            if (features == 0) {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeDecimal", "(Ljava/math/BigDecimal;)V", false);
            } else {
                mw.visitLdcInsn(features);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeDecimal", "(Ljava/math/BigDecimal;J)V", false);
            }
        } else if (fieldClass == BigInteger.class) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            if (features == 0) {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeBigInt", "(Ljava/math/BigInteger;)V", false);
            } else {
                mw.visitLdcInsn(features);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeBigInt", "(Ljava/math/BigInteger;J)V", false);
            }
        } else {
            // fw.getObjectWriter(w, value.getClass());
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);

            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER,
                    "getObjectWriter", METHOD_DESC_GET_OBJECT_WRITER, true);

            // objectWriter.write(jw, ctx, value);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitLdcInsn(fieldName);
            mwc.loadFieldType(i, fieldType);
            mw.visitLdcInsn(features);
            mw.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    TYPE_OBJECT_WRITER,
                    (features & JSONWriter.Feature.BeanToArray.mask) != 0 ? "writeArrayMapping" : "write",
                    METHOD_DESC_WRITE_OBJECT,
                    true
            );
        }

        mw.visitJumpInsn(Opcodes.GOTO, notNull_);

        if (refDetection) {
            int REF_DETECT = mwc.var("REF_DETECT");

            Label endDetect_ = new Label();

            mw.visitVarInsn(Opcodes.ILOAD, REF_DETECT);
            mw.visitJumpInsn(Opcodes.IFEQ, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);

            mw.visitLabel(endDetect_);
        }

        mw.visitLabel(null_);

        // if (!jw.isWriteNulls())
        if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
            long nullFeatures = JSONWriter.Feature.WriteNulls.mask;
            if (fieldClass == AtomicLongArray.class
                    || fieldClass == AtomicIntegerArray.class
                    || Collection.class.isAssignableFrom(fieldClass)
                    || fieldClass.isArray()) {
                nullFeatures |= JSONWriter.Feature.WriteNullListAsEmpty.mask;
                nullFeatures |= JSONWriter.Feature.NullAsDefaultValue.mask;
            } else if (Number.class.isAssignableFrom(fieldClass)) {
                nullFeatures |= JSONWriter.Feature.WriteNullNumberAsZero.mask;
                nullFeatures |= JSONWriter.Feature.NullAsDefaultValue.mask;
            } else if (fieldClass == Boolean.class) {
                nullFeatures |= JSONWriter.Feature.WriteNullBooleanAsFalse.mask;
                nullFeatures |= JSONWriter.Feature.NullAsDefaultValue.mask;
            } else if (fieldClass == String.class) {
                nullFeatures |= JSONWriter.Feature.WriteNullStringAsEmpty.mask;
                nullFeatures |= JSONWriter.Feature.NullAsDefaultValue.mask;
            }
            mwc.genIsEnabled(nullFeatures, notNull_);
//            mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
//            mw.visitJumpInsn(Opcodes.IFEQ, notNull_);
        }

        // writeFieldName(w);
        gwFieldName(mwc, i);

        // jw.writeNulll
        String WRITE_NULL_METHOD;
        if (fieldClass == AtomicLongArray.class
                || fieldClass == AtomicIntegerArray.class
                || Collection.class.isAssignableFrom(fieldClass)
                || fieldClass.isArray()) {
            WRITE_NULL_METHOD = "writeArrayNull";
        } else if (Number.class.isAssignableFrom(fieldClass)) {
            WRITE_NULL_METHOD = "writeNumberNull";
        } else if (fieldClass == Boolean.class) {
            WRITE_NULL_METHOD = "writeBooleanNull";
        } else if (fieldClass == String.class) {
            WRITE_NULL_METHOD = "writeStringNull";
        } else {
            WRITE_NULL_METHOD = "writeNull";
        }
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, WRITE_NULL_METHOD, "()V", false);

        mw.visitLabel(notNull_);
    }

    private void gwFieldValueList(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        Type fieldType = fieldWriter.getFieldType();
        Class<?> fieldClass = fieldWriter.getFieldClass();
        String fieldName = fieldWriter.getFieldName();
        MethodWriter mw = mwc.mw;

        int LIST = mwc.var(fieldClass);
        int REF_PATH = mwc.var("REF_PATH");

        boolean listStr = false;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            listStr = actualTypeArguments.length == 1 && actualTypeArguments[0] == String.class;
        }

        int FIELD_VALUE = mwc.var(fieldClass);

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        Label null_ = new Label(), notNull_ = new Label();
        mw.visitJumpInsn(Opcodes.IFNULL, null_);

        {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            mwc.genIsEnabled(JSONWriter.Feature.ReferenceDetection.mask, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitJumpInsn(Opcodes.IF_ACMPNE, refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn("..");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V", false);

            mw.visitJumpInsn(Opcodes.GOTO, notNull_);

            mw.visitLabel(refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn(fieldName);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "setPath", METHOD_DESC_SET_PATH, false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, REF_PATH);
            mw.visitJumpInsn(Opcodes.IFNULL, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, REF_PATH);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE, false);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
            mw.visitJumpInsn(Opcodes.GOTO, notNull_);

            mw.visitLabel(endDetect_);
        }

        {
            Label notWriteEmptyArrayEnd_ = new Label();
            mwc.genIsEnabled(JSONWriter.Feature.NotWriteEmptyArray.mask, notWriteEmptyArrayEnd_);

            mw.visitVarInsn(Opcodes.ALOAD, LIST);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Collection", "isEmpty", "()Z", true);
            mw.visitJumpInsn(Opcodes.IFEQ, notWriteEmptyArrayEnd_);

            mw.visitJumpInsn(Opcodes.GOTO, notNull_);

            mw.visitLabel(notWriteEmptyArrayEnd_);
        }

        // listStr
        if (listStr) {
            // void writeListStr(JSONWriter jw, List<String> list)
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitInsn(Opcodes.ICONST_1);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeListStr", METHOD_DESC_WRITE_LIST, true);
        } else {
            // void writeList(JSONWriter jw, ObjectWriterContext ctx, List list) {
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitInsn(Opcodes.ICONST_1);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeList", METHOD_DESC_WRITE_LIST, true);
        }
        mw.visitJumpInsn(Opcodes.GOTO, notNull_);

        mw.visitLabel(null_);
        // if (!jw.isWriteNulls())
        mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
        mw.visitJumpInsn(Opcodes.IFEQ, notNull_);

        // writeFieldName(w);
        gwFieldName(mwc, i);

        // jw.writeNulll
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeArrayNull", "()V", false);

        mw.visitLabel(notNull_);
    }

    private void gwFieldValueJSONB(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;
        Class objectType = mwc.objectClass;
        Class<?> fieldClass = fieldWriter.getFieldClass();

        if (fieldClass == boolean.class) {
            gwFieldValueBooleanV(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == boolean[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == char.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == char[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == byte.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == byte[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == short.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == short[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == int.class) {
            gwFieldValueInt32V(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == int[].class) {
            gwFieldValueIntVA(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == long.class) {
            gwFieldValueInt64V(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == long[].class) {
            gwFieldValueInt64VA(mwc, fieldWriter, OBJECT, i, true);
        } else if (fieldClass == float.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == float[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
        } else if (fieldClass == double.class) {
            gwFieldName(mwc, i);
            gwValue(mwc, fieldWriter, OBJECT);
        } else if (fieldClass == double[].class) {
            gwFieldValueArray(mwc, fieldWriter, OBJECT, i);
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
            gwFieldValueObjectJSONB(mwc, fieldWriter, OBJECT, i);
        }
    }

    private void gwInt32(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        boolean jsonb = mwc.jsonb;
        String classNameType = mwc.classNameType;
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.getFieldClass();

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);

        mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
        mw.visitJumpInsn(Opcodes.IFNE, writeNullValue_);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(writeNullValue_);

        gwFieldName(mwc, i);

        // TODO : if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsEmpty.mask)) == 0) {
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNumberNull", "()V", false);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(notNull_);

        if (jsonb) {
            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);

            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);

            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeInt32", "(I)V", false);
        } else {
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);

            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);

            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeInt32", METHOD_DESC_WRITE_I, true);
        }
        mw.visitLabel(endIfNull_);
    }

    private void gwInt64(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        boolean jsonb = mwc.jsonb;
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.getFieldClass();
        String classNameType = mwc.classNameType;

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);

        mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
        mw.visitJumpInsn(Opcodes.IFNE, writeNullValue_);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(writeNullValue_);

        gwFieldName(mwc, i);

        // TODO if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsEmpty.mask)) == 0) {
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNumberNull", "()V", false);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(notNull_);

        if (jsonb) {
            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);

            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);

            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeInt64", "(J)V", false);
        } else {
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);

            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);

            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeInt64", METHOD_DESC_WRITE_J, true);
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwFieldValueObjectJSONB(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.getFieldClass();
        String fieldName = fieldWriter.getFieldName();

        boolean refDetection = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);

        int FIELD_VALUE = mwc.var(fieldClass);

        Integer REF_PATH = null;
        if (refDetection) {
            REF_PATH = mwc.var("REF_PATH");
        }

        Label endIfNull_ = new Label(), notNull_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNULL, endIfNull_);

        if (!fieldWriter.isFieldClassSerializable()) {
            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isIgnoreNoneSerializable", "()Z", false);
            mw.visitJumpInsn(Opcodes.IFNE, endIfNull_);
        }

        /**
         * boolean refDetect = jsonWriter.isRefDetect();
         * if (refDetect) {
         *     if (value == object) {
         *         writeFieldName(jsonWriter);
         *         jsonWriter.writeReference("..");
         *         goto endIfNull_
         *     }
         *
         *     String refPath = context.setPath(name, value);
         *     if (refPath != null) {
         *         writeFieldName(jsonWriter);
         *         jsonWriter.writeReference(refPath);
         *         context.popPath();
         *         goto endIfNull_
         *     }
         * }
         */

        if (refDetection) {
            Label endDetect_ = new Label(), refSetPath_ = new Label();

            int REF_DETECT = mwc.var("REF_DETECT");

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            if (fieldClass == Object.class) {
                mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isRefDetect", "(Ljava/lang/Object;)Z", false);
            } else {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isRefDetect", "()Z", false);
            }
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ISTORE, REF_DETECT);
            mw.visitJumpInsn(Opcodes.IFEQ, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitJumpInsn(Opcodes.IF_ACMPNE, refSetPath_);

            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn("..");
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", "(Ljava/lang/String;)V", false);

            mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

            mw.visitLabel(refSetPath_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn(fieldName);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "setPath", METHOD_DESC_SET_PATH, false);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ASTORE, REF_PATH);
            mw.visitJumpInsn(Opcodes.IFNULL, endDetect_);

            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, REF_PATH);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeReference", METHOD_DESC_WRITE_REFERENCE, false);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);
            mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

            mw.visitLabel(endDetect_);
        }

        gwFieldName(mwc, i);

        // fw.getObjectWriter(w, value.getClass());
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                TYPE_FIELD_WRITER,
                "getObjectWriter",
                METHOD_DESC_GET_OBJECT_WRITER,
                true);

        // objectWriter.write(jw, ctx, value);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitLdcInsn(fieldName);
        mwc.loadFieldType(i, fieldWriter.getFieldType());
        mw.visitLdcInsn(fieldWriter.getFeatures());
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_OBJECT_WRITER, "writeJSONB", METHOD_DESC_WRITE_OBJECT, true);

        if (refDetection) {
            int REF_DETECT = mwc.var("REF_DETECT");

            Label endDetect_ = new Label();

            mw.visitVarInsn(Opcodes.ILOAD, REF_DETECT);
            mw.visitJumpInsn(Opcodes.IFEQ, endDetect_);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "popPath", "(Ljava/lang/Object;)V", false);

            mw.visitLabel(endDetect_);
        }

        mw.visitLabel(endIfNull_);
    }

    private void gwFieldValueDate(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        boolean jsonb = mwc.jsonb;
        MethodWriter mw = mwc.mw;
        Type fieldType = fieldWriter.getFieldType();
        Class<?> fieldClass = fieldWriter.getFieldClass();
        Member member = fieldWriter.getFieldOrMethod();
        String fieldName = fieldWriter.getFieldName();
        String format = fieldWriter.getFormat();

        Label null_ = new Label(), writeNull_ = new Label(), endIfNull_ = new Label();

        int FIELD_VALUE = mwc.var(fieldClass);

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);
        mw.visitJumpInsn(Opcodes.IFNULL, null_);

        // void writeEnum(JSONWriter jw, Enum e)
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Date", "getTime", "()J", false);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeDate", METHOD_DESC_WRITE_J, true);
        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(null_);

        // if (!jw.isWriteNulls())
        mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
        mw.visitJumpInsn(Opcodes.IFNE, writeNull_);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(writeNull_);
        gwFieldName(mwc, i);

        // jw.writeNulll
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeNull", "()V", false);

        mw.visitLabel(endIfNull_);
    }

    private void gwFieldValueArray(MethodWriterContext mwc, FieldWriter fieldWriter, int OBJECT, int i) {
        MethodWriter mw = mwc.mw;
        Class fieldClass = fieldWriter.getFieldClass();

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

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, mwc.classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, methodName, methodDesc, true);
    }

    private void gwFieldName(MethodWriterContext mwc, int i) {
        MethodWriter mw = mwc.mw;
        String classNameType = mwc.classNameType;

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeFieldName", METHOD_DESC_WRITE_FIELD_NAME, true);
    }

    private void gwFieldValueInt64VA(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i,
            boolean jsonb
    ) {
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.getFieldClass();

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);

        mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
        mw.visitJumpInsn(Opcodes.IFNE, writeNullValue_);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(writeNullValue_);

        gwFieldName(mwc, i);

        // TODO if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsEmpty.mask)) == 0) {
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeArrayNull", "()V", false);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(notNull_);

        gwFieldName(mwc, i);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeInt64", "([J)V", false);

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
        String format = fieldWriter.getFormat();
        String classNameType = mwc.classNameType;

        int FIELD_VALUE = mwc.var(long.class);
        int WRITE_DEFAULT_VALUE = mwc.var(NOT_WRITE_DEFAULT_VALUE);
        Label notDefaultValue_ = new Label(), endWriteValue_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP2);
        mw.visitVarInsn(Opcodes.LSTORE, FIELD_VALUE);
        mw.visitInsn(Opcodes.LCONST_0);
        mw.visitInsn(Opcodes.LCMP);
        mw.visitJumpInsn(Opcodes.IFNE, notDefaultValue_);

        mw.visitVarInsn(Opcodes.ILOAD, WRITE_DEFAULT_VALUE);
        mw.visitJumpInsn(Opcodes.IFEQ, notDefaultValue_);

        mw.visitJumpInsn(Opcodes.GOTO, endWriteValue_);

        mw.visitLabel(notDefaultValue_);
        if (jsonb) {
            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.LLOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeInt64", "(J)V", false);
        } else {
            // Int32FieldWriter.writeInt64(JSONWriter w, long value);
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.LLOAD, FIELD_VALUE);

            if ("iso8601".equals(format)) {
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeDate", METHOD_DESC_WRITE_J, true);
            } else {
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeInt64", METHOD_DESC_WRITE_J, true);
            }
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
        Class<?> fieldClass = fieldWriter.getFieldClass();

        int FIELD_VALUE = mwc.var(fieldClass);

        Label endIfNull_ = new Label(), notNull_ = new Label(), writeNullValue_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNONNULL, notNull_);

        mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
        mw.visitJumpInsn(Opcodes.IFNE, writeNullValue_);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(writeNullValue_);

        gwFieldName(mwc, i);

        // TODO if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsEmpty.mask)) == 0) {
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeArrayNull", "()V", false);

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(notNull_);

        gwFieldName(mwc, i);

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeInt32", "([I)V", false);

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
        String format = fieldWriter.getFormat();
        String classNameType = mwc.classNameType;

        int FIELD_VALUE = mwc.var(int.class);
        Integer WRITE_DEFAULT_VALUE = mwc.var(NOT_WRITE_DEFAULT_VALUE);
        Label notDefaultValue_ = new Label(), endWriteValue_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
        mw.visitJumpInsn(Opcodes.IFNE, notDefaultValue_);

        mw.visitVarInsn(Opcodes.ILOAD, WRITE_DEFAULT_VALUE);
        mw.visitJumpInsn(Opcodes.IFEQ, notDefaultValue_);

        mw.visitJumpInsn(Opcodes.GOTO, endWriteValue_);

        mw.visitLabel(notDefaultValue_);

        if ("string".equals(format)) {
            gwFieldName(mwc, i);

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitVarInsn(Opcodes.ILOAD, FIELD_VALUE);
            mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeString", "(Ljava/lang/String;)V", false);
        } else {
            if (jsonb) {
                gwFieldName(mwc, i);

                mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
                mw.visitVarInsn(Opcodes.ILOAD, FIELD_VALUE);
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeInt32", "(I)V", false);
            } else {
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);

                mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
                mw.visitVarInsn(Opcodes.ILOAD, FIELD_VALUE);
                mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeInt32", METHOD_DESC_WRITE_I, true);
            }
        }

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

        genGetObject(mwc, fieldWriter, OBJECT);
        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ISTORE, FIELD_VALUE);
        mw.visitJumpInsn(Opcodes.IFNE, notDefaultValue_);

        mw.visitVarInsn(Opcodes.ILOAD, WRITE_DEFAULT_VALUE);
        mw.visitJumpInsn(Opcodes.IFEQ, notDefaultValue_);

        mw.visitJumpInsn(Opcodes.GOTO, endWriteValue_);

        mw.visitLabel(notDefaultValue_);

        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ILOAD, FIELD_VALUE);
        mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "writeBool", METHOD_DESC_WRITE_Z, true);

        mw.visitLabel(endWriteValue_);
    }

    private void gwFieldValueString(
            MethodWriterContext mwc,
            FieldWriter fieldWriter,
            int OBJECT,
            int i
    ) {
        boolean jsonb = mwc.jsonb;
        long features = fieldWriter.getFeatures() | mwc.objectFeatures;
        MethodWriter mw = mwc.mw;
        Class<?> fieldClass = fieldWriter.getFieldClass();
        String format = fieldWriter.getFormat();

        int FIELD_VALUE = mwc.var(fieldClass);

        Label null_ = new Label(), endIfNull_ = new Label();

        genGetObject(mwc, fieldWriter, OBJECT);

        mw.visitInsn(Opcodes.DUP);
        mw.visitVarInsn(Opcodes.ASTORE, FIELD_VALUE);

        mw.visitJumpInsn(Opcodes.IFNULL, null_);

        // void writeFieldName(JSONWriter w)
        gwFieldName(mwc, i);

        // void writeString(String str)
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitVarInsn(Opcodes.ALOAD, FIELD_VALUE);
        if ("trim".equals(format)) {
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "trim", "()Ljava/lang/String;", false);
        }
        mw.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                TYPE_JSON_WRITER,
                jsonb && "symbol".equals(format) ? "writeSymbol" : "writeString",
                "(Ljava/lang/String;)V",
                false
        );

        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        mw.visitLabel(null_);

        Label writeNullValue_ = new Label(), writeNull_ = new Label();

        final long defaultValueMask = JSONWriter.Feature.NullAsDefaultValue.mask
                | JSONWriter.Feature.WriteNullNumberAsZero.mask
                | JSONWriter.Feature.WriteNullBooleanAsFalse.mask
                | JSONWriter.Feature.WriteNullListAsEmpty.mask
                | JSONWriter.Feature.WriteNullStringAsEmpty.mask;

        // if (!jw.isWriteNulls())
        if ((features & (JSONWriter.Feature.WriteNulls.mask
                | defaultValueMask)) == 0) {
            mw.visitVarInsn(Opcodes.ILOAD, mwc.var(WRITE_NULLS));
            mw.visitJumpInsn(Opcodes.IFNE, writeNull_);

            mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);
        }

        mw.visitLabel(writeNull_);
        // writeFieldName(w);
        gwFieldName(mwc, i);

        if ((features & defaultValueMask) == 0) {
            long mask = JSONWriter.Feature.NullAsDefaultValue.mask;
            if (fieldClass == String.class) {
                mask |= JSONWriter.Feature.WriteNullStringAsEmpty.mask;
            } else if (fieldClass == Boolean.class) {
                mask |= JSONWriter.Feature.WriteNullBooleanAsFalse.mask;
            } else if (Number.class.isAssignableFrom(fieldClass)) {
                mask |= JSONWriter.Feature.WriteNullNumberAsZero.mask;
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                mask |= JSONWriter.Feature.WriteNullListAsEmpty.mask;
            }

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitLdcInsn(mask);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "isEnabled", "(J)Z", false);
            mw.visitJumpInsn(Opcodes.IFEQ, writeNullValue_);
        }

        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitLdcInsn("");
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeString", "(Ljava/lang/String;)V", false);
        mw.visitJumpInsn(Opcodes.GOTO, endIfNull_);

        // jw.writeNulll
        mw.visitLabel(writeNullValue_);
        mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "writeStringNull", "()V", false);

        mw.visitLabel(endIfNull_);
    }

    private void genMethodInit(List<FieldWriter> fieldWriters, ClassWriter cw, String classNameType) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
                "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V");
        mw.visitVarInsn(Opcodes.ALOAD, THIS);
        mw.visitVarInsn(Opcodes.ALOAD, 1);
        mw.visitVarInsn(Opcodes.ALOAD, 2);
        mw.visitVarInsn(Opcodes.ALOAD, 3);
        mw.visitVarInsn(Opcodes.LLOAD, 4);
        mw.visitVarInsn(Opcodes.ALOAD, 6);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_OBJECT_WRITER_ADAPTER, "<init>", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;JLjava/util/List;)V", false);

        for (int i = 0; i < fieldWriters.size(); i++) {
            Type fieldType = fieldWriters.get(i).getFieldType();

            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitInsn(Opcodes.DUP);

            mw.visitFieldInsn(Opcodes.GETFIELD, TYPE_OBJECT_WRITER_ADAPTER, "fieldWriterArray", DESC_FIELD_WRITER_ARRAY);
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
            mw.visitInsn(Opcodes.AALOAD); // fieldWriterArray
            mw.visitTypeInsn(Opcodes.CHECKCAST,
                    TYPE_FIELD_WRITER); // cast
            mw.visitFieldInsn(Opcodes.PUTFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
        }

        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(7, 7);
        mw.visitEnd();
    }

    private void genFields(List<FieldWriter> fieldWriters, ClassWriter cw) {
        for (int i = 0; i < fieldWriters.size(); i++) {
            cw.visitField(
                    Opcodes.ACC_PUBLIC,
                    fieldWriter(i),
                    DESC_FIELD_WRITER
            ).visitEnd();
        }
    }

    @Override
    public <T> FieldWriter<T> createFieldWriter(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            ObjectWriter initObjectWriter
    ) {
        Class<?> declaringClass = field.getDeclaringClass();
        if (!declaringClass.getName().startsWith("java.lang")) {
            field.setAccessible(true);
        }

        Class<?> fieldClass = field.getType();
        Type fieldType = field.getGenericType();

        if (initObjectWriter != null) {
            if (fieldClass == byte.class) {
                fieldType = fieldClass = Byte.class;
            } else if (fieldClass == short.class) {
                fieldType = fieldClass = Short.class;
            } else if (fieldClass == int.class) {
                fieldType = fieldClass = Integer.class;
            } else if (fieldClass == long.class) {
                fieldType = fieldClass = Long.class;
            } else if (fieldClass == float.class) {
                fieldType = fieldClass = Float.class;
            } else if (fieldClass == double.class) {
                fieldType = fieldClass = Double.class;
            } else if (fieldClass == boolean.class) {
                fieldType = fieldClass = Boolean.class;
            }

            FieldWriterObjectField objImp = new FieldWriterObjectField(
                    fieldName,
                    ordinal,
                    features,
                    format,
                    label,
                    fieldType,
                    fieldClass,
                    field
            );
            objImp.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objImp.initObjectWriter = initObjectWriter;
            }
            return objImp;
        }

        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return new FieldWriterBooleanField(fieldName, ordinal, features, format, label, field, fieldClass);
        }

        if (fieldClass == byte.class) {
            return new FieldWriterInt8ValField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == short.class) {
            return new FieldWriterInt16ValField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == int.class) {
            if (JDKUtils.UNSAFE_SUPPORT) {
                return new FieldWriterInt32ValUF<>(fieldName, ordinal, features, format, label, field);
            } else {
                return new FieldWriterInt32Val(fieldName, ordinal, features, format, label, field);
            }
        }

        if (fieldClass == long.class) {
            if (format == null || format.isEmpty()) {
                return new FieldWriterInt64ValField(fieldName, ordinal, features, format, label, field);
            }
            return new FieldWriterMillisField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == float.class) {
            return new FieldWriterFloatValField(fieldName, ordinal, format, label, field);
        }

        if (fieldClass == double.class) {
            return new FieldWriterDoubleValField(fieldName, ordinal, format, label, field);
        }

        if (fieldClass == char.class) {
            return new FieldWriterCharValField(fieldName, ordinal, format, label, field);
        }

        if (fieldClass == Integer.class) {
            return new FieldWriterInt32Field(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == Long.class) {
            return new FieldWriterInt64Field(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == Short.class) {
            return new FieldWriterInt16Field(fieldName, ordinal, features, format, label, field, fieldClass);
        }

        if (fieldClass == Byte.class) {
            return new FieldWriterInt8Field(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == BigInteger.class) {
            return new FieldWriterBigIntField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == BigDecimal.class) {
            return new FieldWriterBigDecimalField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == java.util.Date.class) {
            if (format != null) {
                format = format.trim();

                if (format.isEmpty()) {
                    format = null;
                }
            }

            return new FieldWriterDateField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass == String.class) {
            return new FieldWriterStringField(fieldName, ordinal, features, format, label, field);
        }

        if (fieldClass.isEnum()) {
            Member enumValueField = BeanUtils.getEnumValueField(fieldClass);
            if (enumValueField == null) {
                return new FIeldWriterEnumField(fieldName, ordinal, features, format, label, fieldClass, field);
            }
        }

        if (fieldClass == List.class || fieldClass == ArrayList.class) {
            Type itemType = null;
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            }
            return new FieldWriterListField(fieldName, itemType, ordinal, features, format, label, fieldType, fieldClass, field);
        }

        if (fieldClass.isArray()) {
            Class<?> itemClass = fieldClass.getComponentType();

            if (declaringClass == Throwable.class && "stackTrace".equals(fieldName)) {
                try {
                    Method method = Throwable.class.getMethod("getStackTrace");
                    return new FieldWriterObjectArrayMethod(fieldName, itemClass, ordinal, features, format, label, fieldType, fieldClass, method);
                } catch (NoSuchMethodException ignored) {
                }
            }

            boolean base64 = fieldClass == byte[].class && "base64".equals(format);
            if (!base64) {
                return new FieldWriterObjectArrayField(fieldName, itemClass, ordinal, features, format, label, fieldType, fieldClass, field);
            }
        }

        return new FieldWriterObjectFieldUF(fieldName, ordinal, features, format, label, field.getGenericType(), fieldClass, field);
    }

    void genGetObject(MethodWriterContext mwc, FieldWriter fieldWriter, int OBJECT) {
        MethodWriter mw = mwc.mw;
        Class objectClass = mwc.objectClass;
        final String TYPE_OBJECT = ASMUtils.type(objectClass);
        Class fieldClass = fieldWriter.getFieldClass();
        Member member = fieldWriter.getFieldOrMethod();

        if (member instanceof Method) {
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT, member.getName(), "()" + ASMUtils.desc(fieldClass), false);
            return;
        }

        if (Modifier.isPublic(objectClass.getModifiers())
                && Modifier.isPublic(member.getModifiers())
                && !classLoader.isExternalClass(objectClass)
        ) {
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
            mw.visitFieldInsn(Opcodes.GETFIELD, TYPE_OBJECT, member.getName(), ASMUtils.desc(fieldClass));
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

        mw.visitFieldInsn(Opcodes.GETSTATIC, ObjectWriterCreatorASMUtils.TYPE_UNSAFE_UTILS, "UNSAFE", "Lsun/misc/Unsafe;");
        mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
        mw.visitLdcInsn(
                UnsafeUtils.objectFieldOffset(field));
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/misc/Unsafe", methodName, methodDes, false);
        if (castToType != null) {
            mw.visitTypeInsn(Opcodes.CHECKCAST, castToType);
        }
    }

    private void genGetFieldReader(
            List<FieldWriter> fieldWriters,
            ClassWriter cw,
            String classNameType,
            ObjectWriterAdapter objectReaderAdapter) {
        MethodWriter mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "getFieldWriter",
                "(J)" + DESC_FIELD_WRITER
        );

        final int HASH_CODE_64 = 1;
        final int HASH_CODE_32 = 3;

        Label rtnlt = new Label();
        if (fieldWriters.size() <= 6) {
            for (int i = 0; i < fieldWriters.size(); ++i) {
                Label next_ = new Label(), get_ = new Label();
                String fieldName = fieldWriters.get(i).getFieldName();
                long hashCode64 = Fnv.hashCode64(fieldName);

                mw.visitVarInsn(Opcodes.LLOAD, HASH_CODE_64);
                mw.visitLdcInsn(hashCode64);
                mw.visitInsn(Opcodes.LCMP);
                mw.visitJumpInsn(Opcodes.IFNE, next_);

                mw.visitLabel(get_);
                mw.visitVarInsn(Opcodes.ALOAD, THIS);
                mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(i), DESC_FIELD_WRITER);
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
                    mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(index), DESC_FIELD_WRITER);
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

    static class MethodWriterContext {
        final Class objectClass;
        final long objectFeatures;
        final String classNameType;
        final MethodWriter mw;
        final Map<Object, Integer> variants = new LinkedHashMap<>();
        final boolean jsonb;
        int maxVariant;

        public MethodWriterContext(
                Class objectClass,
                long objectFeatures,
                String classNameType,
                MethodWriter mw,
                int maxVariant,
                boolean jsonb
        ) {
            this.objectClass = objectClass;
            this.objectFeatures = objectFeatures;
            this.classNameType = classNameType;
            this.mw = mw;
            this.jsonb = jsonb;
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

        void genVariantsMethodBefore() {
            Label notDefault_ = new Label(), end_ = new Label();

            mw.visitVarInsn(Opcodes.ALOAD, JSON_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_JSON_WRITER, "getFeatures", "()J", false);
            mw.visitVarInsn(Opcodes.LSTORE, var2(CONTEXT_FEATURES));

            genIsEnabled(JSONWriter.Feature.NotWriteDefaultValue.mask);
            mw.visitInsn(Opcodes.DUP);
            mw.visitVarInsn(Opcodes.ISTORE, var(NOT_WRITE_DEFAULT_VALUE));
            mw.visitJumpInsn(Opcodes.IFEQ, notDefault_);

            mw.visitInsn(Opcodes.ICONST_0);
            mw.visitVarInsn(Opcodes.ISTORE, var(WRITE_NULLS));
            mw.visitInsn(Opcodes.LCONST_0);
            mw.visitVarInsn(Opcodes.LSTORE, var2(CONTEXT_FEATURES));
            mw.visitJumpInsn(Opcodes.GOTO, end_);

            mw.visitLabel(notDefault_);

            long features = JSONWriter.Feature.WriteNulls.mask
                    | JSONWriter.Feature.NullAsDefaultValue.mask
                    | JSONWriter.Feature.WriteNullBooleanAsFalse.mask
                    | JSONWriter.Feature.WriteNullStringAsEmpty.mask
                    | JSONWriter.Feature.WriteNullNumberAsZero.mask
                    | JSONWriter.Feature.WriteNullListAsEmpty.mask;
            genIsEnabled(features);
            mw.visitVarInsn(Opcodes.ISTORE, var(WRITE_NULLS));

            mw.visitLabel(end_);
        }

        void genIsEnabled(long features, Label elseLabel) {
            mw.visitVarInsn(Opcodes.LLOAD, var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(features);
            mw.visitInsn(Opcodes.LAND);
            mw.visitInsn(Opcodes.LCONST_0);
            mw.visitInsn(Opcodes.LCMP);
            mw.visitJumpInsn(Opcodes.IFEQ, elseLabel);
        }

        void genIsEnabled(long features) {
            Label L1 = new Label(), L2 = new Label();
            mw.visitVarInsn(Opcodes.LLOAD, var2(CONTEXT_FEATURES));
            mw.visitLdcInsn(features);
            mw.visitInsn(Opcodes.LAND);
            mw.visitInsn(Opcodes.LCONST_0);
            mw.visitInsn(Opcodes.LCMP);
            mw.visitJumpInsn(Opcodes.IFEQ, L1);
            mw.visitInsn(Opcodes.ICONST_1);
            mw.visitJumpInsn(Opcodes.GOTO, L2);
            mw.visitLabel(L1);
            mw.visitInsn(Opcodes.ICONST_0);
            mw.visitLabel(L2);
        }

        private void loadFieldType(int fieldIndex, Type fieldType) {
            if (fieldType instanceof Class && fieldType.getTypeName().startsWith("java")) {
                mw.visitLdcInsn((Class) fieldType);
                return;
            }
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitFieldInsn(Opcodes.GETFIELD, classNameType, fieldWriter(fieldIndex), DESC_FIELD_WRITER);
            mw.visitMethodInsn(Opcodes.INVOKEINTERFACE, TYPE_FIELD_WRITER, "getFieldType", "()Ljava/lang/reflect/Type;", true);
        }
    }
}
