package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.internal.codegen.Label;
import com.alibaba.fastjson2.internal.graalmeta.ReflectionMetadata;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.*;
import static com.alibaba.fastjson2.internal.processor.JavacTreeUtils.*;
import static com.alibaba.fastjson2.internal.processor.JavacTreeUtils.qualIdent;
import static com.alibaba.fastjson2.util.JDKUtils.*;

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
    private Messager messager;
    private JavacTrees javacTrees;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        ProcessingEnvironment unwrappedProcessingEnv = unwrapProcessingEnv(processingEnv);
        super.init(unwrappedProcessingEnv);
        this.messager = unwrappedProcessingEnv.getMessager();
        this.javacTrees = JavacTrees.instance(unwrappedProcessingEnv);
        Context context = ((JavacProcessingEnvironment) unwrappedProcessingEnv).getContext();
        this.names = Names.instance(context);
        initialize(TreeMaker.instance(context), names, unwrappedProcessingEnv.getElementUtils());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Analysis analysis = new Analysis(processingEnv);
        Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.jsonCompiledElement);
        if (!compiledJsons.isEmpty()) {
            analysis.processAnnotation(analysis.jsonCompiledDeclaredType, compiledJsons);
        }

        Map<String, StructInfo> structs = analysis.analyze();
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(JSONCompiled.class);
        Set<ReflectionMetadata> reflects = new HashSet<>(elementsAnnotatedWith.size() * 2);
        elementsAnnotatedWith.forEach(element -> {
            StructInfo structInfo = structs.get(element.toString());
            java.util.List<AttributeInfo> attributeInfos = structInfo.getReaderAttributes();
            int fieldsSize = attributeInfos.size();
            Class readSuperClass = getReadSuperClass(fieldsSize);
            Class writeSuperClass = getWriteSuperClass(fieldsSize);

            JCTree tree = javacTrees.getTree(element);
            pos(tree.pos);
            tree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl beanClassDecl) {
                    super.visitClassDef(beanClassDecl);
                    String beanClassFQN = beanClassDecl.sym.toString();
                    if (element.toString().equals(beanClassFQN)) {
                        // initialization
                        String innerReadClassFQN = findConverterName(structInfo, "_FASTJSONReader");
                        String innerReadClassName = innerReadClassFQN.substring(innerReadClassFQN.lastIndexOf('.') + 1);
                        JCTree.JCExpression beanType = null;
                        if (beanClassFQN.contains(".")) {
                            if (element instanceof Symbol.ClassSymbol) {
                                Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
                                String owner = classSymbol.owner.toString();
                                int dotIdx = owner.indexOf(".");
                                beanType = field(dotIdx == -1 ? ident(owner) : qualIdent(owner),
                                        beanClassFQN.substring(beanClassFQN.lastIndexOf(".") + 1));
                            }
                        } else {
                            int dotIdx = beanClassFQN.indexOf(".");
                            beanType = dotIdx == -1 ? ident(beanClassFQN) : qualIdent(beanClassFQN);
                        }
                        JCTree.JCNewClass beanNew = newClass(null, null, beanType, null, null);
                        JCTree.JCIdent objectType = ident("Object");

                        // add deserializer class
                        addInnerClassIfAbsent(beanClassDecl, beanClassFQN, innerReadClassName, "deserializer");

                        // generate inner read class
                        JCTree.JCClassDecl innerReadClass = genInnerClass(innerReadClassName, readSuperClass);

                        // generate fields if necessary
                        final boolean generatedFields = fieldsSize < 128;
                        if (generatedFields) {
                            innerReadClass.defs = innerReadClass.defs.prependList(genFields(attributeInfos, readSuperClass));
                        }

                        // generate constructor
                        innerReadClass.defs = innerReadClass.defs.append(genReadConstructor(beanType, beanNew, attributeInfos, readSuperClass, generatedFields));

                        // generate createInstance
                        innerReadClass.defs = innerReadClass.defs.append(genCreateInstance(objectType, beanNew));

                        // generate readObject
                        innerReadClass.defs = innerReadClass.defs.append(
                                genReadObject(objectType, beanType, beanNew, attributeInfos, structInfo, false));

                        // generate readJSONBObject
                        innerReadClass.defs = innerReadClass.defs.append(
                                genReadObject(objectType, beanType, beanNew, attributeInfos, structInfo, true));

                        // link with inner class
                        beanClassDecl.defs = beanClassDecl.defs.append(innerReadClass);

                        // initialization
                        String innerWriteClassFQN = findConverterName(structInfo, "_FASTJSONWriter");
                        String innerWriteClassName = innerWriteClassFQN.substring(innerWriteClassFQN.lastIndexOf('.') + 1);

                        // add serializer class
                        addInnerClassIfAbsent(beanClassDecl, beanClassFQN, innerWriteClassName, "serializer");

                        // generate inner read class
                        JCTree.JCClassDecl innerWriteClass = genInnerClass(innerWriteClassName, writeSuperClass);

                        // generate fields if necessary
                        if (generatedFields) {
                            innerWriteClass.defs = innerWriteClass.defs.prependList(genFields(attributeInfos, writeSuperClass));
                        }

                        // generate constructor
                        innerWriteClass.defs = innerWriteClass.defs.append(genWriteConstructor(beanType, beanNew, attributeInfos, writeSuperClass, generatedFields));

                        // generate write
                        innerWriteClass.defs = innerWriteClass.defs.append(genWrite(objectType, beanType, beanNew, attributeInfos, structInfo, false));

                        // link with inner class
                        beanClassDecl.defs = beanClassDecl.defs.append(innerWriteClass);

                        // generate source if debug is true
                        if (isDebug(beanClassDecl)) {
                            messager.printMessage(Diagnostic.Kind.WARNING, "Whoops! You have changed the debug of " +
                                    "JSONCompiled from false to true for " + structInfo.binaryName + ", which means the " +
                                    "additional source file being generated. It's usually not recommended to enable debug " +
                                    "until you are in developer mode");
                            genSource(structInfo, javacTrees.getPath(element));
                        }

                        // generate reflect-config.json only for graal
                        if (GRAAL) {
                            ReflectionMetadata readMeta = new ReflectionMetadata();
                            readMeta.setName(innerReadClassFQN);
                            readMeta.setAllPublicConstructors(true);
                            reflects.add(readMeta);
                            ReflectionMetadata writeMeta = new ReflectionMetadata();
                            writeMeta.setName(innerWriteClassFQN);
                            writeMeta.setAllPublicConstructors(true);
                            reflects.add(writeMeta);
                        }
                    }
                }
            });
        });
        if (!reflects.isEmpty()) {
            genReflect(reflects);
        }
        return true;
    }

    private void addInnerClassIfAbsent(JCTree.JCClassDecl beanClassDecl,
                                       String beanClassFQN,
                                       String innerClassName,
                                       String key) {
        JCTree.JCExpression jsonTypeIdent = qualIdent("com.alibaba.fastjson2.annotation.JSONType");
        List<JCTree.JCAnnotation> annotations = beanClassDecl.mods.annotations;
        Optional<JCTree.JCAnnotation> jsonTypeAnnoOpt = annotations.stream()
                .filter(a -> a.getAnnotationType().type.tsym.toString().equals(jsonTypeIdent.type.tsym.toString()))
                .findAny();
        String beanClassName = beanClassFQN.substring(beanClassFQN.lastIndexOf('.') + 1);
        JCTree.JCIdent lhs = ident(key);
        JCTree.JCFieldAccess rhs = field(field(ident(beanClassName), innerClassName), names._class);
        if (jsonTypeAnnoOpt.isPresent()) {
            JCTree.JCAnnotation jsonTypeAnno = jsonTypeAnnoOpt.get();
            Optional<JCTree.JCAssign> jsonTypeAsgOpt = jsonTypeAnno.args.stream()
                    .map(a -> (JCTree.JCAssign) a)
                    .filter(a2 -> key.equals(a2.lhs.toString()))
                    .findAny();
            if (jsonTypeAsgOpt.isPresent()) {
                JCTree.JCAssign deserializerAssign = jsonTypeAsgOpt.get();
                if ("Void.class".equals(deserializerAssign.rhs.toString())) {
                    deserializerAssign.rhs = rhs;
                }
            } else {
                jsonTypeAnno.args = jsonTypeAnno.args.prepend(assign(lhs, rhs));
            }
        } else {
            JCTree.JCAnnotation jsonTypeAnno = annotation(jsonTypeIdent, List.of(assign(lhs, rhs)));
            beanClassDecl.mods.annotations = annotations.prepend(jsonTypeAnno);
        }
    }

    private JCTree.JCClassDecl genInnerClass(String className, Class superClass) {
        if (ObjectReaderAdapter.class.isAssignableFrom(superClass)) {
            return defClass(Flags.PUBLIC | Flags.STATIC | Flags.FINAL,
                    className,
                    null,
                    qualIdent(superClass.getName()),
                    null,
                    null);
        } else if (ObjectWriterAdapter.class.isAssignableFrom(superClass)) {
            return defClass(Flags.PUBLIC | Flags.STATIC | Flags.FINAL,
                    className,
                    null,
                    qualIdent(superClass.getName()),
                    List.of(qualIdent("com.alibaba.fastjson2.writer.ObjectWriter")),
                    null);
        } else {
            return null;
        }
    }

    private List<JCTree> genFields(java.util.List<AttributeInfo> attributeInfos, Class superClass) {
        ListBuffer<JCTree> stmts = new ListBuffer<>();
        int fieldsSize = attributeInfos.size();
        if (ObjectReaderAdapter.class.isAssignableFrom(superClass)) {
            JCTree.JCExpression fieldReaderType = qualIdent(FieldReader.class.getName());
            JCTree.JCExpression objectReaderType = qualIdent(ObjectReader.class.getName());
            if (superClass == ObjectReaderAdapter.class) {
                for (int i = 0; i < fieldsSize; ++i) {
                    stmts.append(defVar(Flags.PRIVATE, fieldReader(i), fieldReaderType));
                }

                for (int i = 0; i < fieldsSize; i++) {
                    stmts.append(defVar(Flags.PRIVATE, fieldObjectReader(i), objectReaderType));
                }
            }

            for (int i = 0; i < fieldsSize; ++i) {
                AttributeInfo field = attributeInfos.get(i);
                String fieldType = field.type.toString();
                if (fieldType.startsWith("java.util.List<") || fieldType.startsWith("java.util.Map<java.lang.String,")) {
                    stmts.append(defVar(Flags.PRIVATE, fieldItemObjectReader(i), objectReaderType));
                }
            }
        } else if (ObjectWriterAdapter.class.isAssignableFrom(superClass)) {
            JCTree.JCExpression fieldWriterType = qualIdent(FieldWriter.class.getName());
            if (superClass == ObjectWriterAdapter.class) {
                for (int i = 0; i < fieldsSize; ++i) {
                    stmts.append(defVar(Flags.PRIVATE, fieldWriter(i), fieldWriterType));
                }
            }
        }
        return stmts.toList();
    }

    private JCTree.JCMethodDecl genReadConstructor(
            JCTree.JCExpression beanType,
            JCTree.JCNewClass beanNew,
            java.util.List<AttributeInfo> fields,
            Class superClass,
            boolean generatedFields) {
        JCTree.JCMemberReference lambda = constructorRef(beanType);
        JCTree.JCExpression fieldReaderType = qualIdent(FieldReader.class.getName());
        ListBuffer<JCTree.JCMethodInvocation> fieldReaders = new ListBuffer<>();
        JCTree.JCExpression objectReadersType = qualIdent(ObjectReaders.class.getName());
        int fieldsSize = fields.size();
        for (int i = 0; i < fieldsSize; ++i) {
            JCTree.JCMethodInvocation readerMethod = genFieldReader(beanType, fields.get(i), objectReadersType);
            if (readerMethod != null) {
                fieldReaders.append(readerMethod);
            }
        }
        JCTree.JCNewArray fieldReadersArray = newArray(fieldReaderType, null, List.from(fieldReaders));
        long features = 0L;
        JCTree.JCMethodInvocation superMethod = method(
                ident(names._super),
                List.of(
                        field(beanType, names._class),
                        defNull(),
                        defNull(),
                        literal(features),
                        lambda,
                        defNull(),
                        fieldReadersArray
                )
        );
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        stmts.append(exec(superMethod));
        // initialize fields if necessary
        if (superClass == ObjectReaderAdapter.class && generatedFields) {
            stmts.appendList(genInitFields(fieldsSize, superClass));
        }
        return defMethod(Flags.PUBLIC, names.init, type(TypeTag.VOID), null, null, null, block(stmts.toList()), null);
    }

    private JCTree.JCMethodDecl genWriteConstructor(
            JCTree.JCExpression beanType,
            JCTree.JCNewClass beanNew,
            java.util.List<AttributeInfo> fields,
            Class superClass,
            boolean generatedFields) {
        JCTree.JCExpression fieldWriterType = qualIdent(FieldWriter.class.getName());
        ListBuffer<JCTree.JCMethodInvocation> fieldWriters = new ListBuffer<>();
        JCTree.JCExpression objectWritersType = qualIdent(ObjectWriters.class.getName());
        int fieldsSize = fields.size();
        for (int i = 0; i < fieldsSize; ++i) {
            JCTree.JCMethodInvocation writerMethod = null;
            AttributeInfo attributeInfo = fields.get(i);
            if (attributeInfo.getMethod != null) {
                writerMethod = writerMethod(beanType, objectWritersType, attributeInfo, true);
            } else if (attributeInfo.field != null) {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                if (fieldName.equals(attributeInfo.name)) {
                    writerMethod = writerMethod(beanType, objectWritersType, attributeInfo, false);
                } else {
                    writerMethod = writerMethod(beanType, objectWritersType, attributeInfo, false);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.WARNING, "not implemented yet");
            }
            if (writerMethod != null) {
                fieldWriters.append(writerMethod);
            }
        }
        JCTree.JCNewArray fieldReadersArray = newArray(fieldWriterType, null, List.from(fieldWriters));
        JCTree.JCMethodInvocation fieldReadersList = method(qualIdent("java.util.Arrays"), "asList", fieldReadersArray);
        JCTree.JCMethodInvocation superMethod = method(
                ident(names._super),
                List.of(
                        field(beanType, names._class),
                        defNull(),
                        defNull(),
                        literal(0L),
                        fieldReadersList
                )
        );
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        stmts.append(exec(superMethod));
        // initialize fields if necessary
        if (superClass == ObjectWriterAdapter.class && generatedFields) {
            stmts.appendList(genInitFields(fields.size(), superClass));
        }
        return defMethod(Flags.PUBLIC, names.init, type(TypeTag.VOID), null, null, null, block(stmts.toList()), null);
    }

    private JCTree.JCMethodInvocation writerMethod(
            JCTree.JCExpression beanType,
            JCTree.JCExpression objectWritersType,
            AttributeInfo attributeInfo,
            boolean isMethodReference) {
        String type = attributeInfo.type.toString();
        if ("boolean".equals(type)
                || "char".equals(type)
                || "byte".equals(type)
                || "short".equals(type)
                || "int".equals(type)
                || "long".equals(type)
                || "float".equals(type)
                || "double".equals(type)) {
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(objectWritersType, "fieldWriter", literal(attributeInfo.name), memberedReference);
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(
                        defVar("bean", beanType),
                        field(ident("bean"), fieldName)
                );
                return method(objectWritersType, "fieldWriter", literal(fieldName), lambda);
            }
        } else if ("java.lang.Class".equals(type)) {
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(
                        objectWritersType,
                        "fieldWriter",
                        literal(attributeInfo.name),
                        field(beanType, names._class),
                        memberedReference
                );
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(
                        defVar("bean", beanType),
                        field(ident("bean"), fieldName)
                );
                return method(
                        objectWritersType,
                        "fieldWriter",
                        literal(fieldName),
                        field(beanType, names._class),
                        lambda
                );
            }
        } else if ("java.lang.Type".equals(type)) {
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(
                        objectWritersType,
                        "fieldWriter",
                        literal(attributeInfo.name),
                        field(beanType, names._class),
                        field(beanType, names._class),
                        memberedReference
                );
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(
                        List.of(
                                defVar("bean", beanType)
                        ),
                        field(ident("bean"), fieldName)
                );
                return method(
                        objectWritersType,
                        "fieldWriter",
                        literal(fieldName),
                        field(beanType, names._class),
                        field(beanType, names._class),
                        lambda
                );
            }
        } else if ("java.util.List<java.lang.String>".equals(type)) {
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(objectWritersType, "fieldWriterListString", literal(attributeInfo.name), memberedReference);
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(
                        defVar("bean", beanType),
                        field(ident("bean"), fieldName)
                );
                return method(objectWritersType, "fieldWriterListString", literal(fieldName), lambda);
            }
        } else if (type.startsWith("java.util.List")
                || type.startsWith("java.util.ArrayList")
                || type.startsWith("java.util.LinkedList")
                || type.startsWith("java.util.concurrent.CopyOnWriteArrayList")) {
            String itemType;
            if (type.contains("<")) {
                itemType = type.substring(type.indexOf("<") + 1, type.length() - 1);
            } else {
                itemType = type;
            }
            JCTree.JCFieldAccess itemTypeClass = field(qualIdent(itemType), names._class);
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(objectWritersType, "fieldWriterList", literal(attributeInfo.name), itemTypeClass, memberedReference);
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(
                        defVar("bean", beanType),
                        field(ident("bean"), fieldName)
                );
                return method(objectWritersType, "fieldWriterList", literal(fieldName), itemTypeClass, lambda);
            }
        } else {
            JCTree.JCFieldAccess fieldClass;
            if (type.contains("[")) {
                fieldClass = field(arrayIdentType(type), names._class);
            } else {
                int idx = type.indexOf("<");
                if (idx != -1) {
                    type = type.substring(0, idx);
                }
                fieldClass = field(identType(type), names._class);
            }
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(objectWritersType, "fieldWriter", literal(attributeInfo.name), fieldClass, memberedReference);
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(
                        defVar("bean", beanType),
                        field(ident("bean"), fieldName)
                );
                return method(objectWritersType, "fieldWriter", literal(fieldName), fieldClass, lambda);
            }
        }
    }

    private JCTree.JCMethodInvocation genFieldReader(
            JCTree.JCExpression beanType,
            AttributeInfo attributeInfo,
            JCTree.JCExpression objectReadersType
    ) {
        if (attributeInfo.setMethod == null && attributeInfo.field == null) {
            messager.printMessage(Diagnostic.Kind.WARNING, "not implemented yet");
            return null;
        }
        JCTree.JCMethodInvocation readerMethod;
        String fieldType = attributeInfo.type.toString();
        JCTree.JCLiteral fieldName = literal(attributeInfo.name);
        JCTree.JCExpression lambda = null;
        if (attributeInfo.setMethod != null) {
            lambda = methodRef(beanType, attributeInfo.setMethod.getSimpleName().toString());
        }

        JCTree.JCExpression identType;
        String methodName = null;
        switch (fieldType) {
            case "byte":
                methodName = "fieldReaderByte";
                identType = type(TypeTag.BYTE);
                break;
            case "short":
                identType = type(TypeTag.SHORT);
                methodName = "fieldReaderShort";
                break;
            case "int":
                identType = type(TypeTag.INT);
                methodName = "fieldReaderInt";
                break;
            case "long":
                identType = type(TypeTag.LONG);
                methodName = "fieldReaderLong";
                break;
            case "char":
                identType = type(TypeTag.CHAR);
                methodName = "fieldReaderChar";
                break;
            case "float":
                identType = type(TypeTag.FLOAT);
                methodName = "fieldReaderFloat";
                break;
            case "double":
                identType = type(TypeTag.DOUBLE);
                methodName = "fieldReaderDouble";
                break;
            case "boolean":
                identType = type(TypeTag.BOOLEAN);
                methodName = "fieldReaderBool";
                break;
            case "java.lang.String":
                identType = qualIdent("java.lang.String");
                methodName = "fieldReaderString";
                break;
            default:
                identType = getFieldValueType(fieldType);
                break;
        }

        if (lambda == null) {
            JCTree.JCVariableDecl object = defVar("o", beanType);
            JCTree.JCExpression valueType = identType;
            if (fieldType.startsWith("java.util.Map<")) {
                valueType = qualIdent("java.util.Map");
            }
            JCTree.JCVariableDecl fieldValue = defVar("v", valueType);
            List<JCTree.JCVariableDecl> args = List.of(object, fieldValue);

            lambda = lambda(
                    args,
                    assign(
                            field(
                                    ident("o"),
                                    attributeInfo.field.getSimpleName().toString()),
                            ident("v")));
        }

        if (methodName != null) {
            return method(objectReadersType, methodName, fieldName, lambda);
        }

        readerMethod = genFieldReaderList(objectReadersType, fieldType, fieldName, lambda);
        if (readerMethod != null) {
            return readerMethod;
        }

        readerMethod = genFieldReaderMap(objectReadersType, fieldType, fieldName, lambda);
        if (readerMethod != null) {
            return readerMethod;
        }

        if (fieldType.indexOf('<') == -1) {
            readerMethod = method(
                    objectReadersType,
                    "fieldReader",
                    fieldName,
                    field(getFieldValueType(fieldType), names._class),
                    lambda
            );
        } else {
            JCTree.JCFieldAccess fieldClassRef = field(beanType, names._class);
            String fieldReaderMethodName;
            List<JCTree.JCExpression> args;
            if (attributeInfo.setMethod != null) {
                fieldReaderMethodName = "fieldReaderWithMethod";
                args = List.of(literal(attributeInfo.name), fieldClassRef, literal(methodName));
            } else {
                JCTree.JCLiteral fieldNameLiteral = literal(attributeInfo.field.getSimpleName().toString());
                if (fieldName.toString().equals(attributeInfo.name)) {
                    args = List.of(fieldNameLiteral, fieldClassRef);
                } else {
                    args = List.of(literal(attributeInfo.name), fieldClassRef, fieldNameLiteral);
                }
                fieldReaderMethodName = "fieldReaderWithField";
            }
            readerMethod = method(objectReadersType, fieldReaderMethodName, args);
        }
        return readerMethod;
    }

    private JCTree.JCMethodInvocation genFieldReaderList(
            JCTree.JCExpression objectReadersType,
            String fieldType,
            JCTree.JCLiteral fieldName,
            JCTree.JCExpression lambda
    ) {
        String listType = null, itemType = null;
        String[] listTypes = {"java.util.List", "java.util.ArrayList"};
        for (String item : listTypes) {
            if (fieldType.startsWith(item)) {
                if (fieldType.length() != item.length()
                        && fieldType.charAt(item.length()) == '<'
                ) {
                    int lastIndex = fieldType.lastIndexOf('>');
                    String temp = fieldType.substring(item.length() + 1, lastIndex);
                    if (temp.indexOf(',') == -1 && temp.indexOf('?') == -1) {
                        listType = item;
                        itemType = temp;
                    }
                    break;
                }
            }
        }

        if (listType == null) {
            return null;
        }

        JCTree.JCMethodInvocation readerMethod;
        if ("java.lang.String".equals(itemType) && "java.util.List".equals(listType)) {
            readerMethod = method(
                    objectReadersType,
                    "fieldReaderListStr",
                    fieldName,
                    lambda
            );
        } else {
            readerMethod = method(
                    objectReadersType,
                    "fieldReaderList",
                    fieldName,
                    field(getFieldValueType(itemType), names._class),
                    constructorRef(qualIdent("java.util.ArrayList")),
                    lambda
            );
        }

        return readerMethod;
    }

    private JCTree.JCMethodInvocation genFieldReaderMap(
            JCTree.JCExpression objectReadersType,
            String fieldType,
            JCTree.JCLiteral fieldName,
            JCTree.JCExpression lambda
    ) {
        String mapType = null, keyType = null, valueType = null;
        String[] mapTypes = {"java.util.Map", "java.util.HashMap", "java.util.TreeMap"};
        for (String item : mapTypes) {
            if (fieldType.startsWith(item)) {
                if (fieldType.length() != item.length()
                        && fieldType.charAt(item.length()) == '<'
                ) {
                    int lastIndex = fieldType.lastIndexOf('>');
                    String keyValueType = fieldType.substring(item.length() + 1, lastIndex);
                    if (keyValueType.indexOf('<') == -1 && keyValueType.indexOf('?') == -1) {
                        int commaIndex = keyValueType.indexOf(',');
                        if (commaIndex == -1) {
                            continue;
                        }

                        keyType = keyValueType.substring(0, commaIndex);
                        valueType = keyValueType.substring(commaIndex + 1);

                        if (keyType.indexOf('<') != -1
                                || keyType.indexOf('?') != -1
                                || valueType.indexOf('<') != -1
                                || valueType.indexOf('?') != -1
                        ) {
                            keyType = null;
                            valueType = null;
                            continue;
                        }

                        mapType = item;
                    }
                    break;
                }
            }
        }

        if (mapType == null) {
            return null;
        }

        return method(
                objectReadersType,
                "fieldReaderMap",
                fieldName,
                field(getFieldValueType(mapType), names._class),
                field(getFieldValueType(keyType), names._class),
                field(getFieldValueType(valueType), names._class),
                lambda
        );
    }

    private List<JCTree.JCStatement> genInitFields(int fieldsSize, Class superClass) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        if (ObjectReaderAdapter.class.isAssignableFrom(superClass)) {
            JCTree.JCFieldAccess fieldReaders = field(ident(names._this), "fieldReaders");
            for (int i = 0; i < fieldsSize; ++i) {
                stmts.append(exec(assign(ident(fieldReader(i)), indexed(fieldReaders, literal(i)))));
            }
        } else if (ObjectWriterAdapter.class.isAssignableFrom(superClass)) {
            JCTree.JCFieldAccess fieldWriters = field(ident(names._this), "fieldWriterArray");
            for (int i = 0; i < fieldsSize; ++i) {
                stmts.append(exec(assign(ident(fieldWriter(i)), cast(qualIdent(FieldWriter.class.getName()), indexed(fieldWriters, literal(i))))));
            }
        }
        return stmts.toList();
    }

    private JCTree.JCMethodDecl genCreateInstance(JCTree.JCIdent objectType, JCTree.JCNewClass beanNew) {
        JCTree.JCVariableDecl featuresVar = defVar("features", TypeTag.LONG);
        return defMethod(Flags.PUBLIC, "createInstance", objectType, List.of(featuresVar), block(defReturn(beanNew)));
    }

    private JCTree.JCMethodDecl genReadObject(
            JCTree.JCIdent objectType,
            JCTree.JCExpression beanType,
            JCTree.JCNewClass beanNew,
            java.util.List<AttributeInfo> attributeInfos,
            StructInfo structInfo,
            boolean isJsonb
    ) {
        int fieldNameLengthMin = 0, fieldNameLengthMax = 0;
        for (int i = 0; i < attributeInfos.size(); ++i) {
            String fieldName = attributeInfos.get(i).name;

            int fieldNameLength = fieldName.getBytes(StandardCharsets.UTF_8).length;
            if (i == 0) {
                fieldNameLengthMin = fieldNameLength;
                fieldNameLengthMax = fieldNameLength;
            } else {
                fieldNameLengthMin = Math.min(fieldNameLength, fieldNameLengthMin);
                fieldNameLengthMax = Math.max(fieldNameLength, fieldNameLengthMax);
            }
        }

        JCTree.JCVariableDecl jsonReaderVar = defVar("jsonReader", qualIdent(JSONReader.class.getName()));
        JCTree.JCIdent jsonReaderIdent = ident(jsonReaderVar);
        JCTree.JCVariableDecl fieldTypeVar = defVar("fieldType", qualIdent(Type.class.getName()));
        JCTree.JCVariableDecl fieldNameVar = defVar("fieldName", objectType);
        JCTree.JCVariableDecl featuresVar = defVar("features", TypeTag.LONG);

        JCTree.JCReturn nullReturn = defReturn(defNull());

        ListBuffer<JCTree.JCStatement> readObjectBody = new ListBuffer<>();

        JCTree.JCMethodInvocation nextIfNullMethod = method(jsonReaderIdent, "nextIfNull");
        readObjectBody.append(defIf(nextIfNullMethod, nullReturn));

        readObjectBody.append(exec(method(jsonReaderIdent, "nextIfObjectStart")));

        JCTree.JCVariableDecl features2Var = defVar(
                Flags.PARAMETER,
                "features2",
                TypeTag.LONG,
                bitOr(
                        ident("features"),
                        field(ident(names._this), "features")
                )
        );
        readObjectBody.append(features2Var);

        JCTree.JCVariableDecl objectVar = defVar("object", beanType, beanNew);
        JCTree.JCIdent objectIdent = ident(objectVar);
        readObjectBody.append(objectVar);

        int fieldsSize = attributeInfos.size();

        JCTree.JCLabeledStatement loopLabel = label("_while");
        JCTree.JCWhileLoop loopHead = whileLoop(unary(JCTree.Tag.NOT, method(jsonReaderIdent, "nextIfObjectEnd")), null);
        ListBuffer<JCTree.JCStatement> loopBody = new ListBuffer<>();

        boolean switchGen = false;
        if (fieldNameLengthMin >= 2 && fieldNameLengthMax <= 43) {
            loopBody.appendList(genRead243(attributeInfos, jsonReaderIdent, structInfo, loopLabel, objectIdent, beanType, isJsonb));
            switchGen = true;
        }

        JCTree.JCFieldAccess readFieldNameHashCode = field(jsonReaderIdent, "readFieldNameHashCode");
        JCTree.JCVariableDecl hashCode64Var = defVar("hashCode64", TypeTag.LONG, method(readFieldNameHashCode));
        JCTree.JCExpression hashCode64 = ident(hashCode64Var);
        loopBody.append(hashCode64Var);

        if (switchGen && !isJsonb) {
            loopBody.append(
                    exec(method(
                            field(ident(names._this), "readFieldValue"),
                            List.of(hashCode64, jsonReaderIdent, ident(features2Var), ident(objectVar))
                    )));
        } else {
            if (fieldsSize <= 6) {
                for (int i = 0; i < fieldsSize; ++i) {
                    AttributeInfo attributeInfo = attributeInfos.get(i);
                    List<JCTree.JCStatement> readFieldValueStmts = genReadFieldValue(
                            attributeInfo,
                            jsonReaderIdent,
                            i,
                            structInfo,
                            loopLabel,
                            objectIdent,
                            beanType,
                            isJsonb);
                    loopBody.appendList(List.of(defIf(eq(hashCode64, attributeInfo.nameHashCode), block(readFieldValueStmts))));
                }
            } else {
                Map<Integer, java.util.List<Long>> map = new TreeMap<>();
                Map<Long, AttributeInfo> mapping = new TreeMap<>();
                Map<Long, Integer> mappingIndex = new TreeMap<>();
                for (int i = 0; i < fieldsSize; ++i) {
                    AttributeInfo attr = attributeInfos.get(i);
                    long fieldNameHash = attr.nameHashCode;
                    int hashCode32 = (int) (fieldNameHash ^ (fieldNameHash >>> 32));
                    java.util.List<Long> hashCode64List = map.computeIfAbsent(hashCode32, k -> new ArrayList<>());
                    hashCode64List.add(fieldNameHash);
                    mapping.put(fieldNameHash, attr);
                    mappingIndex.put(fieldNameHash, i);
                }
                int[] hashCode32Keys = new int[map.size()];
                int off = 0;
                for (Integer key : map.keySet()) {
                    hashCode32Keys[off++] = key;
                }
                Arrays.sort(hashCode32Keys);
                JCTree.JCVariableDecl hashCode32Var = getHashCode32Var(hashCode64);
                loopBody.append(hashCode32Var);
                JCTree.JCExpression hashCode32 = ident(hashCode32Var);

                JCTree.JCLabeledStatement switchLabel = label("_switch");
                ListBuffer<JCTree.JCCase> cases = new ListBuffer<>();
                for (int i = 0; i < hashCode32Keys.length; ++i) {
                    java.util.List<Long> hashCode64Array = map.get(hashCode32Keys[i]);
                    List<JCTree.JCStatement> stmts = List.nil();
                    Long fieldNameHash = null;
                    if (hashCode64Array.size() == 1 && hashCode64Array.get(0) == hashCode32Keys[i]) {
                        fieldNameHash = hashCode64Array.get(0);
                        int index = mappingIndex.get(fieldNameHash);
                        AttributeInfo attributeInfo = mapping.get(fieldNameHash);
                        stmts = stmts.appendList(genReadFieldValue(
                                attributeInfo,
                                jsonReaderIdent,
                                index,
                                structInfo,
                                loopLabel,
                                objectIdent,
                                beanType,
                                isJsonb));
                        stmts.append(defContinue(loopLabel));
                    } else {
                        for (int j = 0; j < hashCode64Array.size(); ++j) {
                            fieldNameHash = hashCode64Array.get(j);
                            int index = mappingIndex.get(fieldNameHash);
                            AttributeInfo field = mapping.get(fieldNameHash);
                            List<JCTree.JCStatement> stmtsIf = genReadFieldValue(
                                    field,
                                    jsonReaderIdent,
                                    index,
                                    structInfo,
                                    loopLabel,
                                    objectIdent,
                                    beanType,
                                    isJsonb);
                            stmts = stmts.append(defIf(eq(hashCode64, fieldNameHash), block(stmtsIf)));
                            stmts.append(defContinue(loopLabel));
                        }
                        stmts.append(defBreak(switchLabel));
                    }
                    cases.append(defCase(getHashCode32Var(literal(fieldNameHash)).getInitializer(), stmts));
                }
                switchLabel.body = defSwitch(hashCode32, cases.toList());
                loopBody.append(switchLabel);
            }

            loopBody.append(
                    defIf(
                            method(
                                    ident(names._this),
                                    "readFieldValueWithLCase",
                                    jsonReaderIdent,
                                    objectIdent,
                                    ident(hashCode64Var),
                                    ident(features2Var)
                            ),
                            defContinue(loopLabel)
                    )
            );
            JCTree.JCFieldAccess processExtraField = field(ident(names._this), "processExtra");
            loopBody.append(exec(method(processExtraField, jsonReaderIdent, objectIdent, ident(features2Var))));
        }

        loopHead.body = block(loopBody.toList());
        loopLabel.body = loopHead;
        readObjectBody.append(loopLabel);

        if (!isJsonb) {
            readObjectBody.append(exec(method(jsonReaderIdent, "nextIfComma")));
        }

        readObjectBody.append(defReturn(objectIdent));

        return defMethod(
                Flags.PUBLIC,
                isJsonb ? "readJSONBObject" : "readObject",
                objectType,
                List.of(jsonReaderVar, fieldTypeVar, fieldNameVar, featuresVar),
                block(readObjectBody.toList())
        );
    }

    private JCTree.JCMethodDecl genWrite(
            JCTree.JCIdent objectType,
            JCTree.JCExpression beanType,
            JCTree.JCNewClass beanNew,
            java.util.List<AttributeInfo> attributeInfos,
            StructInfo structInfo,
            boolean jsonb
    ) {
        int fieldNameLengthMin = 0, fieldNameLengthMax = 0;
        for (int i = 0; i < attributeInfos.size(); ++i) {
            String fieldName = attributeInfos.get(i).name;

            int fieldNameLength = fieldName.getBytes(StandardCharsets.UTF_8).length;
            if (i == 0) {
                fieldNameLengthMin = fieldNameLength;
                fieldNameLengthMax = fieldNameLength;
            } else {
                fieldNameLengthMin = Math.min(fieldNameLength, fieldNameLengthMin);
                fieldNameLengthMax = Math.max(fieldNameLength, fieldNameLengthMax);
            }
        }

        ListBuffer<JCTree.JCStatement> writeBody = new ListBuffer<>();
        MethodWriterContext mwc = new MethodWriterContext(beanType, objectType, jsonb);
        mwc.genVariantsMethodBefore(writeBody);

        writeBody.append(
                defIfReturn(
                        ne(bitAnd(mwc.contextFeatures, IgnoreErrorGetter.mask | UnquoteFieldName.mask), 0L),
                        exec(method(
                                names._super,
                                "write",
                                mwc.jsonWriter,
                                mwc.object,
                                mwc.fieldName,
                                mwc.fieldType,
                                mwc.features
                        ))
                )
        );

        writeBody.append(
                defIfReturn(
                        mwc.jsonWriterField("jsonb"),
                        defIf(
                                ne(bitAnd(mwc.contextFeatures, literal(BeanToArray.mask)), 0L),
                                block(exec(method(
                                        names._this,
                                        "writeArrayMappingJSONB",
                                        mwc.jsonWriter,
                                        mwc.object,
                                        mwc.fieldName,
                                        mwc.fieldType,
                                        mwc.features))
                                ),
                                block(exec(method(
                                        names._this,
                                        "writeJSONB",
                                        mwc.jsonWriter,
                                        mwc.object,
                                        mwc.fieldName,
                                        mwc.fieldType,
                                        mwc.features))
                                )
                        )
                )
        );

        writeBody.append(
                defIfReturn(
                        ne(bitAnd(mwc.contextFeatures, BeanToArray.mask), 0L),
                        exec(method(
                                names._this,
                                "writeArrayMapping",
                                mwc.jsonWriter,
                                mwc.object,
                                mwc.fieldName,
                                mwc.fieldType,
                                mwc.features))
                )
        );

        writeBody.append(
                defIfReturn(
                        method(names._this, "hasFilter", mwc.jsonWriter),
                        exec(
                                method(
                                        names._this,
                                        "writeWithFilter",
                                        mwc.jsonWriter,
                                        mwc.object,
                                        mwc.fieldName,
                                        mwc.fieldType,
                                        mwc.features
                                )
                        )
                )
        );

        writeBody.append(
                defIfReturn(
                        ne(bitAnd(mwc.contextFeatures, IgnoreNoneSerializable.mask), 0L),
                        exec(mwc.jsonWriterMethod("writeNull"))
                )
        );

        writeBody.append(
                defIfReturn(
                        ne(bitAnd(mwc.contextFeatures, ErrorOnNoneSerializable.mask), 0L),
                        exec(method(names._this, "errorOnNoneSerializable"))
                )
        );

        writeBody.append(exec(mwc.jsonWriterMethod("startObject")));
        JCTree.JCVariableDecl var7Var = defVar("var7", true);
        writeBody.append(var7Var);
        writeBody.append(
                defIf(
                        and(
                                notNull(mwc.object),
                                and(
                                        ne(method(mwc.object, "getClass"), mwc.fieldType),
                                        method(
                                                mwc.jsonWriter,
                                                "isWriteTypeInfo",
                                                mwc.object,
                                                mwc.fieldType,
                                                mwc.features
                                        )
                                )
                        ),
                        block(exec(
                                assign(
                                        ident(var7Var),
                                        bitXor(
                                                method(names._this, "writeTypeInfo", mwc.jsonWriter),
                                                literal(true)
                                        )
                                )
                        )),
                        null));

        for (int i = 0; i < attributeInfos.size(); ++i) {
            AttributeInfo attributeInfo = attributeInfos.get(i);
            writeBody.appendList(
                    genWriteField(
                            mwc,
                            attributeInfo,
                            i
                    )
            );
        }

        writeBody.append(exec(mwc.jsonWriterMethod("endObject")));

        return defMethod(
                Flags.PUBLIC,
                "write",
                type(TypeTag.VOID),
                List.of(
                        defVar("jsonWriter", qualIdent(JSONWriter.class.getName())),
                        defVar("object", objectType),
                        defVar("fieldName", objectType),
                        defVar("fieldType", qualIdent(Type.class.getName())),
                        defVar("features", TypeTag.LONG)
                ),
                block(writeBody.toList())
        );
    }

    private JCTree.JCStatement genWriteFieldName(MethodWriterContext mwc, AttributeInfo attributeInfo, int i) {
        String methodName;
        if (!mwc.jsonb) {
            byte[] fieldNameUTF8 = attributeInfo.name.getBytes(StandardCharsets.UTF_8);

            boolean asciiName = true;
            for (int j = 0; j < fieldNameUTF8.length; j++) {
                if (fieldNameUTF8[j] < 0) {
                    asciiName = false;
                    break;
                }
            }

            int length = fieldNameUTF8.length;
            if (length >= 2 && length <= 16 && asciiName) {
                long name1 = 0;
                int name12 = 0;
                long name1SQ = 0;
                int name1SQ2 = 0;
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
                        byte[] name1Bytes = new byte[4];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = '"';
                        name1Bytes[3] = ':';
                        name12 = UNSAFE.getInt(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        name1Bytes[2] = '\'';
                        name1SQ2 = UNSAFE.getInt(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        methodName = "writeName9Raw";
                        break;
                    }
                    case 10: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = '"';
                        name1Bytes[4] = ':';
                        name1 = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        name1Bytes[3] = '\'';
                        name1SQ = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        methodName = "writeName10Raw";
                        break;
                    }
                    case 11: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = '"';
                        name1Bytes[5] = ':';

                        name1 = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        name1Bytes[4] = '\'';
                        name1SQ = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        methodName = "writeName11Raw";
                        break;
                    }
                    case 12: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = fieldNameUTF8[11];
                        name1Bytes[5] = '"';
                        name1Bytes[6] = ':';

                        name1 = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        name1Bytes[5] = '\'';
                        name1SQ = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        methodName = "writeName12Raw";
                        break;
                    }
                    case 13: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = fieldNameUTF8[11];
                        name1Bytes[5] = fieldNameUTF8[12];
                        name1Bytes[6] = '"';
                        name1Bytes[7] = ':';

                        name1 = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        name1Bytes[6] = '\'';
                        name1SQ = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        methodName = "writeName13Raw";
                        break;
                    }
                    case 14: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        byte[] name1Bytes = new byte[8];
                        name1Bytes[0] = fieldNameUTF8[7];
                        name1Bytes[1] = fieldNameUTF8[8];
                        name1Bytes[2] = fieldNameUTF8[9];
                        name1Bytes[3] = fieldNameUTF8[10];
                        name1Bytes[4] = fieldNameUTF8[11];
                        name1Bytes[5] = fieldNameUTF8[12];
                        name1Bytes[6] = fieldNameUTF8[13];
                        name1Bytes[7] = '"';

                        name1 = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        name1Bytes[7] = '\'';
                        name1SQ = UNSAFE.getLong(name1Bytes, ARRAY_BYTE_BASE_OFFSET);

                        methodName = "writeName14Raw";
                        break;
                    }
                    case 15: {
                        bytes[0] = '"';
                        System.arraycopy(fieldNameUTF8, 0, bytes, 1, 7);
                        name1 = UNSAFE.getLong(fieldNameUTF8, ARRAY_BYTE_BASE_OFFSET + 7);
                        name1SQ = name1;
                        methodName = "writeName15Raw";
                        break;
                    }
                    case 16: {
                        System.arraycopy(fieldNameUTF8, 0, bytes, 0, 8);
                        name1 = UNSAFE.getLong(fieldNameUTF8, ARRAY_BYTE_BASE_OFFSET + 8);
                        name1SQ = name1;
                        methodName = "writeName16Raw";
                        break;
                    }
                    default:
                        throw new IllegalStateException("length : " + length);
                }
                long nameIn64DoubleQuote = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET);
                for (int j = 0; j < bytes.length; j++) {
                    if (bytes[j] == '"') {
                        bytes[j] = '\'';
                    }
                }
                long nameIn64SingleQuote = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET);
                JCTree.JCConditional ternary = ternary(mwc.nameDirect, literal(nameIn64DoubleQuote), literal(nameIn64SingleQuote));
                if (length == 9) {
                    ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
                    JCTree.JCConditional ternary2 = ternary(mwc.nameDirect, name12, name1SQ2);
                    JCTree.JCVariableDecl name1Var = defVar("name1", TypeTag.INT, ternary2);
                    stmts.append(defVar("name1", TypeTag.INT, ternary2));
                    stmts.append(exec(mwc.jsonWriterMethod(methodName, ternary, name1Var)));
                    return block(stmts.toList());
                } else if (length > 9) {
                    JCTree.JCConditional ternary2 = ternary(mwc.nameDirect, name1, name1SQ);
                    return exec(mwc.jsonWriterMethod(methodName, ternary, ternary2));
                } else {
                    return exec(mwc.jsonWriterMethod(methodName, ternary));
                }
            }
        } else {
            byte[] fieldNameUTF8 = JSONB.toBytes(attributeInfo.name);
            int length = fieldNameUTF8.length;
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
                    break;
                case 10:
                    methodName = "writeName10Raw";
                    break;
                case 11:
                    methodName = "writeName11Raw";
                    break;
                case 12:
                    methodName = "writeName12Raw";
                    break;
                case 13:
                    methodName = "writeName13Raw";
                    break;
                case 14:
                    methodName = "writeName14Raw";
                    break;
                case 15:
                    methodName = "writeName15Raw";
                    break;
                case 16:
                    methodName = "writeName16Raw";
                    break;
                default:
                    break;
            }
        }
        return exec(method(field(names._this, fieldWriter(i)), mwc.jsonb ? "writeFieldNameJSONB" : "writeFieldName", mwc.jsonWriter));
    }

    private ListBuffer<JCTree.JCStatement> genWriteField(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        if ("boolean".equals(type)) {
            return genWriteFieldValueBooleanV(mwc, attributeInfo, i);
        } else if ("boolean[]".equals(type)
                || "byte[]".equals(type)
                || "char[]".equals(type)
                || "short[]".equals(type)
                || "float[]".equals(type)
                || "double[]".equals(type)) {
            return stmts.appendList(genWriteFieldValueArray(mwc, attributeInfo, i));
        } else if ("int".equals(type)) {
            return stmts.appendList(genWriteFieldValueInt32V(mwc, attributeInfo, i));
        } else if ("char".equals(type)
                || "byte".equals(type)
                || "int".equals(type)
                || "short".equals(type)
                || "float".equals(type)
                || "double".equals(type)) {
            stmts.append(genWriteFieldName(mwc, attributeInfo, i));
            return stmts.appendList(genWriteFieldValue(mwc, attributeInfo, i, null));
        } else if ("int[]".equals(type)) {
            return stmts.appendList(genWriteFieldValueIntVA(mwc, attributeInfo, i));
        } else if ("long".equals(type)) {
            return stmts.appendList(genWriteFieldValueInt64V(mwc, attributeInfo, i));
        } else if ("long[]".equals(type)) {
            return stmts.appendList(genWriteFieldValueInt64VA(mwc, attributeInfo, i));
        } else if ("java.lang.Integer".equals(type)) {
            return stmts.appendList(genWriteInt32(mwc, attributeInfo, i));
        } else if ("java.lang.Long".equals(type)) {
            return stmts.appendList(genWriteInt64(mwc, attributeInfo, i));
        } else if ("java.lang.Float".equals(type)) {
            return stmts.appendList(genWriteFloat(mwc, attributeInfo, i));
        } else if ("java.lang.Double".equals(type)) {
            return stmts.appendList(genWriteDouble(mwc, attributeInfo, i));
        } else if ("java.math.BigDecimal".equals(type)) {
            return stmts.appendList(genWriteBigDecimal(mwc, attributeInfo, i));
        } else if ("java.lang.Boolean".equals(type)) {
            return stmts.appendList(genWriteBoolean(mwc, attributeInfo, i));
        } else if ("java.lang.String".equals(type)) {
            return stmts.appendList(genWriteFieldValueString(mwc, attributeInfo, i));
        } else if (attributeInfo.type instanceof com.sun.tools.javac.code.Type.ClassType
                && ((com.sun.tools.javac.code.Type.ClassType) attributeInfo.type).supertype_field != null
                && "java.lang.Enum".equals(((com.sun.tools.javac.code.Type.ClassType) attributeInfo.type).supertype_field.tsym.toString())) {
            return stmts.appendList(genWriteFieldValueEnum(mwc, attributeInfo, i));
        } else if ("java.util.Date".equals(type)) {
            return stmts.appendList(genWriteFieldValueDate(mwc, attributeInfo, i));
        } else if (type.contains("java.util.List")) {
            return stmts.appendList(genWriteFieldValueList(mwc, attributeInfo, i));
        } else {
            return stmts.appendList(genWriteFieldValueObject(mwc, attributeInfo, i));
        }
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValue(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i,
            JCTree.JCExpression fieldValue
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        if (fieldValue == null) {
            fieldValue = genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType);
        }
        List<JCTree.JCExpression> fieldValueExprs = List.of(fieldValue);
        JCTree.JCBinary writeAsStringBinary = ne(
                bitAnd(
                        field(field(ident(names._this), fieldWriter(i)), "features"),
                        WriteNonStringValueAsString.mask
                ),
                0
        );
        if ("boolean".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeBool", fieldValueExprs)));
        } else if ("char".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeChar", fieldValueExprs)));
        } else if ("byte".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(mwc.jsonWriterMethod("writeString", fieldValueExprs))),
                    block(exec(mwc.jsonWriterMethod("writeInt8", fieldValueExprs)))));
        } else if ("short".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(mwc.jsonWriterMethod("writeString", fieldValueExprs))),
                    block(exec(mwc.jsonWriterMethod("writeInt16", fieldValueExprs)))));
        } else if ("int".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(mwc.jsonWriterMethod("writeString", fieldValueExprs))),
                    block(exec(mwc.jsonWriterMethod("writeInt32", fieldValueExprs)))));
        } else if ("Integer".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeInt32", fieldValueExprs)));
        } else if ("long".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(mwc.jsonWriterMethod("writeString", fieldValueExprs))),
                    block(exec(mwc.jsonWriterMethod("writeInt64", fieldValueExprs)))));
        } else if ("Long".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeInt64", fieldValueExprs)));
        } else if ("float".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(mwc.jsonWriterMethod("writeString", fieldValueExprs))),
                    block(exec(mwc.jsonWriterMethod("writeFloat", fieldValueExprs)))));
        } else if ("double".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(mwc.jsonWriterMethod("writeString", fieldValueExprs))),
                    block(exec(mwc.jsonWriterMethod("writeDouble", fieldValueExprs)))));
        } else if ("boolean[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeBool", fieldValueExprs)));
        } else if ("char[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeString", fieldValueExprs)));
        } else if ("byte[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeBinary", fieldValueExprs)));
        } else if ("short[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeInt16", fieldValueExprs)));
        } else if ("int[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeInt32", fieldValueExprs)));
        } else if ("long[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeInt64", fieldValueExprs)));
        } else if ("float[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeFloat", fieldValueExprs)));
        } else if ("double[]".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeDouble", fieldValueExprs)));
        } else if ("BigDecimal".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeDecimal", fieldValueExprs)));
        } else if ("Enum".equals(type)) {
            stmts.append(exec(mwc.jsonWriterMethod("writeEnum", fieldValueExprs)));
        } else {
            throw new UnsupportedOperationException();
        }
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueBooleanV(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl boolVar = defVar("bool" + i, TypeTag.BOOLEAN, genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(boolVar);
        stmts.append(defIf(unary(JCTree.Tag.NOT, ident(boolVar)), block(List.nil())));
        stmts.append(exec(method(field(names._this, fieldWriter(i)), "writeBool", mwc.jsonWriter, boolVar)));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueArray(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        String type = attributeInfo.type.toString();
        String methodName;
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        List<JCTree.JCExpression> fieldValueExprs = List.of(mwc.jsonWriter, genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        if ("char[]".equals(type)) {
            methodName = "writeString";
        } else if ("boolean[]".equals(type)) {
            methodName = "writeBool";
        } else if ("byte[]".equals(type)) {
            methodName = "writeBinary";
        } else if ("short[]".equals(type)) {
            methodName = "writeInt16";
        } else if ("float[]".equals(type)) {
            methodName = "writeFloat";
        } else if ("double[]".equals(type)) {
            methodName = "writeDouble";
        } else if ("enum".equals(type)) {
            methodName = "writeEnumJSONB";
        } else {
            throw new UnsupportedOperationException();
        }
        return stmts.append(exec(method(field(names._this, fieldWriter(i)), methodName, fieldValueExprs)));
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueInt32V(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl int32Var = defVar("int32" + i, TypeTag.INT, genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(int32Var);
        ListBuffer<JCTree.JCStatement> ifStmts = new ListBuffer<>();
        ifStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        ifStmts.appendList(
                genWriteFieldValue(mwc, attributeInfo, i, ident(int32Var)));
        stmts.append(defIf(
                or(ne(int32Var, 0), not(mwc.notWriteDefaultValue)),
                block(ifStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueIntVA(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String elemType = ((ArrayType) attributeInfo.type).getComponentType().toString();
        JCTree.JCVariableDecl intArrayVar = defVar("intArray" + i, arrayIdentType(elemType), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(intArrayVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        notZeroStmts.append(exec(mwc.jsonWriterMethod("writeArrayNull")));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        nullStmts.append(defIf(mwc.writeNulls, block(notZeroStmts.toList())));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        notNullStmts.append(exec(mwc.jsonWriterMethod("writeInt32", intArrayVar)));

        stmts.append(defIf(eq(intArrayVar, defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueInt64V(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl int64Var = defVar("int64" + i, TypeTag.LONG, genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(int64Var);
        ListBuffer<JCTree.JCStatement> ifStmts = new ListBuffer<>();
        ifStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        ifStmts.appendList(genWriteFieldValue(mwc, attributeInfo, i, ident(int64Var)));
        stmts.append(defIf(or(ne(int64Var, 0), not(mwc.notWriteDefaultValue)),
                block(ifStmts.toList()),
                null));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueInt64VA(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String elemType = ((ArrayType) attributeInfo.type).getComponentType().toString();
        JCTree.JCVariableDecl longArrayVar = defVar("longArray" + i, arrayIdentType(elemType), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(longArrayVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        notZeroStmts.append(exec(mwc.jsonWriterMethod("writeArrayNull")));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        nullStmts.append(defIf(mwc.writeNulls, block(notZeroStmts.toList())));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        notNullStmts.append(exec(mwc.jsonWriterMethod("writeInt64", longArrayVar)));

        stmts.append(defIf(eq(longArrayVar, defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteBigDecimal(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();

        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl fieldValue = defVar("bigDecimal" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(fieldValue);

        stmts.append(
                defIf(
                        or(notNull(fieldValue), isEnable(mwc.contextFeatures, WriteNulls)),
                        block(
                                genWriteFieldName(mwc, attributeInfo, i),
                                exec(mwc.jsonWriterMethod("writeDecimal", fieldValue))
                        )
                )
        );
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteBoolean(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();

        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl fieldValue = defVar("Boolean" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(fieldValue);

        stmts.append(
                defIf(
                        or(notNull(fieldValue), isEnable(mwc.contextFeatures, WriteNulls)),
                        block(
                                genWriteFieldName(mwc, attributeInfo, i),
                                defIf(
                                        notNull(fieldValue),
                                    block(
                                        exec(mwc.jsonWriterMethod("writeBool", fieldValue))
                                    ),
                                    block(
                                            exec(mwc.jsonWriterMethod("writeNull"))
                                    )
                                )
                        )
                )
        );
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteInt32(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();

        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl integerVar = defVar("integer" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(integerVar);

        stmts.append(
                defIf(
                        ne(integerVar, defNull()),
                        block(
                                genWriteFieldName(mwc, attributeInfo, i),
                                exec(mwc.jsonWriterMethod("writeInt32", integerVar))
                        ),
                        defIf(
                                isEnable(mwc.contextFeatures, WriteNulls, NullAsDefaultValue, WriteNullNumberAsZero),
                                block(
                                        genWriteFieldName(mwc, attributeInfo, i),
                                        exec(mwc.jsonWriterMethod("writeNumberNull"))
                                )
                        )
                )
        );
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteInt64(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();

        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl integerVar = defVar("long" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(integerVar);

        stmts.append(
                defIf(
                        ne(integerVar, defNull()),
                        block(
                                genWriteFieldName(mwc, attributeInfo, i),
                                exec(mwc.jsonWriterMethod("writeInt64", integerVar))
                        ),
                        defIf(
                                isEnable(mwc.contextFeatures, WriteNulls, NullAsDefaultValue, WriteNullNumberAsZero),
                                block(
                                        genWriteFieldName(mwc, attributeInfo, i),
                                        exec(mwc.jsonWriterMethod("writeInt64Null"))
                                )
                        )
                )
        );
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFloat(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl floatVar = defVar("float" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(floatVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        notZeroStmts.append(exec(mwc.jsonWriterMethod("writeNumberNull")));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        JCTree.JCExpression writeAsStringBinary = literal(BrowserCompatible.mask | WriteBooleanAsNumber.mask | WriteNullStringAsEmpty.mask);
        nullStmts.append(defIf(ne(bitAnd(mwc.contextFeatures, writeAsStringBinary), 0), block(notZeroStmts.toList())));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(exec(method(field(names._this, fieldWriter(i)), "writeFloat", mwc.jsonWriter, floatVar)));

        stmts.append(defIf(eq(floatVar, defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteDouble(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl doubleVar = defVar("double" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(doubleVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        notZeroStmts.append(exec(mwc.jsonWriterMethod("writeNumberNull")));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        JCTree.JCExpression writeAsStringBinary = literal(BrowserCompatible.mask | WriteBooleanAsNumber.mask | WriteNullStringAsEmpty.mask);
        nullStmts.append(defIf(ne(bitAnd(mwc.contextFeatures, writeAsStringBinary), 0), block(notZeroStmts.toList())));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(exec(method(field(names._this, fieldWriter(i)), "writeDouble", mwc.jsonWriter, doubleVar)));

        stmts.append(defIf(eq(doubleVar, defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueString(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        final long writeNullFeatures = WriteNulls.mask | NullAsDefaultValue.mask | WriteNullStringAsEmpty.mask;
        final long writeNullAsEmptyFeatures = JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask;

        JCTree.JCIdent fieldValue = ident("string" + i);
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        stmts.append(
                defVar(
                        fieldValue.name,
                        qualIdent(attributeInfo.type.toString()),
                        genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType)
                )
        );
        stmts.append(
                defIf(
                        notNull(fieldValue),
                        block(
                                genWriteFieldName(mwc, attributeInfo, i),
                                exec(mwc.jsonWriterMethod("writeString", fieldValue))
                        ),
                        block(defIf(
                                ne(bitAnd(mwc.contextFeatures, writeNullFeatures), 0L),
                                block(
                                        genWriteFieldName(mwc, attributeInfo, i),
                                        defIf(
                                                ne(bitAnd(mwc.contextFeatures, writeNullAsEmptyFeatures), 0L),
                                                block(exec(mwc.jsonWriterMethod("writeString", ""))),
                                                block(exec(mwc.jsonWriterMethod("writeStringNull")))
                                        )
                                )
                        ))
                )
        );
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueEnum(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl enumVar = defVar("enum" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(enumVar);
        stmts.append(
                defIf(
                        ne(enumVar, defNull()),
                        block(
                                method(
                                        field(names._this, fieldWriter(i)),
                                        "writeEnum",
                                        mwc.jsonWriter,
                                        ident(enumVar)
                                )
                        ),
                        defIf(
                                mwc.writeNulls,
                                block(
                                        genWriteFieldName(mwc, attributeInfo, i),
                                        exec(mwc.jsonWriterMethod("writeNull"))
                                )
                        )
                )
        );
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueDate(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl dateVar = defVar("date" + i, qualIdent(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(dateVar);
        stmts.append(
                defIf(
                        ne(dateVar, defNull()),
                        block(
                                exec(method(
                                        field(names._this, fieldWriter(i)),
                                        "writeDate",
                                        mwc.jsonWriter,
                                        method(dateVar, "getTime")
                                ))
                        ),
                        defIf(
                                mwc.writeNulls,
                                block(
                                        genWriteFieldName(mwc, attributeInfo, i),
                                        exec(mwc.jsonWriterMethod("writeNull"))
                                )
                        )
                )
        );
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueList(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl listStrVar = defVar("listStr" + i, ident("String"), defNull());
        stmts.append(listStrVar);
        JCTree.JCVariableDecl listVar = defVar("list" + i, qualIdent("java.util.List"), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
        stmts.append(listVar);

        JCTree.JCLabeledStatement label = label("listLabel" + i);
        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        ListBuffer<JCTree.JCStatement> labelStmts = new ListBuffer<>();

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(defIf(eq(mwc.object, listVar),
                block(exec(mwc.jsonWriterMethod("writeReference", "..")),
                        defBreak(label))));

        notZeroStmts.append(exec(assign(listStrVar, mwc.jsonWriterMethod("setPath", field(ident(names._this), fieldWriter(i)), listVar))));

        notZeroStmts.append(defIf(ne(listStrVar, defNull()),
                block(exec(mwc.jsonWriterMethod("writeReference", listStrVar)),
                        exec(mwc.jsonWriterMethod("popPath", listVar)),
                        defBreak(label))));

        labelStmts.append(defIf(ne(bitAnd(mwc.contextFeatures, ReferenceDetection.mask), 0L), block(notZeroStmts.toList())));

        JCTree.JCBinary binary = eq(bitAnd(mwc.contextFeatures, NotWriteEmptyArray), 0L);
        JCTree.JCUnary unary = unary(JCTree.Tag.NOT, method(listVar, "isEmpty"));

        ListBuffer<JCTree.JCStatement> notEmptyStmts = new ListBuffer<>();
        notEmptyStmts.append(genWriteFieldName(mwc, attributeInfo, i));
        String type = attributeInfo.type.toString();
        if ("java.util.List<java.lang.String>".equals(type)) {
            notEmptyStmts.append(exec(mwc.jsonWriterMethod("writeString", listVar)));
        } else {
            notEmptyStmts.append(
                    exec(method(
                            field(names._this, fieldWriter(i)),
                            "writeListValue",
                            mwc.jsonWriter,
                            listVar
                    ))
            );
        }
        notEmptyStmts.append(exec(mwc.jsonWriterMethod("popPath", listVar)));
        labelStmts.append(defIf(or(binary, unary), block(notEmptyStmts.toList())));

        JCTree.JCExpression writeAsStringBinary = literal(WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask);
        JCTree.JCIf notNUllIf = defIf(ne(bitAnd(mwc.contextFeatures, writeAsStringBinary), 0L),
                block(
                        genWriteFieldName(mwc, attributeInfo, i),
                        exec(mwc.jsonWriterMethod("writeArrayNull"))
                )
        );

        label.body = block(labelStmts.toList());
        notNullStmts.append(label);
        stmts.append(defIf(ne(listVar, defNull()), block(notNullStmts.toList()), block(notNUllIf)));
        return stmts;
    }

    private JCTree.JCExpression genWriteFieldValue(AttributeInfo attributeInfo,
                                                   JCTree.JCIdent objectIdent,
                                                   JCTree.JCExpression beanType) {
        if (attributeInfo.getMethod != null) {
            return method(cast(beanType, objectIdent), attributeInfo.getMethod.getSimpleName().toString());
        } else {
            String name = attributeInfo.name;
            VariableElement field = attributeInfo.field;
            if (field instanceof Symbol.VarSymbol) {
                Name symbolName = ((Symbol.VarSymbol) field).name;
                if (symbolName != null) {
                    String fieldName = symbolName.toString();
                    if (!name.equals(fieldName)) {
                        name = fieldName;
                    }
                }
            }
            return field(cast(beanType, objectIdent), name);
        }
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueObject(
            MethodWriterContext mwc,
            AttributeInfo attributeInfo,
            int i
    ) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        if (type.contains("[")) {
            JCTree.JCVariableDecl objectStrVar = defVar("objectStr" + i, ident("String"), defNull());
            stmts.append(objectStrVar);
            JCTree.JCVariableDecl objectIntVar = defVar("objectInt" + i, 0);
            stmts.append(objectIntVar);
            JCTree.JCLabeledStatement outerLabel = label("objectOuterLabel" + i);
            ListBuffer<JCTree.JCStatement> outerLabelStmts = new ListBuffer<>();
            String elemType = ((ArrayType) attributeInfo.type).getComponentType().toString();
            JCTree.JCVariableDecl objectVar = defVar("object" + i, arrayIdentType(elemType), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
            outerLabelStmts.append(objectVar);

            JCTree.JCLabeledStatement innerLabel = label("objectInnerLabel" + i);
            ListBuffer<JCTree.JCStatement> innerLabelStmts = new ListBuffer<>();

            ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
            JCTree.JCVariableDecl objectLongVar = defVar("objectLong" + i, TypeTag.LONG, bitAnd(mwc.contextFeatures, ReferenceDetection.mask));
            notNullStmts.append(objectLongVar);
            notNullStmts.append(exec(
                    assign(
                            objectIntVar,
                            ternary(
                                    eq(objectLongVar, 0),
                                    literal(0),
                                    ternary(lt(objectLongVar, 0), -1, 1)
                            )
                    )
            ));
            notNullStmts.append(defIf(eq(objectIntVar, 0), defBreak(innerLabel)));

            ListBuffer<JCTree.JCStatement> eqStmts = new ListBuffer<>();
            eqStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            eqStmts.append(exec(mwc.jsonWriterMethod("writeReference", literal(".."))));
            eqStmts.append(defBreak(outerLabel));
            notNullStmts.append(defIf(eq(mwc.object, objectVar), block(eqStmts.toList())));

            notNullStmts.append(exec(
                    assign(
                            objectStrVar,
                            method(
                                    mwc.jsonWriter,
                                    "setPath",
                                    field(names._this, fieldWriter(i)),
                                    objectVar
                            )
                    )
            ));
            notNullStmts.append(defIf(eq(objectStrVar, defNull()), block(defBreak(innerLabel))));

            notNullStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            notNullStmts.append(exec(mwc.jsonWriterMethod("writeReference", "..")));

            innerLabelStmts.append(defIf(ne(ident(objectVar), defNull()), block(notNullStmts.toList())));

            ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
            notZeroStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            notZeroStmts.append(exec(mwc.jsonWriterMethod("writeArrayNull")));
            JCTree.JCExpression writeAsStringBinary = literal(WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask);
            innerLabelStmts.append(defIf(ne(bitAnd(mwc.contextFeatures, writeAsStringBinary), 0), block(notZeroStmts.toList())));

            innerLabelStmts.append(defBreak(outerLabel));

            innerLabel.body = block(innerLabelStmts.toList());
            outerLabelStmts.append(innerLabel);

            ListBuffer<JCTree.JCStatement> notEmptyArrayStmts = new ListBuffer<>();
            notEmptyArrayStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            notEmptyArrayStmts.append(
                    exec(method(
                            method(field(names._this, fieldWriter(i)), "getObjectWriter", mwc.jsonWriter, field(arrayIdentType(type), names._class)),
                            "write",
                            mwc.jsonWriter,
                            ident(objectVar),
                            literal(attributeInfo),
                            method(objectVar, "getClass"),
                            literal(0L)
                    ))
            );
            notEmptyArrayStmts.append(
                    defIf(
                            ne(objectIntVar, 0),
                            exec(mwc.jsonWriterMethod("popPath", objectVar))
                    )
            );
            JCTree.JCBinary binary1 = eq(bitAnd(mwc.contextFeatures, NotWriteEmptyArray.mask), 0);
            JCTree.JCBinary binary2 = ne(field(objectVar, "length"), 0);
            outerLabelStmts.append(defIf(and(binary1, binary2), block(notEmptyArrayStmts.toList())));

            outerLabel.body = block(outerLabelStmts.toList());
            stmts.append(outerLabel);
        } else if (type.contains("java.util.Map<")) {
            JCTree.JCLabeledStatement outerLabel = label("objectOuterLabel" + i);
            ListBuffer<JCTree.JCStatement> outerLabelStmts = new ListBuffer<>();

            JCTree.JCVariableDecl objectStrVar = defVar("objectStr" + i, ident("String"), defNull());
            outerLabelStmts.append(objectStrVar);

            JCTree.JCVariableDecl objectVar = defVar("object" + i, getFieldValueType(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
            outerLabelStmts.append(objectVar);
            JCTree.JCVariableDecl objectIntVar = defVar("objectInt" + i, TypeTag.INT, literal(0));
            outerLabelStmts.append(objectIntVar);

            JCTree.JCLabeledStatement innerLabel = label("objectInnerLabel" + i);
            ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
            notNullStmts.append(defIf(mwc.jsonWriterMethod("isIgnoreNoneSerializable", objectVar), block(defBreak(outerLabel))));

            JCTree.JCVariableDecl objectLongVar = defVar("objectLong" + i, TypeTag.LONG, bitAnd(mwc.contextFeatures, ReferenceDetection.mask));
            notNullStmts.append(objectLongVar);
            notNullStmts.append(exec(
                    assign(
                            objectIntVar,
                            ternary(
                                    eq(objectLongVar, 0),
                                    literal(0),
                                    ternary(lt(objectLongVar, 0), -1, 1)
                            )
                    )
            ));
            notNullStmts.append(defIf(eq(objectIntVar, 0), defBreak(innerLabel)));

            notNullStmts.append(defIf(eq(mwc.object, objectVar),
                    block(genWriteFieldName(mwc, attributeInfo, i),
                            exec(mwc.jsonWriterMethod("writeReference", literal(".."))),
                            defBreak(outerLabel)), null));

            notNullStmts.append(exec(assign(objectStrVar, mwc.jsonWriterMethod("setPath", field(ident(names._this), fieldWriter(i)), ident(objectVar)))));
            notNullStmts.append(defIf(eq(objectStrVar, defNull()), defBreak(innerLabel)));

            notNullStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            notNullStmts.append(exec(mwc.jsonWriterMethod("writeReference", objectStrVar)));
            notNullStmts.append(exec(mwc.jsonWriterMethod("popPath", objectVar)));

            ListBuffer<JCTree.JCStatement> innerLabelStmts = new ListBuffer<>();
            innerLabelStmts.append(defIf(ne(ident(objectVar), defNull()), block(notNullStmts.toList())));

            JCTree.JCBinary binary = ne(bitAnd(mwc.contextFeatures, WriteMapNullValue.mask), 0);
            innerLabelStmts.append(
                    defIf(
                            binary,
                            block(
                                    genWriteFieldName(mwc, attributeInfo, i),
                                    exec(mwc.jsonWriterMethod("writeNull"))
                            )
                    )
            );
            innerLabelStmts.append(defBreak(outerLabel));
            innerLabel.body = block(innerLabelStmts.toList());

            outerLabelStmts.append(innerLabel);
            outerLabelStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            outerLabelStmts.append(
                    exec(method(
                            method(
                                    field(names._this, fieldWriter(i)),
                                    "getObjectWriter",
                                    mwc.jsonWriter,
                                    method(objectVar, "getClass")
                            ),
                            "write",
                            mwc.jsonWriter, ident(objectVar),
                            literal(attributeInfo.name),
                            field(field(names._this, fieldWriter(i)), "fieldType"),
                            literal(0L))
                    )
            );
            outerLabelStmts.append(
                    defIf(
                            ne(objectIntVar, 0),
                            exec(mwc.jsonWriterMethod("popPath", objectVar))
                    )
            );

            outerLabel.body = block(outerLabelStmts.toList());
            stmts.append(outerLabel);
        } else {
            String WRITE_NULL_METHOD;
            if ("AtomicLongArray".equals(type)
                    || "AtomicIntegerArray".equals(type)
                    || "Collection.class.isAssignableFrom".equals(type)
                    || "isArray".equals(type)) {
                WRITE_NULL_METHOD = "writeArrayNull";
            } else if ("java.lang.Number".equals(type)) {
                WRITE_NULL_METHOD = "writeNumberNull";
            } else if ("Boolean".equals(type)) {
                WRITE_NULL_METHOD = "writeBooleanNull";
            } else if ("String".equals(type)
                    || "Appendable".equals(type)
                    || "StringBuffer".equals(type)
                    || "StringBuilder".equals(type)) {
                WRITE_NULL_METHOD = "writeStringNull";
            } else {
                WRITE_NULL_METHOD = "writeNull";
            }

            JCTree.JCLabeledStatement outerLabel = label("objectOuterLabel" + i);
            ListBuffer<JCTree.JCStatement> outerLabelStmts = new ListBuffer<>();

            JCTree.JCVariableDecl objectStrVar = defVar("objectStr" + i, ident("String"), defNull());
            outerLabelStmts.append(objectStrVar);

            JCTree.JCVariableDecl objectVar = defVar("object" + i, getFieldValueType(type), genWriteFieldValue(attributeInfo, mwc.object, mwc.beanType));
            outerLabelStmts.append(objectVar);
            JCTree.JCVariableDecl objectIntVar = defVar("objectInt" + i, TypeTag.INT, literal(0));
            outerLabelStmts.append(objectIntVar);

            JCTree.JCLabeledStatement innerLabel = label("objectInnerLabel" + i);
            ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();

            boolean noneSerializable = false;
            if (attributeInfo.type instanceof com.sun.tools.javac.code.Type.ClassType) {
                List<com.sun.tools.javac.code.Type> interfacesField = ((com.sun.tools.javac.code.Type.ClassType) attributeInfo.type).interfaces_field;
                if (interfacesField == null || interfacesField.stream().noneMatch(f -> "java.io.Serializable".equals(f.toString()))) {
                    noneSerializable = true;
                }
            }

            if (noneSerializable) {
                notNullStmts.append(defIf(mwc.jsonWriterMethod("isIgnoreNoneSerializable", objectVar), defBreak(outerLabel)));
            }

            JCTree.JCVariableDecl objectLongVar = defVar("objectLong" + i, TypeTag.LONG, bitAnd(mwc.contextFeatures, ReferenceDetection.mask));
            notNullStmts.append(objectLongVar);
            notNullStmts.append(exec(assign(objectIntVar, ternary(eq(objectLongVar, 0), literal(0), ternary(lt(objectLongVar, 0), -1, 1)))));
            notNullStmts.append(defIf(eq(objectIntVar, 0), defBreak(innerLabel)));

            notNullStmts.append(defIf(eq(mwc.object, ident(objectVar)),
                    block(genWriteFieldName(mwc, attributeInfo, i),
                            exec(mwc.jsonWriterMethod("writeReference", "..")),
                            defBreak(outerLabel))));

            notNullStmts.append(
                    exec(
                            assign(
                                    objectStrVar,
                                    method(
                                            mwc.jsonWriter,
                                            "setPath",
                                            field(names._this, fieldWriter(i)),
                                            objectVar
                                    )
                            )
                    )
            );
            notNullStmts.append(defIf(eq(objectStrVar, defNull()), defBreak(innerLabel)));

            notNullStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            notNullStmts.append(exec(mwc.jsonWriterMethod("writeReference", objectStrVar)));
            notNullStmts.append(exec(mwc.jsonWriterMethod("popPath", objectVar)));

            ListBuffer<JCTree.JCStatement> innerLabelStmts = new ListBuffer<>();
            innerLabelStmts.append(defIf(ne(objectVar, defNull()), block(notNullStmts.toList())));

            JCTree.JCBinary binary = ne(bitAnd(mwc.contextFeatures, WriteMapNullValue.mask), 0);
            innerLabelStmts.append(defIf(binary,
                    block(genWriteFieldName(mwc, attributeInfo, i),
                            exec(mwc.jsonWriterMethod(WRITE_NULL_METHOD))),
                    null));
            innerLabelStmts.append(defBreak(outerLabel));
            innerLabel.body = block(innerLabelStmts.toList());

            outerLabelStmts.append(innerLabel);
            outerLabelStmts.append(genWriteFieldName(mwc, attributeInfo, i));
            outerLabelStmts.append(
                    exec(method(
                            method(
                                    field(names._this, fieldWriter(i)),
                                    "getObjectWriter",
                                    mwc.jsonWriter,
                                    method(objectVar, "getClass")
                            ),
                            "write",
                            mwc.jsonWriter,
                            ident(objectVar),
                            literal(attributeInfo.name),
                            field(field(ident(names._this), fieldWriter(i)), "fieldType"),
                            literal(0L))
                    )
            );
            outerLabelStmts.append(
                    defIf(
                            ne(objectIntVar, 0),
                            exec(mwc.jsonWriterMethod("popPath", objectVar))
                    )
            );

            outerLabel.body = block(outerLabelStmts.toList());
            stmts.append(outerLabel);
        }
        return stmts;
    }

    private JCTree.JCVariableDecl getHashCode32Var(JCTree.JCExpression hashCode64) {
        JCTree.JCBinary usrBinary = binary(JCTree.Tag.USR, hashCode64, literal(32));
        JCTree.JCPrimitiveTypeTree intType = type(TypeTag.INT);
        JCTree.JCTypeCast hashCode32Cast = cast(intType, bitXor(hashCode64, parens(usrBinary)));
        return defVar("hashCode32", intType, hashCode32Cast);
    }

    private List<JCTree.JCStatement> genReadFieldValue(
            AttributeInfo attributeInfo,
            JCTree.JCIdent jsonReaderIdent,
            int i,
            StructInfo structInfo,
            JCTree.JCLabeledStatement loopLabel,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            boolean isJsonb) {
        JCTree.JCExpression valueExpr = null;
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();

        boolean referenceDetect = structInfo.referenceDetect;
        if (referenceDetect) {
            referenceDetect = isReference(type);
        }

        JCTree.JCFieldAccess fieldReaderField = field(ident(names._this), fieldReader(i));
        if (referenceDetect) {
            ListBuffer<JCTree.JCStatement> thenStmts = new ListBuffer<>();
            JCTree.JCMethodInvocation readReferenceMethod = method(jsonReaderIdent, "readReference");
            JCTree.JCVariableDecl refVar = defVar("ref", ident("String"), readReferenceMethod);
            thenStmts.append(refVar);
            JCTree.JCMethodInvocation addResolveTaskMethod = method(fieldReaderField, "addResolveTask", jsonReaderIdent, objectIdent, ident(refVar));
            thenStmts.append(exec(addResolveTaskMethod));
            thenStmts.append(defContinue(loopLabel));
            stmts.append(defIf(method(jsonReaderIdent, "isReference"), block(thenStmts.toList())));
        }

        String readDirectMethod = getReadDirectMethod(type);
        if (readDirectMethod != null) {
            valueExpr = method(jsonReaderIdent, readDirectMethod);
        } else {
            JCTree.JCExpression fieldValueType = getFieldValueType(type);
            JCTree.JCVariableDecl fieldValueVar = defVar(attributeInfo.name, fieldValueType);
            stmts.append(fieldValueVar);

            if (type.startsWith("java.util.List<")) {
                valueExpr = genFieldValueList(
                        type,
                        attributeInfo,
                        jsonReaderIdent,
                        fieldValueVar,
                        loopLabel,
                        stmts,
                        i,
                        referenceDetect,
                        fieldReaderField,
                        beanType,
                        isJsonb);
            } else if (type.startsWith("java.util.Map<java.lang.String,")) {
                valueExpr = genFieldValueMap(type, attributeInfo, jsonReaderIdent, fieldValueVar, loopLabel, stmts, i, referenceDetect, isJsonb);
            }

            if (valueExpr == null) {
                JCTree.JCIdent objectReaderIdent = ident(fieldObjectReader(i));
                JCTree.JCMethodInvocation getObjectReaderMethod = method(fieldReaderField, "getObjectReader", jsonReaderIdent);
                JCTree.JCAssign objectReaderAssign = assign(objectReaderIdent, getObjectReaderMethod);
                stmts.append(defIf(eq(objectReaderIdent, defNull()), block(exec(objectReaderAssign))));
                JCTree.JCMethodInvocation objectMethod = method(
                        field(ident(names._this), fieldObjectReader(i)),
                        isJsonb ? "readJSONBObject" : "readObject",
                        jsonReaderIdent,
                        field(fieldReaderField, "fieldType"),
                        literal(attributeInfo.name),
                        literal(0L)
                );
                stmts.append(exec(assign(fieldValueVar, cast(fieldValueType, objectMethod))));
                valueExpr = ident(fieldValueVar);
            }
        }

        if (attributeInfo.setMethod != null) {
            stmts.append(exec(method(objectIdent, attributeInfo.setMethod.getSimpleName().toString(), valueExpr)));
        } else if (attributeInfo.field != null) {
            stmts.append(exec(assign(field(objectIdent, attributeInfo.field.getSimpleName().toString()), valueExpr)));
        } else {
            messager.printMessage(Diagnostic.Kind.WARNING, "not implemented yet");
        }
        stmts.append(defContinue(loopLabel));
        return stmts.toList();
    }

    private List<JCTree.JCStatement> genRead243(
            java.util.List<AttributeInfo> attributeInfos,
            JCTree.JCIdent jsonReaderIdent,
            StructInfo structInfo,
            JCTree.JCLabeledStatement loopLabel,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            boolean isJsonb) {
        IdentityHashMap<AttributeInfo, Integer> readerIndexMap = new IdentityHashMap<>();
        Map<Integer, java.util.List<AttributeInfo>> name0Map = new TreeMap<>();
        for (int i = 0; i < attributeInfos.size(); ++i) {
            AttributeInfo field = attributeInfos.get(i);
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

            java.util.List<AttributeInfo> fieldReaders = name0Map.get(name0);
            if (fieldReaders == null) {
                fieldReaders = new ArrayList<>();
                name0Map.put(name0, fieldReaders);
            }
            fieldReaders.add(field);
        }

        int[] switchKeys = new int[name0Map.size()];
        Label[] labels = new Label[name0Map.size()];
        {
            Iterator<Integer> it = name0Map.keySet().iterator();
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
                switchKeys[i] = it.next();
            }
        }

        List<JCTree.JCStatement> stmts = List.nil();
        ListBuffer<JCTree.JCCase> cases = new ListBuffer<>();

        JCTree.JCLabeledStatement switchLabel = label("_switch2");
        for (int i = 0; i < labels.length; i++) {
            int name0 = switchKeys[i];
            java.util.List<AttributeInfo> fieldReaders = name0Map.get(name0);
            ListBuffer<JCTree.JCStatement> caseStmts = new ListBuffer<>();
            for (int j = 0; j < fieldReaders.size(); j++) {
                AttributeInfo fieldReader = fieldReaders.get(j);
                int fieldReaderIndex = readerIndexMap.get(fieldReader);
                byte[] fieldName = fieldReader.name.getBytes(StandardCharsets.UTF_8);
                int fieldNameLength = fieldName.length;
                JCTree.JCMethodInvocation nextIfMethod;
                switch (fieldNameLength) {
                    case 2:
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match2");
                        break;
                    case 3:
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match3");
                        break;
                    case 4:
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match4", literal(fieldName[3]));
                        break;
                    case 5: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match5", literal(name1));
                        break;
                    }
                    case 6: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = fieldName[5];
                        bytes4[3] = '"';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match6", literal(name1));
                        break;
                    }
                    case 7: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match7", literal(name1));
                        break;
                    }
                    case 8: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match8", literal(name1), literal(fieldName[7]));
                        break;
                    }
                    case 9: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match9", literal(name1));
                        break;
                    }
                    case 10: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match10", literal(name1));
                        break;
                    }
                    case 11: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match11", literal(name1));
                        break;
                    }
                    case 12: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match12", literal(name1), literal(fieldName[11]));
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match13", literal(name1), literal(name2));
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match14", name1, name2);
                        break;
                    }
                    case 15: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match15", name1, name2);
                        break;
                    }
                    case 16: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match16", name1, name2, fieldName[15]);
                        break;
                    }
                    case 17: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match17", name1, name2);
                        break;
                    }
                    case 18: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match18", name1, name2);
                        break;
                    }
                    case 19: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match19", name1, name2);
                        break;
                    }
                    case 20: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match20", name1, name2, fieldName[19]);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match21", name1, name2, name3);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match22", name1, name2, name3);
                        break;
                    }
                    case 23: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match23", name1, name2, name3);
                        break;
                    }
                    case 24: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match24", name1, name2, name3, fieldName[23]);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match25", name1, name2, name3);
                        break;
                    }
                    case 26: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 19, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name3 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match26", name1, name2, name3);
                        break;
                    }
                    case 27: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match27", name1, name2, name3);
                        break;
                    }
                    case 28: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match28", name1, name2, name3, fieldName[27]);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match29", name1, name2, name3, name4);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match30", name1, name2, name3, name4);
                        break;
                    }
                    case 31: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match31", name1, name2, name3, name4);
                        break;
                    }
                    case 32: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match32", name1, name2, name3, name4, fieldName[31]);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match33", name1, name2, name3, name4);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match34", name1, name2, name3, name4);
                        break;
                    }
                    case 35: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match35", name1, name2, name3, name4);
                        break;
                    }
                    case 36: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match36", name1, name2, name3, name4, fieldName[35]);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match37", name1, name2, name3, name4, name5);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match38", name1, name2, name3, name4, name5);
                        break;
                    }
                    case 39: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match39", name1, name2, name3, name4, name5);
                        break;
                    }
                    case 40: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match40", name1, name2, name3, name4, name5, fieldName[39]);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match41", name1, name2, name3, name4, name5);
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
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match42", name1, name2, name3, name4, name5);
                        break;
                    }
                    case 43: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        long name5 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMethod = method(jsonReaderIdent, "nextIfName4Match43", name1, name2, name3, name4, name5);
                        break;
                    }
                    default:
                        throw new IllegalStateException("fieldNameLength " + fieldNameLength);
                }
                List<JCTree.JCStatement> readFieldValueStmts = genReadFieldValue(
                        fieldReader,
                        jsonReaderIdent,
                        fieldReaderIndex,
                        structInfo,
                        loopLabel,
                        objectIdent,
                        beanType,
                        isJsonb);
                caseStmts.append(defIf(nextIfMethod, block(readFieldValueStmts)));
            }
            caseStmts.append(defBreak(switchLabel));
            cases.append(defCase(literal(name0), caseStmts.toList()));
        }
        switchLabel.body = defSwitch(method(jsonReaderIdent, "getRawInt"), cases.toList());
        stmts = stmts.append(switchLabel);
        return stmts;
    }

    private JCTree.JCExpression genFieldValueList(
            String type,
            AttributeInfo attributeInfo,
            JCTree.JCIdent jsonReaderIdent,
            JCTree.JCVariableDecl fieldValueVar,
            JCTree.JCLabeledStatement loopLabel,
            ListBuffer<JCTree.JCStatement> stmts,
            int i,
            boolean referenceDetect,
            JCTree.JCFieldAccess fieldReaderField,
            JCTree.JCExpression beanType,
            boolean isJsonb) {
        String itemType = type.substring(15, type.length() - 1);
        boolean itemTypeIsClass = itemType.indexOf('<') == -1;
        if (itemTypeIsClass) {
            JCTree.JCMethodInvocation nextIfNullMethod = method(jsonReaderIdent, "nextIfNull");
            stmts.append(defIf(nextIfNullMethod,
                    block(exec(assign(fieldValueVar, defNull()))),
                    block(exec(assign(fieldValueVar, newClass(null, null, qualIdent("java.util.ArrayList"), null, null))))));

            String readDirectMethod = getReadDirectMethod(itemType);
            JCTree.JCIdent itemReaderIdent = ident(fieldItemObjectReader(i));
            if (readDirectMethod == null) {
                JCTree.JCFieldAccess getItemObjectReaderField = field(fieldReaderField, "getItemObjectReader");
                JCTree.JCExpressionStatement getItemObjectReaderExec = exec(assign(itemReaderIdent, method(getItemObjectReaderField, jsonReaderIdent)));
                stmts.append(defIf(eq(itemReaderIdent, defNull()), block(getItemObjectReaderExec)));
            }

            if (referenceDetect) {
                referenceDetect = isReference(itemType);
            }

            JCTree.JCVariableDecl for_iVar;
            if ("i".equals(attributeInfo.name)) {
                for_iVar = defVar("j", TypeTag.INT, literal(0));
            } else {
                for_iVar = defVar("i", TypeTag.INT, literal(0));
            }
            JCTree.JCMethodInvocation nextIfArrayStartMethod = method(jsonReaderIdent, "nextIfArrayStart");
            JCTree.JCMethodInvocation nextIfArrayEndMethod = method(jsonReaderIdent, "nextIfArrayEnd");
            ListBuffer<JCTree.JCStatement> whileStmts = new ListBuffer<>();
            JCTree.JCExpression item;
            if (readDirectMethod != null) {
                item = method(jsonReaderIdent, readDirectMethod);
            } else {
                item = cast(
                        qualIdent(itemType),
                        method(
                                itemReaderIdent,
                                isJsonb ? "readJSONBObject" : "readObject",
                                jsonReaderIdent, defNull(),
                                defNull(),
                                literal(0L)
                        )
                );
            }

            if (referenceDetect) {
                JCTree.JCVariableDecl listItemVar = defVar(attributeInfo.name + "_item", getFieldValueType(itemType), item);
                ListBuffer<JCTree.JCStatement> isReferenceStmts = new ListBuffer<>();
                JCTree.JCMethodInvocation readReferenceMethod = method(jsonReaderIdent, "readReference");
                JCTree.JCVariableDecl refVar = defVar("ref", ident("String"), readReferenceMethod);
                isReferenceStmts.append(refVar);
                JCTree.JCMethodInvocation addResolveTaskMethod = method(
                        jsonReaderIdent,
                        "addResolveTask",
                        ident(fieldValueVar),
                        ident(for_iVar),
                        method(
                                qualIdent("com.alibaba.fastjson2.JSONPath"),
                                "of",
                                ident(refVar)
                        )
                );
                isReferenceStmts.append(exec(addResolveTaskMethod));
                isReferenceStmts.append(exec(method(fieldValueVar, "add", defNull())));
                isReferenceStmts.append(defContinue(loopLabel));
                whileStmts.append(defIf(method(jsonReaderIdent, "isReference"), block(isReferenceStmts.toList())));
                whileStmts.append(listItemVar);
                item = ident(listItemVar);
            }

            whileStmts.append(exec(method(fieldValueVar, "add", item)));

            ListBuffer<JCTree.JCStatement> condStmts = new ListBuffer<>();
            if (referenceDetect) {
                condStmts.append(forLoop(
                        List.of(for_iVar),
                        unary(JCTree.Tag.NOT, nextIfArrayEndMethod),
                        List.of(exec(unary(JCTree.Tag.PREINC, ident(for_iVar)))),
                        block(whileStmts.toList())));
            } else {
                condStmts.append(whileLoop(unary(JCTree.Tag.NOT, nextIfArrayEndMethod), block(whileStmts.toList())));
            }

            stmts.append(defIf(nextIfArrayStartMethod, block(condStmts.toList())));
            return ident(fieldValueVar.name);
        }
        return null;
    }

    private JCTree.JCExpression genFieldValueMap(
            String type,
            AttributeInfo attributeInfo,
            JCTree.JCIdent jsonReaderIdent,
            JCTree.JCVariableDecl fieldValueVar,
            JCTree.JCLabeledStatement loopLabel,
            ListBuffer<JCTree.JCStatement> stmts,
            int i,
            boolean referenceDetect,
            boolean isJsonb
    ) {
        String itemType = type.substring(31, type.length() - 1);
        JCTree.JCMethodInvocation nextIfNullMethod = method(jsonReaderIdent, "nextIfNull");

        boolean readDirect = supportReadDirect(itemType);

        ListBuffer<JCTree.JCStatement> elseStmts = new ListBuffer<>();
        JCTree.JCIdent itemReaderIdent = ident(fieldItemObjectReader(i));
        if (!readDirect) {
            JCTree.JCFieldAccess getObjectReaderField = field(jsonReaderIdent, "getObjectReader");
            JCTree.JCExpressionStatement getItemObjectReaderExec = exec(
                    assign(
                            itemReaderIdent,
                            method(getObjectReaderField, field(qualIdent(itemType), names._class))
                    )
            );
            elseStmts.append(defIf(eq(itemReaderIdent, defNull()), block(getItemObjectReaderExec)));
        }

        elseStmts.append(exec(assign(fieldValueVar, newClass(null, null, qualIdent("java.util.HashMap"), null, null))));

        JCTree.JCMethodInvocation nextIfObjectStartMethod = method(jsonReaderIdent, "nextIfObjectStart");
        elseStmts.append(exec(nextIfObjectStartMethod));

        JCTree.JCMethodInvocation nextIfObjectEndMethod = method(jsonReaderIdent, "nextIfObjectEnd");
        ListBuffer<JCTree.JCStatement> whileStmts = new ListBuffer<>();

        if (referenceDetect) {
            referenceDetect = isReference(itemType);
        }

        JCTree.JCExpression mapEntryValueExpr;
        if (readDirect) {
            mapEntryValueExpr = cast(qualIdent(itemType), method(jsonReaderIdent, getReadDirectMethod(itemType)));
        } else {
            mapEntryValueExpr = cast(
                    qualIdent(itemType),
                    method(
                            itemReaderIdent,
                            isJsonb ? "readJSONBObject" : "readObject",
                            jsonReaderIdent,
                            field(qualIdent(itemType), names._class),
                            literal(attributeInfo.name), ident("features")
                    )
            );
        }

        JCTree.JCVariableDecl mapKeyVar = defVar(
                attributeInfo.name + "_key",
                ident("String"),
                method(jsonReaderIdent, "readFieldName")
        );
        whileStmts.append(mapKeyVar);
        JCTree.JCIdent mapKeyIdent = ident(mapKeyVar);

        if (referenceDetect) {
            ListBuffer<JCTree.JCStatement> isReferenceStmts = new ListBuffer<>();
            JCTree.JCMethodInvocation readReferenceMethod = method(jsonReaderIdent, "readReference");
            JCTree.JCVariableDecl refVar = defVar("ref", ident("String"), readReferenceMethod);
            isReferenceStmts.append(refVar);
            JCTree.JCMethodInvocation addResolveTaskMethod = method(
                    jsonReaderIdent,
                    "addResolveTask",
                    ident(fieldValueVar),
                    mapKeyIdent,
                    method(qualIdent("com.alibaba.fastjson2.JSONPath"), "of", ident(refVar))
            );
            isReferenceStmts.append(exec(addResolveTaskMethod));
            whileStmts.append(
                    defIf(
                            method(jsonReaderIdent, "isReference"),
                            block(isReferenceStmts.toList()),
                            exec(method(fieldValueVar, "put", mapKeyIdent, mapEntryValueExpr))
                    )
            );
        } else {
            whileStmts.append(exec(method(fieldValueVar, "put", mapKeyIdent, mapEntryValueExpr)));
        }

        elseStmts.append(whileLoop(unary(JCTree.Tag.NOT, nextIfObjectEndMethod), block(whileStmts.toList())));

        elseStmts.append(exec(method(jsonReaderIdent, "nextIfComma")));

        stmts.append(defIf(nextIfNullMethod,
                block(exec(assign(fieldValueVar, defNull()))),
                block(elseStmts.toList())));

        return ident(fieldValueVar);
    }

    private void genSource(StructInfo structInfo, TreePath treePath) {
        Filer filer = processingEnv.getFiler();
        String fullQualifiedName = structInfo.binaryName;
        JavaFileObject converterFile = null;
        try {
            if (treePath.getCompilationUnit() instanceof JCTree.JCCompilationUnit) {
                JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
                converterFile = filer.createSourceFile(fullQualifiedName, structInfo.element);
                String fileName = converterFile.getName() + ".txt";
                Path filePath = Paths.get(fileName);
                String dirName = fileName.substring(0, fileName.lastIndexOf(File.separator));
                Path dirPath = Paths.get(dirName);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                }
                try (Writer writer = new FileWriter(filePath.toString())) {
                    writer.write(compilationUnit.toString());
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.WARNING, "Failed to generate source file for "
                            + fullQualifiedName + " caused by " + e.getMessage());
                }
            } else {
                messager.printMessage(Diagnostic.Kind.WARNING, "Failed to generate source file for "
                        + fullQualifiedName + " caused by invalid compilation unit");
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Failed to generate source file for "
                    + fullQualifiedName + " caused by " + e.getMessage());
        } finally {
            if (converterFile != null) {
                converterFile.delete();
            }
            if (filer instanceof JavacFiler) {
                ((JavacFiler) filer).close();
            }
        }
    }

    private void genReflect(Set<ReflectionMetadata> reflects) {
        File file = null;
        try {
            file = new File("");
            String absolutePath = file.getAbsolutePath() +
                    File.separator +
                    Arrays.stream("src/main/resources/META-INF/native-image/reflect-config.json".split("/"))
                            .sequential()
                            .collect(Collectors.joining(File.separator));
            int idx = absolutePath.lastIndexOf(File.separator);
            Path dirPath = Paths.get(absolutePath.substring(0, idx));
            Path filePath = Paths.get(absolutePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String content = reader.lines().collect(Collectors.joining());
                java.util.List<ReflectionMetadata> reflectionMetadata = JSON.parseArray(content, ReflectionMetadata.class);
                if (reflectionMetadata != null) {
                    reflects.addAll(reflectionMetadata);
                }
                String jsonString = JSON.toJSONString(reflects, PrettyFormat);
                try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                    writer.write(jsonString);
                }
            }
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Failed to generate reflect-config.json caused by " + e.getMessage());
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    private boolean isDebug(JCTree.JCClassDecl beanClassDecl) {
        JCTree.JCExpression jsonCompiledIdent = qualIdent(JSONCompiled.class.getName());
        List<JCTree.JCAnnotation> annotations = beanClassDecl.mods.annotations;
        Optional<JCTree.JCAnnotation> jsonCompiledAnnoOpt = annotations.stream()
                .filter(a -> a.getAnnotationType().type.tsym.toString().equals(jsonCompiledIdent.type.tsym.toString()))
                .findAny();
        if (jsonCompiledAnnoOpt.isPresent()) {
            JCTree.JCAnnotation jsonCompiledAnno = jsonCompiledAnnoOpt.get();
            Optional<JCTree.JCAssign> jsonCompiledAsgOpt = jsonCompiledAnno.args.stream()
                    .map(a -> (JCTree.JCAssign) a)
                    .filter(a2 -> "debug".equals(a2.lhs.toString()))
                    .findAny();
            if (jsonCompiledAsgOpt.isPresent()) {
                JCTree.JCAssign jcAssign = jsonCompiledAsgOpt.get();
                return "true".equals(jcAssign.rhs.toString());
            }
        }
        return false;
    }

    private JCTree.JCExpression getFieldValueType(String type) {
        if (type.contains("[")) {
            return arrayIdentType(type);
        }
        if (type.contains("<")) {
            return collectionIdent(type);
        }
        return qualIdent(type);
    }

    private String findConverterName(StructInfo structInfo, String suffix) {
        int dotIndex;
        dotIndex = structInfo.binaryName.lastIndexOf('$');
        if (dotIndex != -1) {
            dotIndex = structInfo.binaryName.lastIndexOf('.');
        }
        String className = structInfo.binaryName.substring(dotIndex + 1);
        if (dotIndex == -1) {
            return className + suffix;
        }
        String packageName = structInfo.binaryName.substring(0, dotIndex);
        return packageName + '.' + className + suffix;
    }

    static final class MethodWriterContext {
        final JCTree.JCExpression beanType;
        final JCTree.JCIdent objectType;
        final JCTree.JCIdent jsonWriter = ident("jsonWriter");
        final JCTree.JCIdent object = ident("object");
        final JCTree.JCIdent fieldName = ident("fieldName");
        final JCTree.JCIdent fieldType = ident("fieldType");
        final JCTree.JCIdent features = ident("features");

        final JCTree.JCIdent contextFeatures = ident("contextFeatures");
        final JCTree.JCIdent notWriteDefaultValue = ident("notWriteDefaultValue");
        final JCTree.JCIdent nameDirect = ident("nameDirect");
        final JCTree.JCIdent writeNulls = ident("writeNulls");
        final boolean jsonb;

        public MethodWriterContext(JCTree.JCExpression beanType, JCTree.JCIdent objectType, boolean jsonb) {
            this.beanType = beanType;
            this.objectType = objectType;
            this.jsonb = jsonb;
        }

        void genVariantsMethodBefore(ListBuffer<JCTree.JCStatement> body) {
            JCTree.JCVariableDecl contextFeaturesVar
                    = defVar("contextFeatures", TypeTag.LONG, jsonWriterMethod("getFeatures"));
            body.append(contextFeaturesVar);

            JCTree.JCVariableDecl nameDirect;
            if (!jsonb) {
                nameDirect = defVar("nameDirect", TypeTag.BOOLEAN,
                        and(
                                not(jsonWriterField("useSingleQuote")),
                                isDisable(contextFeatures, UnquoteFieldName, UseSingleQuotes)
                        )
                );
            } else {
                nameDirect = defVar("nameDirect", TypeTag.BOOLEAN,
                        ternary(
                                notNull(jsonWriterField("symbolTable")),
                                literal(false),
                                isDisable(contextFeatures, WriteNameAsSymbol)
                        )
                );
            }
            body.append(nameDirect);

            body.append(
                    defVar("notWriteDefaultValue", TypeTag.BOOLEAN, isEnable(contextFeatures, NotWriteDefaultValue))
            );

            body.append(
                    defVar(
                            "writeNulls",
                            TypeTag.BOOLEAN,
                            ternary(this.notWriteDefaultValue, isEnable(contextFeatures, WriteNulls, NullAsDefaultValue), false)
                    )
            );
        }

        JCTree.JCExpression jsonWriterField(String fieldName) {
            return field(jsonWriter, fieldName);
        }

        JCTree.JCExpression jsonWriterMethod(String fieldName) {
            return method(jsonWriter, fieldName);
        }

        JCTree.JCExpression jsonWriterMethod(String fieldName, List<JCTree.JCExpression> args) {
            return method(jsonWriter, fieldName, args);
        }

        JCTree.JCExpression jsonWriterMethod(String fieldName, JCTree.JCExpression arg) {
            return method(jsonWriter, fieldName, List.of(arg));
        }

        JCTree.JCExpression jsonWriterMethod(String fieldName, JCTree.JCVariableDecl arg) {
            return method(jsonWriter, fieldName, List.of(ident(arg)));
        }

        JCTree.JCExpression jsonWriterMethod(String fieldName, String arg) {
            return method(jsonWriter, fieldName, List.of(literal(arg)));
        }

        JCTree.JCExpression jsonWriterMethod(
                String fieldName,
                JCTree.JCExpression arg0,
                JCTree.JCExpression arg1
        ) {
            return method(jsonWriter, fieldName, List.of(arg0, arg1));
        }

        JCTree.JCExpression jsonWriterMethod(
                String fieldName,
                JCTree.JCExpression arg0,
                JCTree.JCVariableDecl arg1
        ) {
            return method(jsonWriter, fieldName, List.of(arg0, ident(arg1)));
        }
    }
}
