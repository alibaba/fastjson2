package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.internal.CodeGenUtils;
import com.alibaba.fastjson2.internal.codegen.Block;
import com.alibaba.fastjson2.internal.codegen.ClassWriter;
import com.alibaba.fastjson2.internal.codegen.MethodWriter;
import com.alibaba.fastjson2.internal.codegen.Opcodes;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.util.BeanUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import static com.alibaba.fastjson2.internal.CodeGenUtils.fieldObjectReader;
import static com.alibaba.fastjson2.internal.CodeGenUtils.fieldReader;
import static com.alibaba.fastjson2.internal.codegen.Opcodes.*;

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
        Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.jsonCompiledEleement);

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

//        final List<String> allConfigurations = new ArrayList<>(configurations.keySet());
//        if (configurationFileName != null) {
//            try {
//                FileObject configFile = processingEnv.getFiler()
//                        .createSourceFile(configurationFileName, originatingElements.toArray(new Element[0]));
//                try (Writer writer = configFile.openWriter()) {
//                    if (!buildRootConfiguration(writer, configurationFileName, generatedFiles, processingEnv))
//                        return false;
//                    allConfigurations.add(configurationFileName);
//                } catch (Exception e) {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
//                            "Failed saving configuration file " + configurationFileName);
//                }
//            } catch (IOException e) {
//                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
//                        "Failed creating configuration file " + configurationFileName);
//            }
//        }

        return false;
    }

    static String findConverterName(StructInfo structInfo) {
        int dotIndex = structInfo.binaryName.lastIndexOf('.');
        String className = structInfo.binaryName.substring(dotIndex + 1);
        if (dotIndex == -1) {
            return className + "_FASTJOSNReader";
        }
        String packageName = structInfo.binaryName.substring(0, dotIndex);
        Package packageClass = Package.getPackage(packageName);
        boolean useDslPackage = packageClass != null && packageClass.isSealed() || structInfo.binaryName.startsWith("java.");
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
            genFields(fields.size(), cw, supperClass);
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
                if (attr.setMethod != null) {
                    fieldReaders[i] = invoke(
                            getStatic(ObjectReaderCreator.class, "INSTANCE"),
                            "createFieldReader",
                            ldc(attr.name),
                            invokeStatic(
                                    BeanUtils.class,
                                    "getSetter",
                                    ldc(ObjectReaderCreator.class),
                                    ldc(attr.field.getSimpleName().toString())
                            )
                    );
                } else if (attr.field != null) {
                    fieldReaders[i] = invoke(
                            getStatic(ObjectReaderCreator.class, "INSTANCE"),
                            "createFieldReader",
                            ldc(attr.name),
                            invokeStatic(
                                    BeanUtils.class,
                                    "getDeclaredField",
                                    ldc(ObjectReaderCreator.class),
                                    ldc(attr.field.getSimpleName().toString())
                            )
                    );
                } else {
                    throw new IOException("TODO");
                }
            }
            mw.invoke(SUPER,
                    "<init>",
                    getStatic(className, "class"),
                    Opcodes.ldc(si.typeKey),
                    Opcodes.ldc(null),
                    Opcodes.ldc(si.readerFeatures),
                    Opcodes.ldc(null),
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

    static void genFields(int attributes, ClassWriter cw, Class objectReaderSuper) {
        if (objectReaderSuper == ObjectReaderAdapter.class) {
            for (int i = 0; i < attributes; i++) {
                cw.field(Modifier.PUBLIC, fieldReader(i), FieldReader.class);
            }

            for (int i = 0; i < attributes; i++) {
                cw.field(Modifier.PUBLIC, fieldObjectReader(i), ObjectReader.class);
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

        Opcodes.Op jsonReader = var("jsonReader");
        Opcodes.Op features = var("features");
        Opcodes.Op fieldType = var("fieldType");
        Opcodes.Op fieldName = var("fieldName");
        Opcodes.OpName object = var("object");

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
                genReadFieldValue(ifStmt, i, field, jsonReader, object, forLabel, jsonb);
//                ifStmt.continueStmt();
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
                for (int j = 0; j < hashCode64Array.size(); ++j) {
                    Long fieldNameHash = hashCode64Array.get(j);
                    int index = mappingIndex.get(fieldNameHash);
                    AttributeInfo field = mapping.get(fieldNameHash);
                    Block.IfStmt ifStmt = label.ifStmt(eq(hashCode64, ldc(fieldNameHash)));
                    genReadFieldValue(ifStmt, index, field, jsonReader, object, forLabel, jsonb);
                    ifStmt.continueStmt(forLabel);
                }
            }
        }

        mw.ret(object);
    }

    static void genReadFieldValue(
            Block mw,
            int i,
            AttributeInfo field,
            Opcodes.Op jsonReader,
            Opcodes.Op object,
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
            default:
                OpName fieldValue = var(field.name);
                mw.declare(type, fieldValue);
                Block.IfStmt isRef = mw.ifStmt(invoke(jsonReader, "isReference"));

                OpName ref = var("ref");
                isRef.declare(String.class, ref, invoke(jsonReader, "readReference"));
                isRef.stmt(invoke(var(CodeGenUtils.fieldReader(i)), "addResolveTask", jsonReader, object, ref));
                isRef.continueStmt(continueLabel);

                Block elseStmt = isRef.elseStmt();
                elseStmt.ifNull(var(CodeGenUtils.fieldObjectReader(i)))
                        .putField(
                                CodeGenUtils.fieldObjectReader(i),
                                invoke(var(CodeGenUtils.fieldReader(i)), "getObjectReader", jsonReader)
                        );

                elseStmt.stmt(
                        assign(
                                fieldValue,
                                cast(
                                        invoke(
                                                var(CodeGenUtils.fieldObjectReader(i)),
                                                jsonb ? "readJSONBObject" : "readObject",
                                                jsonReader,
                                                getField(var(CodeGenUtils.fieldReader(i)), "fieldType"),
                                                ldc(field.name),
                                                ldc(field.readerFeatures)
                                        ),
                                        type
                                )
                        )
                );

//                throw new JSONException("TODO : " + type);
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
