package com.alibaba.fastjson2.fastcode;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

public class CharacterDataLatin1Test {
    public static void main(String[] args) throws Throwable {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            byte b = (byte) i;
            int cp = b & 0xff;

            boolean r0 = cp != toUpperCaseEx(cp);
            boolean r1 = isLowerCaseEx(cp);
            if (r0) {
                System.out.println(cp + "\t0x" + Integer.toHexString(cp)
                        + "\t" + new String(new byte[]{b}, StandardCharsets.ISO_8859_1) + "\t" + r0
                        + "\t" + new String(new byte[]{(byte) toUpperCaseEx(cp)}, StandardCharsets.ISO_8859_1));
            }
            if (r0 != r1) {
                System.out.println("error " + i);
            }
        }

        System.out.println();
        String str1 = new String(new byte[]{(byte) 0xb5}, StandardCharsets.ISO_8859_1);
        String str2 = new String(new byte[]{(byte) 0xdf}, StandardCharsets.ISO_8859_1);
        System.out.println(str1 + "\t" + str1.toUpperCase());
        System.out.println(str2 + "\t" + str2.toUpperCase());
    }

    static boolean isLowerCaseEx(int ch) {
        return ch >= 'a' && (ch <= 'z' || ch == 0xb5 || (ch >= 0xdf && ch != 0xf7));
    }

    static int toUpperCaseEx(int cp) throws Throwable {
        Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafeField.get(null);

        Class<?> charbinClass = Class.forName("java.lang.CharacterDataLatin1");
        Field field = charbinClass.getDeclaredField("instance");
        long fieldOffset = unsafe.staticFieldOffset(field);
        Object instance = unsafe.getObject(charbinClass, fieldOffset);

        Class lookupClass = MethodHandles.Lookup.class;
        Field implLookup = lookupClass.getDeclaredField("IMPL_LOOKUP");
        MethodHandles.Lookup trustedLookup = (MethodHandles.Lookup) unsafe.getObject(lookupClass,
                UNSAFE.staticFieldOffset(implLookup));

        MethodHandles.lookup();
        MethodHandle toLowerCase = trustedLookup
                .findVirtual(charbinClass, "toUpperCaseEx", MethodType.methodType(int.class, int.class));

        return (Integer) toLowerCase.invoke(instance, cp);
    }
}
