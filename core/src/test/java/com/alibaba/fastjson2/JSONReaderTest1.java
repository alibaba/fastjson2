package com.alibaba.fastjson2;

import com.alibaba.fastjson2_vo.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderTest1 {
    @Test
    public void test() {
        String str = "{\"v0000\":+000.00}";
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(LongValue1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Long1.class).getV0000());
            }
        }

        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Int1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Integer1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(FloatValue1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Float1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(DoubleValue1.class).getV0000());
            }
        }
        {
            JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
            for (JSONReader jsonReader : jsonReaders) {
                assertEquals(0, jsonReader.read(Double1.class).getV0000());
            }
        }
    }

    @Test
    public void testDecimal() {
        String str = "{\"id\":+000.00}";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            assertEquals(new BigDecimal("0.00"), jsonReader.read(BigDecimal1.class).getId());
        }
    }

    @Test
    public void testInteger() {
        String str = "{\"id\":+000.00}";
        JSONReader[] jsonReaders = TestUtils.createJSONReaders(str);
        for (JSONReader jsonReader : jsonReaders) {
            assertEquals(BigInteger.ZERO, jsonReader.read(BigInteger1.class).getId());
        }
    }
}
