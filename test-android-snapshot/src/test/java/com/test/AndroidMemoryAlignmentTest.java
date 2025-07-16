package com.test;

import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 Android ARM 内存对齐问题修复
 * 测试 fastjson2 2.0.58.android5-SNAPSHOT 版本
 */
public class AndroidMemoryAlignmentTest {

    @Test
    public void testIsNULLMemoryAlignment() {
        System.out.println("=== 测试 Android ARM 内存对齐修复 ===");
        System.out.println("版本: 2.0.58.android5-SNAPSHOT");
        
        // 创建包含 "null" 字符串的 char 数组
        char[] nullChars = {'n', 'u', 'l', 'l'};
        
        // 测试基本的 NULL 检测功能
        assertTrue(IOUtils.isNULL(nullChars, 0), "应该正确识别 'null' 字符串");
        
        // 创建更大的数组来测试不同的偏移量（可能导致内存未对齐）
        char[] largeBuffer = new char[20];
        
        // 在不同偏移位置放置 "null"
        for (int offset = 0; offset <= 16; offset++) {
            // 清空缓冲区
            java.util.Arrays.fill(largeBuffer, ' ');
            
            // 在指定偏移位置放置 "null"
            if (offset + 4 <= largeBuffer.length) {
                largeBuffer[offset] = 'n';
                largeBuffer[offset + 1] = 'u';
                largeBuffer[offset + 2] = 'l';
                largeBuffer[offset + 3] = 'l';
                
                // 测试在各种偏移量下都能正常工作（不崩溃）
                boolean result = IOUtils.isNULL(largeBuffer, offset);
                assertTrue(result, "偏移量 " + offset + " 处应该正确识别 'null'");
                System.out.println("✅ 偏移量 " + offset + " 测试通过");
            }
        }
        
        // 测试非 "null" 字符串
        char[] notNull = {'t', 'r', 'u', 'e'};
        assertFalse(IOUtils.isNULL(notNull, 0), "应该正确识别非 'null' 字符串");
        
        char[] mixed = {'x', 'n', 'u', 'l', 'l', 'y'};
        assertTrue(IOUtils.isNULL(mixed, 1), "应该在偏移位置正确识别 'null'");
        assertFalse(IOUtils.isNULL(mixed, 0), "应该在偏移位置正确识别非 'null'");
        
        System.out.println("✅ 所有内存对齐测试通过！");
    }

    @Test
    public void testGetLongUnalignedSafety() {
        System.out.println("=== 测试 getLongUnaligned 安全性 ===");
        
        char[] buffer = new char[8];
        
        // 测试不同的字符组合
        buffer[0] = 0x1234;
        buffer[1] = 0x5678;
        buffer[2] = 0x9ABC;
        buffer[3] = 0xDEF0;
        
        // 调用 getLongUnaligned，确保不会崩溃
        long result = IOUtils.getLongUnaligned(buffer, 0);
        
        // 验证结果的正确性（按小端序拼接）
        long expected = 0x1234L | (0x5678L << 16) | (0x9ABCL << 32) | (0xDEF0L << 48);
        assertEquals(expected, result, "getLongUnaligned 应该正确拼接字符");
        
        System.out.printf("✅ getLongUnaligned 测试通过: 0x%016X\n", result);
        
        // 测试边界情况
        char[] smallBuffer = {'a', 'b', 'c', 'd'};
        long smallResult = IOUtils.getLongUnaligned(smallBuffer, 0);
        long expectedSmall = 'a' | ((long) 'b' << 16) | ((long) 'c' << 32) | ((long) 'd' << 48);
        assertEquals(expectedSmall, smallResult, "应该正确处理较小的缓冲区");
        
        System.out.println("✅ 边界情况测试通过！");
    }

    @Test
    public void testJsonParsing() {
        System.out.println("=== 测试 JSON 解析功能 ===");
        
        // 测试原始崩溃场景的模拟
        String jsonString = "{\"value\": null, \"number\": 123}";
        
        try {
            // 这里应该不会崩溃
            Object result = com.alibaba.fastjson2.JSON.parse(jsonString);
            assertNotNull(result);
            System.out.println("✅ JSON 解析测试通过: " + result);
        } catch (Exception e) {
            fail("JSON 解析不应该崩溃: " + e.getMessage());
        }
    }
}
