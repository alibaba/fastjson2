package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.internal.CodeGenUtils;
import com.alibaba.fastjson2.internal.codegen.ClassWriter;
import com.alibaba.fastjson2.internal.codegen.MethodWriter;
import com.alibaba.fastjson2.internal.codegen.Opcodes;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;

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
import java.util.*;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.internal.CodeGenUtils.fieldObjectReader;
import static com.alibaba.fastjson2.internal.CodeGenUtils.fieldReader;
import static com.alibaba.fastjson2.internal.codegen.Opcodes.SUPER;
import static com.alibaba.fastjson2.internal.codegen.Opcodes.var;

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
            return String.format("_%s_DslJsonConverter", className);
        }
        String packageName = structInfo.binaryName.substring(0, dotIndex);
        Package packageClass = Package.getPackage(packageName);
        boolean useDslPackage = packageClass != null && packageClass.isSealed() || structInfo.binaryName.startsWith("java.");
        return String.format("%s%s._%s_DslJsonConverter", useDslPackage ? "dsl_json." : "", packageName, className);
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

        Class supperClass = CodeGenUtils.getSupperClass(si.attributes.size());
        ClassWriter cw = new ClassWriter(packageName, generateClassName, supperClass, new Class[0]);

        final boolean generatedFields = si.attributes.size() < 128;
        if (generatedFields) {
            genFields(si.attributes.size(), cw, supperClass);
        }

        {
            MethodWriter mw = cw.method(
                    Modifier.PUBLIC,
                    "<init>",
                    void.class,
                    new Class[]{Class.class, Supplier.class, FieldReader[].class},
                    new String[]{"objectClass", "supplier", "fieldReaders"}
            );
            mw.invoke(SUPER,
                    "<init>",
                    var("objectClass"),
                    Opcodes.ldc(si.typeKey),
                    Opcodes.ldc(null),
                    Opcodes.ldc(si.readerFeatures),
                    Opcodes.ldc(null),
                    var("supplier"),
                    Opcodes.ldc(null),
                    var("fieldReaders")
            );

            genInitFields(si.attributes.size(), generatedFields, "fieldReaders", mw, supperClass);
        }

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
}
