package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue940 {
    @Test
    public void test() {
        String json = "{\"data\":\"1.111\"}";
        InstantReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(BigDecimal.class, InstantReader.INSTANCE);
        Bean bean = JSON.parseObject(json, Bean.class, JSONFactory.createReadContext(provider));
        assertEquals(new BigDecimal("1.111"), bean.data);
        assertEquals(1, InstantReader.INSTANCE.count.get());
    }

    static class InstantReader
            implements ObjectReader<BigDecimal> {
        static final InstantReader INSTANCE = new InstantReader();

        AtomicInteger count = new AtomicInteger();

        @Override
        public BigDecimal readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.nextIfNull()) {
                return null;
            }
            count.incrementAndGet();
            return jsonReader.readBigDecimal();
        }
    }

    static class Bean {
        BigDecimal data;

        public BigDecimal getData() {
            return data;
        }

        public void setData(BigDecimal data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "TestJsonObj{" +
                    "data=" + data +
                    '}';
        }
    }

    @Test
    public void test1() {
        String json = "{\"data\":\"1.111\"}";
        InstantReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(BigDecimal.class, InstantReader.INSTANCE);
        Bean1 bean = JSON.parseObject(json, Bean1.class, JSONFactory.createReadContext(provider));
        assertEquals(new BigDecimal("1.111"), bean.data);
        assertEquals(1, InstantReader.INSTANCE.count.get());
    }

    public static class Bean1 {
        public BigDecimal data;
    }

    @Test
    public void test2() {
        String json = "{\"data\":\"1.111\"}";
        InstantReader.INSTANCE.count.set(0);
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(BigDecimal.class, InstantReader.INSTANCE);
        Bean2 bean = JSON.parseObject(json, Bean2.class, JSONFactory.createReadContext(provider));
        assertEquals(new BigDecimal("1.111"), bean.data);
        assertEquals(1, InstantReader.INSTANCE.count.get());
    }

    private static class Bean2 {
        private final BigDecimal data;

        public Bean2(BigDecimal data) {
            this.data = data;
        }
    }
}
