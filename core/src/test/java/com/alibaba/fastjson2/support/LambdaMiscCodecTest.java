package com.alibaba.fastjson2.support;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaMiscCodecTest {
    @Test
    public void testToLong() throws Throwable {
        LambdaMiscCodec.ReflectToLongFunction function = new LambdaMiscCodec.ReflectToLongFunction(
                Long.class.getMethod("longValue")
        );
        assertEquals(123L, function.applyAsLong(123L));
    }

    @Test
    public void testLong() throws Throwable {
        LambdaMiscCodec.ReflectLongFunction function = new LambdaMiscCodec.ReflectLongFunction(
                Long.class.getConstructor(long.class)
        );
        assertEquals(123L, function.apply(123L));
    }

    @Test
    public void testFactor() throws Throwable {
        LambdaMiscCodec.FactoryFunction function = new LambdaMiscCodec.FactoryFunction(
                Collections.class.getMethod("singleton", Object.class)
        );
        assertEquals(Collections.singleton(123L), function.apply(123L));
    }

    @Test
    public void testConstructorFunction() throws Throwable {
        LambdaMiscCodec.ConstructorFunction function = new LambdaMiscCodec.ConstructorFunction(
                String.class.getConstructor(char[].class)
        );

        String str = "abcdefg";
        assertEquals(str, function.apply(str.toCharArray()));
    }

    @Test
    public void testBiFunction() throws Throwable {
        LambdaMiscCodec.ConstructorBiFunction function = new LambdaMiscCodec.ConstructorBiFunction(
                MutablePair.class.getConstructor(Object.class, Object.class)
        );

        String left = "a", right = "b";
        MutablePair pair = (MutablePair) function.apply(left, right);
        assertEquals(left, pair.left);
        assertEquals(right, pair.right);
    }

    @Test
    public void testBiFunction1() throws Throwable {
        LambdaMiscCodec.ReflectBiFunction function = new LambdaMiscCodec.ReflectBiFunction(
                Pair.class.getMethod("of", Object.class, Object.class)
        );

        String left = "a", right = "b";
        Pair pair = (Pair) function.apply(left, right);
        assertEquals(left, pair.getLeft());
        assertEquals(right, pair.getRight());
    }

    @Test
    public void testSupplier() throws Throwable {
        LambdaMiscCodec.ReflectSupplier function = new LambdaMiscCodec.ReflectSupplier(
                Collections.class.getMethod("emptyList")
        );
        assertEquals(Collections.emptyList(), function.get());
    }

    @Test
    public void testToInt() throws Throwable {
        LambdaMiscCodec.ReflectToIntFunction function = new LambdaMiscCodec.ReflectToIntFunction(
                Integer.class.getMethod("intValue")
        );
        assertEquals(123, function.applyAsInt(123));
    }

    @Test
    public void testObjInt() throws Throwable {
        LambdaMiscCodec.ReflectObjIntConsumer function = new LambdaMiscCodec.ReflectObjIntConsumer(
                Bean.class.getMethod("setId", int.class)
        );
        Bean bean = new Bean();
        function.accept(bean, 123);
        assertEquals(123, bean.id);
    }

    public static class Bean {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    @Test
    public void testConstructFunction() throws Throwable {
        LambdaMiscCodec.ConstructorFunction function = new LambdaMiscCodec.ConstructorFunction(
                BeanX.class.getConstructor(Bean.class)
        );
        Bean bean = new Bean();
        BeanX apply = (BeanX) function.apply(bean);
        assertSame(bean, apply.bean);
    }

    @Test
    public void testConstructSupplier() throws Throwable {
        LambdaMiscCodec.ConstructorSupplier supplier = new LambdaMiscCodec.ConstructorSupplier(
                Bean.class.getConstructor()
        );
        Bean bean = (Bean) supplier.get();
        assertNotNull(bean);
    }

    public static class BeanX {
        private Bean bean;

        public BeanX(Bean bean) {
            this.bean = bean;
        }
    }

    @Test
    public void testError() throws Throwable {
        LambdaMiscCodec.ConstructorFunction function = new LambdaMiscCodec.ConstructorFunction(
                BeanError.class.getConstructor(Bean.class)
        );
        assertThrows(Exception.class, () -> function.apply(new Bean()));

        LambdaMiscCodec.ConstructorSupplier supplier = new LambdaMiscCodec.ConstructorSupplier(
                BeanError.class.getConstructor(Bean.class)
        );
        assertThrows(Exception.class, () -> supplier.get());
    }

    public static class BeanError {
        public BeanError(Bean bean) {
            throw new RuntimeException();
        }
    }
}
