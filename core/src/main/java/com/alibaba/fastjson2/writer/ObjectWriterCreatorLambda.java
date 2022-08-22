package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.ToByteFunction;
import com.alibaba.fastjson2.function.ToCharFunction;
import com.alibaba.fastjson2.function.ToFloatFunction;
import com.alibaba.fastjson2.function.ToShortFunction;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class ObjectWriterCreatorLambda
        extends ObjectWriterCreator {
    // Android not support
    public static ObjectWriterCreatorLambda INSTANCE = new ObjectWriterCreatorLambda();

    static boolean isExternalClass(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();

        if (classLoader == null) {
            return false;
        }

        ClassLoader current = ObjectWriterCreatorLambda.class.getClassLoader();
        while (current != null) {
            if (current == classLoader) {
                return false;
            }

            current = current.getParent();
        }

        return true;
    }

    @Override
    public <T> FieldWriter<T> createFieldWriter(
            ObjectWriterProvider provider,
            Class<T> objectClass,
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            ObjectWriter initObjectWriter
    ) {
        int modifiers = objectClass.getModifiers();
        if (!Modifier.isPublic(modifiers) || isExternalClass(objectClass)) {
            return super.createFieldWriter(provider, objectClass, fieldName, ordinal, features, format, label, method, initObjectWriter);
        }

        if (initObjectWriter != null) {
            Class<?> fieldClass = method.getReturnType();
            Type fieldType = method.getGenericReturnType();

            FieldWriterObjectMethod objMethod = new FieldWriterObjectMethod(fieldName, ordinal, features, format, label, fieldType, fieldClass, method);
            objMethod.initValueClass = fieldClass;
            if (initObjectWriter != ObjectWriterBaseModule.VoidObjectWriter.INSTANCE) {
                objMethod.initObjectWriter = initObjectWriter;
            }
            return objMethod;
        }

        Class<?> returnClass = method.getReturnType();
        Type returnType = method.getGenericReturnType();
        Object lambda = lambdaSetter(objectClass, returnClass, method);

        if (returnClass == int.class) {
            return new FieldWriterInt32ValFunc(fieldName, ordinal, features, format, label, method, (ToIntFunction<T>) lambda);
        }

        if (returnClass == long.class) {
            if (format == null || format.isEmpty()) {
                return new FieldWriterInt64ValFunc(fieldName, ordinal, features, format, label, method, (ToLongFunction) lambda);
            }

            return new FieldWriterMillisFunc(fieldName, ordinal, features, format, label, method, (ToLongFunction) lambda);
        }

        if (returnClass == boolean.class) {
            return new FieldWriterBoolValFunc(fieldName, ordinal, features, format, label, method, (Predicate<T>) lambda);
        }

        if (returnClass == Boolean.class) {
            return new FieldWriterBooleanFunc(fieldName, ordinal, features, format, label, method, (Function) lambda);
        }

        if (returnClass == short.class) {
            return new FieldWriterInt16ValFunc(fieldName, ordinal, features, format, label, method, (ToShortFunction) lambda);
        }

        if (returnClass == byte.class) {
            return new FieldWriterInt8ValFunc(fieldName, ordinal, features, format, label, method, (ToByteFunction) lambda);
        }

        if (returnClass == float.class) {
            return new FieldWriterFloatValueFunc(fieldName, ordinal, features, format, label, method, (ToFloatFunction) lambda);
        }

        if (returnClass == double.class) {
            return new FieldWriterDoubleValueFunc(fieldName, ordinal, features, format, label, method, (ToDoubleFunction) lambda);
        }

        if (returnClass == char.class) {
            return new FieldWriterCharValFunc(fieldName, ordinal, features, format, label, method, (ToCharFunction) lambda);
        }

        Function function = (Function) lambda;

        return createFieldWriter(provider, objectClass, fieldName, ordinal, features, format, label, returnType, returnClass, method, function);
    }

    private static Map<Class, LambdaInfo> fieldReaderMapping = new HashMap<>();
    private static Map<Class, MethodType> methodTypeMapping = new HashMap<>();
    private static MethodType METHODTYPE_FUNCTION = MethodType.methodType(Function.class);

    static {
        fieldReaderMapping.put(boolean.class, new LambdaInfo(Predicate.class, "test"));
        fieldReaderMapping.put(char.class, new LambdaInfo(ToCharFunction.class, "applyAsChar"));
        fieldReaderMapping.put(byte.class, new LambdaInfo(ToByteFunction.class, "applyAsByte"));
        fieldReaderMapping.put(short.class, new LambdaInfo(ToShortFunction.class, "applyAsShort"));
        fieldReaderMapping.put(int.class, new LambdaInfo(ToIntFunction.class, "applyAsInt"));
        fieldReaderMapping.put(long.class, new LambdaInfo(ToLongFunction.class, "applyAsLong"));
        fieldReaderMapping.put(float.class, new LambdaInfo(ToFloatFunction.class, "applyAsFloat"));
        fieldReaderMapping.put(double.class, new LambdaInfo(ToDoubleFunction.class, "applyAsDouble"));

        fieldReaderMapping.forEach((k, v) -> methodTypeMapping.put(k, MethodType.methodType(v.supplierClass)));
    }

    static class LambdaInfo {
        final Class supplierClass;
        final String methodName;

        LambdaInfo(Class supplierClass, String methodName) {
            this.supplierClass = supplierClass;
            this.methodName = methodName;
        }
    }

    static Object lambdaSetter(Class objectType, Class fieldClass, Method method) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        LambdaInfo buildInfo = fieldReaderMapping.get(fieldClass);
        if (buildInfo == null) {
            buildInfo = new LambdaInfo(Function.class, "apply");
        }

        MethodType invokedType = methodTypeMapping.getOrDefault(fieldClass, METHODTYPE_FUNCTION);
        try {
            MethodHandle target = lookup.findVirtual(objectType,
                    method.getName(),
                    MethodType.methodType(fieldClass)
            );
            MethodType func = target.type();

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    buildInfo.methodName,
                    invokedType,
                    func.erase(),
                    target,
                    func
            );

            return callSite
                    .getTarget()
                    .invoke();
        } catch (Throwable e) {
            throw new JSONException("create fieldLambdaGetter error, method : " + method, e);
        }
    }
}
