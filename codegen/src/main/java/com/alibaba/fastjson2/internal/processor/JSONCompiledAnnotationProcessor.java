package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.internal.asm.Label;
import com.alibaba.fastjson2.internal.codegen.Block;
import com.alibaba.fastjson2.internal.codegen.ClassWriter;
import com.alibaba.fastjson2.internal.codegen.MethodWriter;
import com.alibaba.fastjson2.internal.codegen.Opcodes;
import com.alibaba.fastjson2.reader.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.alibaba.fastjson2.internal.codegen.Opcodes.*;
import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.fieldObjectReader;
import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.fieldReader;
import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "com.alibaba.fastjson2.annotation.JSONCompiled",
        "com.alibaba.fastjson2.annotation.JSONBuilder",
        "com.alibaba.fastjson2.annotation.JSONCreator",
        "com.alibaba.fastjson2.annotation.JSONField",
        "com.alibaba.fastjson2.annotation.JSONType"
})
public class JSONCompiledAnnotationProcessor
        extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || annotations.isEmpty()) {
            return false;
        }

        Analysis analysis = new Analysis(processingEnv);
        Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.jsonCompiledElement);

        if (!compiledJsons.isEmpty()) {
            analysis.processAnnotation(analysis.compiledJsonType, compiledJsons);
        }

        Map<String, StructInfo> structs = analysis.analyze();
        final Map<String, StructInfo> generatedFiles = new HashMap<>();
        final List<Element> originatingElements = new ArrayList<>();
