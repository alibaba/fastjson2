package com.alibaba.fastjson2.internal.processor;

import com.alibaba.fastjson2.JSONWriter;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import java.lang.reflect.*;

import static com.alibaba.fastjson2.internal.processor.CodeGenUtils.JVM_VERSION;

final class JavacTreeUtils {
    private static TreeMaker treeMaker;
    private static Names names;
    private static Elements elements;

    private JavacTreeUtils() {
        throw new UnsupportedOperationException("this class can not be instantiated");
    }

    static void initialize(TreeMaker _treeMaker, Names _names, Elements _elements) {
        treeMaker = _treeMaker;
        names = _names;
        elements = _elements;
    }

    static ProcessingEnvironment unwrapProcessingEnv(ProcessingEnvironment processingEnv) {
        if (processingEnv instanceof JavacProcessingEnvironment) {
            return processingEnv;
        }
        // IntelliJ >2020.3 wraps the processing environment in a dynamic proxy.
        ProcessingEnvironment unwrappedIntelliJ = unwrapIntelliJ(processingEnv);
        if (unwrappedIntelliJ != null) {
            return unwrapProcessingEnv(unwrappedIntelliJ);
        }
        // Gradle incremental build wraps the processing environment.
        for (Class<?> envClass = processingEnv.getClass(); envClass != null; envClass = envClass.getSuperclass()) {
            ProcessingEnvironment unwrappedGradle = unwrapGradle(envClass, processingEnv);
            if (unwrappedGradle != null) {
                return unwrapProcessingEnv(unwrappedGradle);
            }
        }
        throw new IllegalArgumentException("failed to retrieve JavacProcessingEnvironment");
    }

    private static ProcessingEnvironment unwrapIntelliJ(ProcessingEnvironment processingEnv) {
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(processingEnv);
            Field field = handler.getClass().getDeclaredField("val$delegateTo");
            field.setAccessible(true);
            Object object = field.get(handler);
            if (object instanceof ProcessingEnvironment) {
                return (ProcessingEnvironment) object;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // just ignore
        }
        return null;
    }

