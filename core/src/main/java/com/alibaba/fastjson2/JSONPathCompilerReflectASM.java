package com.alibaba.fastjson2;

import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.internal.asm.ClassWriter;
import com.alibaba.fastjson2.internal.asm.MethodWriter;
import com.alibaba.fastjson2.internal.asm.Opcodes;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.DynamicClassLoader;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;

class JSONPathCompilerReflectASM
        extends JSONPathCompilerReflect {
    // GraalVM not support
    // Android not support
    static final AtomicLong seed = new AtomicLong();
    static final JSONPathCompilerReflectASM INSTANCE = new JSONPathCompilerReflectASM(
            DynamicClassLoader.getInstance()
    );

    static final String DESC_OBJECT_READER = ASMUtils.desc(ObjectReader.class);
    static final String DESC_FIELD_READER = ASMUtils.desc(FieldReader.class);
    static final String DESC_OBJECT_WRITER = ASMUtils.desc(ObjectWriter.class);
    static final String DESC_FIELD_WRITER = ASMUtils.desc(FieldWriter.class);
    static final String TYPE_SINGLE_NAME_PATH_TYPED = ASMUtils.type(SingleNamePathTyped.class);
    static final String METHOD_SINGLE_NAME_PATH_TYPED_INIT = "(Ljava/lang/String;Ljava/lang/Class;" + DESC_OBJECT_READER + DESC_FIELD_READER + DESC_OBJECT_WRITER + DESC_FIELD_WRITER + ")V";

    static final int THIS = 0;

    protected final DynamicClassLoader classLoader;

    public JSONPathCompilerReflectASM() {
        this.classLoader = new DynamicClassLoader();
    }

    public JSONPathCompilerReflectASM(DynamicClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private boolean support(Class objectClass) {
        boolean externalClass = classLoader.isExternalClass(objectClass);
        int objectClassModifiers = objectClass.getModifiers();
        return Modifier.isAbstract(objectClassModifiers)
                || Modifier.isInterface(objectClassModifiers)
                || !Modifier.isPublic(objectClassModifiers)
                || externalClass;
    }

    @Override
    protected JSONPath compileSingleNamePath(Class objectClass, JSONPathSingleName path) {
        if (support(objectClass)) {
            return super.compileSingleNamePath(objectClass, path);
        }

        String fieldName = path.name;

        String TYPE_OBJECT = ASMUtils.type(objectClass);

        ObjectReader objectReader = path.getReaderContext().getObjectReader(objectClass);
        FieldReader fieldReader = objectReader.getFieldReader(fieldName);

        ObjectWriter objectWriter = path.getWriterContext().getObjectWriter(objectClass);
        FieldWriter fieldWriter = objectWriter.getFieldWriter(fieldName);

        ClassWriter cw = new ClassWriter(null);

        String className = "JSONPath_" + seed.incrementAndGet();
        String classNameType;
        String classNameFull;

        Package pkg = JSONPathCompilerReflectASM.class.getPackage();
        if (pkg != null) {
            String packageName = pkg.getName();
            int packageNameLength = packageName.length();
            int charsLength = packageNameLength + 1 + className.length();
            char[] chars = new char[charsLength];
            packageName.getChars(0, packageName.length(), chars, 0);
            chars[packageNameLength] = '.';
            className.getChars(0, className.length(), chars, packageNameLength + 1);
            classNameFull = new String(chars);

            chars[packageNameLength] = '/';
            for (int i = 0; i < packageNameLength; ++i) {
                if (chars[i] == '.') {
                    chars[i] = '/';
                }
            }
            classNameType = new String(chars);
        } else {
            classNameType = className;
            classNameFull = className;
        }

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER, classNameType, TYPE_SINGLE_NAME_PATH_TYPED, new String[]{});

        {
            final int PATH = 1, CLASS = 2, OBJECT_READER = 3, FIELD_READER = 4, OBJECT_WRITER = 5, FIELD_WRITER = 6;

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    METHOD_SINGLE_NAME_PATH_TYPED_INIT,
                    64
            );
            mw.visitVarInsn(Opcodes.ALOAD, THIS);
            mw.visitVarInsn(Opcodes.ALOAD, PATH);
            mw.visitVarInsn(Opcodes.ALOAD, CLASS);
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT_READER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_READER);
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT_WRITER);
            mw.visitVarInsn(Opcodes.ALOAD, FIELD_WRITER);

            mw.visitMethodInsn(Opcodes.INVOKESPECIAL, TYPE_SINGLE_NAME_PATH_TYPED, "<init>", METHOD_SINGLE_NAME_PATH_TYPED_INIT, false);

            mw.visitInsn(Opcodes.RETURN);
            mw.visitMaxs(3, 3);
        }

        if (fieldReader != null) {
            Class fieldClass = fieldReader.fieldClass;
            int OBJECT = 1, VALUE = 2;

            if (fieldClass == int.class) {
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "setInt",
                        "(Ljava/lang/Object;I)V",
                        64
                );
                mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
                mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
                mw.visitVarInsn(Opcodes.ILOAD, VALUE);

                gwSetValue(mw, TYPE_OBJECT, fieldReader);

                mw.visitInsn(Opcodes.RETURN);
                mw.visitMaxs(2, 2);
            }
            if (fieldClass == long.class) {
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "setLong",
                        "(Ljava/lang/Object;J)V",
                        64
                );
                mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
                mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
                mw.visitVarInsn(Opcodes.LLOAD, VALUE);

                gwSetValue(mw, TYPE_OBJECT, fieldReader);

                mw.visitInsn(Opcodes.RETURN);
                mw.visitMaxs(2, 2);
            }

            {
                MethodWriter mw = cw.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "set",
                        "(Ljava/lang/Object;Ljava/lang/Object;)V",
                        64
                );
                mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
                mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
                mw.visitVarInsn(Opcodes.ALOAD, VALUE);
                if (fieldClass == int.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
                } else if (fieldClass == long.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
                } else if (fieldClass == float.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
                } else if (fieldClass == double.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D", false);
                } else if (fieldClass == short.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S", false);
                } else if (fieldClass == byte.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "byteValue", "()B", false);
                } else if (fieldClass == boolean.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                } else if (fieldClass == char.class) {
                    mw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
                    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                }
                gwSetValue(mw, TYPE_OBJECT, fieldReader);

                mw.visitInsn(Opcodes.RETURN);
                mw.visitMaxs(2, 2);
            }
        }

        if (fieldWriter != null) {
            Class fieldClass = fieldReader.fieldClass;

            int OBJECT = 1;

            MethodWriter mw = cw.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "eval",
                    "(Ljava/lang/Object;)Ljava/lang/Object;",
                    64
            );
            mw.visitVarInsn(Opcodes.ALOAD, OBJECT);
            mw.visitTypeInsn(Opcodes.CHECKCAST, TYPE_OBJECT);
            gwGetValue(mw, TYPE_OBJECT, fieldWriter);
            if (fieldClass == int.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            } else if (fieldClass == long.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            } else if (fieldClass == float.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            } else if (fieldClass == double.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            } else if (fieldClass == short.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
            } else if (fieldClass == byte.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            } else if (fieldClass == boolean.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            } else if (fieldClass == char.class) {
                mw.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            }

            mw.visitInsn(Opcodes.ARETURN);
            mw.visitMaxs(2, 2);
        }

        byte[] code = cw.toByteArray();

        Class<?> readerClass = classLoader.defineClassPublic(classNameFull, code, 0, code.length);

        try {
            Constructor<?> constructor = readerClass.getConstructors()[0];
            return (JSONPath) constructor
                    .newInstance(path.path, objectClass, objectReader, fieldReader, objectWriter, fieldWriter);
        } catch (Throwable e) {
            throw new JSONException("compile jsonpath error, path " + path.path + ", objectType " + objectClass.getTypeName(), e);
        }

