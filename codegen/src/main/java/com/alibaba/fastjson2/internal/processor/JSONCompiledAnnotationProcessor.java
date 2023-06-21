package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
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
import java.util.*;

import static com.alibaba.fastjson2.internal.codegen.Opcodes.*;
import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.fieldObjectReader;
import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.fieldReader;

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
                Block label = switchStmt.lable(i);
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
