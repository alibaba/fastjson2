package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.internal.codegen.Block;
import com.alibaba.fastjson2.internal.codegen.ClassWriter;
import com.alibaba.fastjson2.internal.codegen.MethodWriter;
import com.alibaba.fastjson2.internal.codegen.Opcodes;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.BeanUtils;

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
                    fieldReader = invoke(
                            getStatic(ObjectReaderCreator.class, "INSTANCE"),
                            "createFieldReader",
                            ldc(attr.name),
                            invokeStatic(
                                    BeanUtils.class,
                                    "getSetter",
                                    getStatic(className, "class"),
                                    ldc(methodName)
                            )
                    );
                } else if (attr.field != null) {
                    fieldReader = invoke(
                            getStatic(ObjectReaderCreator.class, "INSTANCE"),
                            "createFieldReader",
                            ldc(attr.name),
                            invokeStatic(
                                    BeanUtils.class,
                                    "getDeclaredField",
                                    getStatic(className, "class"),
                                    ldc(attr.field.getSimpleName().toString())
                            )
                    );

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
            if (fieldType.startsWith("java.util.List<")) {
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

        mw.declare(className, object, allocate(className));

        String forLabel = "_for";
        mw.label(forLabel);
        Block.ForStmt forStmt = mw.forStmt(null, null, null, null);
        forStmt.ifStmt(invoke(jsonReader, "nextIfObjectEnd"))
                .breakStmt();

        OpName hashCode64 = var("hashCode64");
        forStmt.declare(long.class, hashCode64, invoke(jsonReader, "readFieldNameHashCode"));
        forStmt.ifStmt(eq(hashCode64, ldc(0))).breakStmt(null);

        if (fields.size() <= 6) {
            for (int i = 0; i < fields.size(); ++i) {
                AttributeInfo field = fields.get(i);
                long fieldNameHash = field.nameHashCode;
                Block.IfStmt ifStmt = forStmt.ifStmt(eq(hashCode64, ldc(fieldNameHash)));
                genReadFieldValue(ifStmt, i, si, field, jsonReader, object, forLabel, jsonb);
                ifStmt.continueStmt();
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
            forStmt.declare(int.class, hashCode32, cast(eor(hashCode64, urs(hashCode64, ldc(32))), int.class));
            Block.SwitchStmt switchStmt = forStmt.switchStmt(hashCode32, hashCode32Keys);
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
        }

        forStmt.stmt(invoke(null, "processExtra", jsonReader, object));

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
        Op value;
        String type = field.type.toString();
        switch (type) {
            case "boolean":
                value = Opcodes.invoke(jsonReader, "readBoolValue");
                break;
            case "byte":
                value = cast(invoke(jsonReader, "readInt32Value"), byte.class);
                break;
            case "short":
                value = cast(invoke(jsonReader, "readInt32Value"), short.class);
                break;
            case "int":
                value = Opcodes.invoke(jsonReader, "readInt32Value");
                break;
            case "long":
                value = Opcodes.invoke(jsonReader, "readInt64Value");
                break;
            case "float":
                value = Opcodes.invoke(jsonReader, "readFloatValue");
                break;
            case "double":
                value = Opcodes.invoke(jsonReader, "readDoubleValue");
                break;
            case "char":
                value = invoke(jsonReader, "readCharValue");
                break;
            case "int[]":
                value = invoke(jsonReader, "readInt32ValueArray");
                break;
            case "long[]":
                value = invoke(jsonReader, "readInt64ValueArray");
                break;
            case "java.lang.String":
                value = Opcodes.invoke(jsonReader, "readString");
                break;
            case "java.lang.Integer":
                value = invoke(jsonReader, "readInt32");
                break;
            case "java.lang.Long":
                value = invoke(jsonReader, "readInt64");
                break;
            case "java.lang.Float":
                value = invoke(jsonReader, "readFloat");
                break;
            case "java.lang.readDouble":
                value = invoke(jsonReader, "readDouble");
                break;
            case "java.math.BigDecimal":
                value = invoke(jsonReader, "readBigDecimal");
                break;
            case "java.math.BigInteger":
                value = invoke(jsonReader, "readBigInteger");
                break;
            case "java.util.UUID":
                value = invoke(jsonReader, "readUUID");
                break;
            case "java.lang.String[]":
                value = invoke(jsonReader, "readStringArray");
                break;
            case "java.time.LocalDate":
                value = invoke(jsonReader, "readLocalDate");
                break;
            case "java.time.OffsetDateTime":
                value = invoke(jsonReader, "readOffsetDateTime");
                break;
            default:
                if (info.referenceDetect) {
                    Block.IfStmt isRef = mw.ifStmt(invoke(jsonReader, "isReference"));

                    OpName ref = var("ref");
                    isRef.declare(String.class, ref, invoke(jsonReader, "readReference"));
                    isRef.stmt(invoke(var(CodeGenUtils.fieldReader(i)), "addResolveTask", jsonReader, object, ref));
                    isRef.continueStmt(continueLabel);
                }

                OpName fieldValue = var(field.name);
                mw.declare(type, fieldValue);

                boolean list = type.startsWith("java.util.List<");
                if (list) {
                    String itemType = type.substring(15, type.length() - 1);
                    boolean itemTypeIsClass = itemType.indexOf('<') == -1;
                    if (itemTypeIsClass) {
                        Block.IfStmt nextIfNull = mw.ifStmt(invoke(jsonReader, "nextIfNull"));
                        nextIfNull.stmt(assign(fieldValue, ldc(null)));
                        Block nextIfNullElse = nextIfNull.elseStmt();
                        nextIfNullElse.stmt(assign(fieldValue, allocate(ArrayList.class)));
                        boolean stringItemClass = "java.lang.String".equals(itemType);
                        OpName itemReader = var(CodeGenUtils.fieldItemObjectReader(i));
                        if (!stringItemClass) {
                            nextIfNullElse.ifNull(itemReader).stmt(
                                    assign(
                                            itemReader,
                                            invoke(getField(THIS, fieldReader(i)), "getItemObjectReader", jsonReader)
                                    )
                            );
                        }

                        Block.IfStmt nextIfMatch = nextIfNullElse.ifStmt(invoke(jsonReader, "nextIfArrayStart"));
                        Block.WhileStmt whileStmt = nextIfMatch.whileStmtStmt(not(invoke(jsonReader, "nextIfArrayEnd")));
                        Op item;
                        if (stringItemClass) {
                            item = invoke(jsonReader, "readString");
                        } else {
                            item = cast(invoke(itemReader, "readObject", jsonReader, ldc(null), ldc(null), ldc(0L)), itemType);
                        }
                        whileStmt.stmt(invoke(fieldValue, "add", item));

                        value = fieldValue;
                        break;
                    }
                }

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

                value = fieldValue;
                break;
        }

        if (field.setMethod != null) {
            mw.invoke(object, field.setMethod.getSimpleName().toString(), value);
        } else if (field.field != null) {
            mw.putField(object, field.field.getSimpleName().toString(), value);
        } else {
            throw new JSONException("TODO");
        }
    }
}
