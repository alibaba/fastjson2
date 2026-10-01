package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadOnlyAtomicArrayRegressionTest {
    @Test
    public void finalFieldsAreReadThroughTheirPropertyAccessors() {
        ObjectReader<FinalFields> reader = ObjectReaderCreator.INSTANCE.createObjectReader(FinalFields.class);
        try (JSONReader json = JSONReader.of("{\"integers\":[123],\"longs\":[4294967296]}")) {
            FinalFields bean = reader.readObject(json);
            assertEquals(123, bean.integers.get(0));
            assertEquals(4294967296L, bean.longs.get(0));
        }
    }

    @Test
    public void conversionPreservesLongValuesOutsideIntegerRange() {
        LongGetter bean = JSON.parseObject("{\"values\":[4294967296,-4294967297]}").to(LongGetter.class);
        assertEquals(4294967296L, bean.values.get(0));
        assertEquals(-4294967297L, bean.values.get(1));
    }

    @Test
    public void getterStillReadsLongValues() {
        LongGetter bean = JSON.parseObject("{\"values\":[4294967296,-4294967297]}", LongGetter.class);
        assertEquals(4294967296L, bean.values.get(0));
        assertEquals(-4294967297L, bean.values.get(1));
    }

    public static class FinalFields {
        public final AtomicIntegerArray integers = new AtomicIntegerArray(1);
        public final AtomicLongArray longs = new AtomicLongArray(1);
    }

    public static class LongGetter {
        private final AtomicLongArray values = new AtomicLongArray(2);

        public AtomicLongArray getValues() {
            return values;
        }
    }
}
