/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for the bug fix in JSONWriterUTF16.writeString(char[], int, int, boolean)
 *
 * Bug: The loop condition was incorrect, causing wrong character range to be processed.
 *
 * Before fix:
 *   for (int i = offset; i < len; ++i) {
 *
 * After fix:
 *   for (int i = offset, end = Math.min(offset + len, str.length); i < end; ++i) {
 *
 * This test verifies that the fix works correctly.
 */
public class JSONWriterUTF16BugFixTest {
    @Test
    public void testWriteStringWithOffsetAndLength() {
        // Create a character array
        char[] chars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};

        // Create JSONWriterUTF16 instance
        JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider);
        JSONWriterUTF16 writer = new JSONWriterUTF16(context);

        // Test case 1: offset=2, len=3, should output "cde"
        writer.writeString(chars, 2, 3, true);
        String result = writer.toString();
        System.out.println("Result: " + result); // Should output "cde"
        assertEquals("\"cde\"", result, "Expected \"cde\" but got " + result);
    }

    @Test
    public void testWriteStringWithOffsetAndLengthEdgeCases() {
        // Create a character array
        char[] chars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};

        // Test case 1: offset=0, len=3, should output "abc"
        JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider);
        JSONWriterUTF16 writer = new JSONWriterUTF16(context);
        writer.writeString(chars, 0, 3, true);
        String result1 = writer.toString();
        System.out.println("Result1: " + result1);
        assertEquals("\"abc\"", result1);

        // Test case 2: offset=7, len=3, should output "hij"
        // Note: Array length is 10, indices 7,8,9 are valid, so should output "hij"
        context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider);
        writer = new JSONWriterUTF16(context);
        writer.writeString(chars, 7, 3, true);
        String result2 = writer.toString();
        System.out.println("Result2: " + result2);
        assertEquals("\"hij\"", result2);
    }

    @Test
    public void testWriteStringWithOffsetBeyondArrayLength() {
        // Create a character array
        char[] chars = {'a', 'b', 'c', 'd', 'e'};

        // Test case: offset=3, len=10, should only output "de" (indices 3,4)
        JSONWriter.Context context = new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider);
        JSONWriterUTF16 writer = new JSONWriterUTF16(context);
        writer.writeString(chars, 3, 10, true);
        String result = writer.toString();
        System.out.println("Result: " + result);
        assertEquals("\"de\"", result);
    }
}
