package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectReaderExceptionTest {
    @Test
    public void test() {
        ObjectReaderException<RuntimeException> objectReader = new ObjectReaderException(RuntimeException.class);
        RuntimeException exception = objectReader.readObject(JSONReader.of("{\"message\":\"xx\"}"));
        assertNotNull(exception);
        assertEquals("xx", exception.getMessage());
    }
}
