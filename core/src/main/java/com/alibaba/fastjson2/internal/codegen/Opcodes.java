package com.alibaba.fastjson2.internal.codegen;

import com.alibaba.fastjson2.JSONException;

import static com.alibaba.fastjson2.internal.codegen.ClassWriter.getTypeName;

public class Opcodes {
    public static OpName THIS = var("this");
    public static OpName SUPER = var("super");

    public static Op arrayGet(Op owner, Op index) {
        return new ArrayGet(owner, index);
    }

    public static Op ldc(Object value) {
        return new OpConstant(value);
    }

    public static OpName var(String name) {
        return new OpName(name);
    }

    public static Op invoke(Op owner, String method, Op... args) {
        return new OpInvoke(owner, method, args);
    }

    public static Op invoke(String method, Op... args) {
        return new OpInvoke(THIS, method, args);
    }

    public static Op invokeStatic(Class type, String method, Op... args) {
        return new OpInvoke(var(getTypeName(type)), method, args);
    }

    public static MethodRef methodRef(String type, String method) {
        return new MethodRef(type, method);
    }

    public static Op allocate(Class type, Op... args) {
        return new OpAllocate(getTypeName(type), args);
    }

    public static Op allocate(String type, Op... args) {
        return new OpAllocate(type, args);
    }

    public static Op allocateArray(Class type, Op size, Op... values) {
        return new OpAllocateArray(getTypeName(type), size, values);
    }

    public static Op allocateArray(String type, Op size, Op... values) {
        return new OpAllocateArray(type, size, values);
    }

    public static Op putField(Op owner, String fieldName, Op value) {
        return new PutField(owner, fieldName, value);
    }

    public static Op getStatic(Class type, String field) {
        return new GetStatic(getTypeName(type), field);
    }

    public static Op getStatic(String type, String field) {
        return new GetStatic(type, field);
    }

    public static Op getField(String field) {
        return new GetField(THIS, field);
    }

    public static Op getField(Op owner, String field) {
        return new GetField(owner, field);
    }

    public static Op getField(String owner, String field) {
        return new GetField(var(owner), field);
    }

    public static Op eqNull(Op value) {
        return new OpBinary(value, "==", ldc(null));
    }

    public static Op notNull(Op value) {
        return new OpBinary(value, "!=", ldc(null));
    }

    public static Op cast(Op value, Class type) {
        return new Cast(getTypeName(type), value);
    }

    public static Op bitOr(Op a, Op b) {
        return new OpBinary(a, "|", b);
    }

    public static Op and(Op a, Op b) {
        return new OpBinary(a, "&&", b);
    }

    public static Op and(Op a, Op b, Op c) {
        return new OpBinary(new OpBinary(a, "&&", b), "&&", c);
    }

    public static Op assign(Op a, Op b) {
        return new OpBinary(a, "=", b);
    }

    public static Op assign(String name, Op b) {
        return new OpBinary(var(name), "=", b);
    }

    public static Op ne(Op a, Op b) {
        return new OpBinary(a, "!=", b);
    }

    public static Op eq(Op a, Op b) {
        return new OpBinary(a, "==", b);
    }

    public static Op urs(Op a, Op b) {
        return new OpBinary(a, ">>>", b);
    }

    public static Op eor(Op a, Op b) {
        return new OpBinary(a, "^", b);
    }

    public static Op lt(Op a, Op b) {
        return new OpBinary(a, "<", b);
    }

    public static Op increment(Op value) {
        return new OpUnary(value, "++");
    }

    static class ArrayGet
            implements Op {
        final Op array;
        final Op index;

        public ArrayGet(Op array, Op index) {
            this.array = array;
            this.index = index;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            array.toString(mw, buf, indent);
            buf.append('[');
            index.toString(mw, buf, indent);
            buf.append(']');
        }
    }

    static class PutField
            implements Op {
        public final Op owner;
        public final String fieldName;
        public final Op value;

        public PutField(Op owner, String fieldName, Op value) {
            this.owner = owner;
            this.fieldName = fieldName;
            this.value = value;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            owner.toString(mw, buf, indent);
            buf.append('.');
            buf.append(fieldName).append(" = ");
            value.toString(mw, buf, indent);
        }
    }

    static class GetStatic
            implements Op {
        public final String type;
        public final String fieldName;

        public GetStatic(String type, String fieldName) {
            this.type = type;
            this.fieldName = fieldName;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            buf.append(type).append('.').append(fieldName);
        }
    }

    static class GetField
            implements Op {
        public final Op owner;
        public final String fieldName;

        public GetField(Op owner, String fieldName) {
            this.owner = owner;
            this.fieldName = fieldName;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            owner.toString(mw, buf, indent);
            buf.append('.').append(fieldName);
        }
    }

    static class Cast
            implements Op {
        public final String type;
        public final Op value;

        public Cast(String type, Op value) {
            this.type = type;
            this.value = value;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            buf.append('(').append(type).append(") ");
            boolean quote = value instanceof OpBinary;
            if (quote) {
                buf.append('(');
            }
            value.toString(mw, buf, indent);
            if (quote) {
                buf.append(')');
            }
        }
    }

