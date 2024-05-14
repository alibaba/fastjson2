package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.internal.codegen.Label;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.google.googlejavaformat.java.Formatter;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.*;
import static com.alibaba.fastjson2.internal.processor.JavacTreeUtils.*;
import static com.alibaba.fastjson2.internal.processor.JavacTreeUtils.qualIdent;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

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
                        int dotIdx = innerReadClassFQN.lastIndexOf('.');
                        String innerReadClassName = innerReadClassFQN.substring(dotIdx + 1);
                        JCTree.JCExpression beanType = null;
                        if (beanClassFQN.contains(".")) {
                            if (element instanceof Symbol.ClassSymbol) {
                                Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) element;
                                String owner = classSymbol.owner.toString();
                                dotIdx = owner.indexOf(".");
                                beanType = field(dotIdx == -1 ? ident(owner) : qualIdent(owner), beanClassFQN.substring(beanClassFQN.lastIndexOf(".") + 1));
                            }
                        } else {
                            dotIdx = beanClassFQN.indexOf(".");
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

                        // generate source file
//                        genSource(innerReadClassFQN, structInfo, innerReadClass);

                        // initialization
                        String innerWriteClassFQN = findConverterName(structInfo, "_FASTJSONWriter");
                        dotIdx = innerWriteClassFQN.lastIndexOf('.');
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

                        // generate source file
//                        genSource(innerWriteClassFQN, structInfo, innerWriteClass);
                    }
                }
            });
        });
        return true;
    }

    private void addInnerClassIfAbsent(JCTree.JCClassDecl beanClassDecl, String beanClassFQN, String innerClassName, String key) {
        JCTree.JCExpression jsonTypeIdent = qualIdent("com.alibaba.fastjson2.annotation.JSONType");
        List<JCTree.JCAnnotation> annotations = beanClassDecl.mods.annotations;
        Optional<JCTree.JCAnnotation> jsonTypeAnnoOpt = annotations.stream()
                .filter(a -> a.getAnnotationType().type.tsym.toString().equals(jsonTypeIdent.type.tsym.toString()))
                .findAny();
        int dotIdx = beanClassFQN.lastIndexOf('.');
        String beanClassName = beanClassFQN.substring(dotIdx + 1);
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
        JCTree.JCMethodInvocation superMethod = method(ident(names._super), List.of(field(beanType, names._class), defNull(), defNull(), literal(TypeTag.LONG, features), lambda, defNull(), fieldReadersArray));
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
        JCTree.JCMethodInvocation fieldReadersList = method(field(qualIdent("java.util.Arrays"), "asList"), List.of(fieldReadersArray));
        JCTree.JCMethodInvocation superMethod = method(ident(names._super), List.of(field(beanType, names._class), defNull(), defNull(), literal(TypeTag.LONG, 0L), fieldReadersList));
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
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(attributeInfo.name), memberedReference));
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(List.of(defVar(Flags.PARAMETER, "bean", beanType)), field(ident("bean"), fieldName));
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(fieldName), lambda));
            }
        } else if ("java.lang.Class".equals(type)) {
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(attributeInfo.name), field(beanType, names._class), memberedReference));
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(List.of(defVar(Flags.PARAMETER, "bean", beanType)), field(ident("bean"), fieldName));
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(fieldName), field(beanType, names._class), lambda));
            }
        } else if ("java.lang.Type".equals(type)) {
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(attributeInfo.name), field(beanType, names._class), field(beanType, names._class), memberedReference));
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(List.of(defVar(Flags.PARAMETER, "bean", beanType)), field(ident("bean"), fieldName));
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(fieldName), field(beanType, names._class), field(beanType, names._class), lambda));
            }
        } else if ("java.util.List<java.lang.String>".equals(type)) {
            if (isMethodReference) {
                String methodName = attributeInfo.getMethod.getSimpleName().toString();
                JCTree.JCMemberReference memberedReference = methodRef(beanType, methodName);
                return method(field(objectWritersType, "fieldWriterListString"), List.of(literal(attributeInfo.name), memberedReference));
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(List.of(defVar(Flags.PARAMETER, "bean", beanType)), field(ident("bean"), fieldName));
                return method(field(objectWritersType, "fieldWriterListString"), List.of(literal(fieldName), lambda));
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
                return method(field(objectWritersType, "fieldWriterList"), List.of(literal(attributeInfo.name), itemTypeClass, memberedReference));
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(List.of(defVar(Flags.PARAMETER, "bean", beanType)), field(ident("bean"), fieldName));
                return method(field(objectWritersType, "fieldWriterList"), List.of(literal(fieldName), itemTypeClass, lambda));
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
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(attributeInfo.name), fieldClass, memberedReference));
            } else {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                JCTree.JCLambda lambda = lambda(List.of(defVar(Flags.PARAMETER, "bean", beanType)), field(ident("bean"), fieldName));
                return method(field(objectWritersType, "fieldWriter"), List.of(literal(fieldName), fieldClass, lambda));
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
            JCTree.JCVariableDecl object = defVar(Flags.PARAMETER, "o", beanType);
            JCTree.JCExpression valueType = identType;
            if (fieldType.startsWith("java.util.Map<")) {
                valueType = qualIdent("java.util.Map");
            }
            JCTree.JCVariableDecl fieldValue = defVar(Flags.PARAMETER, "v", valueType);
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
            return method(field(objectReadersType, methodName), List.of(fieldName, lambda));
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
                    field(objectReadersType, "fieldReader"),
                    List.of(
                            fieldName,
                            field(getFieldValueType(fieldType), names._class),
                            lambda
                    )
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
            readerMethod = method(field(objectReadersType, fieldReaderMethodName), args);
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
                    field(objectReadersType, "fieldReaderListStr"),
                    List.of(
                            fieldName,
                            lambda
                    )
            );
        } else {
            readerMethod = method(
                    field(objectReadersType, "fieldReaderList"),
                    List.of(
                            fieldName,
                            field(getFieldValueType(itemType), names._class),
                            constructorRef(qualIdent("java.util.ArrayList")),
                            lambda
                    )
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
                field(objectReadersType, "fieldReaderMap"),
                List.of(
                        fieldName,
                        field(getFieldValueType(mapType), names._class),
                        field(getFieldValueType(keyType), names._class),
                        field(getFieldValueType(valueType), names._class),
                        lambda
                )
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
                stmts.append(exec(assign(ident(fieldWriter(i)), cast(qualIdent("com.alibaba.fastjson2.writer.FieldWriter"), indexed(fieldWriters, literal(i))))));
            }
        }
        return stmts.toList();
    }

    private JCTree.JCMethodDecl genCreateInstance(JCTree.JCIdent objectType, JCTree.JCNewClass beanNew) {
        JCTree.JCVariableDecl featuresVar = defVar(Flags.PARAMETER, "features", type(TypeTag.LONG));
        return defMethod(Flags.PUBLIC, "createInstance", objectType, null, List.of(featuresVar), null, block(defReturn(beanNew)), null);
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

        JCTree.JCVariableDecl jsonReaderVar = defVar(Flags.PARAMETER, "jsonReader", qualIdent(JSONReader.class.getName()));
        JCTree.JCIdent jsonReaderIdent = ident(jsonReaderVar.name);
        JCTree.JCVariableDecl fieldTypeVar = defVar(Flags.PARAMETER, "fieldType", qualIdent(Type.class.getName()));
        JCTree.JCVariableDecl fieldNameVar = defVar(Flags.PARAMETER, "fieldName", objectType);
        JCTree.JCVariableDecl featuresVar = defVar(Flags.PARAMETER, "features", type(TypeTag.LONG));

        JCTree.JCReturn nullReturn = defReturn(defNull());

        ListBuffer<JCTree.JCStatement> readObjectBody = new ListBuffer<>();

        JCTree.JCMethodInvocation nextIfNullMethod = method(field(jsonReaderIdent, "nextIfNull"));
        readObjectBody.append(defIf(nextIfNullMethod, block(nullReturn), null));

        readObjectBody.append(exec(method(field(jsonReaderIdent, "nextIfObjectStart"))));

        JCTree.JCVariableDecl features2Var = defVar(
                Flags.PARAMETER,
                "features2",
                type(TypeTag.LONG),
                binary(
                        JCTree.Tag.BITOR,
                        ident("features"),
                        field(ident(names._this), "features")
                )
        );
        readObjectBody.append(features2Var);

        JCTree.JCVariableDecl objectVar = defVar(Flags.PARAMETER, "object", beanType, beanNew);
        JCTree.JCIdent objectIdent = ident(objectVar.name);
        readObjectBody.append(objectVar);

        int fieldsSize = attributeInfos.size();

        JCTree.JCLabeledStatement loopLabel = label("_while", null);
        JCTree.JCWhileLoop loopHead = whileLoop(unary(JCTree.Tag.NOT, method(field(jsonReaderIdent, "nextIfObjectEnd"))), null);
        ListBuffer<JCTree.JCStatement> loopBody = new ListBuffer<>();

        boolean switchGen = false;
        if (fieldNameLengthMin >= 2 && fieldNameLengthMax <= 43) {
            loopBody.appendList(genRead243(attributeInfos, jsonReaderIdent, structInfo, loopLabel, objectIdent, beanType, isJsonb));
            switchGen = true;
        }

        JCTree.JCFieldAccess readFieldNameHashCode = field(jsonReaderIdent, "readFieldNameHashCode");
        JCTree.JCVariableDecl hashCode64Var = defVar(Flags.PARAMETER, "hashCode64", type(TypeTag.LONG), method(readFieldNameHashCode));
        JCTree.JCExpression hashCode64 = ident(hashCode64Var.name);
        loopBody.append(hashCode64Var);

        if (switchGen && !isJsonb) {
            loopBody.append(
                    exec(method(
                            field(ident(names._this), "readFieldValue"),
                            List.of(hashCode64, jsonReaderIdent, ident(features2Var.name), ident(objectVar.name))
                    )));
        } else {
            if (fieldsSize <= 6) {
                for (int i = 0; i < fieldsSize; ++i) {
                    AttributeInfo attributeInfo = attributeInfos.get(i);
                    List<JCTree.JCStatement> readFieldValueStmts = genReadFieldValue(attributeInfo, jsonReaderIdent, i, structInfo, loopLabel, objectIdent, beanType, isJsonb);
                    loopBody.appendList(List.of(defIf(binary(JCTree.Tag.EQ, literal(TypeTag.LONG, attributeInfo.nameHashCode), hashCode64), block(readFieldValueStmts), null)));
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
                JCTree.JCExpression hashCode32 = ident(hashCode32Var.name);

                JCTree.JCLabeledStatement switchLabel = label("_switch", null);
                ListBuffer<JCTree.JCCase> cases = new ListBuffer<>();
                for (int i = 0; i < hashCode32Keys.length; ++i) {
                    java.util.List<Long> hashCode64Array = map.get(hashCode32Keys[i]);
                    List<JCTree.JCStatement> stmts = List.nil();
                    Long fieldNameHash = null;
                    if (hashCode64Array.size() == 1 && hashCode64Array.get(0) == hashCode32Keys[i]) {
                        fieldNameHash = hashCode64Array.get(0);
                        int index = mappingIndex.get(fieldNameHash);
                        AttributeInfo attributeInfo = mapping.get(fieldNameHash);
                        stmts = stmts.appendList(genReadFieldValue(attributeInfo, jsonReaderIdent, index, structInfo, loopLabel, objectIdent, beanType, isJsonb));
                        stmts.append(defContinue(loopLabel));
                    } else {
                        for (int j = 0; j < hashCode64Array.size(); ++j) {
                            fieldNameHash = hashCode64Array.get(j);
                            int index = mappingIndex.get(fieldNameHash);
                            AttributeInfo field = mapping.get(fieldNameHash);
                            List<JCTree.JCStatement> stmtsIf = genReadFieldValue(field, jsonReaderIdent, index, structInfo, loopLabel, objectIdent, beanType, isJsonb);
                            stmts = stmts.append(defIf(binary(JCTree.Tag.EQ, hashCode64, literal(fieldNameHash)), block(stmtsIf), null));
                            stmts.append(defContinue(loopLabel));
                        }
                        stmts.append(defBreak(switchLabel));
                    }
                    cases.append(defCase(getHashCode32Var(literal(fieldNameHash)).getInitializer(), stmts));
                }
                switchLabel.body = defSwitch(hashCode32, cases.toList());
                loopBody.append(switchLabel);
            }

            loopBody.append(defIf(method(field(ident(names._this), "readFieldValueWithLCase"), List.of(jsonReaderIdent, objectIdent, ident(hashCode64Var.name), ident(features2Var.name))), block(defContinue(loopLabel)), null));
            JCTree.JCFieldAccess processExtraField = field(ident(names._this), "processExtra");
            loopBody.append(exec(method(processExtraField, List.of(jsonReaderIdent, objectIdent, ident(features2Var.name)))));
        }

        loopHead.body = block(loopBody.toList());
        loopLabel.body = loopHead;
        readObjectBody.append(loopLabel);

        if (!isJsonb) {
            readObjectBody.append(exec(method(field(jsonReaderIdent, "nextIfComma"))));
        }

        readObjectBody.append(defReturn(objectIdent));

        return defMethod(
                Flags.PUBLIC,
                isJsonb ? "readJSONBObject" : "readObject",
                objectType,
                null,
                List.of(jsonReaderVar, fieldTypeVar, fieldNameVar, featuresVar),
                null,
                block(readObjectBody.toList()),
                null
        );
    }

    private JCTree.JCMethodDecl genWrite(
            JCTree.JCIdent objectType,
            JCTree.JCExpression beanType,
            JCTree.JCNewClass beanNew,
            java.util.List<AttributeInfo> attributeInfos,
            StructInfo structInfo,
            boolean isJsonb) {
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

        JCTree.JCVariableDecl jsonWriterVar = defVar(Flags.PARAMETER, "jsonWriter", qualIdent(JSONWriter.class.getName()));
        JCTree.JCIdent jsonWriterIdent = ident(jsonWriterVar.name);
        JCTree.JCVariableDecl objectVar = defVar(Flags.PARAMETER, "object", objectType);
        JCTree.JCIdent objectIdent = ident(objectVar.name);
        JCTree.JCVariableDecl fieldNameVar = defVar(Flags.PARAMETER, "fieldName", objectType);
        JCTree.JCVariableDecl fieldTypeVar = defVar(Flags.PARAMETER, "fieldType", qualIdent(Type.class.getName()));
        JCTree.JCVariableDecl featuresVar = defVar(Flags.PARAMETER, "features", type(TypeTag.LONG));

        ListBuffer<JCTree.JCStatement> writeBody = new ListBuffer<>();

        JCTree.JCVariableDecl contextFeaturesVar = defVar(Flags.PARAMETER, "contextFeatures", type(TypeTag.LONG), method(field(jsonWriterIdent, "getFeatures")));
        JCTree.JCIdent contextFeaturesIdent = ident(contextFeaturesVar.name);
        writeBody.append(contextFeaturesVar);

        JCTree.JCUnary unary = unary(JCTree.Tag.NOT, field(jsonWriterIdent, "useSingleQuote"));
        JCTree.JCBinary binary = binary(JCTree.Tag.NE, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, UnquoteFieldName.mask), literal(TypeTag.LONG, UseSingleQuotes.mask)), literal(0));
        JCTree.JCVariableDecl quoteVar = defVar(Flags.PARAMETER, "quote", type(TypeTag.BOOLEAN), binary(JCTree.Tag.AND, unary, binary));
        JCTree.JCIdent quoteIdent = ident(quoteVar.name);
        writeBody.append(quoteVar);

        JCTree.JCVariableDecl notWriteDefaultValueVar = defVar(Flags.PARAMETER, "notWriteDefaultValue", type(TypeTag.LONG), binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, NotWriteDefaultValue.mask)));
        writeBody.append(notWriteDefaultValueVar);

        JCTree.JCConditional ternary12 = ternary(binary(JCTree.Tag.EQ, ident(notWriteDefaultValueVar.name), literal(0)), literal(0), ternary(binary(JCTree.Tag.LT, ident(notWriteDefaultValueVar.name), literal(0)), literal(-1), literal(1)));
        JCTree.JCVariableDecl var12Var = defVar(Flags.PARAMETER, "var12", type(TypeTag.INT), ternary12);
        writeBody.append(var12Var);

        JCTree.JCVariableDecl nullAsDefaultVar = defVar(Flags.PARAMETER, "nullAsDefault", type(TypeTag.LONG), literal(0L));
        JCTree.JCIdent nullAsDefaultIdent = ident(nullAsDefaultVar.name);
        writeBody.append(nullAsDefaultVar);
        ListBuffer<JCTree.JCStatement> thenStmts = new ListBuffer<>();
        thenStmts.append(exec(assign(nullAsDefaultIdent, binary(JCTree.Tag.BITAND, contextFeaturesIdent, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, WriteNulls.mask), literal(TypeTag.LONG, NullAsDefaultValue.mask))))));
        JCTree.JCConditional ternary17 = ternary(binary(JCTree.Tag.EQ, nullAsDefaultIdent, literal(0)), literal(0), ternary(binary(JCTree.Tag.LT, nullAsDefaultIdent, literal(0)), literal(-1), literal(1)));
        JCTree.JCVariableDecl var17Var = defVar(Flags.PARAMETER, "var17", type(TypeTag.INT), ternary17);
        thenStmts.append(var17Var);
        writeBody.append(defIf(binary(JCTree.Tag.NE, ident(var12Var.name), literal(0)), block(defVar(Flags.PARAMETER, "var13", type(TypeTag.BOOLEAN), literal(false))), block(thenStmts.toList())));

        ListBuffer<JCTree.JCStatement> elseStmts = new ListBuffer<>();

        elseStmts.append(exec(method(field(jsonWriterIdent, "startObject"))));
        JCTree.JCVariableDecl var7Var = defVar(Flags.PARAMETER, "var7", type(TypeTag.BOOLEAN), literal(true));
        elseStmts.append(var7Var);
        JCTree.JCBinary notnullBinary = binary(JCTree.Tag.NE, objectIdent, defNull());
        JCTree.JCBinary notClassBinary = binary(JCTree.Tag.NE, method(field(objectIdent, "getClass")), ident(fieldTypeVar.name));
        JCTree.JCMethodInvocation isWriteTypeInfoMethod = method(field(jsonWriterIdent, "isWriteTypeInfo"), List.of(objectIdent, ident(fieldTypeVar.name), ident(featuresVar.name)));
        elseStmts.append(defIf(binary(JCTree.Tag.AND, notnullBinary, binary(JCTree.Tag.AND, notClassBinary, isWriteTypeInfoMethod)),
                block(exec(assign(ident(var7Var.name), binary(JCTree.Tag.BITXOR, method(field(ident(names._this), "writeTypeInfo"), List.of(jsonWriterIdent)), literal(true))))),
                null));

        for (int i = 0; i < attributeInfos.size(); ++i) {
            AttributeInfo attributeInfo = attributeInfos.get(i);
            elseStmts.appendList(genWriteField(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, nullAsDefaultIdent, isJsonb));
        }

        elseStmts.append(exec(method(field(jsonWriterIdent, "endObject"))));

        JCTree.JCIf errorOnNoneSerializableIf = defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, ErrorOnNoneSerializable.mask)), literal(TypeTag.LONG, 0L)),
                block(List.of(exec(method(field(ident(names._this), "errorOnNoneSerializable"))))),
                block(elseStmts.toList()));

        JCTree.JCIf ignoreNoneSerializableIf = defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, IgnoreNoneSerializable.mask)), literal(TypeTag.LONG, 0L)),
                block(List.of(exec(method(field(jsonWriterIdent, "writeNull"))))),
                block(List.of(errorOnNoneSerializableIf)));

        JCTree.JCIf hasFilterIf = defIf(method(field(ident(names._this), "hasFilter"), List.of(ident(jsonWriterVar.name))),
                block(exec(method(field(ident(names._this), "writeWithFilter"), List.of(ident(jsonWriterVar.name), objectIdent, ident(fieldNameVar.name), ident(fieldTypeVar.name), ident(featuresVar.name))))),
                block(List.of(ignoreNoneSerializableIf)));

        JCTree.JCIf beanToArrayIf = defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, BeanToArray.mask)), literal(TypeTag.LONG, 0L)),
                block(exec(method(field(ident(names._this), "writeArrayMapping"), List.of(ident(jsonWriterVar.name), objectIdent, ident(fieldNameVar.name), ident(fieldTypeVar.name), ident(featuresVar.name))))),
                block(List.of(hasFilterIf)));

        JCTree.JCIf jsonbIfStmts = defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, BeanToArray.mask)), literal(TypeTag.LONG, 0L)),
                block(exec(method(field(ident(names._this), "writeArrayMappingJSONB"), List.of(ident(jsonWriterVar.name), objectIdent, ident(fieldNameVar.name), ident(fieldTypeVar.name), ident(featuresVar.name))))),
                block(exec(method(field(ident(names._this), "writeJSONB"), List.of(ident(jsonWriterVar.name), objectIdent, ident(fieldNameVar.name), ident(fieldTypeVar.name), ident(featuresVar.name))))));
        JCTree.JCIf jsonbIf = defIf(field(jsonWriterIdent, "jsonb"), block(jsonbIfStmts), block(List.of(beanToArrayIf)));

        writeBody.append(defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, IgnoreErrorGetter.mask), literal(TypeTag.LONG, UnquoteFieldName.mask))), literal(TypeTag.LONG, 0L)),
                block(exec(method(field(ident(names._super), "write"), List.of(ident(jsonWriterVar.name), objectIdent, ident(fieldNameVar.name), ident(fieldTypeVar.name), ident(featuresVar.name))))),
                block(List.of(jsonbIf))));

        return defMethod(Flags.PUBLIC, "write", type(TypeTag.VOID), null, List.of(jsonWriterVar, objectVar, fieldNameVar, fieldTypeVar, featuresVar), null, block(writeBody.toList()), null);
    }

    private JCTree.JCStatement genWriteFieldName(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent quoteIdent,
            boolean isJsonb) {
        String methodName;
        if (!isJsonb) {
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
                JCTree.JCConditional ternary = ternary(quoteIdent, literal(TypeTag.LONG, nameIn64DoubleQuote), literal(TypeTag.LONG, nameIn64SingleQuote));
                if (length == 9) {
                    ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
                    JCTree.JCConditional ternary2 = ternary(quoteIdent, literal(TypeTag.INT, name12), literal(TypeTag.INT, name1SQ2));
                    JCTree.JCVariableDecl name1Var = defVar(Flags.PARAMETER, "name1", type(TypeTag.INT), ternary2);
                    stmts.append(defVar(Flags.PARAMETER, "name1", type(TypeTag.INT), ternary2));
                    stmts.append(exec(method(field(jsonWriterIdent, methodName), List.of(ternary, ident(name1Var.name)))));
                    return block(stmts.toList());
                } else if (length > 9) {
                    JCTree.JCConditional ternary2 = ternary(quoteIdent, literal(TypeTag.LONG, name1), literal(TypeTag.LONG, name1SQ));
                    return exec(method(field(jsonWriterIdent, methodName), List.of(ternary, ternary2)));
                } else {
                    return exec(method(field(jsonWriterIdent, methodName), List.of(ternary)));
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
        return null;
    }

    private ListBuffer<JCTree.JCStatement> genWriteField(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            JCTree.JCIdent nullAsDefaultIdent,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        if ("boolean".equals(type)) {
            return genWriteFieldValueBooleanV(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, isJsonb);
        } else if ("boolean[]".equals(type)
                || "byte[]".equals(type)
                || "char[]".equals(type)
                || "short[]".equals(type)
                || "float[]".equals(type)
                || "double[]".equals(type)) {
            return stmts.appendList(genWriteFieldValueArray(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, isJsonb));
        } else if ("int".equals(type)) {
            return stmts.appendList(genWriteFieldValueInt32V(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, quoteIdent, var12Var, isJsonb));
        } else if ("char".equals(type)
                || "byte".equals(type)
                || "int".equals(type)
                || "short".equals(type)
                || "float".equals(type)
                || "double".equals(type)) {
            stmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            return stmts.appendList(genWriteFieldValue(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, isJsonb));
        } else if ("int[]".equals(type)) {
            return stmts.appendList(genWriteFieldValueIntVA(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, quoteIdent, var12Var, nullAsDefaultIdent, isJsonb));
        } else if ("long".equals(type)) {
            return stmts.appendList(genWriteFieldValueInt64V(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, quoteIdent, var12Var, isJsonb));
        } else if ("long[]".equals(type)) {
            return stmts.appendList(genWriteFieldValueInt64VA(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, quoteIdent, var12Var, nullAsDefaultIdent, isJsonb));
        } else if ("java.lang.Integer".equals(type)) {
            return stmts.appendList(genWriteInt32(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else if ("java.lang.Long".equals(type)) {
            return stmts.appendList(genWriteInt64(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else if ("java.lang.Float".equals(type)) {
            return stmts.appendList(genWriteFloat(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else if ("java.lang.Double".equals(type)) {
            return stmts.appendList(genWriteDouble(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else if ("java.lang.String".equals(type)) {
            return stmts.appendList(genWriteFieldValueString(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else if (attributeInfo.type instanceof com.sun.tools.javac.code.Type.ClassType
                && ((com.sun.tools.javac.code.Type.ClassType) attributeInfo.type).supertype_field != null
                && "java.lang.Enum".equals(((com.sun.tools.javac.code.Type.ClassType) attributeInfo.type).supertype_field.tsym.toString())) {
            return stmts.appendList(genWriteFieldValueEnum(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else if ("java.util.Date".equals(type)) {
            return stmts.appendList(genWriteFieldValueDate(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else if (type.contains("java.util.List")) {
            return stmts.appendList(genWriteFieldValueList(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, var12Var, isJsonb));
        } else {
            return stmts.appendList(genWriteFieldValueObject(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, contextFeaturesIdent, quoteIdent, isJsonb));
        }
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValue(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        List<JCTree.JCExpression> fieldValueExprs = List.of(genWriteFieldValue(attributeInfo, objectIdent, beanType));
        JCTree.JCBinary writeAsStringBinary = binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, field(field(ident(names._this), fieldWriter(i)), "features"), literal(TypeTag.LONG, WriteNonStringValueAsString.mask)), literal(0));
        if ("boolean".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeBool"), fieldValueExprs)));
        } else if ("char".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeChar"), fieldValueExprs)));
        } else if ("byte".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(method(field(jsonWriterIdent, "writeString"), fieldValueExprs))),
                    block(exec(method(field(jsonWriterIdent, "writeInt8"), fieldValueExprs)))));
        } else if ("short".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(method(field(jsonWriterIdent, "writeString"), fieldValueExprs))),
                    block(exec(method(field(jsonWriterIdent, "writeInt16"), fieldValueExprs)))));
        } else if ("int".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(method(field(jsonWriterIdent, "writeString"), fieldValueExprs))),
                    block(exec(method(field(jsonWriterIdent, "writeInt32"), fieldValueExprs)))));
        } else if ("Integer".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeInt32"), fieldValueExprs)));
        } else if ("long".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(method(field(jsonWriterIdent, "writeString"), fieldValueExprs))),
                    block(exec(method(field(jsonWriterIdent, "writeInt64"), fieldValueExprs)))));
        } else if ("Long".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeInt64"), fieldValueExprs)));
        } else if ("float".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(method(field(jsonWriterIdent, "writeString"), fieldValueExprs))),
                    block(exec(method(field(jsonWriterIdent, "writeFloat"), fieldValueExprs)))));
        } else if ("double".equals(type)) {
            stmts.append(defIf(writeAsStringBinary,
                    block(exec(method(field(jsonWriterIdent, "writeString"), fieldValueExprs))),
                    block(exec(method(field(jsonWriterIdent, "writeDouble"), fieldValueExprs)))));
        } else if ("boolean[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeBool"), fieldValueExprs)));
        } else if ("char[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeString"), fieldValueExprs)));
        } else if ("byte[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeBinary"), fieldValueExprs)));
        } else if ("short[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeInt16"), fieldValueExprs)));
        } else if ("int[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeInt32"), fieldValueExprs)));
        } else if ("long[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeInt64"), fieldValueExprs)));
        } else if ("float[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeFloat"), fieldValueExprs)));
        } else if ("double[]".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeDouble"), fieldValueExprs)));
        } else if ("BigDecimal".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeDecimal"), fieldValueExprs)));
        } else if ("Enum".equals(type)) {
            stmts.append(exec(method(field(jsonWriterIdent, "writeEnum"), fieldValueExprs)));
        } else {
            throw new UnsupportedOperationException();
        }
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueBooleanV(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl boolVar = defVar(Flags.PARAMETER, "bool" + i, type(TypeTag.BOOLEAN), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(boolVar);
        stmts.append(defIf(unary(JCTree.Tag.NOT, ident(boolVar.name)), block(List.nil()), null));
        stmts.append(exec(method(field(field(ident(names._this), fieldWriter(i)), "writeBool"), List.of(jsonWriterIdent, ident(boolVar.name)))));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueArray(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            boolean isJsonb) {
        String type = attributeInfo.type.toString();
        String methodName;
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        List<JCTree.JCExpression> fieldValueExprs = List.of(jsonWriterIdent, genWriteFieldValue(attributeInfo, objectIdent, beanType));
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
        return stmts.append(exec(method(field(field(ident(names._this), fieldWriter(i)), methodName), fieldValueExprs)));
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueInt32V(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl int32Var = defVar(Flags.PARAMETER, "int32" + i, type(TypeTag.INT), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(int32Var);
        ListBuffer<JCTree.JCStatement> ifStmts = new ListBuffer<>();
        ifStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        ifStmts.appendList(genWriteFieldValue(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, isJsonb));
        stmts.append(defIf(binary(JCTree.Tag.OR, binary(JCTree.Tag.NE, ident(int32Var.name), literal(0)), binary(JCTree.Tag.EQ, ident(var12Var.name), literal(0))),
                block(ifStmts.toList()),
                null));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueIntVA(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            JCTree.JCIdent nullAsDefaultIdent,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String elemType = ((ArrayType) attributeInfo.type).getComponentType().toString();
        JCTree.JCVariableDecl intArrayVar = defVar(Flags.PARAMETER, "intArray" + i, arrayIdentType(elemType), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(intArrayVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notZeroStmts.append(exec(method(field(jsonWriterIdent, "writeArrayNull"))));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        nullStmts.append(defIf(binary(JCTree.Tag.NE, nullAsDefaultIdent, literal(0)), block(notZeroStmts.toList()), null));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notNullStmts.append(exec(method(field(jsonWriterIdent, "writeInt32"), List.of(ident(intArrayVar.name)))));

        stmts.append(defIf(binary(JCTree.Tag.EQ, ident(intArrayVar.name), defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueInt64V(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl int64Var = defVar(Flags.PARAMETER, "int64" + i, type(TypeTag.LONG), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(int64Var);
        ListBuffer<JCTree.JCStatement> ifStmts = new ListBuffer<>();
        ifStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        ifStmts.appendList(genWriteFieldValue(jsonWriterIdent, attributeInfo, objectIdent, beanType, i, isJsonb));
        stmts.append(defIf(binary(JCTree.Tag.OR, binary(JCTree.Tag.NE, ident(int64Var.name), literal(0)), binary(JCTree.Tag.EQ, ident(var12Var.name), literal(0))),
                block(ifStmts.toList()),
                null));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueInt64VA(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            JCTree.JCIdent nullAsDefaultIdent,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String elemType = ((ArrayType) attributeInfo.type).getComponentType().toString();
        JCTree.JCVariableDecl longArrayVar = defVar(Flags.PARAMETER, "longArray" + i, arrayIdentType(elemType), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(longArrayVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notZeroStmts.append(exec(method(field(jsonWriterIdent, "writeArrayNull"))));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        nullStmts.append(defIf(binary(JCTree.Tag.NE, nullAsDefaultIdent, literal(0)), block(notZeroStmts.toList()), null));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notNullStmts.append(exec(method(field(jsonWriterIdent, "writeInt64"), List.of(ident(longArrayVar.name)))));

        stmts.append(defIf(binary(JCTree.Tag.EQ, ident(longArrayVar.name), defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteInt32(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl integerVar = defVar(Flags.PARAMETER, "integer" + i, qualIdent(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(integerVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notZeroStmts.append(exec(method(field(jsonWriterIdent, "writeNumberNull"))));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        JCTree.JCBinary writeAsStringBinary = binary(JCTree.Tag.BITOR, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, BrowserCompatible.mask), literal(TypeTag.LONG, WriteBooleanAsNumber.mask)), literal(TypeTag.LONG, WriteNullStringAsEmpty.mask));
        nullStmts.append(defIf(binary(JCTree.Tag.NE, writeAsStringBinary, literal(0)), block(notZeroStmts.toList()), null));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notNullStmts.append(exec(method(field(jsonWriterIdent, "writeInt32"), List.of(ident(integerVar.name)))));

        stmts.append(defIf(binary(JCTree.Tag.EQ, ident(integerVar.name), defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteInt64(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl longVar = defVar(Flags.PARAMETER, "long" + i, qualIdent(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(longVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notZeroStmts.append(exec(method(field(jsonWriterIdent, "writeInt64Null"))));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        JCTree.JCBinary writeAsStringBinary = binary(JCTree.Tag.BITOR, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, BrowserCompatible.mask), literal(TypeTag.LONG, WriteBooleanAsNumber.mask)), literal(TypeTag.LONG, WriteNullStringAsEmpty.mask));
        nullStmts.append(defIf(binary(JCTree.Tag.NE, writeAsStringBinary, literal(0)), block(notZeroStmts.toList()), null));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notNullStmts.append(exec(method(field(jsonWriterIdent, "writeInt64"), List.of(ident(longVar.name)))));

        stmts.append(defIf(binary(JCTree.Tag.EQ, ident(longVar.name), defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFloat(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl floatVar = defVar(Flags.PARAMETER, "float" + i, qualIdent(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(floatVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notZeroStmts.append(exec(method(field(jsonWriterIdent, "writeNumberNull"))));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        JCTree.JCBinary writeAsStringBinary = binary(JCTree.Tag.BITOR, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, BrowserCompatible.mask), literal(TypeTag.LONG, WriteBooleanAsNumber.mask)), literal(TypeTag.LONG, WriteNullStringAsEmpty.mask));
        nullStmts.append(defIf(binary(JCTree.Tag.NE, writeAsStringBinary, literal(0)), block(notZeroStmts.toList()), null));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(exec(method(field(field(ident(names._this), fieldWriter(i)), "writeFloat"), List.of(jsonWriterIdent, ident(floatVar.name)))));

        stmts.append(defIf(binary(JCTree.Tag.EQ, ident(floatVar.name), defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }
    private ListBuffer<JCTree.JCStatement> genWriteDouble(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl doubleVar = defVar(Flags.PARAMETER, "double" + i, qualIdent(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(doubleVar);

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notZeroStmts.append(exec(method(field(jsonWriterIdent, "writeNumberNull"))));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        JCTree.JCBinary writeAsStringBinary = binary(JCTree.Tag.BITOR, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, BrowserCompatible.mask), literal(TypeTag.LONG, WriteBooleanAsNumber.mask)), literal(TypeTag.LONG, WriteNullStringAsEmpty.mask));
        nullStmts.append(defIf(binary(JCTree.Tag.NE, writeAsStringBinary, literal(0)), block(notZeroStmts.toList()), null));

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(exec(method(field(field(ident(names._this), fieldWriter(i)), "writeDouble"), List.of(jsonWriterIdent, ident(doubleVar.name)))));

        stmts.append(defIf(binary(JCTree.Tag.EQ, ident(doubleVar.name), defNull()), block(nullStmts.toList()), block(notNullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueString(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl stringVar = defVar(Flags.PARAMETER, "string" + i, qualIdent(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(stringVar);

        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        if (JVM_VERSION <= 8) {
            JCTree.JCTypeCast charsCast = cast(arrayIdentType("char"), method(field(field(qualIdent("com.alibaba.fastjson2.util.JDKUtils"), "UNSAFE"), "getObject"), List.of(ident(stringVar.name), literal(TypeTag.LONG, FIELD_STRING_VALUE_OFFSET))));
            notNullStmts.append(exec(method(field(jsonWriterIdent, "writeString"), List.of(charsCast))));
        } else {
            JCTree.JCTypeCast charsCast = cast(arrayIdentType("byte"), method(field(field(qualIdent("com.alibaba.fastjson2.util.JDKUtils"), "UNSAFE"), "getObject"), List.of(ident(stringVar.name), literal(TypeTag.LONG, FIELD_STRING_VALUE_OFFSET))));
            notNullStmts.append(exec(method(field(jsonWriterIdent, "writeStringLatin1"), List.of(charsCast))));
        }

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        notZeroStmts.append(defIf(method(field(jsonWriterIdent, "isEnabled"), List.of(contextFeaturesIdent)),
                block(exec(method(field(jsonWriterIdent, "writeString"), List.of(literal(""))))),
                block(exec(method(field(jsonWriterIdent, "writeStringNull"))))));

        ListBuffer<JCTree.JCStatement> nullStmts = new ListBuffer<>();
        JCTree.JCBinary binary1 = binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, binary(JCTree.Tag.BITOR, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, WriteNulls.mask), literal(TypeTag.LONG, NullAsDefaultValue.mask)), literal(TypeTag.LONG, WriteNullStringAsEmpty.mask))), literal(TypeTag.LONG, 0L));
        JCTree.JCBinary binary2 = binary(JCTree.Tag.EQ, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, NotWriteDefaultValue.mask)), literal(TypeTag.LONG, 0L));
        nullStmts.append(defIf(binary(JCTree.Tag.AND, binary1, binary2), block(notZeroStmts.toList()), null));

        stmts.append(defIf(binary(JCTree.Tag.NE, ident(stringVar.name), defNull()), block(notNullStmts.toList()), block(nullStmts.toList())));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueEnum(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl enumVar = defVar(Flags.PARAMETER, "enum" + i, qualIdent(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(enumVar);
        stmts.append(defIf(binary(JCTree.Tag.NE, ident(enumVar.name), defNull()),
                block(List.of(exec(method(field(field(ident(names._this), fieldWriter(i)), "writeEnum"), List.of(jsonWriterIdent, ident(enumVar.name)))))),
                        defIf(binary(JCTree.Tag.NE, ident(var12Var.name), literal(TypeTag.LONG, 0L)),
                                block(List.of(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb),
                                        exec(method(field(jsonWriterIdent, "writeNull"))))),
                                null)));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueDate(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        JCTree.JCVariableDecl dateVar = defVar(Flags.PARAMETER, "date" + i, qualIdent(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(dateVar);
        stmts.append(defIf(binary(JCTree.Tag.NE, ident(dateVar.name), defNull()),
                block(List.of(exec(method(field(field(ident(names._this), fieldWriter(i)), "writeDate"), List.of(jsonWriterIdent, method(field(ident(dateVar.name), "getTime"))))))),
                        defIf(binary(JCTree.Tag.NE, ident(var12Var.name), literal(TypeTag.LONG, 0L)),
                                block(List.of(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb),
                                        exec(method(field(jsonWriterIdent, "writeNull"))))),
                                null)));
        return stmts;
    }

    private ListBuffer<JCTree.JCStatement> genWriteFieldValueList(
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            JCTree.JCVariableDecl var12Var,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        JCTree.JCVariableDecl listStrVar = defVar(Flags.PARAMETER, "listStr" + i, ident("String"), defNull());
        stmts.append(listStrVar);
        JCTree.JCVariableDecl listVar = defVar(Flags.PARAMETER, "list" + i, qualIdent("java.util.List"), genWriteFieldValue(attributeInfo, objectIdent, beanType));
        stmts.append(listVar);

        JCTree.JCLabeledStatement label = label("listLabel" + i, null);
        ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
        ListBuffer<JCTree.JCStatement> labelStmts = new ListBuffer<>();

        ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
        notZeroStmts.append(defIf(binary(JCTree.Tag.EQ, objectIdent, ident(listVar.name)),
                block(List.of(exec(method(field(jsonWriterIdent, "writeReference"), List.of(literal("..")))), defBreak(label))),
                null));

        notZeroStmts.append(exec(assign(ident(listStrVar.name), method(field(jsonWriterIdent, "setPath"), List.of(field(ident(names._this), fieldWriter(i)), ident(listVar.name))))));

        notZeroStmts.append(defIf(binary(JCTree.Tag.NE, ident(listStrVar.name), defNull()),
                block(List.of(
                        exec(method(field(jsonWriterIdent, "writeReference"), List.of(ident(listStrVar.name)))),
                        exec(method(field(jsonWriterIdent, "popPath"), List.of(ident(listVar.name)))),
                        defBreak(label))),
                null));

        labelStmts.append(defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, ReferenceDetection.mask)), literal(0L)), block(notZeroStmts.toList()), null));

        JCTree.JCBinary binary = binary(JCTree.Tag.EQ, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, NotWriteEmptyArray.mask)), literal(TypeTag.LONG, 0L));
        JCTree.JCUnary unary = unary(JCTree.Tag.NOT, method(field(ident(listVar.name), "isEmpty")));

        ListBuffer<JCTree.JCStatement> notEmptyStmts = new ListBuffer<>();
        notEmptyStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
        String type = attributeInfo.type.toString();
        if ("java.util.List<java.lang.String>".equals(type)) {
            notEmptyStmts.append(exec(method(field(jsonWriterIdent, "writeString"), List.of(ident(listVar.name)))));
        } else {
            notEmptyStmts.append(exec(method(field(field(ident(names._this), fieldWriter(i)), "writeListValue"), List.of(jsonWriterIdent, ident(listVar.name)))));
        }
        notEmptyStmts.append(exec(method(field(jsonWriterIdent, "popPath"), List.of(ident(listVar.name)))));
        labelStmts.append(defIf(binary(JCTree.Tag.OR, binary, unary), block(notEmptyStmts.toList()), null));

        JCTree.JCBinary writeAsStringBinary = binary(JCTree.Tag.BITOR, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, WriteNulls.mask), literal(TypeTag.LONG, NullAsDefaultValue.mask)), literal(TypeTag.LONG, WriteNullListAsEmpty.mask));
        JCTree.JCIf notNUllIf = defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, writeAsStringBinary), literal(TypeTag.LONG, 0L)),
                block(List.of(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb), exec(method(field(jsonWriterIdent, "writeArrayNull"))))), null);

        label.body = block(labelStmts.toList());
        notNullStmts.append(label);
        stmts.append(defIf(binary(JCTree.Tag.NE, ident(listVar.name), defNull()), block(notNullStmts.toList()), block(notNUllIf)));
        return stmts;
    }

    private JCTree.JCExpression genWriteFieldValue(AttributeInfo attributeInfo, JCTree.JCIdent objectIdent, JCTree.JCExpression beanType) {
        if (attributeInfo.getMethod != null) {
            return method(field(cast(beanType, objectIdent), attributeInfo.getMethod.getSimpleName().toString()));
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
            JCTree.JCIdent jsonWriterIdent,
            AttributeInfo attributeInfo,
            JCTree.JCIdent objectIdent,
            JCTree.JCExpression beanType,
            int i,
            JCTree.JCIdent contextFeaturesIdent,
            JCTree.JCIdent quoteIdent,
            boolean isJsonb) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        String type = attributeInfo.type.toString();
        if (type.contains("[")) {
            JCTree.JCVariableDecl objectStrVar = defVar(Flags.PARAMETER, "objectStr" + i, ident("String"), defNull());
            stmts.append(objectStrVar);
            JCTree.JCVariableDecl objectIntVar = defVar(Flags.PARAMETER, "objectInt" + i, type(TypeTag.INT), literal(0));
            stmts.append(objectIntVar);
            JCTree.JCLabeledStatement outerLabel = label("objectOuterLabel" + i, null);
            ListBuffer<JCTree.JCStatement> outerLabelStmts = new ListBuffer<>();
            String elemType = ((ArrayType) attributeInfo.type).getComponentType().toString();
            JCTree.JCVariableDecl objectVar = defVar(Flags.PARAMETER, "object" + i, arrayIdentType(elemType), genWriteFieldValue(attributeInfo, objectIdent, beanType));
            outerLabelStmts.append(objectVar);

            JCTree.JCLabeledStatement innerLabel = label("objectInnerLabel" + i, null);
            ListBuffer<JCTree.JCStatement> innerLabelStmts = new ListBuffer<>();

            ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
            JCTree.JCVariableDecl objectLongVar = defVar(Flags.PARAMETER, "objectLong" + i, type(TypeTag.LONG), binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, ReferenceDetection.mask)));
            notNullStmts.append(objectLongVar);
            notNullStmts.append(exec(assign(ident(objectIntVar.name), ternary(binary(JCTree.Tag.EQ, ident(objectLongVar.name), literal(0)), literal(0), ternary(binary(JCTree.Tag.LT, ident(objectLongVar.name), literal(0)), literal(-1), literal(1))))));
            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, ident(objectIntVar.name), literal(0)), block(defBreak(innerLabel)), null));

            ListBuffer<JCTree.JCStatement> eqStmts = new ListBuffer<>();
            eqStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            eqStmts.append(exec(method(field(jsonWriterIdent, "writeReference"), List.of(literal("..")))));
            eqStmts.append(defBreak(outerLabel));
            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, objectIdent, ident(objectVar.name)), block(eqStmts.toList()), null));

            notNullStmts.append(exec(assign(ident(objectStrVar.name), method(field(jsonWriterIdent, "setPath"), List.of(field(ident(names._this), fieldWriter(i)), ident(objectVar.name))))));
            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, ident(objectStrVar.name), defNull()), block(defBreak(innerLabel)), null));

            notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            notNullStmts.append(exec(method(field(jsonWriterIdent, "writeReference"), List.of(literal("..")))));

            innerLabelStmts.append(defIf(binary(JCTree.Tag.NE, ident(objectVar.name), defNull()), block(notNullStmts.toList()), null));

            ListBuffer<JCTree.JCStatement> notZeroStmts = new ListBuffer<>();
            notZeroStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            notZeroStmts.append(exec(method(field(jsonWriterIdent, "writeArrayNull"))));
            JCTree.JCBinary writeAsStringBinary = binary(JCTree.Tag.BITOR, binary(JCTree.Tag.BITOR, literal(TypeTag.LONG, WriteNulls.mask), literal(TypeTag.LONG, NullAsDefaultValue.mask)), literal(TypeTag.LONG, WriteNullListAsEmpty.mask));
            innerLabelStmts.append(defIf(binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, writeAsStringBinary), literal(0)), block(notZeroStmts.toList()), null));

            innerLabelStmts.append(defBreak(outerLabel));

            innerLabel.body = block(innerLabelStmts.toList());
            outerLabelStmts.append(innerLabel);

            ListBuffer<JCTree.JCStatement> notEmptyArrayStmts = new ListBuffer<>();
            notEmptyArrayStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            notEmptyArrayStmts.append(
                    exec(method(field(method(field(field(ident(names._this), fieldWriter(i)), "getObjectWriter"), List.of(jsonWriterIdent, field(arrayIdentType(type), names._class))),
                            "write"), List.of(jsonWriterIdent, ident(objectVar.name), literal(attributeInfo.name), method(field(ident(objectVar.name), "getClass")), literal(TypeTag.LONG, 0L)))));
            notEmptyArrayStmts.append(defIf(binary(JCTree.Tag.NE, ident(objectIntVar.name), literal(0)), block(exec(method(field(jsonWriterIdent, "popPath"), List.of(ident(objectVar.name))))), null));
            JCTree.JCBinary binary1 = binary(JCTree.Tag.EQ, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, NotWriteEmptyArray.mask)), literal(0));
            JCTree.JCBinary binary2 = binary(JCTree.Tag.NE, field(ident(objectVar.name), "length"), literal(0));
            outerLabelStmts.append(defIf(binary(JCTree.Tag.AND, binary1, binary2), block(notEmptyArrayStmts.toList()), null));

            outerLabel.body = block(outerLabelStmts.toList());
            stmts.append(outerLabel);
        } else if (type.contains("java.util.Map<")) {
            JCTree.JCLabeledStatement outerLabel = label("objectOuterLabel" + i, null);
            ListBuffer<JCTree.JCStatement> outerLabelStmts = new ListBuffer<>();

            JCTree.JCVariableDecl objectStrVar = defVar(Flags.PARAMETER, "objectStr" + i, ident("String"), defNull());
            outerLabelStmts.append(objectStrVar);

            JCTree.JCVariableDecl objectVar = defVar(Flags.PARAMETER, "object" + i, getFieldValueType(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
            outerLabelStmts.append(objectVar);
            JCTree.JCVariableDecl objectIntVar = defVar(Flags.PARAMETER, "objectInt" + i, type(TypeTag.INT), literal(0));
            outerLabelStmts.append(objectIntVar);

            JCTree.JCLabeledStatement innerLabel = label("objectInnerLabel" + i, null);
            ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();
            notNullStmts.append(defIf(method(field(jsonWriterIdent, "isIgnoreNoneSerializable"), List.of(ident(objectVar.name))), block(defBreak(outerLabel)), null));

            JCTree.JCVariableDecl objectLongVar = defVar(Flags.PARAMETER, "objectLong" + i, type(TypeTag.LONG), binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, ReferenceDetection.mask)));
            notNullStmts.append(objectLongVar);
            notNullStmts.append(exec(assign(ident(objectIntVar.name), ternary(binary(JCTree.Tag.EQ, ident(objectLongVar.name), literal(0)), literal(0), ternary(binary(JCTree.Tag.LT, ident(objectLongVar.name), literal(0)), literal(-1), literal(1))))));
            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, ident(objectIntVar.name), literal(0)), block(defBreak(innerLabel)), null));

            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, objectIdent, ident(objectVar.name)),
                    block(List.of(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb),
                            exec(method(field(jsonWriterIdent, "writeReference"), List.of(literal("..")))),
                            defBreak(outerLabel)
                    )), null));

            notNullStmts.append(exec(assign(ident(objectStrVar.name), method(field(jsonWriterIdent, "setPath"), List.of(field(ident(names._this), fieldWriter(i)), ident(objectVar.name))))));
            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, ident(objectStrVar.name), defNull()), block(List.of(defBreak(innerLabel))), null));

            notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            notNullStmts.append(exec(method(field(jsonWriterIdent, "writeReference"), List.of(ident(objectStrVar.name)))));
            notNullStmts.append(exec(method(field(jsonWriterIdent, "popPath"), List.of(ident(objectVar.name)))));

            ListBuffer<JCTree.JCStatement> innerLabelStmts = new ListBuffer<>();
            innerLabelStmts.append(defIf(binary(JCTree.Tag.NE, ident(objectVar.name), defNull()), block(notNullStmts.toList()), null));

            JCTree.JCBinary binary = binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, WriteMapNullValue.mask)), literal(0));
            innerLabelStmts.append(defIf(binary,
                            block(List.of(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb), exec(method(field(jsonWriterIdent, "writeNull"))))),
                            null));
            innerLabelStmts.append(defBreak(outerLabel));
            innerLabel.body = block(innerLabelStmts.toList());

            outerLabelStmts.append(innerLabel);
            outerLabelStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            outerLabelStmts.append(exec(method(field(method(field(field(ident(names._this), fieldWriter(i)), "getObjectWriter"), List.of(jsonWriterIdent, method(field(ident(objectVar.name), "getClass")))),
                    "write"), List.of(jsonWriterIdent, ident(objectVar.name), literal(attributeInfo.name), field(field(ident(names._this), fieldWriter(i)), "fieldType"), literal(TypeTag.LONG, 0L)))));
            outerLabelStmts.append(defIf(binary(JCTree.Tag.NE, ident(objectIntVar.name), literal(0)), block(List.of(exec(method(field(jsonWriterIdent, "popPath"), List.of(ident(objectVar.name)))))), null));

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

            JCTree.JCLabeledStatement outerLabel = label("objectOuterLabel" + i, null);
            ListBuffer<JCTree.JCStatement> outerLabelStmts = new ListBuffer<>();

            JCTree.JCVariableDecl objectStrVar = defVar(Flags.PARAMETER, "objectStr" + i, ident("String"), defNull());
            outerLabelStmts.append(objectStrVar);

            JCTree.JCVariableDecl objectVar = defVar(Flags.PARAMETER, "object" + i, getFieldValueType(type), genWriteFieldValue(attributeInfo, objectIdent, beanType));
            outerLabelStmts.append(objectVar);
            JCTree.JCVariableDecl objectIntVar = defVar(Flags.PARAMETER, "objectInt" + i, type(TypeTag.INT), literal(0));
            outerLabelStmts.append(objectIntVar);

            JCTree.JCLabeledStatement innerLabel = label("objectInnerLabel" + i, null);
            ListBuffer<JCTree.JCStatement> notNullStmts = new ListBuffer<>();

            boolean noneSerializable = false;
            if (attributeInfo.type instanceof com.sun.tools.javac.code.Type.ClassType) {
                List<com.sun.tools.javac.code.Type> interfacesField = ((com.sun.tools.javac.code.Type.ClassType) attributeInfo.type).interfaces_field;
                if (interfacesField == null || interfacesField.stream().noneMatch(f -> "java.io.Serializable".equals(f.toString()))) {
                    noneSerializable = true;
                }
            }

            if (noneSerializable) {
                notNullStmts.append(defIf(method(field(jsonWriterIdent, "isIgnoreNoneSerializable"), List.of(ident(objectVar.name))), block(defBreak(outerLabel)), null));
            }

            JCTree.JCVariableDecl objectLongVar = defVar(Flags.PARAMETER, "objectLong" + i, type(TypeTag.LONG), binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, ReferenceDetection.mask)));
            notNullStmts.append(objectLongVar);
            notNullStmts.append(exec(assign(ident(objectIntVar.name), ternary(binary(JCTree.Tag.EQ, ident(objectLongVar.name), literal(0)), literal(0), ternary(binary(JCTree.Tag.LT, ident(objectLongVar.name), literal(0)), literal(-1), literal(1))))));
            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, ident(objectIntVar.name), literal(0)), block(defBreak(innerLabel)), null));

            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, objectIdent, ident(objectVar.name)),
                    block(List.of(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb),
                            exec(method(field(jsonWriterIdent, "writeReference"), List.of(literal("..")))),
                            defBreak(outerLabel)
                    )), null));

            notNullStmts.append(exec(assign(ident(objectStrVar.name), method(field(jsonWriterIdent, "setPath"), List.of(field(ident(names._this), fieldWriter(i)), ident(objectVar.name))))));
            notNullStmts.append(defIf(binary(JCTree.Tag.EQ, ident(objectStrVar.name), defNull()), block(List.of(defBreak(innerLabel))), null));

            notNullStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            notNullStmts.append(exec(method(field(jsonWriterIdent, "writeReference"), List.of(ident(objectStrVar.name)))));
            notNullStmts.append(exec(method(field(jsonWriterIdent, "popPath"), List.of(ident(objectVar.name)))));

            ListBuffer<JCTree.JCStatement> innerLabelStmts = new ListBuffer<>();
            innerLabelStmts.append(defIf(binary(JCTree.Tag.NE, ident(objectVar.name), defNull()), block(notNullStmts.toList()), null));

            JCTree.JCBinary binary = binary(JCTree.Tag.NE, binary(JCTree.Tag.BITAND, contextFeaturesIdent, literal(TypeTag.LONG, WriteMapNullValue.mask)), literal(0));
            innerLabelStmts.append(defIf(binary,
                    block(List.of(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb),
                            exec(method(field(jsonWriterIdent, WRITE_NULL_METHOD))))),
                    null));
            innerLabelStmts.append(defBreak(outerLabel));
            innerLabel.body = block(innerLabelStmts.toList());

            outerLabelStmts.append(innerLabel);
            outerLabelStmts.append(genWriteFieldName(jsonWriterIdent, attributeInfo, quoteIdent, isJsonb));
            outerLabelStmts.append(exec(method(field(method(field(field(ident(names._this), fieldWriter(i)), "getObjectWriter"), List.of(jsonWriterIdent, method(field(ident(objectVar.name), "getClass")))),
                    "write"), List.of(jsonWriterIdent, ident(objectVar.name), literal(attributeInfo.name), field(field(ident(names._this), fieldWriter(i)), "fieldType"), literal(TypeTag.LONG, 0L)))));
            outerLabelStmts.append(defIf(binary(JCTree.Tag.NE, ident(objectIntVar.name), literal(0)), block(List.of(exec(method(field(jsonWriterIdent, "popPath"), List.of(ident(objectVar.name)))))), null));

            outerLabel.body = block(outerLabelStmts.toList());
            stmts.append(outerLabel);
        }
        return stmts;
    }

    private JCTree.JCVariableDecl getHashCode32Var(JCTree.JCExpression hashCode64) {
        JCTree.JCBinary usrBinary = binary(JCTree.Tag.USR, hashCode64, literal(TypeTag.INT, 32));
        JCTree.JCPrimitiveTypeTree intType = type(TypeTag.INT);
        JCTree.JCTypeCast hashCode32Cast = cast(intType, binary(JCTree.Tag.BITXOR, hashCode64, parens(usrBinary)));
        return defVar(Flags.PARAMETER, "hashCode32", intType, hashCode32Cast);
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
            JCTree.JCMethodInvocation readReferenceMethod = method(field(jsonReaderIdent, "readReference"));
            JCTree.JCVariableDecl refVar = defVar(Flags.PARAMETER, "ref", ident("String"), readReferenceMethod);
            thenStmts.append(refVar);
            JCTree.JCMethodInvocation addResolveTaskMethod = method(field(fieldReaderField, "addResolveTask"), List.of(jsonReaderIdent, objectIdent, ident(refVar.name)));
            thenStmts.append(exec(addResolveTaskMethod));
            thenStmts.append(defContinue(loopLabel));
            stmts.append(defIf(method(field(jsonReaderIdent, "isReference")), block(thenStmts.toList()), null));
        }

        String readDirectMethod = getReadDirectMethod(type);
        if (readDirectMethod != null) {
            valueExpr = method(field(jsonReaderIdent, readDirectMethod));
        } else {
            JCTree.JCExpression fieldValueType = getFieldValueType(type);
            JCTree.JCVariableDecl fieldValueVar = defVar(Flags.PARAMETER, attributeInfo.name, fieldValueType);
            stmts.append(fieldValueVar);

            if (type.startsWith("java.util.List<")) {
                valueExpr = genFieldValueList(type, attributeInfo, jsonReaderIdent, fieldValueVar, loopLabel, stmts, i, referenceDetect, fieldReaderField, beanType, isJsonb);
            } else if (type.startsWith("java.util.Map<java.lang.String,")) {
                valueExpr = genFieldValueMap(type, attributeInfo, jsonReaderIdent, fieldValueVar, loopLabel, stmts, i, referenceDetect, isJsonb);
            }

            if (valueExpr == null) {
                JCTree.JCIdent objectReaderIdent = ident(fieldObjectReader(i));
                JCTree.JCMethodInvocation getObjectReaderMethod = method(field(fieldReaderField, "getObjectReader"), List.of(jsonReaderIdent));
                JCTree.JCAssign objectReaderAssign = assign(objectReaderIdent, getObjectReaderMethod);
                stmts.append(defIf(binary(JCTree.Tag.EQ, objectReaderIdent, defNull()), block(exec(objectReaderAssign)), null));
                JCTree.JCMethodInvocation objectMethod = method(
                        field(field(ident(names._this), fieldObjectReader(i)), isJsonb ? "readJSONBObject" : "readObject"),
                        List.of(
                                jsonReaderIdent,
                                field(fieldReaderField, "fieldType"),
                                literal(attributeInfo.name),
                                literal(TypeTag.LONG, 0L)
                        )
                );
                stmts.append(exec(assign(ident(fieldValueVar.name), cast(fieldValueType, objectMethod))));
                valueExpr = ident(fieldValueVar.name);
            }
        }

        if (attributeInfo.setMethod != null) {
            stmts.append(exec(method(field(objectIdent, attributeInfo.setMethod.getSimpleName().toString()), List.of(valueExpr))));
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

        JCTree.JCLabeledStatement switchLabel = label("_switch2", null);
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match2"));
                        break;
                    case 3:
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match3"));
                        break;
                    case 4:
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match4"), List.of(cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[3]))));
                        break;
                    case 5: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = '"';
                        bytes4[3] = ':';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match5"), List.of(literal(name1)));
                        break;
                    }
                    case 6: {
                        byte[] bytes4 = new byte[4];
                        bytes4[0] = fieldName[3];
                        bytes4[1] = fieldName[4];
                        bytes4[2] = fieldName[5];
                        bytes4[3] = '"';
                        int name1 = UNSAFE.getInt(bytes4, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match6"), List.of(literal(name1)));
                        break;
                    }
                    case 7: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match7"), List.of(literal(name1)));
                        break;
                    }
                    case 8: {
                        int name1 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match8"), List.of(literal(name1), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[7]))));
                        break;
                    }
                    case 9: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match9"), List.of(literal(name1)));
                        break;
                    }
                    case 10: {
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 3, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name1 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match10"), List.of(literal(name1)));
                        break;
                    }
                    case 11: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match11"), List.of(literal(name1)));
                        break;
                    }
                    case 12: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match12"), List.of(literal(name1), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[11]))));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match13"), List.of(literal(name1), literal(name2)));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match14"), List.of(literal(name1), literal(name2)));
                        break;
                    }
                    case 15: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match15"), List.of(literal(name1), literal(name2)));
                        break;
                    }
                    case 16: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        int name2 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match16"), List.of(literal(name1), literal(name2), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[15]))));
                        break;
                    }
                    case 17: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 6);
                        bytes8[6] = '"';
                        bytes8[7] = ':';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match17"), List.of(literal(name1), literal(name2)));
                        break;
                    }
                    case 18: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 11, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name2 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match18"), List.of(literal(name1), literal(name2)));
                        break;
                    }
                    case 19: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match19"), List.of(literal(name1), literal(name2)));
                        break;
                    }
                    case 20: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match20"), List.of(literal(name1), literal(name2), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[19]))));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match21"), List.of(literal(name1), literal(name2), literal(name3)));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match22"), List.of(literal(name1), literal(name2), literal(name3)));
                        break;
                    }
                    case 23: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match23"), List.of(literal(name1), literal(name2), literal(name3)));
                        break;
                    }
                    case 24: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        int name3 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match24"), List.of(literal(name1), literal(name2), literal(name3), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[23]))));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match25"), List.of(literal(name1), literal(name2), literal(name3)));
                        break;
                    }
                    case 26: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        byte[] bytes8 = new byte[8];
                        System.arraycopy(fieldName, 19, bytes8, 0, 7);
                        bytes8[7] = '"';
                        long name3 = UNSAFE.getLong(bytes8, ARRAY_BYTE_BASE_OFFSET);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match26"), List.of(literal(name1), literal(name2), literal(name3)));
                        break;
                    }
                    case 27: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match27"), List.of(literal(name1), literal(name2), literal(name3)));
                        break;
                    }
                    case 28: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match28"), List.of(literal(name1), literal(name2), literal(name3), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[27]))));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match29"), List.of(literal(name1), literal(name2), literal(name3), literal(name4)));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match30"), List.of(literal(name1), literal(name2), literal(name3), literal(name4)));
                        break;
                    }
                    case 31: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match31"), List.of(literal(name1), literal(name2), literal(name3), literal(name4)));
                        break;
                    }
                    case 32: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        int name4 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match32"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[31]))));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match33"), List.of(literal(name1), literal(name2), literal(name3), literal(name4)));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match34"), List.of(literal(name1), literal(name2), literal(name3), literal(name4)));
                        break;
                    }
                    case 35: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match35"), List.of(literal(name1), literal(name2), literal(name3), literal(name4)));
                        break;
                    }
                    case 36: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match36"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[35]))));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match37"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), literal(name5)));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match38"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), literal(name5)));
                        break;
                    }
                    case 39: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match39"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), literal(name5)));
                        break;
                    }
                    case 40: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        int name5 = UNSAFE.getInt(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match40"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), literal(name5), cast(type(TypeTag.BYTE), literal(TypeTag.INT, fieldName[39]))));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match41"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), literal(name5)));
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
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match42"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), literal(name5)));
                        break;
                    }
                    case 43: {
                        long name1 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 3);
                        long name2 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 11);
                        long name3 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 19);
                        long name4 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 27);
                        long name5 = UNSAFE.getLong(fieldName, ARRAY_BYTE_BASE_OFFSET + 35);
                        nextIfMethod = method(field(jsonReaderIdent, "nextIfName4Match43"), List.of(literal(name1), literal(name2), literal(name3), literal(name4), literal(name5)));
                        break;
                    }
                    default:
                        throw new IllegalStateException("fieldNameLength " + fieldNameLength);
                }
                List<JCTree.JCStatement> readFieldValueStmts = genReadFieldValue(fieldReader, jsonReaderIdent, fieldReaderIndex, structInfo, loopLabel, objectIdent, beanType, isJsonb);
                caseStmts.append(defIf(nextIfMethod, block(readFieldValueStmts), null));
            }
            caseStmts.append(defBreak(switchLabel));
            cases.append(defCase(literal(name0), caseStmts.toList()));
        }
        switchLabel.body = defSwitch(method(field(jsonReaderIdent, "getRawInt")), cases.toList());
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
            JCTree.JCMethodInvocation nextIfNullMethod = method(field(jsonReaderIdent, "nextIfNull"));
            stmts.append(defIf(nextIfNullMethod,
                    block(exec(assign(ident(fieldValueVar.name), defNull()))),
                    block(exec(assign(ident(fieldValueVar.name), newClass(null, null, qualIdent("java.util.ArrayList"), null, null))))));

            String readDirectMethod = getReadDirectMethod(itemType);
            JCTree.JCIdent itemReaderIdent = ident(fieldItemObjectReader(i));
            if (readDirectMethod == null) {
                JCTree.JCFieldAccess getItemObjectReaderField = field(fieldReaderField, "getItemObjectReader");
                JCTree.JCExpressionStatement getItemObjectReaderExec = exec(assign(itemReaderIdent, method(getItemObjectReaderField, List.of(jsonReaderIdent))));
                stmts.append(defIf(binary(JCTree.Tag.EQ, itemReaderIdent, defNull()), block(getItemObjectReaderExec), null));
            }

            if (referenceDetect) {
                referenceDetect = isReference(itemType);
            }

            JCTree.JCVariableDecl for_iVar;
            if ("i".equals(attributeInfo.name)) {
                for_iVar = defVar(Flags.PARAMETER, "j", type(TypeTag.INT), literal(0));
            } else {
                for_iVar = defVar(Flags.PARAMETER, "i", type(TypeTag.INT), literal(0));
            }
            JCTree.JCMethodInvocation nextIfArrayStartMethod = method(field(jsonReaderIdent, "nextIfArrayStart"));
            JCTree.JCMethodInvocation nextIfArrayEndMethod = method(field(jsonReaderIdent, "nextIfArrayEnd"));
            ListBuffer<JCTree.JCStatement> whileStmts = new ListBuffer<>();
            JCTree.JCExpression item;
            if (readDirectMethod != null) {
                item = method(field(jsonReaderIdent, readDirectMethod));
            } else {
                item = cast(
                        qualIdent(itemType),
                        method(
                                field(itemReaderIdent, isJsonb ? "readJSONBObject" : "readObject"),
                                List.of(
                                        jsonReaderIdent, defNull(),
                                        defNull(),
                                        literal(TypeTag.LONG, 0L)
                                )
                        )
                );
            }

            if (referenceDetect) {
                JCTree.JCVariableDecl listItemVar = defVar(Flags.PARAMETER, attributeInfo.name + "_item", getFieldValueType(itemType), item);
                ListBuffer<JCTree.JCStatement> isReferenceStmts = new ListBuffer<>();
                JCTree.JCMethodInvocation readReferenceMethod = method(field(jsonReaderIdent, "readReference"));
                JCTree.JCVariableDecl refVar = defVar(Flags.PARAMETER, "ref", ident("String"), readReferenceMethod);
                isReferenceStmts.append(refVar);
                JCTree.JCMethodInvocation addResolveTaskMethod = method(
                        field(jsonReaderIdent, "addResolveTask"),
                        List.of(
                                ident(fieldValueVar.name),
                                ident(for_iVar.name),
                                method(
                                        field(qualIdent("com.alibaba.fastjson2.JSONPath"), "of"),
                                        List.of(ident(refVar.name))
                                )
                        )
                );
                isReferenceStmts.append(exec(addResolveTaskMethod));
                isReferenceStmts.append(exec(method(field(ident(fieldValueVar.name), "add"), List.of(defNull()))));
                isReferenceStmts.append(defContinue(loopLabel));
                whileStmts.append(defIf(method(field(jsonReaderIdent, "isReference")), block(isReferenceStmts.toList()), null));
                whileStmts.append(listItemVar);
                item = ident(listItemVar.name);
            }

            whileStmts.append(exec(method(field(ident(fieldValueVar.name), "add"), List.of(item))));

            ListBuffer<JCTree.JCStatement> condStmts = new ListBuffer<>();
            if (referenceDetect) {
                condStmts.append(forLoop(List.of(for_iVar), unary(JCTree.Tag.NOT, nextIfArrayEndMethod), List.of(exec(unary(JCTree.Tag.PREINC, ident(for_iVar.name)))), block(whileStmts.toList())));
            } else {
                condStmts.append(whileLoop(unary(JCTree.Tag.NOT, nextIfArrayEndMethod), block(whileStmts.toList())));
            }

            stmts.append(defIf(nextIfArrayStartMethod, block(condStmts.toList()), null));
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
        JCTree.JCMethodInvocation nextIfNullMethod = method(field(jsonReaderIdent, "nextIfNull"));

        boolean readDirect = supportReadDirect(itemType);

        ListBuffer<JCTree.JCStatement> elseStmts = new ListBuffer<>();
        JCTree.JCIdent itemReaderIdent = ident(fieldItemObjectReader(i));
        if (!readDirect) {
            JCTree.JCFieldAccess getObjectReaderField = field(jsonReaderIdent, "getObjectReader");
            JCTree.JCExpressionStatement getItemObjectReaderExec = exec(assign(itemReaderIdent, method(getObjectReaderField, List.of(field(qualIdent(itemType), names._class)))));
            elseStmts.append(defIf(binary(JCTree.Tag.EQ, itemReaderIdent, defNull()), block(getItemObjectReaderExec), null));
        }

        elseStmts.append(exec(assign(ident(fieldValueVar.name), newClass(null, null, qualIdent("java.util.HashMap"), null, null))));

        JCTree.JCMethodInvocation nextIfObjectStartMethod = method(field(jsonReaderIdent, "nextIfObjectStart"));
        elseStmts.append(exec(nextIfObjectStartMethod));

        JCTree.JCMethodInvocation nextIfObjectEndMethod = method(field(jsonReaderIdent, "nextIfObjectEnd"));
        ListBuffer<JCTree.JCStatement> whileStmts = new ListBuffer<>();

        if (referenceDetect) {
            referenceDetect = isReference(itemType);
        }

        JCTree.JCExpression mapEntryValueExpr;
        if (readDirect) {
            mapEntryValueExpr = cast(qualIdent(itemType), method(field(jsonReaderIdent, getReadDirectMethod(itemType))));
        } else {
            mapEntryValueExpr = cast(
                    qualIdent(itemType),
                    method(
                            field(itemReaderIdent, isJsonb ? "readJSONBObject" : "readObject"),
                            List.of(
                                    jsonReaderIdent,
                                    field(qualIdent(itemType), names._class),
                                    literal(attributeInfo.name), ident("features")
                            )
                    )
            );
        }

        JCTree.JCExpression mapEntryKeyExpr = method(field(jsonReaderIdent, "readFieldName"));

        if (referenceDetect) {
            JCTree.JCVariableDecl mapKey = defVar(Flags.PARAMETER, attributeInfo.name + "_key", ident("String"), mapEntryKeyExpr);
            whileStmts.append(mapKey);
            JCTree.JCVariableDecl mapValue = defVar(Flags.PARAMETER, attributeInfo.name + "_value", ident("String"));
            whileStmts.append(mapValue);

            ListBuffer<JCTree.JCStatement> isReferenceStmts = new ListBuffer<>();
            JCTree.JCMethodInvocation readReferenceMethod = method(field(jsonReaderIdent, "readReference"));
            JCTree.JCVariableDecl refVar = defVar(Flags.PARAMETER, "ref", ident("String"), readReferenceMethod);
            isReferenceStmts.append(refVar);
            JCTree.JCMethodInvocation addResolveTaskMethod = method(field(jsonReaderIdent, "addResolveTask"), List.of(ident(fieldValueVar.name), ident(mapKey.name), method(field(qualIdent("com.alibaba.fastjson2.JSONPath"), "of"), List.of(ident(refVar.name)))));
            isReferenceStmts.append(exec(addResolveTaskMethod));
            whileStmts.append(defIf(method(field(jsonReaderIdent, "isReference")), block(isReferenceStmts.toList()), exec(method(field(ident(fieldValueVar.name), "put"), List.of(mapEntryKeyExpr, mapEntryValueExpr)))));
        } else {
            whileStmts.append(exec(method(field(ident(fieldValueVar.name), "put"), List.of(mapEntryKeyExpr, mapEntryValueExpr))));
        }

        elseStmts.append(whileLoop(unary(JCTree.Tag.NOT, nextIfObjectEndMethod), block(whileStmts.toList())));

        elseStmts.append(exec(method(field(jsonReaderIdent, "nextIfComma"))));

        stmts.append(defIf(nextIfNullMethod,
                block(exec(assign(ident(fieldValueVar.name), defNull()))),
                block(elseStmts.toList())));

        return ident(fieldValueVar.name);
    }

    private void genSource(String fullQualifiedName, StructInfo structInfo, JCTree.JCClassDecl innerClass) {
        try {
            JavaFileObject converterFile = processingEnv.getFiler().createSourceFile(fullQualifiedName, structInfo.element);
            try (Writer writer = converterFile.openWriter()) {
                int idx = fullQualifiedName.lastIndexOf(".");
                if (idx != -1) {
                    String pkgPath = fullQualifiedName.substring(0, idx);
                    writer.write("package " + pkgPath + ";");
                    writer.write(System.lineSeparator());
                }
                String str = innerClass.toString().replaceFirst("public static final class ", "public final class ")
                        .replaceAll("::<>", "::");
                Formatter formatter = new Formatter();
                writer.write(formatter.formatSource(str));
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed saving compiled json serialization file " + fullQualifiedName + e.getMessage());
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed creating compiled json serialization file " + fullQualifiedName + e.getMessage());
        }
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
}
