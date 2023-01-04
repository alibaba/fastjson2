package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.codec.FieldInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldReaderObjectFieldTest {
    @Test
    public void test() throws Throwable {
        Field f0 = Bean.class.getDeclaredField("f0");
        Field f1 = Bean.class.getDeclaredField("f1");
        Field f2 = Bean.class.getDeclaredField("f2");
        Field f3 = Bean.class.getDeclaredField("f3");
        Field f4 = Bean.class.getDeclaredField("f4");
        Field f5 = Bean.class.getDeclaredField("f5");
        Field f6 = Bean.class.getDeclaredField("f6");
        Field f7 = Bean.class.getDeclaredField("f7");
        Field f8 = Bean.class.getDeclaredField("f8");

        FieldReaderObjectField fr0 = new FieldReaderObjectField("f0", byte.class, byte.class, 0, 0, null, null, null, f0);
        FieldReaderObjectField fr1 = new FieldReaderObjectField("f1", short.class, short.class, 0, 0, null, null, null, f1);
        FieldReaderObjectField fr2 = new FieldReaderObjectField("f2", int.class, int.class, 0, 0, null, null, null, f2);
        FieldReaderObjectField fr3 = new FieldReaderObjectField("f3", long.class, long.class, 0, 0, null, null, null, f3);
        FieldReaderObjectField fr4 = new FieldReaderObjectField("f4", float.class, float.class, 0, 0, null, null, null, f4);
        FieldReaderObjectField fr5 = new FieldReaderObjectField("f5", double.class, double.class, 0, 0, null, null, null, f5);
        FieldReaderObjectField fr6 = new FieldReaderObjectField("f6", boolean.class, boolean.class, 0, 0, null, null, null, f6);
        FieldReaderObjectField fr7 = new FieldReaderObjectField("f7", char.class, char.class, 0, 0, null, null, null, f7);
        FieldReaderObjectField fr8 = new FieldReaderObjectField("f8", Integer.class, Integer.class, 0, 0, null, null, null, f8);

        FieldReaderObjectField[] fieldReaders = new FieldReaderObjectField[]{
                fr0, fr1, fr2, fr3, fr4, fr5, fr6, fr7
        };

        Bean bean = new Bean();
        for (FieldReaderObjectField fieldReader : fieldReaders) {
            assertThrows(Exception.class, () -> fieldReader.accept(bean, new Object()));
        }

        assertThrows(Exception.class, () -> fr8.accept(bean, new Object()));

        fr0.accept(bean, Integer.valueOf(1));
        assertEquals(1, bean.f0);

        fr1.accept(bean, Integer.valueOf(2));
        assertEquals(2, bean.f1);

        fr2.accept(bean, Integer.valueOf(3));
        assertEquals(3, bean.f2);

        fr3.accept(bean, Integer.valueOf(4));
        assertEquals(4, bean.f3);

        fr4.accept(bean, Integer.valueOf(5));
        assertEquals(5, bean.f4);

        fr5.accept(bean, Integer.valueOf(6));
        assertEquals(6, bean.f5);

        fr6.accept(bean, Boolean.TRUE);
        assertEquals(true, bean.f6);

        fr7.accept(bean, Character.valueOf('A'));
        assertEquals('A', bean.f7);
    }

    @Test
    public void test1() throws Throwable {
        Field f0 = Bean.class.getDeclaredField("f0");
        Field f1 = Bean.class.getDeclaredField("f1");
        Field f2 = Bean.class.getDeclaredField("f2");
        Field f3 = Bean.class.getDeclaredField("f3");
        Field f4 = Bean.class.getDeclaredField("f4");
        Field f5 = Bean.class.getDeclaredField("f5");
        Field f6 = Bean.class.getDeclaredField("f6");
        Field f7 = Bean.class.getDeclaredField("f7");
        Field f8 = Bean.class.getDeclaredField("f8");

        long features = FieldInfo.DISABLE_UNSAFE;

        FieldReaderObjectField fr0 = new FieldReaderObjectField("f0", byte.class, byte.class, 0, features, null, null, null, f0);
        FieldReaderObjectField fr1 = new FieldReaderObjectField("f1", short.class, short.class, 0, features, null, null, null, f1);
        FieldReaderObjectField fr2 = new FieldReaderObjectField("f2", int.class, int.class, 0, features, null, null, null, f2);
        FieldReaderObjectField fr3 = new FieldReaderObjectField("f3", long.class, long.class, 0, features, null, null, null, f3);
        FieldReaderObjectField fr4 = new FieldReaderObjectField("f4", float.class, float.class, 0, features, null, null, null, f4);
        FieldReaderObjectField fr5 = new FieldReaderObjectField("f5", double.class, double.class, 0, features, null, null, null, f5);
        FieldReaderObjectField fr6 = new FieldReaderObjectField("f6", boolean.class, boolean.class, 0, features, null, null, null, f6);
        FieldReaderObjectField fr7 = new FieldReaderObjectField("f7", char.class, char.class, 0, features, null, null, null, f7);
        FieldReaderObjectField fr8 = new FieldReaderObjectField("f8", Integer.class, Integer.class, 0, features, null, null, null, f8);

        FieldReaderObjectField[] fieldReaders = new FieldReaderObjectField[]{
                fr0, fr1, fr2, fr3, fr4, fr5, fr6, fr7
        };

        Bean bean = new Bean();
        for (FieldReaderObjectField fieldReader : fieldReaders) {
            assertThrows(Exception.class, () -> fieldReader.accept(bean, new Object()));
        }

        assertThrows(Exception.class, () -> fr8.accept(bean, new Object()));

        fr0.accept(bean, Integer.valueOf(1));
        assertEquals(1, bean.f0);

        fr1.accept(bean, Integer.valueOf(2));
        assertEquals(2, bean.f1);

        fr2.accept(bean, Integer.valueOf(3));
        assertEquals(3, bean.f2);

        fr3.accept(bean, Integer.valueOf(4));
        assertEquals(4, bean.f3);

        fr4.accept(bean, Integer.valueOf(5));
        assertEquals(5, bean.f4);

        fr5.accept(bean, Integer.valueOf(6));
        assertEquals(6, bean.f5);

        fr6.accept(bean, Boolean.TRUE);
        assertEquals(true, bean.f6);

        fr7.accept(bean, Character.valueOf('A'));
        assertEquals('A', bean.f7);
    }

    public static class Bean {
        byte f0;
        short f1;
        int f2;
        long f3;
        float f4;
        double f5;
        boolean f6;
        char f7;
        Integer f8;
    }
}
