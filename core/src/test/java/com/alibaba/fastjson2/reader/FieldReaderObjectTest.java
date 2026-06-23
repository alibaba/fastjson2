package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.util.MapMultiValueType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FieldReaderObjectTest {
    @Test
    public void testMapMultiValueTypeReaderIsBuiltIn() {
        ObjectReader objectReader = new ObjectReaderImplMapMultiValueType(
                MapMultiValueType.of("data", Bean.class)
        );

        assertFalse(FieldReaderObject.isCustomReader(objectReader));
    }

    public static class Bean {
        public int id;
    }
}
