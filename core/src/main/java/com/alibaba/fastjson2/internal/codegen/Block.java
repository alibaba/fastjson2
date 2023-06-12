package com.alibaba.fastjson2.internal.codegen;

import com.alibaba.fastjson2.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson2.internal.codegen.Opcodes.*;

public class Block {
    protected final List<Statement> statements = new ArrayList<>();

    public void invoke(String method, Opcodes.Op... args) {
        statements.add(new StatementOp(Opcodes.invoke(method, args)));
    }

    public void invoke(Opcodes.Op owner, String method, Opcodes.Op... args) {
        statements.add(new StatementOp(Opcodes.invoke(owner, method, args)));
    }

    public void putField(String fieldName, Opcodes.Op value) {
        putField(THIS, fieldName, value);
    }

    public void putField(Opcodes.Op owner, String fieldName, Opcodes.Op value) {
        statements.add(new StatementOp(Opcodes.putField(owner, fieldName, value)));
    }

    public void ret(Opcodes.Op value) {
        statements.add(new StatementRet(value));
    }

    public void continueStmt() {
        statements.add(new StatementContinue(null));
    }

    public void continueStmt(String label) {
        statements.add(new StatementContinue(label));
    }

    public void breakStmt() {
        statements.add(new StatementBreak(null));
    }

    public void breakStmt(String label) {
        statements.add(new StatementBreak(label));
    }

    public IfStmt ifStmt(Opcodes.Op testValue) {
        IfStmt stmt = new IfStmt(testValue);
        statements.add(stmt);
        return stmt;
    }

    public IfStmt ifNull(Opcodes.Op testValue) {
        IfStmt stmt = new IfStmt(eq(testValue, ldc(null)));
        statements.add(stmt);
        return stmt;
    }

    public void newLine() {
        statements.add(new Empty());
    }

    public void label(String label) {
        statements.add(new Label(label));
    }

    public ForStmt forStmt(Class initType, Opcodes.Op init, Opcodes.Op condition, Opcodes.Op increment) {
        ForStmt stmt = new ForStmt(initType, init, condition, increment);
        statements.add(stmt);
        return stmt;
    }

    public SwitchStmt switchStmt(Opcodes.Op op, int[] hashCodes) {
        SwitchStmt stmt = new SwitchStmt(op, hashCodes);
        statements.add(stmt);
        return stmt;
    }

    public Statement stmt(Opcodes.Op op) {
        StatementOp stmt = new StatementOp(op);
        statements.add(stmt);
        return stmt;
    }

    public Statement declare(Class type, Opcodes.OpName name) {
        DeclareStmt stmt = new DeclareStmt(ClassWriter.getTypeName(type), name, null);
        statements.add(stmt);
        return stmt;
    }

    public Statement declare(String type, Opcodes.OpName name) {
        DeclareStmt stmt = new DeclareStmt(type, name, null);
        statements.add(stmt);
        return stmt;
    }

    public Statement declare(Class type, Opcodes.OpName name, Opcodes.Op initValue) {
        DeclareStmt stmt = new DeclareStmt(ClassWriter.getTypeName(type), name, initValue);
        statements.add(stmt);
        return stmt;
    }

    public Statement declare(Class type, Opcodes.OpName name, Object initValue) {
        DeclareStmt stmt = new DeclareStmt(ClassWriter.getTypeName(type), name, initValue == null ? null : ldc(initValue));
        statements.add(stmt);
        return stmt;
    }

    public Statement declare(String type, Opcodes.OpName name, Op initValue) {
        DeclareStmt stmt = new DeclareStmt(type, name, initValue == null ? null : ldc(initValue));
        statements.add(stmt);
        return stmt;
    }

    public interface Statement {
        default void toString(MethodWriter mw, StringBuilder buf, int indent) {
            throw new JSONException("TODO " + getClass().getName());
        }
    }

