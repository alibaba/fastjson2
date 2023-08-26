package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.internal.asm.Label;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.*;
import static com.alibaba.fastjson2.internal.processor.JavacTreeUtils.*;
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
public class JSONCompiledAnnotationProcessor2
        extends AbstractProcessor {
    private Messager messager;
    private JavacTrees javacTrees;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.javacTrees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.names = Names.instance(context);
        initialize(TreeMaker.instance(context), names, processingEnv.getElementUtils());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Analysis analysis = new Analysis(processingEnv);
        Set<? extends Element> compiledJsons = roundEnv.getElementsAnnotatedWith(analysis.jsonCompiledElement);
        if (!compiledJsons.isEmpty()) {
            analysis.processAnnotation(analysis.compiledJsonType, compiledJsons);
        }

        Map<String, StructInfo> structs = analysis.analyze();
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(JSONCompiled.class);
        elementsAnnotatedWith.stream().forEach(element -> {
            StructInfo structInfo = structs.get(element.toString());
            java.util.List<AttributeInfo> attributeInfos = structInfo.getReaderAttributes();
            int fieldsSize = attributeInfos.size();
            Class superClass = getSuperClass(attributeInfos.size());

            JCTree tree = javacTrees.getTree(element);
            pos(tree.pos);
            tree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl beanClassDecl) {
                    super.visitClassDef(beanClassDecl);

                    String beanFQN = beanClassDecl.sym.toString();
                    if (element.toString().equals(beanFQN)) {
                        // initialization
                        String innerClassFQN = findConverterName(structInfo);
                        int dotIdx = innerClassFQN.lastIndexOf('.');
                        if (dotIdx == -1) {
                            messager.printMessage(Diagnostic.Kind.ERROR, String.format("not qualified inner class name for %s", beanFQN));
                        }
                        String innerClassName = innerClassFQN.substring(dotIdx + 1);
                        JCTree.JCExpression beanType = qualIdent(beanFQN);
                        JCTree.JCNewClass beanNew = newClass(null, null, beanType, null, null);
                        JCTree.JCIdent objectType = ident("Object");

                        // generate inner class
                        JCTree.JCClassDecl innerClass = genInnerClass(innerClassName, superClass);

                        // generate fields if necessary
                        final boolean generatedFields = fieldsSize < 128;
                        if (generatedFields) {
                            innerClass.defs = innerClass.defs.prependList(genFields(attributeInfos, superClass));
                        }

                        // generate constructor
                        innerClass.defs = innerClass.defs.append(genConstructor(beanType, beanNew, attributeInfos, superClass, generatedFields));

                        // generate createInstance
                        innerClass.defs = innerClass.defs.append(genCreateInstance(objectType, beanNew));

                        // generate readObject
                        innerClass.defs = innerClass.defs.append(genReadObject(objectType, beanFQN, beanNew, attributeInfos, structInfo, false));

                        // link with inner class
                        beanClassDecl.defs = beanClassDecl.defs.append(innerClass);

                        // generate source file
                        genSource(innerClassFQN, structInfo, innerClass);
                    }
                }
            });
        });
        return true;
    }

    private List<JCTree> genFields(java.util.List<AttributeInfo> attributeInfos, Class superClass) {
        ListBuffer<JCTree> stmts = new ListBuffer<>();
        int fieldsSize = attributeInfos.size();
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
        return stmts.toList();
    }

    private JCTree.JCClassDecl genInnerClass(String className, Class superClass) {
        JCTree.JCClassDecl innerClass = defClass(Flags.PRIVATE | Flags.STATIC,
                className,
                null,
                qualIdent(superClass.getName()),
                null,
                null);
        return innerClass;
    }

    private JCTree.JCMethodDecl genConstructor(JCTree.JCExpression beanType, JCTree.JCNewClass beanNew, java.util.List<AttributeInfo> fields, Class superClass, boolean generatedFields) {
        JCTree.JCLambda lambda = lambda(List.nil(), beanNew);
        JCTree.JCExpression fieldReaderType = qualIdent(FieldReader.class.getName());
        ListBuffer fieldReaders = new ListBuffer<>();
        JCTree.JCExpression objectReadersType = qualIdent(ObjectReaders.class.getName());
        int fieldsSize = fields.size();
        for (int i = 0; i < fieldsSize; ++i) {
            JCTree.JCMethodInvocation readerMethod = null;
            AttributeInfo attributeInfo = fields.get(i);
            if (attributeInfo.setMethod != null) {
                String methodName = attributeInfo.setMethod.getSimpleName().toString();
                readerMethod = method(field(objectReadersType, "fieldReaderWithMethod"), List.of(literal(attributeInfo.name), field(beanType, names._class), literal(methodName)));
            } else if (attributeInfo.field != null) {
                String fieldName = attributeInfo.field.getSimpleName().toString();
                if (fieldName.equals(attributeInfo.name)) {
                    readerMethod = method(field(objectReadersType, "fieldReaderWithField"), List.of(literal(fieldName), field(beanType, names._class)));
                } else {
                    readerMethod = method(field(objectReadersType, "fieldReaderWithField"), List.of(literal(attributeInfo.name), field(beanType, names._class), literal(fieldName)));
                }
            } else {
                messager.printMessage(Diagnostic.Kind.WARNING, "not implemented yet");
            }
            if (readerMethod != null) {
                fieldReaders.append(readerMethod);
            }
        }
        JCTree.JCNewArray fieldReadersArray = newArray(fieldReaderType, null, List.from(fieldReaders));
        JCTree.JCMethodInvocation superMethod = method(ident(names._super), List.of(field(beanType, names._class), defNull(), defNull(), literal(TypeTag.LONG, 0L), defNull(), lambda, defNull(), fieldReadersArray));
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        stmts.append(exec(superMethod));
        // initialize fields if necessary
        if (superClass == ObjectReaderAdapter.class && generatedFields) {
            stmts.appendList(genInitFields(fieldsSize));
        }
        return defMethod(Flags.PUBLIC, names.init, type(TypeTag.VOID), null, null, null, block(stmts.toList()), null);
    }

    private List<JCTree.JCStatement> genInitFields(int fieldsSize) {
        ListBuffer<JCTree.JCStatement> stmts = new ListBuffer<>();
        for (int i = 0; i < fieldsSize; ++i) {
            stmts.append(exec(assign(ident(fieldReader(i)), indexed(field(ident(names._this), "fieldReaders"), literal(i)))));
        }
        return stmts.toList();
    }

    private JCTree.JCMethodDecl genCreateInstance(JCTree.JCIdent objectType, JCTree.JCNewClass beanNew) {
        JCTree.JCVariableDecl featuresVar = defVar(Flags.PARAMETER, "features", type(TypeTag.LONG));
        return defMethod(Flags.PUBLIC, "createInstance", objectType, null, List.of(featuresVar), null, block(defReturn(beanNew)), null);
    }

    private JCTree.JCMethodDecl genReadObject(JCTree.JCIdent objectType, String classNamePath, JCTree.JCNewClass beanNew, java.util.List<AttributeInfo> attributeInfos, StructInfo structInfo, boolean isJsonb) {
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

        JCTree.JCVariableDecl features2Var = defVar(Flags.PARAMETER, "features2", type(TypeTag.LONG), binary(JCTree.Tag.BITOR, ident("features"), field(ident(names._this), "features")));
        readObjectBody.append(features2Var);

        JCTree.JCVariableDecl objectVar = defVar(Flags.PARAMETER, "object", qualIdent(classNamePath), beanNew);
        JCTree.JCIdent objectIdent = ident(objectVar.name);
        readObjectBody.append(objectVar);

        int fieldsSize = attributeInfos.size();

        JCTree.JCLabeledStatement loopLabel = label("_while", null);
        JCTree.JCWhileLoop loopHead = whileLoop(unary(JCTree.Tag.NOT, method(field(jsonReaderIdent, "nextIfObjectEnd"))), null);
        ListBuffer<JCTree.JCStatement> loopBody = new ListBuffer<>();

        if (fieldNameLengthMin >= 2 && fieldNameLengthMax <= 43) {
            loopBody.appendList(genRead243(attributeInfos, jsonReaderIdent, structInfo, loopLabel, objectIdent, false));
        }

        JCTree.JCFieldAccess readFieldNameHashCode = field(jsonReaderIdent, "readFieldNameHashCode");
        JCTree.JCVariableDecl hashCode64Var = defVar(Flags.PARAMETER, "hashCode64", type(TypeTag.LONG), cast(type(TypeTag.LONG), method(readFieldNameHashCode)));
        JCTree.JCExpression hashCode64 = ident(hashCode64Var.name);
        loopBody.append(hashCode64Var);
        if (fieldsSize <= 6) {
            for (int i = 0; i < fieldsSize; ++i) {
                AttributeInfo attributeInfo = attributeInfos.get(i);
                List<JCTree.JCStatement> readFieldValueStmts = genReadFieldValue(attributeInfo, jsonReaderIdent, i, structInfo, loopLabel, objectIdent, isJsonb);
                loopBody.appendList(List.of(defIf(binary(JCTree.Tag.EQ, literal(TypeTag.LONG, attributeInfo.nameHashCode), hashCode64), block(readFieldValueStmts), null)));
            }
        } else {
            Map<Integer, java.util.List<Long>> map = new TreeMap();
            Map<Long, AttributeInfo> mapping = new TreeMap();
            Map<Long, Integer> mappingIndex = new TreeMap();
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
            for (int i = 0; i < fieldsSize; ++i) {
                java.util.List<Long> hashCode64Array = map.get(hashCode32Keys[i]);
                List<JCTree.JCStatement> stmts = List.nil();
                Long fieldNameHash = null;
                if (hashCode64Array.size() == 1 && hashCode64Array.get(0) == hashCode32Keys[i]) {
                    fieldNameHash = hashCode64Array.get(0);
                    int index = mappingIndex.get(fieldNameHash);
                    AttributeInfo attributeInfo = mapping.get(fieldNameHash);
                    stmts = stmts.appendList(genReadFieldValue(attributeInfo, jsonReaderIdent, index, structInfo, loopLabel, objectIdent, isJsonb));
                    stmts.append(defContinue(loopLabel));
                } else {
                    for (int j = 0; j < hashCode64Array.size(); ++j) {
                        fieldNameHash = hashCode64Array.get(j);
                        int index = mappingIndex.get(fieldNameHash);
                        AttributeInfo field = mapping.get(fieldNameHash);
                        List<JCTree.JCStatement> stmtsIf = genReadFieldValue(field, jsonReaderIdent, index, structInfo, loopLabel, objectIdent, isJsonb);
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
        if (structInfo.smartMatch) {
            loopBody.append(defIf(method(field(ident(names._this), "readFieldValueWithLCase"), List.of(jsonReaderIdent, objectIdent, ident(hashCode64Var.name), ident(features2Var.name))), block(defContinue(loopLabel)), null));
        }
        JCTree.JCFieldAccess processExtraField = field(ident(names._this), "processExtra");
        loopBody.append(exec(method(processExtraField, List.of(jsonReaderIdent, objectIdent))));
        loopHead.body = block(loopBody.toList());
        loopLabel.body = loopHead;
        readObjectBody.append(loopLabel);

        readObjectBody.append(exec(method(field(jsonReaderIdent, "nextIfComma"))));

        readObjectBody.append(defReturn(objectIdent));

        return defMethod(Flags.PUBLIC, "readObject", objectType, null, List.of(jsonReaderVar, fieldTypeVar, fieldNameVar, featuresVar), null, block(readObjectBody.toList()), null);
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
                valueExpr = genFieldValueList(type, attributeInfo, jsonReaderIdent, fieldValueVar, loopLabel, stmts, i, referenceDetect, fieldReaderField);
            } else if (type.startsWith("java.util.Map<java.lang.String,")) {
                valueExpr = genFieldValueMap(type, attributeInfo, jsonReaderIdent, fieldValueVar, loopLabel, stmts, i, referenceDetect);
            }

            if (valueExpr == null) {
                JCTree.JCIdent objectReaderIdent = ident(fieldObjectReader(i));
                JCTree.JCMethodInvocation getObjectReaderMethod = method(field(fieldReaderField, "getObjectReader"), List.of(jsonReaderIdent));
                JCTree.JCAssign objectReaderAssign = assign(objectReaderIdent, getObjectReaderMethod);
                stmts.append(defIf(binary(JCTree.Tag.EQ, objectReaderIdent, defNull()), block(exec(objectReaderAssign)), null));
                JCTree.JCMethodInvocation objectMethod = method(field(field(ident(names._this), fieldObjectReader(i)), isJsonb ? "readJSONBObject" : "readObject"), List.of(jsonReaderIdent, field(fieldReaderField, "fieldType"), literal(attributeInfo.name), literal(TypeTag.LONG, 0L)));
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
            Iterator it = name0Map.keySet().iterator();
            for (int i = 0; i < labels.length; i++) {
                labels[i] = new Label();
                switchKeys[i] = (Integer) it.next();
            }
        }

        List<JCTree.JCStatement> stmts = List.nil();
        ListBuffer<JCTree.JCCase> cases = new ListBuffer<>();

        JCTree.JCLabeledStatement switchLabel = label("_switch2", null);
        ListBuffer<JCTree.JCStatement> switchBody = new ListBuffer<>();
        for (int i = 0; i < labels.length; i++) {
            int name0 = switchKeys[i];
            java.util.List<AttributeInfo> fieldReaders = name0Map.get(name0);
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
                List<JCTree.JCStatement> readFieldValueStmts = genReadFieldValue(fieldReader, jsonReaderIdent, fieldReaderIndex, structInfo, loopLabel, objectIdent, isJsonb);
                cases.append(defCase(literal(name0), List.of(defIf(nextIfMethod, block(readFieldValueStmts), null), defBreak(switchLabel))));
            }
        }
        switchLabel.body = defSwitch(method(field(jsonReaderIdent, "getRawInt")), cases.toList());
        stmts = stmts.append(switchLabel);
        return stmts;
    }

    private JCTree.JCExpression genFieldValueList(String type,
                                                  AttributeInfo attributeInfo,
                                                  JCTree.JCIdent jsonReaderIdent,
                                                  JCTree.JCVariableDecl fieldValueVar,
                                                  JCTree.JCLabeledStatement loopLabel,
                                                  ListBuffer<JCTree.JCStatement> stmts,
                                                  int i,
                                                  boolean referenceDetect,
                                                  JCTree.JCFieldAccess fieldReaderField) {
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
                item = cast(qualIdent(itemType), method(field(itemReaderIdent, "readObject"), List.of(jsonReaderIdent, defNull(), defNull(), literal(TypeTag.LONG, 0L))));
            }

            if (referenceDetect) {
                JCTree.JCVariableDecl listItemVar = defVar(Flags.PARAMETER, attributeInfo.name + "_item", getFieldValueType(itemType), item);
                ListBuffer<JCTree.JCStatement> isReferenceStmts = new ListBuffer<>();
                JCTree.JCMethodInvocation readReferenceMethod = method(field(jsonReaderIdent, "readReference"));
                JCTree.JCVariableDecl refVar = defVar(Flags.PARAMETER, "ref", ident("String"), readReferenceMethod);
                isReferenceStmts.append(refVar);
                JCTree.JCMethodInvocation addResolveTaskMethod = method(field(jsonReaderIdent, "addResolveTask"), List.of(ident(fieldValueVar.name), ident(for_iVar.name), method(field(qualIdent("com.alibaba.fastjson2.JSONPath"), "of"), List.of(ident(refVar.name)))));
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

    private JCTree.JCExpression genFieldValueMap(String type,
                                                 AttributeInfo attributeInfo,
                                                 JCTree.JCIdent jsonReaderIdent,
                                                 JCTree.JCVariableDecl fieldValueVar,
                                                 JCTree.JCLabeledStatement loopLabel,
                                                 ListBuffer<JCTree.JCStatement> stmts,
                                                 int i,
                                                 boolean referenceDetect) {
        String itemType = type.substring(31, type.length() - 1);
        boolean itemTypeIsClass = itemType.indexOf('<') == -1;
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
            mapEntryValueExpr = cast(qualIdent(itemType), method(field(itemReaderIdent, "readObject"), List.of(jsonReaderIdent, field(qualIdent(itemType), names._class), literal(attributeInfo.name), ident("features"))));
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
            isReferenceStmts.append(defContinue(loopLabel));
            whileStmts.append(defIf(method(field(jsonReaderIdent, "isReference")), block(isReferenceStmts.toList()), null));
        }
        whileStmts.append(exec(method(field(ident(fieldValueVar.name), "put"), List.of(mapEntryKeyExpr, mapEntryValueExpr))));

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
                String pkgPath = fullQualifiedName.substring(0, idx);
                writer.write("package " + pkgPath + ";");
                writer.write(System.lineSeparator());
                String str = innerClass.toString().replaceFirst("private static class ", "public class ");
                writer.write(str);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed saving compiled json serialization file " + fullQualifiedName);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed creating compiled json serialization file " + fullQualifiedName);
        }
    }

    private JCTree.JCExpression getFieldValueType(String type) {
        if (type.indexOf("[") != -1) {
            return arrayIdent(type);
        }
        if (type.indexOf("<") != -1) {
            return collectionIdent(type);
        }
        return qualIdent(type);
    }

    private String findConverterName(StructInfo structInfo) {
        int dotIndex = structInfo.binaryName.lastIndexOf('.');
        String className = structInfo.binaryName.substring(dotIndex + 1);
        if (dotIndex == -1) {
            return className + "_FASTJOSNReader2";
        }
        String packageName = structInfo.binaryName.substring(0, dotIndex);
        return packageName + '.' + className + "_FASTJOSNReader2";
    }
}