//        return new SingleNamePathTyped(path.path, objectClass, objectReader, fieldReader, objectWriter, fieldWriter);
    }

    private void gwSetValue(MethodWriter mw, String TYPE_OBJECT, FieldReader fieldReader) {
        Method method = fieldReader.method;
        Field field = fieldReader.field;
        Class fieldClass = fieldReader.fieldClass;
        String fieldClassDesc = ASMUtils.desc(fieldClass);

        if (method != null) {
            Class<?> returnType = method.getReturnType();
            String methodDesc = '(' + fieldClassDesc + ')' + ASMUtils.desc(returnType);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT, method.getName(), methodDesc, false);
            if (returnType != Void.TYPE) { // builder
                mw.visitInsn(Opcodes.POP);
            }
        } else {
            mw.visitFieldInsn(Opcodes.PUTFIELD, TYPE_OBJECT, field.getName(), fieldClassDesc);
        }
    }

    private void gwGetValue(MethodWriter mw, String TYPE_OBJECT, FieldWriter fieldWriter) {
        Method method = fieldWriter.method;
        Field field = fieldWriter.field;
        Class fieldClass = fieldWriter.fieldClass;
        String fieldClassDesc = ASMUtils.desc(fieldClass);

        if (method != null) {
            String methodDesc = "()" + fieldClassDesc;
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TYPE_OBJECT, method.getName(), methodDesc, false);
        } else {
            mw.visitFieldInsn(Opcodes.GETFIELD, TYPE_OBJECT, field.getName(), fieldClassDesc);
        }
    }
}