    private static ProcessingEnvironment unwrapGradle(Class<?> delegateClass, ProcessingEnvironment processingEnv) {
        try {
            Field field = delegateClass.getDeclaredField("delegate");
            field.setAccessible(true);
            Object object = field.get(processingEnv);
            if (object instanceof ProcessingEnvironment) {
                return (ProcessingEnvironment) object;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // just ignore
        }
        return null;
    }

    static JCTree.JCLiteral defNull() {
        return literal(TypeTag.BOT, null);
    }

    static Name name(String name) {
        return names.fromString(name);
    }

    static JCTree.JCIdent ident(String name) {
        return treeMaker.Ident(name(name));
    }

    static JCTree.JCIdent ident(Name name) {
        return treeMaker.Ident(name);
    }

    static JCTree.JCIdent ident(JCTree.JCVariableDecl var) {
        return treeMaker.Ident(var.name);
    }

    static JCTree.JCExpression qualIdent(Class type) {
        return qualIdent(type.getName());
    }

    static JCTree.JCExpression qualIdent(String name) {
        TypeElement typeElement = elements.getTypeElement(name);
        if (typeElement != null) {
            return treeMaker.QualIdent((Symbol) typeElement);
        } else {
            return ident(name(name));
        }
    }

    static JCTree.JCExpression arrayIdentType(String name) {
        int idx = name.indexOf("[");
        String type = name;
        int count = 1;
        if (idx != -1) {
            type = name.substring(0, idx);
            count = 0;
            for (char c : name.substring(idx).toCharArray()) {
                if (c == '[') {
                    count++;
                }
            }
        }
        return arrayType(identType(type), count);
    }

    static JCTree.JCExpression identType(String type) {
        JCTree.JCExpression identType;
        switch (type) {
            case "int":
                identType = treeMaker.TypeIdent(TypeTag.INT);
                break;
            case "long":
                identType = treeMaker.TypeIdent(TypeTag.LONG);
                break;
            case "float":
                identType = treeMaker.TypeIdent(TypeTag.FLOAT);
                break;
            case "double":
                identType = treeMaker.TypeIdent(TypeTag.DOUBLE);
                break;
            case "boolean":
                identType = treeMaker.TypeIdent(TypeTag.BOOLEAN);
                break;
            case "char":
                identType = treeMaker.TypeIdent(TypeTag.CHAR);
                break;
            case "byte":
                identType = treeMaker.TypeIdent(TypeTag.BYTE);
                break;
            case "short":
                identType = treeMaker.TypeIdent(TypeTag.SHORT);
                break;
            default:
                identType = qualIdent(type);
        }
        return identType;
    }

    static String boxed(String type) {
        String boxed = type;
        switch (type) {
            case "int":
                boxed = "Integer";
                break;
            case "long":
                boxed = "Long";
                break;
            case "float":
                boxed = "Float";
                break;
            case "double":
                boxed = "Double";
                break;
            case "boolean":
                boxed = "Boolean";
                break;
            case "char":
                boxed = "Character";
                break;
            case "byte":
                boxed = "Byte";
                break;
            case "short":
                boxed = "Short";
                break;
            default:
        }
        return boxed;
    }

    static JCTree.JCArrayTypeTree arrayType(JCTree.JCExpression elemTypeExpr, int dims) {
        if (dims == 1) {
            return treeMaker.TypeArray(elemTypeExpr);
        } else {
            return treeMaker.TypeArray(arrayType(elemTypeExpr, dims - 1));
        }
    }

    static JCTree.JCTypeApply collectionIdent(String type) {
        return collectionType(type);
    }

    private static JCTree.JCTypeApply collectionType(String type) {
        int open = type.indexOf("<");
        int close = type.indexOf(">");
        String clazz = type.substring(0, open);
        if (close == type.length() - 1) {
            String args = type.substring(open + 1, close);
            ListBuffer<JCTree.JCExpression> generics = new ListBuffer<>();
            for (String g : args.split(",")) {
                generics.append(qualIdent(g));
            }
            return typeApply(qualIdent(clazz), generics.toList());
        } else {
            return typeApply(qualIdent(clazz), List.of(collectionType(type.substring(open + 1, close + 1))));
        }
    }

    static JCTree.JCVariableDecl defVar(long flag, String identName, JCTree.JCExpression identType) {
        return defVar(flag, identName, identType, null);
    }

    static JCTree.JCVariableDecl defVar(String identName, JCTree.JCExpression identType) {
        return defVar(Flags.PARAMETER, identName, identType, null);
    }

    static JCTree.JCVariableDecl defVar(long flag, String identName, TypeTag typeTag) {
        return defVar(flag, identName, type(typeTag), null);
    }

    static JCTree.JCVariableDecl defVar(String identName, TypeTag typeTag) {
        return defVar(Flags.PARAMETER, identName, type(typeTag), null);
    }

    static JCTree.JCVariableDecl defVar(String identName, JCTree.JCExpression identType, JCTree.JCExpression init) {
        return defVar(Flags.PARAMETER, identName, identType, init);
    }

    static JCTree.JCVariableDecl defVar(Name identName, JCTree.JCExpression identType, JCTree.JCExpression init) {
        return defVar(Flags.PARAMETER, identName, identType, init);
    }

    static JCTree.JCVariableDecl defVar(long flag, Name identName, JCTree.JCExpression identType, JCTree.JCExpression init) {
        return treeMaker.VarDef(modifiers(flag), identName, identType, init);
    }

    static JCTree.JCVariableDecl defVar(long flag, String identName, JCTree.JCExpression identType, JCTree.JCExpression init) {
        return treeMaker.VarDef(modifiers(flag), name(identName), identType, init);
    }

    static JCTree.JCVariableDecl defVar(String identName, TypeTag typeTag, JCTree.JCExpression init) {
        return treeMaker.VarDef(modifiers(Flags.PARAMETER), name(identName), type(typeTag), init);
    }

    static JCTree.JCVariableDecl defVar(long flag, String identName, TypeTag typeTag, JCTree.JCExpression init) {
        return treeMaker.VarDef(modifiers(flag), name(identName), type(typeTag), init);
    }

    static JCTree.JCVariableDecl defVar(String identName, long init) {
        return defVar(Flags.PARAMETER, identName, TypeTag.LONG, literal(init));
    }

    static JCTree.JCVariableDecl defVar(String identName, int init) {
        return defVar(Flags.PARAMETER, identName, TypeTag.INT, literal(init));
    }

    static JCTree.JCVariableDecl defVar(long flag, String identName, long init) {
        return treeMaker.VarDef(modifiers(flag), name(identName), type(TypeTag.LONG), literal(init));
    }

    static JCTree.JCVariableDecl defVar(long flag, String identName, int init) {
        return treeMaker.VarDef(modifiers(flag), name(identName), type(TypeTag.INT), literal(init));
    }

    static JCTree.JCVariableDecl defVar(String identName, boolean init) {
        return defVar(Flags.PARAMETER, identName, init);
    }

    static JCTree.JCVariableDecl defVar(long flag, String identName, boolean init) {
        return treeMaker.VarDef(modifiers(flag), name(identName), type(TypeTag.BOOLEAN), literal(init));
    }

    static JCTree.JCMethodDecl defMethod(
            long flag,
            String name,
            JCTree.JCExpression rtnType,
            List<JCTree.JCVariableDecl> params,
            JCTree.JCBlock block
    ) {
        return defMethod(flag, name(name), rtnType, null, params, null, block, null);
    }

    static JCTree.JCMethodDecl defMethod(
            long flag,
            String name,
            JCTree.JCExpression rtnType,
            List<JCTree.JCTypeParameter> typeArgs,
            List<JCTree.JCVariableDecl> params,
            List<JCTree.JCExpression> recvArgs,
            JCTree.JCBlock block,
            JCTree.JCExpression defaultValue
    ) {
        return defMethod(flag, name(name), rtnType, typeArgs, params, recvArgs, block, defaultValue);
    }

    static JCTree.JCMethodDecl defMethod(
            long flag,
            Name name,
            JCTree.JCExpression rtnType,
            List<JCTree.JCTypeParameter> typeArgs,
            List<JCTree.JCVariableDecl> params,
            List<JCTree.JCExpression> recvArgs,
            JCTree.JCBlock block,
            JCTree.JCExpression defaultValue
    ) {
        if (typeArgs == null) {
            typeArgs = List.nil();
        }
        if (params == null) {
            params = List.nil();
        }
        if (recvArgs == null) {
            recvArgs = List.nil();
        }
        return treeMaker.MethodDef(modifiers(flag), name, rtnType, typeArgs, params, recvArgs, block, defaultValue);
    }

    static JCTree.JCMethodInvocation method(JCTree.JCExpression method) {
        return method(null, method, null);
    }

    static JCTree.JCMethodInvocation method(JCTree.JCExpression method, List<JCTree.JCExpression> args) {
        return method(null, method, args);
    }

    static JCTree.JCMethodInvocation method(JCTree.JCExpression owner, String methodName, List<JCTree.JCExpression> args) {
        return method(null, field(owner, methodName), args);
    }

    static JCTree.JCMethodInvocation method(JCTree.JCExpression method, JCTree.JCExpression arg0) {
        return method(null, method, List.of(arg0));
    }

    static JCTree.JCMethodInvocation method(JCTree.JCExpression method, JCTree.JCExpression arg0, JCTree.JCExpression arg1, JCTree.JCExpression arg2) {
        return method(null, method, List.of(arg0, arg1, arg2));
    }

    static JCTree.JCMethodInvocation method(Name owner, String methodName, List<JCTree.JCExpression> args) {
        return method(null, field(ident(owner), methodName), args);
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCExpression owner,
            String methodName
    ) {
        return method(null, field(owner, methodName), null);
    }

    static JCTree.JCMethodInvocation method(
            Name owner,
            String methodName
    ) {
        return method(null, field(ident(owner), methodName), null);
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCVariableDecl owner,
            String methodName
    ) {
        return method(null, field(ident(owner), methodName), null);
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner, String methodName
    ) {
        return method(null, field(owner, methodName), null);
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCExpression owner,
            String methodName,
            JCTree.JCExpression arg0
    ) {
        return method(null, field(owner, methodName), List.of(arg0));
    }

    static JCTree.JCMethodInvocation method(
            Name owner,
            String methodName,
            JCTree.JCExpression arg0
    ) {
        return method(null, field(ident(owner), methodName), List.of(arg0));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCVariableDecl owner,
            String methodName,
            JCTree.JCExpression arg0
    ) {
        return method(null, field(ident(owner), methodName), List.of(arg0));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            JCTree.JCExpression arg0
    ) {
        return method(null, field(owner, methodName), List.of(arg0));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            String arg0
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            JCTree.JCVariableDecl arg0
    ) {
        return method(null, field(owner, methodName), List.of(ident(arg0)));
    }

    static JCTree.JCMethodInvocation method(
            Name owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1
    ) {
        return method(null, field(ident(owner), methodName), List.of(arg0, arg1));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCExpression owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1
    ) {
        return method(null, field(owner, methodName), List.of(arg0, arg1));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCExpression owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCVariableDecl arg1
    ) {
        return method(null, field(owner, methodName), List.of(arg0, ident(arg1)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1
    ) {
        return method(null, field(owner, methodName), List.of(arg0, arg1));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            int arg1
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            int arg1,
            byte arg2
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            byte arg2
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            int arg2
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            JCTree.JCExpression arg2
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), arg2));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCVariableDecl owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1
    ) {
        return method(null, field(owner, methodName), List.of(arg0, arg1));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCVariableDecl arg1
    ) {
        return method(null, field(owner, methodName), List.of(arg0, ident(arg1)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCExpression owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1,
            JCTree.JCExpression arg2
    ) {
        return method(null, field(owner, methodName), List.of(arg0, arg1, arg2));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1,
            JCTree.JCExpression arg2
    ) {
        return method(null, field(owner, methodName), List.of(arg0, arg1, arg2));
    }

    static JCTree.JCMethodInvocation method(
            Name owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1,
            JCTree.JCExpression arg2
    ) {
        return method(null, field(ident(owner), methodName), List.of(arg0, arg1, arg2));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            long arg3
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            int arg3
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            byte arg3
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            int arg2,
            byte arg3
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            int arg3,
            byte arg4
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3), literal(arg4)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            long arg3,
            byte arg4
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3), literal(arg4)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            long arg3,
            int arg4
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3), literal(arg4)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            long arg3,
            long arg4
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3), literal(arg4)));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCIdent owner,
            String methodName,
            long arg0,
            long arg1,
            long arg2,
            long arg3,
            int arg4,
            byte arg5
    ) {
        return method(null, field(owner, methodName), List.of(literal(arg0), literal(arg1), literal(arg2), literal(arg3), literal(arg4), literal(arg5)));
    }

    static JCTree.JCMethodInvocation method(
            Name owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1,
            JCTree.JCExpression arg2,
            JCTree.JCExpression... args
    ) {
        return method(null, field(ident(owner), methodName), List.of(arg0, arg1, arg2, args));
    }

    static JCTree.JCMethodInvocation method(
            JCTree.JCExpression owner,
            String methodName,
            JCTree.JCExpression arg0,
            JCTree.JCExpression arg1,
            JCTree.JCExpression arg2,
            JCTree.JCExpression... args
    ) {
        return method(null, field(owner, methodName), List.of(arg0, arg1, arg2, args));
    }

    static JCTree.JCMethodInvocation method(List<JCTree.JCExpression> typeArgs, JCTree.JCExpression method, List<JCTree.JCExpression> args) {
        if (typeArgs == null) {
            typeArgs = List.nil();
        }
        if (args == null) {
            args = List.nil();
        }
        return treeMaker.Apply(typeArgs, method, args);
    }

    static JCTree.JCFieldAccess field(JCTree.JCVariableDecl expr, String name) {
        return treeMaker.Select(ident(expr), name(name));
    }

    static JCTree.JCFieldAccess field(JCTree.JCExpression expr, String name) {
        return treeMaker.Select(expr, name(name));
    }

    static JCTree.JCFieldAccess field(Name expr, String name) {
        return treeMaker.Select(ident(expr), name(name));
    }

    static JCTree.JCFieldAccess field(JCTree.JCExpression expr, Name name) {
        return treeMaker.Select(expr, name);
    }

    static JCTree.JCModifiers modifiers(long flag) {
        return treeMaker.Modifiers(flag);
    }

    static JCTree.JCExpressionStatement exec(JCTree.JCExpression expr) {
        return treeMaker.Exec(expr);
    }

    static JCTree.JCAssign assign(JCTree.JCVariableDecl expr1, JCTree.JCExpression expr2) {
        return treeMaker.Assign(ident(expr1), expr2);
    }

    static JCTree.JCAssign assign(Name expr1, JCTree.JCExpression expr2) {
        return treeMaker.Assign(ident(expr1), expr2);
    }

    static JCTree.JCAssign assign(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Assign(expr1, expr2);
    }

    static JCTree.JCIf defIf(JCTree.JCVariableDecl cond, JCTree.JCStatement thenStmt, JCTree.JCStatement elseStmt) {
        return defIf(ident(cond), thenStmt, elseStmt);
    }

    static JCTree.JCIf defIf(JCTree.JCExpression cond, JCTree.JCStatement thenStmt, JCTree.JCStatement elseStmt) {
        return treeMaker.If(cond, thenStmt, elseStmt);
    }

    static JCTree.JCIf defIf(JCTree.JCExpression cond, JCTree.JCStatement thenStmt) {
        return treeMaker.If(cond, thenStmt, null);
    }

    static JCTree.JCIf defIfReturn(JCTree.JCExpression cond, JCTree.JCStatement thenStmt) {
        return treeMaker.If(cond, block(thenStmt, defReturn()), null);
    }

    static JCTree.JCBinary binary(JCTree.Tag tag, JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(tag, expr1, expr2);
    }

    static JCTree.JCBinary or(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.OR, expr1, expr2);
    }

    static JCTree.JCBinary lt(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.LT, expr1, expr2);
    }

    static JCTree.JCBinary lt(JCTree.JCExpression expr1, int expr2) {
        return treeMaker.Binary(JCTree.Tag.LT, expr1, literal(expr2));
    }

    static JCTree.JCBinary lt(JCTree.JCVariableDecl expr1, int expr2) {
        return treeMaker.Binary(JCTree.Tag.LT, ident(expr1), literal(expr2));
    }

    static JCTree.JCBinary eq(JCTree.JCVariableDecl expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.EQ, ident(expr1), expr2);
    }

    static JCTree.JCBinary eq(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.EQ, expr1, expr2);
    }

    static JCTree.JCBinary eq(JCTree.JCExpression expr1, JCTree.JCVariableDecl expr2) {
        return treeMaker.Binary(JCTree.Tag.EQ, expr1, ident(expr2));
    }

    static JCTree.JCBinary eq(JCTree.JCExpression expr1, int expr2) {
        return treeMaker.Binary(JCTree.Tag.EQ, expr1, literal(expr2));
    }

    static JCTree.JCBinary eq(JCTree.JCExpression expr1, long expr2) {
        return treeMaker.Binary(JCTree.Tag.EQ, expr1, literal(expr2));
    }

    static JCTree.JCBinary eq(JCTree.JCVariableDecl expr1, int expr2) {
        return treeMaker.Binary(JCTree.Tag.EQ, ident(expr1), literal(expr2));
    }

    static JCTree.JCBinary notNull(JCTree.JCVariableDecl expr1) {
        return ne(expr1, defNull());
    }

    static JCTree.JCBinary notNull(JCTree.JCExpression expr1) {
        return ne(expr1, defNull());
    }

    static JCTree.JCBinary ne(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.NE, expr1, expr2);
    }

    static JCTree.JCBinary ne(JCTree.JCVariableDecl expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.NE, ident(expr1), expr2);
    }

    static JCTree.JCBinary ne(JCTree.JCVariableDecl expr1, int expr2) {
        return treeMaker.Binary(JCTree.Tag.NE, ident(expr1), literal(expr2));
    }

    static JCTree.JCBinary ne(JCTree.JCVariableDecl expr1, long expr2) {
        return treeMaker.Binary(JCTree.Tag.NE, ident(expr1), literal(expr2));
    }

    static JCTree.JCBinary ne(Name expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.NE, ident(expr1), expr2);
    }

    static JCTree.JCBinary ne(JCTree.JCExpression expr1, int value) {
        return treeMaker.Binary(JCTree.Tag.NE, expr1, literal(value));
    }

    static JCTree.JCBinary ne(JCTree.JCExpression expr1, long value) {
        return treeMaker.Binary(JCTree.Tag.NE, expr1, literal(value));
    }

    static JCTree.JCBinary and(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.AND, expr1, expr2);
    }

    static JCTree.JCBinary bitXor(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.BITXOR, expr1, expr2);
    }

    static JCTree.JCBinary bitAnd(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.BITAND, expr1, expr2);
    }

    static JCTree.JCBinary bitAnd(JCTree.JCExpression expr1, long value) {
        return treeMaker.Binary(JCTree.Tag.BITAND, expr1, literal(value));
    }

    static JCTree.JCBinary bitAnd(JCTree.JCVariableDecl expr1, long value) {
        return treeMaker.Binary(JCTree.Tag.BITAND, ident(expr1), literal(value));
    }

    static JCTree.JCBinary bitAnd(JCTree.JCExpression expr1, JSONWriter.Feature value) {
        return treeMaker.Binary(JCTree.Tag.BITAND, expr1, literal(value.mask));
    }

    static JCTree.JCBinary bitOr(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(JCTree.Tag.BITOR, expr1, expr2);
    }

    static JCTree.JCUnary unary(JCTree.Tag tag, JCTree.JCExpression expr) {
        return treeMaker.Unary(tag, expr);
    }

    static JCTree.JCUnary not(JCTree.JCExpression expr) {
        return treeMaker.Unary(JCTree.Tag.NOT, expr);
    }

    static JCTree.JCUnary not(JCTree.JCVariableDecl var) {
        return treeMaker.Unary(JCTree.Tag.NOT, ident(var));
    }

    static JCTree.JCUnary not(JCTree.JCExpression expr, String name) {
        return not(field(expr, name));
    }

    static JCTree.JCBlock block(JCTree.JCStatement stmt) {
        return block(0L, stmt);
    }

    static JCTree.JCBlock block(JCTree.JCMethodInvocation expr) {
        return block(0L, exec(expr));
    }

    static JCTree.JCBlock block(JCTree.JCStatement... stmts) {
        return block(0L, List.from(stmts));
    }

    static JCTree.JCBlock block(List<JCTree.JCStatement> stmts) {
        return block(0L, stmts);
    }

    static JCTree.JCBlock block(long pos, JCTree.JCStatement stmt) {
        return block(pos, List.of(stmt));
    }

    static JCTree.JCBlock block(long pos, List<JCTree.JCStatement> stmts) {
        return treeMaker.Block(pos, stmts);
    }

    static JCTree.JCLiteral literal(TypeTag tag, Object object) {
        return treeMaker.Literal(tag, object);
    }

    static JCTree.JCExpression literal(byte value) {
        return cast(type(TypeTag.BYTE), treeMaker.Literal(TypeTag.INT, value));
    }

    static JCTree.JCLiteral literal(int value) {
        return treeMaker.Literal(TypeTag.INT, value);
    }

    static JCTree.JCLiteral literal(long value) {
        return treeMaker.Literal(TypeTag.LONG, value);
    }

    static JCTree.JCLiteral literal(Object object) {
        return treeMaker.Literal(object);
    }

    static JCTree.JCTypeCast cast(JCTree type, JCTree.JCExpression expr) {
        return treeMaker.TypeCast(type, expr);
    }

    static JCTree.JCTypeCast cast(TypeTag typeTage, JCTree.JCExpression expr) {
        return treeMaker.TypeCast(type(typeTage), expr);
    }

    static JCTree.JCNewClass newClass(JCTree.JCExpression encl, List<JCTree.JCExpression> typeArgs, JCTree.JCExpression clazz, List<JCTree.JCExpression> args, JCTree.JCClassDecl def) {
        if (typeArgs == null) {
            typeArgs = List.nil();
        }
        if (args == null) {
            args = List.nil();
        }
        return treeMaker.NewClass(encl, typeArgs, clazz, args, def);
    }

    static JCTree.JCPrimitiveTypeTree type(TypeTag tag) {
        return treeMaker.TypeIdent(tag);
    }

    static JCTree.JCLabeledStatement label(String name) {
        return label(name, null);
    }

    static JCTree.JCLabeledStatement label(String name, JCTree.JCStatement stmt) {
        return treeMaker.Labelled(name(name), stmt);
    }

    static JCTree.JCBreak defBreak(JCTree.JCLabeledStatement labeledStatement) {
        Class<? extends TreeMaker> clazz = treeMaker.getClass();
        try {
            Method method = clazz.getDeclaredMethod("Break", Name.class);
            return (JCTree.JCBreak) method.invoke(treeMaker, labeledStatement.label);
        } catch (Exception e) {
            try {
                Method method = clazz.getDeclaredMethod("Break", JCTree.JCExpression.class);
                return (JCTree.JCBreak) method.invoke(treeMaker, new Object[]{null});
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    static JCTree.JCContinue defContinue(JCTree.JCLabeledStatement labeledStatement) {
        return defContinue(labeledStatement.label);
    }

    static JCTree.JCContinue defContinue(Name name) {
        return treeMaker.Continue(name);
    }

    static JCTree.JCForLoop forLoop(List<JCTree.JCStatement> initStmts, JCTree.JCExpression condExpr, List<JCTree.JCExpressionStatement> stepExprs, JCTree.JCStatement bodyStmt) {
        if (initStmts == null) {
            initStmts = List.nil();
        }
        if (stepExprs == null) {
            stepExprs = List.nil();
        }
        return treeMaker.ForLoop(initStmts, condExpr, stepExprs, bodyStmt);
    }

    static JCTree.JCCase defCase(JCTree.JCExpression matchExpr, List<JCTree.JCStatement> matchStmts) {
        if (matchStmts == null) {
            matchStmts = List.nil();
        }
        Class<? extends TreeMaker> clazz = treeMaker.getClass();
        try {
            Method method = clazz.getDeclaredMethod("Case", JCTree.JCExpression.class, List.class);
            return (JCTree.JCCase) method.invoke(treeMaker, matchExpr, matchStmts);
        } catch (Exception e) {
            try {
                Class<?> caseKind = Class.forName("com.sun.source.tree.CaseTree$CaseKind");
                Field statement = Class.forName("com.sun.tools.javac.tree.JCTree$JCCase").getDeclaredField("STATEMENT");
                Method method = clazz.getDeclaredMethod("Case", caseKind, List.class, List.class, JCTree.class);
                if (JVM_VERSION >= 19 && (matchExpr instanceof JCTree.JCLiteral || matchExpr instanceof JCTree.JCTypeCast)) {
                    Class<?> constantCaseLabel = Class.forName("com.sun.tools.javac.tree.JCTree$JCConstantCaseLabel");
                    Constructor<?> constructor = constantCaseLabel.getDeclaredConstructor(JCTree.JCExpression.class);
                    constructor.setAccessible(true);
                    return (JCTree.JCCase) method.invoke(treeMaker, statement.get(null), List.of(constructor.newInstance(matchExpr)), List.of(block(matchStmts)), null);
                } else {
                    return (JCTree.JCCase) method.invoke(treeMaker, statement.get(null), List.of(matchExpr), List.of(block(matchStmts)), null);
                }
            } catch (Exception e2) {
                try {
                    Class<?> caseKind = Class.forName("com.sun.source.tree.CaseTree$CaseKind");
                    Field statement = Class.forName("com.sun.tools.javac.tree.JCTree$JCCase").getDeclaredField("STATEMENT");
                    Method method = clazz.getDeclaredMethod("Case", caseKind, List.class, JCTree.JCExpression.class, List.class, JCTree.class);
                    if (matchExpr instanceof JCTree.JCLiteral || matchExpr instanceof JCTree.JCTypeCast) {
                        Class<?> constantCaseLabel = Class.forName("com.sun.tools.javac.tree.JCTree$JCConstantCaseLabel");
                        Constructor<?> constructor = constantCaseLabel.getDeclaredConstructor(JCTree.JCExpression.class);
                        constructor.setAccessible(true);
                        return (JCTree.JCCase) method.invoke(treeMaker, statement.get(null), List.of(constructor.newInstance(matchExpr)), null, List.of(block(matchStmts)), null);
                    } else {
                        return (JCTree.JCCase) method.invoke(treeMaker, statement.get(null), List.of(matchExpr), null, List.of(block(matchStmts)), null);
                    }
                } catch (Exception e3) {
                    throw new RuntimeException(e3);
                }
            }
        }
    }

    static JCTree.JCSwitch defSwitch(JCTree.JCExpression selectorExpr, List<JCTree.JCCase> cases) {
        if (cases == null) {
            cases = List.nil();
        }
        return treeMaker.Switch(selectorExpr, cases);
    }

    static JCTree.JCReturn defReturn(JCTree.JCExpression expr) {
        return treeMaker.Return(expr);
    }

    static JCTree.JCReturn defReturn() {
        return treeMaker.Return(null);
    }

    static JCTree.JCParens parens(JCTree.JCExpression expr) {
        return treeMaker.Parens(expr);
    }

    static JCTree.JCWhileLoop whileLoop(JCTree.JCExpression condExpr, JCTree.JCStatement bodyStmt) {
        return treeMaker.WhileLoop(condExpr, bodyStmt);
    }

    static JCTree.JCTypeApply typeApply(JCTree.JCExpression clazz, List<JCTree.JCExpression> args) {
        if (args == null) {
            args = List.nil();
        }
        return treeMaker.TypeApply(clazz, args);
    }

    static JCTree.JCArrayAccess indexed(JCTree.JCExpression indexedExpr, JCTree.JCExpression indexExpr) {
        return treeMaker.Indexed(indexedExpr, indexExpr);
    }

    static JCTree.JCLambda lambda(JCTree.JCVariableDecl arg, JCTree body) {
        return treeMaker.Lambda(List.of(arg), body);
    }

    static JCTree.JCLambda lambda(List<JCTree.JCVariableDecl> args, JCTree body) {
        if (args == null) {
            args = List.nil();
        }
        return treeMaker.Lambda(args, body);
    }

    static JCTree.JCMemberReference constructorRef(JCTree.JCExpression beanClass) {
        return treeMaker.Reference(MemberReferenceTree.ReferenceMode.NEW, names.init, beanClass, null);
    }

    static JCTree.JCMemberReference methodRef(JCTree.JCExpression beanClass, String methodName) {
        return treeMaker.Reference(MemberReferenceTree.ReferenceMode.INVOKE, names.fromString(methodName), beanClass, null);
    }

    static JCTree.JCNewArray newArray(JCTree.JCExpression elemTypeExpr, List<JCTree.JCExpression> dimsExprs, List<JCTree.JCExpression> elemDataExprs) {
        if (dimsExprs == null) {
            dimsExprs = List.nil();
        }
        return treeMaker.NewArray(elemTypeExpr, dimsExprs, elemDataExprs);
    }

    static JCTree.JCClassDecl defClass(long flag, String name, List<JCTree.JCTypeParameter> typeArgs, JCTree.JCExpression extendExpr, List<JCTree.JCExpression> implementExprs, List<JCTree> defs) {
        if (typeArgs == null) {
            typeArgs = List.nil();
        }
        if (implementExprs == null) {
            implementExprs = List.nil();
        }
        if (defs == null) {
            defs = List.nil();
        }
        return treeMaker.ClassDef(modifiers(flag), name(name), typeArgs, extendExpr, implementExprs, defs);
    }

    static JCTree.JCAnnotation annotation(JCTree type, List<JCTree.JCExpression> args) {
        if (args == null) {
            args = List.nil();
        }
        return treeMaker.Annotation(type, args);
    }

    static JCTree.JCConditional ternary(JCTree.JCExpression cond, JCTree.JCExpression trueExpr, JCTree.JCExpression falseExpr) {
        return treeMaker.Conditional(cond, trueExpr, falseExpr);
    }

    static JCTree.JCConditional ternary(JCTree.JCVariableDecl cond, JCTree.JCExpression trueExpr, JCTree.JCExpression falseExpr) {
        return treeMaker.Conditional(ident(cond), trueExpr, falseExpr);
    }

    static JCTree.JCConditional ternary(JCTree.JCExpression cond, JCTree.JCExpression trueExpr, boolean falseExpr) {
        return treeMaker.Conditional(cond, trueExpr, literal(falseExpr));
    }

    static JCTree.JCConditional ternary(JCTree.JCExpression cond, int trueExpr, int falseExpr) {
        return treeMaker.Conditional(cond, literal(trueExpr), literal(falseExpr));
    }

    static JCTree.JCConditional ternary(JCTree.JCVariableDecl cond, int trueExpr, int falseExpr) {
        return treeMaker.Conditional(ident(cond), literal(trueExpr), literal(falseExpr));
    }

    static JCTree.JCConditional ternary(JCTree.JCExpression cond, long trueExpr, long falseExpr) {
        return treeMaker.Conditional(cond, literal(trueExpr), literal(falseExpr));
    }

    static void pos(int pos) {
        treeMaker.pos = pos;
    }

    static JCTree.JCBinary isDisable(JCTree.JCExpression featureValues, JSONWriter.Feature feature) {
        return eq(bitAnd(featureValues, literal(feature.mask)), 0);
    }

    static JCTree.JCBinary isDisable(JCTree.JCExpression featureValues, JSONWriter.Feature feature0, JSONWriter.Feature feature1) {
        return eq(bitAnd(featureValues, literal(feature0.mask | feature1.mask)), 0);
    }

    static JCTree.JCBinary isEnable(JCTree.JCExpression featureValues, JSONWriter.Feature feature) {
        return ne(bitAnd(featureValues, literal(feature.mask)), 0);
    }

    static JCTree.JCBinary isEnable(JCTree.JCExpression featureValues, JSONWriter.Feature feature0, JSONWriter.Feature feature1) {
        return ne(bitAnd(featureValues, literal(feature0.mask | feature1.mask)), 0);
    }
    static JCTree.JCBinary isEnable(
            JCTree.JCExpression featureValues,
            JSONWriter.Feature feature0,
            JSONWriter.Feature feature1,
            JSONWriter.Feature feature2
    ) {
        return ne(bitAnd(featureValues, feature0.mask | feature1.mask | feature2.mask), 0);
    }
}
