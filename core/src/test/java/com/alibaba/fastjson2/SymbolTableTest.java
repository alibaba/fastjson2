package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SymbolTableTest {
    @Test
    public void test() {
        SymbolTable symbolTable = new SymbolTable(Integer.class, Long.class);
        assertEquals(Integer.class.getName(), symbolTable.getName(1));
        assertEquals(Long.class.getName(), symbolTable.getName(2));
    }
}
