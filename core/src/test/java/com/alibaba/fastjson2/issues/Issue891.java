package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue891 {
    private int writerCount;
    private int readerCount;

    @BeforeEach
    public void setUp() {
        JSON.register(long.class, new ObjectWriter<Long>() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                writerCount++;
                Long value = (Long) object;
                if (value == null) {
                    jsonWriter.writeNull();
                } else {
                    jsonWriter.writeString(Long.toString(value));
                }
            }
        });

        JSON.register(long.class, new ObjectReader<Long>() {
            @Override
            public Long readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
                readerCount++;
                final Object o = jsonReader.readAny();
                if (o instanceof String) {
                    return Long.valueOf((String) o);
                }
                throw new UnsupportedOperationException();
            }
        });
    }

    @AfterEach
    public void tearDown() {
        JSON.register(long.class, (ObjectWriter) null);
        JSON.register(long.class, (ObjectReader) null);
    }

    @Test
    public void test() {
        Bean t = new Bean();
        t.setStatus(12345678L);
        assertEquals("{\"status\":\"12345678\"}", JSON.toJSONString(t));
        assertTrue(writerCount > 0);

        String str = "{\"status\":\"12345678\"}";
        Bean v = JSON.parseObject(str, Bean.class);
        assertEquals(12345678, v.getStatus());
        assertTrue(readerCount > 0);
    }

    public static class Bean {
        long status;

        public long getStatus() {
            return status;
        }

        public void setStatus(long status) {
            this.status = status;
        }
    }
}
