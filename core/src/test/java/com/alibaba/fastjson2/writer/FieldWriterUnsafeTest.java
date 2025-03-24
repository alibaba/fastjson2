package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FieldWriterUnsafeTest {
    @Test
    public void test() {
        ObjectWriter objectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(Bean.class);
        assertEquals(-1, objectWriter.getFieldWriter("f0").fieldOffset);
        assertEquals(-1, objectWriter.getFieldWriter("f1").fieldOffset);
        assertNotEquals(-1, objectWriter.getFieldWriter("f2").fieldOffset);
        assertNotEquals(-1, objectWriter.getFieldWriter("f3").fieldOffset);
        assertEquals(-1, objectWriter.getFieldWriter("f4").fieldOffset);
        assertEquals(-1, objectWriter.getFieldWriter("f5").fieldOffset);
        assertEquals(-1, objectWriter.getFieldWriter("f6").fieldOffset);
    }

    public static class Bean {
        public byte f0;
        public short f1;
        public int f2;
        public long f3;
        public float f4;
        public double f5;
        public char f6;
    }
}
