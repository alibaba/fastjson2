package com.alibaba.fastjson2.internal.processor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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

    static JCTree.JCExpression qualIdent(String name) {
        TypeElement typeElement = elements.getTypeElement(name);
        if (typeElement != null) {
            return treeMaker.QualIdent((Symbol) typeElement);
        } else {
            return ident(name(name));
        }
    }

    static JCTree.JCExpression arrayIdent(String name) {
        int idx = name.indexOf("[");
        String type = name.substring(0, idx);
        int count = 0;
        for (char c : name.substring(idx).toCharArray()) {
            if (c == '[') {
                count++;
            }
        }
        JCTree.JCExpression elemTypeExpr;
        switch (type) {
            case "int":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.INT);
                break;
            case "long":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.LONG);
                break;
            case "float":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.FLOAT);
                break;
            case "double":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.DOUBLE);
                break;
            case "boolean":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.BOOLEAN);
                break;
            case "char":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.CHAR);
                break;
            case "byte":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.BYTE);
                break;
            case "short":
                elemTypeExpr = treeMaker.TypeIdent(TypeTag.SHORT);
                break;
            default:
                elemTypeExpr = qualIdent(type);
        }
        return arrayType(elemTypeExpr, count);
    }

    private static JCTree.JCArrayTypeTree arrayType(JCTree.JCExpression elemTypeExpr, int dims) {
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

    static JCTree.JCVariableDecl defVar(long flag, String identName, JCTree.JCExpression identType, JCTree.JCExpression init) {
        return treeMaker.VarDef(modifiers(flag), name(identName), identType, init);
    }

    static JCTree.JCMethodDecl defMethod(long flag, String name, JCTree.JCExpression rtnType, List<JCTree.JCTypeParameter> typeArgs, List<JCTree.JCVariableDecl> params, List<JCTree.JCExpression> recvArgs, JCTree.JCBlock block, JCTree.JCExpression defaultValue) {
        return defMethod(flag, name(name), rtnType, typeArgs, params, recvArgs, block, defaultValue);
    }

    static JCTree.JCMethodDecl defMethod(long flag, Name name, JCTree.JCExpression rtnType, List<JCTree.JCTypeParameter> typeArgs, List<JCTree.JCVariableDecl> params, List<JCTree.JCExpression> recvArgs, JCTree.JCBlock block, JCTree.JCExpression defaultValue) {
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

    static JCTree.JCMethodInvocation method(List<JCTree.JCExpression> typeArgs, JCTree.JCExpression method, List<JCTree.JCExpression> args) {
        if (typeArgs == null) {
            typeArgs = List.nil();
        }
        if (args == null) {
            args = List.nil();
        }
        return treeMaker.Apply(typeArgs, method, args);
    }

    static JCTree.JCFieldAccess field(JCTree.JCExpression expr, String name) {
        return treeMaker.Select(expr, name(name));
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

    static JCTree.JCAssign assign(JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Assign(expr1, expr2);
    }

    static JCTree.JCIf defIf(JCTree.JCExpression cond, JCTree.JCStatement thenStmt, JCTree.JCStatement elseStmt) {
        return treeMaker.If(cond, thenStmt, elseStmt);
    }

    static JCTree.JCBinary binary(JCTree.Tag tag, JCTree.JCExpression expr1, JCTree.JCExpression expr2) {
        return treeMaker.Binary(tag, expr1, expr2);
    }

    static JCTree.JCUnary unary(JCTree.Tag tag, JCTree.JCExpression expr) {
        return treeMaker.Unary(tag, expr);
    }

    static JCTree.JCBlock block(JCTree.JCStatement stmt) {
        return block(0L, stmt);
    }

    static JCTree.JCBlock block(long pos, JCTree.JCStatement stmt) {
        return block(pos, List.of(stmt));
    }

    static JCTree.JCBlock block(List<JCTree.JCStatement> stmts) {
        return block(0L, stmts);
    }

    static JCTree.JCBlock block(long pos, List<JCTree.JCStatement> stmts) {
        return treeMaker.Block(pos, stmts);
    }

    static JCTree.JCLiteral literal(TypeTag tag, Object object) {
        return treeMaker.Literal(tag, object);
    }

    static JCTree.JCLiteral literal(Object object) {
        return treeMaker.Literal(object);
    }

    static JCTree.JCTypeCast cast(JCTree type, JCTree.JCExpression expr) {
        return treeMaker.TypeCast(type, expr);
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

    static JCTree.JCLabeledStatement label(String name, JCTree.JCStatement stmt) {
        return treeMaker.Labelled(name(name), stmt);
    }

    static JCTree.JCBreak defBreak(JCTree.JCLabeledStatement labeledStatement) {
        return defBreak(labeledStatement.label);
    }

    static JCTree.JCBreak defBreak(Name name) {
        return treeMaker.Break(name);
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
                return (JCTree.JCCase) method.invoke(treeMaker, statement.get(null), List.of(matchExpr), List.of(block(matchStmts)), null);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
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

    static JCTree.JCLambda lambda(List<JCTree.JCVariableDecl> args, JCTree body) {
        if (args == null) {
            args = List.nil();
        }
        return treeMaker.Lambda(args, body);
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

    static void pos(int pos) {
        treeMaker.pos = pos;
    }
}
