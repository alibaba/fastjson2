package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.function.ToByteFunction;
import org.junit.jupiter.api.Test;

import java.lang.invoke.*;
import java.util.function.ToIntFunction;

import static com.alibaba.fastjson2.util.JDKUtils.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JDKUtilsTest {
    @Test
    public void test17() {
        if (STRING_CREATOR_JDK11 == null) {
            return;
        }

        String str = "abc";
        byte[] value = JDKUtils.STRING_VALUE.apply(str);

        String str1 = STRING_CREATOR_JDK11.apply(value, LATIN1);
        byte[] value1 = JDKUtils.STRING_VALUE.apply(str1);

        assertSame(value, value1);
    }

    public interface ObjByteFunction<T, R> {
        R apply(T t, byte u);
    }

    public interface ObjIntFunction<T, R> {
        R apply(T t, int u);
    }

    @Test
    public void lookupJDK8() throws Throwable {
        if (JVM_VERSION != 8) {
            return;
        }

        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(String.class);

        MethodHandle strC = lookup.findConstructor(
                String.class, MethodType.methodType(void.class, char[].class, boolean.class)
        );

        MethodHandles.Lookup caller = JDKUtils.trustedLookup(ObjIntFunction.class);
        CallSite callSite = LambdaMetafactory.metafactory(
                caller,
                "apply",
                MethodType.methodType(ObjByteFunction.class),
                MethodType.methodType(Object.class, Object.class, boolean.class),
                strC,
                MethodType.methodType(String.class, char[].class, boolean.class)
        );
        MethodHandle target = callSite.getTarget();
        ObjByteFunction<byte[], String> stringCreatorJDK8 = (ObjByteFunction<byte[], String>) target.invokeExact();
        assertNotNull(stringCreatorJDK8);
    }

    @Test
    public void lookupJDK17_StringCoder() throws Throwable {
        if (JVM_VERSION == 8) {
            return;
        }

        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(String.class);

        MethodHandle methodHandle = lookup.findVirtual(
                String.class, "coder", MethodType.methodType(byte.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "applyAsByte",
                MethodType.methodType(ToIntFunction.class),
                MethodType.methodType(int.class, Object.class),
                methodHandle,
                MethodType.methodType(byte.class, String.class)
        );
        MethodHandle target = callSite.getTarget();
        ToIntFunction<String> stringCreatorJDK11 = (ToIntFunction<String>) target.invokeExact();
        assertNotNull(stringCreatorJDK11);
    }

    @Test
    public void lookup_byte() throws Throwable {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(PrivateBeanByte.class);

        MethodHandle methodHandle = lookup.findVirtual(
                PrivateBeanByte.class, "coder", MethodType.methodType(byte.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "applyAsByte",
                MethodType.methodType(ToByteFunction.class),
                MethodType.methodType(byte.class, Object.class),
                methodHandle,
                MethodType.methodType(byte.class, PrivateBeanByte.class)
        );
        MethodHandle target = callSite.getTarget();
        ToByteFunction<String> func = (ToByteFunction<String>) target.invokeExact();
        assertNotNull(func);
    }

    private static class PrivateBeanByte {
        private byte coder;

        byte coder() {
            return coder;
        }
    }

    @Test
    public void lookup_int() throws Throwable {
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(PrivateBeanInt.class);

        MethodHandle methodHandle = lookup.findVirtual(
                PrivateBeanInt.class, "coder", MethodType.methodType(int.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                lookup,
                "applyAsInt",
                MethodType.methodType(ToIntFunction.class),
                MethodType.methodType(int.class, Object.class),
                methodHandle,
                MethodType.methodType(int.class, PrivateBeanInt.class)
        );
        MethodHandle target = callSite.getTarget();
        ToIntFunction<PrivateBeanInt> func = (ToIntFunction<PrivateBeanInt>) target.invokeExact();
        assertNotNull(func);
    }

    private static class PrivateBeanInt {
        private byte coder;

        int coder() {
            return coder;
        }
    }
}
