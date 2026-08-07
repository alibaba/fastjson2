package com.alibaba.fastjson2.fastcode;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;

public class BigDecimalTest {
    @Test
    public void test() throws Throwable {
        Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafeField.get(null);
        Method objectFieldOffset = Unsafe.class.getMethod("objectFieldOffset", Field.class);
        long stringCacheOffset = (Long) objectFieldOffset.invoke(
                unsafe,
                BigDecimal.class.getDeclaredField("stringCache")
        );

        BigDecimal d = BigDecimal.valueOf(12345, 2);
        unsafe.putObject(d, stringCacheOffset, null);

        d.toString();
        System.out.println(unsafe.getObject(d, stringCacheOffset));
        unsafe.putObject(d, stringCacheOffset, null);
        System.out.println(unsafe.getObject(d, stringCacheOffset));
    }
}
