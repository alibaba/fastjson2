package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserDefineReader {
    @Test
    public void testLong1() {
        String json = "{\"data\":\"123\"}";
        LongReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(Long.class, LongReader.INSTANCE);
        BeanLong1 bean = JSON.parseObject(json, BeanLong1.class, JSONFactory.createReadContext(provider));
        assertEquals(123L, bean.data);
        assertEquals(1, LongReader.INSTANCE.count.get());
    }

    @Test
    public void testLong2() {
        String json = "{\"data\":\"123\"}";
        LongReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(Long.class, LongReader.INSTANCE);
        BeanLong2 bean = JSON.parseObject(json, BeanLong2.class, JSONFactory.createReadContext(provider));
        assertEquals(123L, bean.data);
        assertEquals(1, LongReader.INSTANCE.count.get());
    }

    @Test
    public void testLong3() {
        String json = "{\"data\":\"123\"}";
        LongReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(Long.class, LongReader.INSTANCE);
        BeanLong3 bean = JSON.parseObject(json, BeanLong3.class, JSONFactory.createReadContext(provider));
        assertEquals(123L, bean.data);
        assertEquals(1, LongReader.INSTANCE.count.get());
    }

    @Test
    public void testLong4() {
        String json = "{\"data\":\"123\"}";
        LongReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(Long.class, LongReader.INSTANCE);
        BeanLong4 bean = JSON.parseObject(json, BeanLong4.class, JSONFactory.createReadContext(provider));
        assertEquals(123L, bean.data);
        assertEquals(1, LongReader.INSTANCE.count.get());
    }

    static class LongReader
            implements ObjectReader<Long> {
        static final LongReader INSTANCE = new LongReader();

        AtomicInteger count = new AtomicInteger();

        @Override
        public Long readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }
            count.incrementAndGet();
            return jsonReader.readInt64();
        }
    }

    public static class BeanLong1 {
        public Long data;
    }

    public static class BeanLong2 {
        public long data;
    }

    public static class BeanLong3 {
        private long data;

        public long getData() {
            return data;
        }

        public void setData(long data) {
            this.data = data;
        }
    }

    private static class BeanLong4 {
        private long data;

        public BeanLong4(long data) {
            this.data = data;
        }
    }

    @Test
    public void testBigInteger1() {
        String json = "{\"data\":\"123\"}";
        BigIntegerReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(BigInteger.class, BigIntegerReader.INSTANCE);
        BeanBigInteger1 bean = JSON.parseObject(json, BeanBigInteger1.class, JSONFactory.createReadContext(provider));
        assertEquals(BigInteger.valueOf(123), bean.data);
        assertEquals(1, BigIntegerReader.INSTANCE.count.get());
    }

    static class BigIntegerReader
            implements ObjectReader<BigInteger> {
        static final BigIntegerReader INSTANCE = new BigIntegerReader();

        AtomicInteger count = new AtomicInteger();

        @Override
        public BigInteger readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }
            count.incrementAndGet();
            return jsonReader.readBigInteger();
        }
    }

    public static class BeanBigInteger1 {
        public BigInteger data;
    }
}
