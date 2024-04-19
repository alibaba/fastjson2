package com.alibaba.fastjson2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SymbolTableTest {
	@Test
	void constructorWithNoInput() {
		SymbolTable symbolTable = new SymbolTable();
		assertEquals(0, symbolTable.size());
	}

	@Test
	void constructorWithSingleInput() {
		SymbolTable symbolTable = new SymbolTable("apple");
		assertEquals(1, symbolTable.size());
		assertEquals("apple", symbolTable.getName(1));
	}

	@Test
	void constructorWithMultipleInputs() {
		SymbolTable symbolTable = new SymbolTable("apple", "banana", "cherry", "date");
		assertEquals(4, symbolTable.size());
		assertEquals("apple", symbolTable.getName(1));
		assertEquals("banana", symbolTable.getName(2));
		assertEquals("cherry", symbolTable.getName(3));
		assertEquals("date", symbolTable.getName(4));
		
	}

	@Test
	void constructorWithDuplicateInputs() {
		SymbolTable symbolTable = new SymbolTable("apple", "banana", "cherry", "apple", "banana");
		assertEquals(3, symbolTable.size());
		assertEquals("apple", symbolTable.getName(1));
		assertEquals("banana", symbolTable.getName(2));
		assertEquals("cherry", symbolTable.getName(3));
		
	}

}
