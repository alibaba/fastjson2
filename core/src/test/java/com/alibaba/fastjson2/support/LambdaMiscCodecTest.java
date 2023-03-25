package com.alibaba.fastjson2.support;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
