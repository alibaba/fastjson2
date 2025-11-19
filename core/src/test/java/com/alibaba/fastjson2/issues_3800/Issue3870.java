package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3870 {
    @Test
    public void test() {
        JSON.register(BigInteger.class, new BigIntWriter());
        assertEquals("{\"b\":\"456\"}", JSON.toJSONString(new TestData()));
    }

    public static class BigIntWriter implements ObjectWriter {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object instanceof BigInteger) {
                jsonWriter.writeString("456");
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestData {
        BigInteger b = new BigInteger("123");
    }
}
