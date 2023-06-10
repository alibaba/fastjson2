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