//        Set<? extends Element> jsonConverters = roundEnv.getElementsAnnotatedWith(analysis.converterElement);
//        Map<String, Element> configurations = analysis.processConverters(jsonConverters);

        for (Map.Entry<String, StructInfo> entry : structs.entrySet()) {
            String typeName = entry.getKey();
            StructInfo info = entry.getValue();

            String classNamePath = findConverterName(info);
            try {
                JavaFileObject converterFile = processingEnv.getFiler().createSourceFile(classNamePath, info.element);
                try (Writer writer = converterFile.openWriter()) {
                    buildCode(writer, processingEnv, entry.getKey(), info, structs);
                    generatedFiles.put(classNamePath, info);
                    originatingElements.add(info.element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Failed saving compiled json serialization file " + classNamePath);
                }
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Failed creating compiled json serialization file " + classNamePath);
            }
        }

        return false;
    }

    static String findConverterName(StructInfo structInfo) {
        int dotIndex = structInfo.binaryName.lastIndexOf('.');
        String className = structInfo.binaryName.substring(dotIndex + 1);
        if (dotIndex == -1) {
            return className + "_FASTJOSNReader";
        }
        String packageName = structInfo.binaryName.substring(0, dotIndex);
        return packageName + '.' + className + "_FASTJOSNReader";
    }

    private static void buildCode(
            final Writer code,
            final ProcessingEnvironment environment,
            final String className,
            final StructInfo si,
            final Map<String, StructInfo> structs
    ) throws IOException {
        final String generateFullClassName = findConverterName(si);
        final int dotIndex = generateFullClassName.lastIndexOf('.');
        final String generateClassName = generateFullClassName.substring(dotIndex + 1);

        String packageName = null;
        if (dotIndex != -1) {
            packageName = generateFullClassName.substring(0, dotIndex);
        }

        List<AttributeInfo> fields = si.getReaderAttributes();

        Class supperClass = CodeGenUtils.getSupperClass(fields.size());
        ClassWriter cw = new ClassWriter(packageName, generateClassName, supperClass, new Class[0]);

        final boolean generatedFields = fields.size() < 128;
        if (generatedFields) {
            genFields(fields, cw, supperClass);
        }

        {
            MethodWriter mw = cw.method(
                    Modifier.PUBLIC,
                    "<init>",
                    void.class,
                    new Class[]{},
                    new String[]{}
            );

            Op[] fieldReaders = new Op[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                AttributeInfo attr = fields.get(i);
                String fieldType = attr.type.toString();
                boolean fieldTypeIsClass = fieldType.indexOf('<') == -1;
                Op fieldReader;
                if (attr.setMethod != null) {
                    String methodName = attr.setMethod.getSimpleName().toString();
                    fieldReader = invokeStatic(
                            ObjectReaders.class,
                            "fieldReaderWithMethod",
                            ldc(attr.name),
                            getStatic(className, "class"),
                            ldc(methodName)
                    );
                } else if (attr.field != null) {
                    String fieldName = attr.field.getSimpleName().toString();
                    if (fieldName.equals(attr.name)) {
                        fieldReader = invokeStatic(
                                ObjectReaders.class,
                                "fieldReaderWithField",
                                ldc(fieldName),
                                getStatic(className, "class")
                        );
                    } else {
                        fieldReader = invokeStatic(
                                ObjectReaders.class,
                                "fieldReaderWithField",
                                ldc(attr.name),
                                getStatic(className, "class"),
                                ldc(fieldName)
                        );
                    }

                    fieldReaders[i] = fieldReader;
                } else {
                    throw new IOException("TODO");
                }
                fieldReaders[i] = fieldReader;
            }
            mw.invoke(SUPER,
                    "<init>",
                    getStatic(className, "class"),
                    Opcodes.ldc(si.typeKey),
                    Opcodes.ldc(null),
                    Opcodes.ldc(si.readerFeatures),
                    Opcodes.methodRef(className, "new"),
                    Opcodes.ldc(null),
                    Opcodes.allocateArray(FieldReader.class, null, fieldReaders)
            );

            genInitFields(fields.size(), generatedFields, "fieldReaders", mw, supperClass);
        }

        {
            MethodWriter mw = cw.method(
                    Modifier.PUBLIC,
                    "createInstance",
                    Object.class,
                    new Class[]{long.class},
                    new String[]{"features"}
            );

            mw.ret(
                    allocate(className)
            );
        }

        genMethodReadObject(className, si, fields, cw, false);

        code.write(cw.toString());
    }

    static void genInitFields(
            int fieldReaderArray,
            boolean generatedFields,
            String fieldReaders,
            MethodWriter mw,
            Class objectReaderSuper
    ) {
        if (objectReaderSuper != ObjectReaderAdapter.class || !generatedFields) {
            return;
        }

        for (int i = 0; i < fieldReaderArray; i++) {
            mw.putField(fieldReader(i), Opcodes.arrayGet(var(fieldReaders), Opcodes.ldc(i)));
        }
    }

    static void genFields(List<AttributeInfo> fields, ClassWriter cw, Class objectReaderSuper) {
        int fieldCount = fields.size();
        if (objectReaderSuper == ObjectReaderAdapter.class) {
            for (int i = 0; i < fieldCount; i++) {
                cw.field(Modifier.PUBLIC, fieldReader(i), FieldReader.class);
            }

            for (int i = 0; i < fieldCount; i++) {
                cw.field(Modifier.PUBLIC, fieldObjectReader(i), ObjectReader.class);
            }
        }

        for (int i = 0; i < fields.size(); i++) {
            AttributeInfo field = fields.get(i);
            String fieldType = field.type.toString();

            if (fieldType.startsWith("java.util.List<")
                    || fieldType.startsWith("java.util.Map<java.lang.String,")
            ) {
                cw.field(Modifier.PUBLIC, CodeGenUtils.fieldItemObjectReader(i), ObjectReader.class);
            }
        }
    }

    public static void genMethodReadObject(
            String className,
            StructInfo si,
            List<AttributeInfo> fields,
            ClassWriter cw,
            boolean jsonb
    ) {
        int fieldNameLengthMin = 0, fieldNameLengthMax = 0;
        for (int i = 0; i < fields.size(); ++i) {
            String fieldName = fields.get(i).name;

            int fieldNameLength = fieldName.getBytes(StandardCharsets.UTF_8).length;
            if (i == 0) {
                fieldNameLengthMin = fieldNameLength;
                fieldNameLengthMax = fieldNameLength;
            } else {
                fieldNameLengthMin = Math.min(fieldNameLength, fieldNameLengthMin);
                fieldNameLengthMax = Math.max(fieldNameLength, fieldNameLengthMax);
            }
        }

        MethodWriter mw = cw.method(
                Modifier.PUBLIC,
                "readObject",
                Object.class,
                new Class[]{JSONReader.class, Type.class, Object.class, long.class},
                new String[]{"jsonReader", "fieldType", "fieldName", "features"}
        );

        Op jsonReader = var("jsonReader");
        Op features = var("features");
        Op fieldType = var("fieldType");
        Op fieldName = var("fieldName");
        OpName object = var("object");

        mw.ifStmt(invoke(jsonReader, "nextIfNull"))
                .ret(Opcodes.ldc(null));

        mw.newLine();
        mw.stmt(invoke(jsonReader, "nextIfObjectStart"));

        OpName features2 = var("features2");
        mw.declare(long.class, features2, bitOr(var("features"), getField(THIS, "features")));
        mw.newLine();

        mw.declare(className, object, allocate(className));
        mw.newLine();

        String forLabel = null;
        if (fields.size() > 6) {
            forLabel = "_while";
            mw.label(forLabel);
        }

        Block.WhileStmt whileStmt = mw.whileStmtStmt(
                not(invoke(jsonReader, "nextIfObjectEnd"))
        );

        if (fieldNameLengthMin >= 2 && fieldNameLengthMax <= 43) {
            genRead243(si, whileStmt, forLabel, fields, jsonReader, object);
        }

        OpName hashCode64 = var("hashCode64");
        whileStmt.declare(long.class, hashCode64, invoke(jsonReader, "readFieldNameHashCode"));
        whileStmt.newLine();

        if (fields.size() <= 6) {
            for (int i = 0; i < fields.size(); ++i) {
                AttributeInfo field = fields.get(i);
                long fieldNameHash = field.nameHashCode;
                Block.IfStmt ifStmt = whileStmt.ifStmt(eq(hashCode64, ldc(fieldNameHash)));
                genReadFieldValue(ifStmt, i, si, field, jsonReader, object, forLabel, jsonb);
                ifStmt.continueStmt();
                whileStmt.newLine();
            }
        } else {
            Map<Integer, List<Long>> map = new TreeMap();
            Map<Long, AttributeInfo> mapping = new TreeMap();
            Map<Long, Integer> mappingIndex = new TreeMap();

            for (int i = 0; i < fields.size(); i++) {
                AttributeInfo field = fields.get(i);
                long fieldNameHash = field.nameHashCode;
                int hashCode32 = (int) (fieldNameHash ^ (fieldNameHash >>> 32));
                List<Long> hashCode64List = map.computeIfAbsent(hashCode32, k -> new ArrayList<>());
                hashCode64List.add(fieldNameHash);
                mapping.put(fieldNameHash, field);
                mappingIndex.put(fieldNameHash, i);
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
            OpName hashCode32 = var("hashCode32");
            whileStmt.declare(int.class, hashCode32, cast(eor(hashCode64, urs(hashCode64, ldc(32))), int.class));
            Block.SwitchStmt switchStmt = whileStmt.switchStmt(hashCode32, hashCode32Keys);
            for (int i = 0; i < switchStmt.labels(); i++) {
                Block label = switchStmt.label(i);
                List<Long> hashCode64Array = map.get(switchStmt.labelKey(i));

                if (hashCode64Array.size() == 1 && hashCode64Array.get(0) == hashCode32Keys[i]) {
                    Long fieldNameHash = hashCode64Array.get(0);
                    int index = mappingIndex.get(fieldNameHash);
                    AttributeInfo field = mapping.get(fieldNameHash);
                    genReadFieldValue(label, index, si, field, jsonReader, object, forLabel, jsonb);
                    label.continueStmt(forLabel);
                } else {
                    for (int j = 0; j < hashCode64Array.size(); ++j) {
                        Long fieldNameHash = hashCode64Array.get(j);
                        int index = mappingIndex.get(fieldNameHash);
                        AttributeInfo field = mapping.get(fieldNameHash);
                        Block.IfStmt ifStmt = label.ifStmt(eq(hashCode64, ldc(fieldNameHash)));
                        genReadFieldValue(ifStmt, index, si, field, jsonReader, object, forLabel, jsonb);
                        ifStmt.continueStmt(forLabel);
                    }
                    label.breakStmt();
                }
            }
            whileStmt.newLine();
        }

        if (si.smartMatch) {
            whileStmt.ifStmt(invoke(THIS, "readFieldValueWithLCase", jsonReader, object, hashCode64, features2))
                    .continueStmt(forLabel);
            whileStmt.newLine();
        }

        whileStmt.stmt(invoke(null, "processExtra", jsonReader, object));

        mw.newLine();
        mw.invoke(jsonReader, "nextIfComma");

        mw.newLine();
        mw.ret(object);
    }

    private static void genRead243(
            StructInfo info,
            Block.WhileStmt whileStmt,
            String forLabel,
            List<AttributeInfo> fields,
            Op jsonReader,
            Op object
    ) {
        IdentityHashMap<AttributeInfo, Integer> readerIndexMap = new IdentityHashMap<>();
        Map<Integer, List<AttributeInfo>> name0Map = new TreeMap<>();
        for (int i = 0; i < fields.size(); ++i) {
            AttributeInfo field = fields.get(i);
            readerIndexMap.put(field, i);

            byte[] fieldName = field.name.getBytes(StandardCharsets.UTF_8);
            byte[] name0Bytes = new byte[4];
            name0Bytes[0] = '"';
            if (fieldName.length == 2) {
                System.arraycopy(fieldName, 0, name0Bytes, 1, 2);
                name0Bytes[3] = '"';
            } else {
                System.arraycopy(fieldName, 0, name0Bytes, 1, 3);
            }

            int name0 = UNSAFE.getInt(name0Bytes, ARRAY_BYTE_BASE_OFFSET);

            List<AttributeInfo> fieldReaders = name0Map.get(name0);
            if (fieldReaders == null) {
                fieldReaders = new ArrayList<>();
                name0Map.put(name0, fieldReaders);
            }
            fieldReaders.add(field);
        }

        int[] switchKeys = new int[name0Map.size()];
        Label[] labels = new Label[name0Map.size()];
        {
            Iterator it = name0Map.keySet().iterator();
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
                switchKeys[i] = (Integer) it.next();
            }
        }

        Op getRawInt = invoke(jsonReader, "getRawInt");
        Block.SwitchStmt switchStmt = whileStmt.switchStmt(getRawInt, switchKeys);

        for (int i = 0; i < labels.length; i++) {
            int name0 = switchKeys[i];

            Block label = switchStmt.label(i);

            List<AttributeInfo> fieldReaders = name0Map.get(name0);
            for (int j = 0; j < fieldReaders.size(); j++) {
                AttributeInfo fieldReader = fieldReaders.get(j);
                int fieldReaderIndex = readerIndexMap.get(fieldReader);
                byte[] fieldName = fieldReader.name.getBytes(StandardCharsets.UTF_8);
                int fieldNameLength = fieldName.length;
                Opcodes.Op nextIfMatch;
                switch (fieldNameLength) {
                    case 2:
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match2");
                        break;
                    case 3:
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match3");
                        break;
                    case 4:
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match4", ldc(fieldName[3]));
                        break;
                    case 5: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match5", ldc(name1));
                        break;
                    }
                    case 6: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = fieldName[5];
                        bytes4[3] = '"';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match6", ldc(name1));
                        break;
                    }
                    case 7: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match7", ldc(name1));
                        break;
                    }
                    case 8: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match8", ldc(name1), ldc(fieldName[7]));
                        break;
                    }
                    case 9: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match9", ldc(name1));
                        break;
                    }
                    case 10: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match10", ldc(name1));
                        break;
                    }
                    case 11: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match11", ldc(name1));
                        break;
                    }
                    case 12: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match12", ldc(name1), ldc(fieldName[11]));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match13", ldc(name1), ldc(name2));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match14", ldc(name1), ldc(name2));
                        break;
                    }
                    case 15: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match15", ldc(name1), ldc(name2));
                        break;
                    }
                    case 16: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match16", ldc(name1), ldc(name2), ldc(fieldName[15]));
                        break;
                    }
                    case 17: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match17", ldc(name1), ldc(name2));
                        break;
                    }
                    case 18: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match18", ldc(name1), ldc(name2));
                        break;
                    }
                    case 19: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match19", ldc(name1), ldc(name2));
                        break;
                    }
                    case 20: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match20", ldc(name1), ldc(name2), ldc(fieldName[19]));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match21", ldc(name1), ldc(name2), ldc(name3));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match22", ldc(name1), ldc(name2), ldc(name3));
                        break;
                    }
                    case 23: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match23", ldc(name1), ldc(name2), ldc(name3));
                        break;
                    }
                    case 24: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match24", ldc(name1), ldc(name2), ldc(name3), ldc(fieldName[23]));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match25", ldc(name1), ldc(name2), ldc(name3));
                        break;
                    }
                    case 26: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 19, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name3 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match26", ldc(name1), ldc(name2), ldc(name3));
                        break;
                    }
                    case 27: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match27", ldc(name1), ldc(name2), ldc(name3));
                        break;
                    }
                    case 28: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match28", ldc(name1), ldc(name2), ldc(name3));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match29", ldc(name1), ldc(name2), ldc(name3), ldc(name4));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match30", ldc(name1), ldc(name2), ldc(name3), ldc(name4));
                        break;
                    }
                    case 31: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match31", ldc(name1), ldc(name2), ldc(name3), ldc(name4));
                        break;
                    }
                    case 32: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match32", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(fieldName[31]));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match33", ldc(name1), ldc(name2), ldc(name3), ldc(name4));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match34", ldc(name1), ldc(name2), ldc(name3), ldc(name4));
                        break;
                    }
                    case 35: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match35", ldc(name1), ldc(name2), ldc(name3), ldc(name4));
                        break;
                    }
                    case 36: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match36", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(fieldName[35]));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match37", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(name5));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match38", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(name5));
                        break;
                    }
                    case 39: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match39", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(name5));
                        break;
                    }
                    case 40: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match40", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(name5), ldc(fieldName[39]));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match41", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(name5));
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
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match42", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(name5));
                        break;
                    }
                    case 43: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        long name5 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMatch = invoke(jsonReader, "nextIfName4Match43", ldc(name1), ldc(name2), ldc(name3), ldc(name4), ldc(name5));
                        break;
                    }
                    default:
                        throw new IllegalStateException("fieldNameLength " + fieldNameLength);
                }

                Block.IfStmt nextIfMatchStmt = label.ifStmt(nextIfMatch);
                genReadFieldValue(nextIfMatchStmt, fieldReaderIndex, info, fieldReader, jsonReader, object, forLabel, false);
                nextIfMatchStmt.continueStmt(forLabel);
                label.breakStmt();
            }
        }
    }

    static void genReadFieldValue(
            Block mw,
            int i,
            StructInfo info,
            AttributeInfo field,
            Op jsonReader,
            Op object,
            String continueLabel,
            boolean jsonb
    ) {
        Op value = null;
        String type = field.type.toString();

        boolean referenceDetect = info.referenceDetect;
        if (referenceDetect) {
            referenceDetect = CodeGenUtils.isReference(type);
        }

        if (referenceDetect) {
            Block.IfStmt isRef = mw.ifStmt(invoke(jsonReader, "isReference"));

            OpName ref = var("ref");
            isRef.declare(String.class, ref, invoke(jsonReader, "readReference"));
            isRef.stmt(invoke(var(CodeGenUtils.fieldReader(i)), "addResolveTask", jsonReader, object, ref));
            isRef.continueStmt(continueLabel);
            mw.newLine();
        }

        String readDirectMethod = CodeGenUtils.getReadDirectMethod(type);
        if (readDirectMethod != null) {
            value = Opcodes.invoke(jsonReader, readDirectMethod);
        } else {
            String fieldName = field.name;
            OpName fieldValue = var(fieldName);
            mw.declare(type, fieldValue);

            boolean list = type.startsWith("java.util.List<");
            boolean mapStr = type.startsWith("java.util.Map<java.lang.String,");

            if (list) {
                value = genReadFieldValueList(mw, i, jsonReader, type, fieldName, value, fieldValue, referenceDetect);
            } else if (mapStr) {
                genReadFieldValueMap(mw, i, jsonReader, type, fieldName, fieldValue, referenceDetect);
                value = fieldValue;
            }

            if (value == null) {
                mw.ifNull(var(CodeGenUtils.fieldObjectReader(i)))
                        .putField(
                                CodeGenUtils.fieldObjectReader(i),
                                invoke(var(CodeGenUtils.fieldReader(i)), "getObjectReader", jsonReader)
                        );
                mw.stmt(
                        assign(
                                fieldValue,
                                cast(
                                        invoke(
                                                var(fieldObjectReader(i)),
                                                jsonb ? "readJSONBObject" : "readObject",
                                                jsonReader,
                                                getField(var(fieldReader(i)), "fieldType"),
                                                ldc(field.name),
                                                ldc(field.readerFeatures)
                                        ),
                                        type
                                )
                        )
                );
            }

            value = fieldValue;
        }

        if (field.setMethod != null) {
            mw.invoke(object, field.setMethod.getSimpleName().toString(), value);
        } else if (field.field != null) {
            mw.putField(object, field.field.getSimpleName().toString(), value);
        } else {
            throw new JSONException("TODO");
        }
    }

    private static Op genReadFieldValueList(
            Block mw,
            int i,
            Op jsonReader,
            String type,
            String fieldName,
            Op value,
            OpName fieldValue,
            boolean referenceDetect
    ) {
        String itemType = type.substring(15, type.length() - 1);
        boolean itemTypeIsClass = itemType.indexOf('<') == -1;
        if (itemTypeIsClass) {
            Block.IfStmt nextIfNull = mw.ifStmt(invoke(jsonReader, "nextIfNull"));
            nextIfNull.stmt(assign(fieldValue, ldc(null)));
            Block nextIfNullElse = nextIfNull.elseStmt();
            nextIfNullElse.stmt(assign(fieldValue, allocate(ArrayList.class)));

            String readDirectMethod = CodeGenUtils.getReadDirectMethod(itemType);
            OpName itemReader = var(CodeGenUtils.fieldItemObjectReader(i));
            if (readDirectMethod == null) {
                nextIfNullElse.ifNull(itemReader).stmt(
                        assign(
                                itemReader,
                                invoke(getField(THIS, fieldReader(i)), "getItemObjectReader", jsonReader)
                        )
                );
            }

            if (referenceDetect) {
                referenceDetect = CodeGenUtils.isReference(itemType);
            }

            OpName for_i;
            if (fieldName.equals("i")) {
                for_i = var("j");
            } else {
                for_i = var("i");
            }
            Block.IfStmt nextIfMatch = nextIfNullElse.ifStmt(invoke(jsonReader, "nextIfArrayStart"));
            Block whileStmt;
            if (referenceDetect) {
                whileStmt = nextIfMatch.forStmt(int.class, assign(for_i, ldc(0)), not(invoke(jsonReader, "nextIfArrayEnd")), increment(for_i));
            } else {
                whileStmt = nextIfMatch.whileStmtStmt(not(invoke(jsonReader, "nextIfArrayEnd")));
            }

            Op item;
            if (readDirectMethod != null) {
                item = invoke(jsonReader, readDirectMethod);
            } else {
                item = cast(invoke(itemReader, "readObject", jsonReader, ldc(null), ldc(null), ldc(0L)), itemType);
            }

            if (referenceDetect) {
                OpName listItem = var(fieldName + "_item");

                Block.IfStmt isReference = whileStmt.ifStmt(invoke(jsonReader, "isReference"));
                OpName ref = var("ref");
                isReference.declare(String.class, ref, invoke(jsonReader, "readReference"));
                isReference.invoke(jsonReader, "addResolveTask", fieldValue, for_i, invokeStatic(JSONPath.class, "of", ref));
                isReference.stmt(invoke(fieldValue, "add", ldc(null)));
                isReference.continueStmt();
                whileStmt.newLine();

                whileStmt.declare(itemType, listItem, item);
                item = listItem;
            }

            whileStmt.stmt(invoke(fieldValue, "add", item));

            value = fieldValue;
        }
        return value;
    }

    private static void genReadFieldValueMap(
            Block mw, int i,
            Op jsonReader,
            String type,
            String fieldName,
            OpName fieldValue,
            boolean referenceDetect
    ) {
        // TODO referenceDetect
        String itemType = type.substring(31, type.length() - 1);
        boolean itemTypeIsClass = itemType.indexOf('<') == -1;
//                    if (itemTypeIsClass) {
//                    }
        Block.IfStmt nextIfNull = mw.ifStmt(invoke(jsonReader, "nextIfNull"));
        nextIfNull.stmt(assign(fieldValue, ldc(null)));
        Block nextIfNullElse = nextIfNull.elseStmt();
        OpName itemReader = var(CodeGenUtils.fieldItemObjectReader(i));

        boolean readDirect = CodeGenUtils.supportReadDirect(itemType);

        if (!readDirect) {
            nextIfNullElse.ifNull(itemReader).stmt(
                    assign(
                            itemReader,
                            invoke(jsonReader, "getObjectReader", getStatic(itemType, "class"))
                    )
            );
            nextIfNullElse.newLine();
        }

        nextIfNullElse.stmt(assign(fieldValue, allocate(HashMap.class)));

        nextIfNullElse.newLine();
        nextIfNullElse.invoke(jsonReader, "nextIfObjectStart");
        Block.WhileStmt readMapWhile = nextIfNullElse.whileStmtStmt(not(invoke(jsonReader, "nextIfObjectEnd")));

        if (referenceDetect) {
            referenceDetect = CodeGenUtils.isReference(itemType);
        }

        Op mapEntryValue;
        if (readDirect) {
            String method = CodeGenUtils.getReadDirectMethod(itemType);
            mapEntryValue = invoke(jsonReader, method);
        } else {
            mapEntryValue = cast(
                    invoke(itemReader, "readObject", jsonReader, getStatic(itemType, "class"), ldc(fieldName), var("features")),
                    itemType
            );
        }

        Op mapEntryKey = invoke(jsonReader, "readFieldName");

        if (referenceDetect) {
            OpName mapKey = var(fieldName + "_key");
            OpName mapValue = var(fieldName + "_value");

            readMapWhile.declare(String.class, mapKey, mapEntryKey);
            mapEntryKey = mapKey;

            readMapWhile.newLine();
            Block.IfStmt isReference = readMapWhile.ifStmt(invoke(jsonReader, "isReference"));
            OpName ref = var("ref");
            isReference.declare(String.class, ref, invoke(jsonReader, "readReference"));
            isReference.invoke(jsonReader, "addResolveTask", fieldValue, mapKey, invokeStatic(JSONPath.class, "of", ref));
            isReference.continueStmt();
            readMapWhile.newLine();

            readMapWhile.declare(itemType, mapValue, mapEntryValue);
            mapEntryValue = mapValue;
        }

        readMapWhile.invoke(
                fieldValue,
                "put",
                mapEntryKey,
                mapEntryValue
        );
        nextIfNullElse.newLine();
        nextIfNullElse.invoke(jsonReader, "nextIfComma");
    }
}