    public interface Op {
        default void toString(MethodWriter mw, StringBuilder buf, int indent) {
            throw new JSONException("TODO " + this.getClass().getName());
        }
    }

    public static class OpInvoke
            implements Op {
        public final Op owner;
        public final String method;
        public final Op[] args;

        public OpInvoke(Op owner, String method, Op[] args) {
            this.owner = owner;
            this.method = method;
            this.args = args;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            if ("<init>".equals(method)) {
                owner.toString(mw, buf, indent);
            } else {
                if (owner != null) {
                    owner.toString(mw, buf, indent);
                    buf.append('.');
                }
                buf.append(method);
            }

            buf.append('(');
            boolean newLine = args.length > 3;
            if (newLine) {
                buf.append("\n\t");
                for (int i = 0; i < indent; i++) {
                    buf.append('\t');
                }
            }

            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    if (newLine) {
                        buf.append(",\n\t");
                        for (int j = 0; j < indent; j++) {
                            buf.append('\t');
                        }
                    } else {
                        buf.append(", ");
                    }
                }
                args[i].toString(mw, buf, indent + 1);
            }

            if (newLine) {
                buf.append("\n");
                for (int i = 0; i < indent; i++) {
                    buf.append('\t');
                }
            }
            buf.append(')');
        }
    }

    public static class MethodRef
            implements Op {
        public final String type;
        public final String method;

        public MethodRef(String type, String method) {
            this.type = type;
            this.method = method;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            buf.append(type);
            buf.append("::");
            buf.append(method);
        }
    }

    public static class OpAllocate
            implements Op {
        public final String type;
        public final Op[] args;

        public OpAllocate(String type, Op[] args) {
            this.type = type;
            this.args = args;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            buf.append("new ").append(type).append('(');
            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    buf.append(", ");
                }
                args[i].toString(mw, buf, indent);
            }
            buf.append(')');
        }
    }

    public static class OpAllocateArray
            implements Op {
        public final String type;
        public final Op size;
        public final Op[] values;

        public OpAllocateArray(String type, Op size, Op[] values) {
            this.type = type;
            this.size = size;
            this.values = values;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            buf.append("new ").append(type).append('[');

            if (size != null) {
                size.toString(mw, buf, indent);
            }
            buf.append(']');
            if (values != null && values.length > 0) {
                buf.append('{');

                buf.append("\n\t");
                for (int i = 0; i < indent; i++) {
                    buf.append('\t');
                }

                for (int i = 0; i < values.length; i++) {
                    if (i != 0) {
                        buf.append(",\n\t");
                        for (int j = 0; j < indent; j++) {
                            buf.append('\t');
                        }
                    }
                    values[i].toString(mw, buf, indent + 1);
                }

                buf.append("\n");
                for (int i = 0; i < indent; i++) {
                    buf.append('\t');
                }
                buf.append('}');
            }
        }
    }

    static class OpConstant
            implements Op {
        public final Object value;

        public OpConstant(Object value) {
            this.value = value;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            if (value == null) {
                buf.append("null");
                return;
            }

            if (value instanceof String) {
                buf.append('"');
                buf.append(((String) value).replace("\"", "\\\""));
                buf.append('"');
                return;
            }

            if (value instanceof Integer) {
                buf.append(((Integer) value).intValue());
                return;
            }

            if (value instanceof Long) {
                buf.append(((Long) value).longValue());
                buf.append('L');
                return;
            }

            if (value instanceof Class) {
                buf.append(getTypeName((Class) value)).append(".class");
                return;
            }

            if (value instanceof Op) {
                ((Op) value).toString(mw, buf, indent);
                return;
            }

            throw new JSONException("TODO : " + value.getClass().getName());
        }
    }

    public static class OpName
            implements Op {
        public final String name;

        public OpName(String name) {
            this.name = name;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            buf.append(name);
        }
    }

    static class OpBinary
            implements Op {
        public final Op left;
        public final String op;
        public final Op right;

        public OpBinary(Op left, String op, Op right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            boolean lq = left instanceof OpBinary;
            if (lq) {
                buf.append('(');
            }
            left.toString(mw, buf, indent);
            if (lq) {
                buf.append(')');
            }

            buf.append(' ').append(op).append(' ');

            boolean rq = right instanceof OpBinary;
            if (rq) {
                buf.append('(');
            }
            right.toString(mw, buf, indent);
            if (rq) {
                buf.append(')');
            }
        }
    }

    static class OpUnary
            implements Op {
        public final Op left;
        public final String op;

        public OpUnary(Op left, String op) {
            this.left = left;
            this.op = op;
        }

        public void toString(MethodWriter mw, StringBuilder buf, int indent) {
            left.toString(mw, buf, indent);
            buf.append(op);
        }
    }
}
