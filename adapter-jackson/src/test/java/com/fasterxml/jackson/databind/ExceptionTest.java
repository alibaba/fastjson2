package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ExceptionTest {
    @Test
    public void test() {
        assertNull(new InvalidDefinitionException(null).getType());
    }
}