    public static class Empty
            implements Statement {
        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
        }
    }

    public static class IfStmt
            extends Block
            implements Statement {
        final Opcodes.Op condition;
        Block elseStmt;

        public IfStmt(Opcodes.Op testValue) {
            this.condition = testValue;
        }

        public Block elseStmt() {
            return elseStmt = new Block();
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);
            buf.append("if (");
            condition.toString(mw, buf, indent);
            buf.append(") {").append('\n');

            for (int i = 0; i < statements.size(); i++) {
                if (i != 0) {
                    buf.append('\n');
                }
                Statement stmt = statements.get(i);
                stmt.toString(mw, buf, indent + 1);
            }

            buf.append('\n');
            mw.ident(buf, indent);
            buf.append("}");

            if (elseStmt != null) {
                buf.append(" else {\n");
                for (int i = 0; i < elseStmt.statements.size(); i++) {
                    if (i != 0) {
                        buf.append('\n');
                    }
                    Statement stmt = elseStmt.statements.get(i);
                    stmt.toString(mw, buf, indent + 1);
                }
                buf.append('\n');
                mw.ident(buf, indent);
                buf.append("}");
            }
        }
    }

    public static class Label
            implements Statement {
        final String label;

        public Label(String label) {
            this.label = label;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);
            buf.append(label).append(':');
        }
    }

    public static class SwitchStmt
            implements Statement {
        final Opcodes.Op op;
        final int[] hashKeys;
        final Block[] lables;
        final Block dflt;

        public SwitchStmt(Opcodes.Op op, int[] hashKeys) {
            this.op = op;
            this.hashKeys = hashKeys;
            this.lables = new Block[hashKeys.length];
            for (int i = 0; i < lables.length; i++) {
                lables[i] = new Block();
            }
            this.dflt = new Block();
        }

        public int labels() {
            return lables.length;
        }

        public Block lable(int index) {
            return lables[index];
        }

        public int labelKey(int index) {
            return hashKeys[index];
        }

        public Block dflt() {
            return dflt;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);
            buf.append("switch (");
            op.toString(mw, buf, indent);
            buf.append(") {\n");

            for (int i = 0; i < lables.length; i++) {
                indent++;

                mw.ident(buf, indent);

                indent++;
                buf.append("case ").append(hashKeys[i]).append(": {").append('\n');

                List<Statement> statements = lables[i].statements;
                for (int j = 0; j < statements.size(); j++) {
                    if (j != 0) {
                        buf.append('\n');
                    }
                    statements.get(j).toString(mw, buf, indent);
                }

                buf.append('\n');
                mw.ident(buf, indent);
                buf.append("break;\n");
                indent--;

                mw.ident(buf, indent);
                buf.append("}\n");
                indent--;
            }

            indent++;
            mw.ident(buf, indent);
            buf.append("default : {\n");

            indent++;
            List<Statement> statements = dflt.statements;
            for (int j = 0; j < statements.size(); j++) {
                if (j != 0) {
                    buf.append('\n');
                }
                statements.get(j).toString(mw, buf, indent);
            }

            if (!statements.isEmpty()) {
                buf.append('\n');
            }
            mw.ident(buf, indent);
            buf.append("break;\n");
            indent--;

            mw.ident(buf, indent);
            buf.append("}\n");

            indent--;

            mw.ident(buf, indent);
            buf.append("}");
        }
    }

    public static class ForStmt
            extends Block
            implements Statement {
        final Class initType;
        final Opcodes.Op init;
        final Opcodes.Op condition;
        final Opcodes.Op increment;

        public ForStmt(Class initType, Opcodes.Op init, Opcodes.Op condition, Opcodes.Op increment) {
            this.initType = initType;
            this.init = init;
            this.condition = condition;
            this.increment = increment;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);
            buf.append("for (");
            if (initType != null) {
                buf.append(initType.getSimpleName()).append(' ');
            }
            if (init != null) {
                init.toString(mw, buf, indent);
            }
            buf.append(';');
            if (condition != null) {
                buf.append(' ');
                condition.toString(mw, buf, indent);
            }
            buf.append(';');
            if (increment != null) {
                buf.append(' ');
                increment.toString(mw, buf, indent);
            }
            buf.append(") {\n");

            for (int i = 0; i < statements.size(); i++) {
                if (i != 0) {
                    buf.append('\n');
                }
                Statement stmt = statements.get(i);
                stmt.toString(mw, buf, indent + 1);
            }

            buf.append('\n');
            mw.ident(buf, indent);
            buf.append("}");
        }
    }

    public static class DeclareStmt
            implements Statement {
        public final String type;
        public final Opcodes.OpName name;
        public final Opcodes.Op initValue;

        public DeclareStmt(String type, Opcodes.OpName name, Opcodes.Op initValue) {
            this.type = type;
            this.name = name;
            this.initValue = initValue;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);
            buf.append(type).append(' ');
            buf.append(name.name);
            if (initValue != null) {
                buf.append(" = ");
                initValue.toString(mw, buf, indent);
            }
            buf.append(';');
        }
    }

    public static class StatementOp
            implements Statement {
        public final Opcodes.Op op;

        StatementOp(Opcodes.Op op) {
            this.op = op;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);
            op.toString(mw, buf, indent);
            buf.append(';');
        }
    }

    public static class StatementRet
            implements Statement {
        public final Opcodes.Op op;

        public StatementRet(Opcodes.Op op) {
            this.op = op;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);

            buf.append("return");
            if (op != null) {
                buf.append(' ');
                op.toString(mw, buf, indent);
            }
            buf.append(';');
        }
    }

    public static class StatementContinue
            implements Statement {
        public final String label;

        public StatementContinue(String label) {
            this.label = label;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);

            buf.append("continue");
            if (label != null) {
                buf.append(' ');
                buf.append(label);
            }
            buf.append(';');
        }
    }

    public static class StatementBreak
            implements Statement {
        public final String label;

        public StatementBreak(String label) {
            this.label = label;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            mw.ident(buf, indent);

            buf.append("break");
            if (label != null) {
                buf.append(' ');
                buf.append(label);
            }
            buf.append(';');
        }
    }
}
