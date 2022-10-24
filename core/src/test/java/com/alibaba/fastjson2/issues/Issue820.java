package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue820 {
    AtomicInteger longCounter = new AtomicInteger();
    AtomicInteger decimalCounter = new AtomicInteger();

    @BeforeEach
    public void init() {
        JSON.register(Long.class, new ObjectWriter<Long>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                Long value = (Long) object;
                if (value == null) {
                    jsonWriter.writeNull();
                } else {
                    jsonWriter.writeString(Long.toString(value));
                }
                longCounter.incrementAndGet();
            }
        });

        JSON.register(BigDecimal.class, new ObjectWriter<BigDecimal>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                BigDecimal value = (BigDecimal) object;
                if (value == null) {
                    jsonWriter.writeNull();
                } else {
                    jsonWriter.writeString(value.toPlainString());
                }
                decimalCounter.incrementAndGet();
            }
        });
    }

    @AfterEach
    public void after() {
        JSON.register(Long.class, (ObjectWriter) null);
        JSON.register(BigDecimal.class, (ObjectWriter) null);
    }

    @Test
    public void test() {
        Map<String, Long> map = new HashMap<>();
        map.put("0", 0L);
        assertEquals("{\"0\":\"0\"}", JSON.toJSONString(map));
        assertEquals("{\"0\":\"0\"}", new String(JSON.toJSONBytes(map)));

        Bean t = new Bean();
        t.setValue(0L);
        t.setValue1(new BigDecimal("0.11"));
        assertEquals("{\"value\":\"0\",\"value1\":\"0.11\"}", JSON.toJSONString(t));
        assertEquals("{\"value\":\"0\",\"value1\":\"0.11\"}", new String(JSON.toJSONBytes(t)));
    }

    @Test
    public void test1() {
        Map<String, BigDecimal> map = new HashMap<>();
        map.put("0", new BigDecimal("0.11"));
        assertEquals("{\"0\":\"0.11\"}", JSON.toJSONString(map));
        assertEquals("{\"0\":\"0.11\"}", new String(JSON.toJSONBytes(map)));
    }

    public static class Bean {
        private Long value;
        private BigDecimal value1;

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

        public BigDecimal getValue1() {
            return value1;
        }

        public void setValue1(BigDecimal value1) {
            this.value1 = value1;
        }
    }
}
