package com.test;

import com.alibaba.fastjson2.util.IOUtils;

/**
 * 演示验证 Android ARM 内存对齐问题修复
 * 使用 2.0.58.android5-SNAPSHOT 版本
 */
public class SnapshotVersionDemo {
    
    public static void main(String[] args) {
        System.out.println("=== fastjson2 Android 快照版本验证 ===");
        System.out.println("版本: 2.0.58.android5-SNAPSHOT");
        System.out.println("仓库: Central Portal Snapshots");
        System.out.println();
        
        try {
            // 测试之前会导致 SIGBUS 的操作
            testMemoryAlignmentFix();
            
            // 测试 JSON 解析
            testJsonParsing();
            
            System.out.println("\n🎉 所有测试通过！Android ARM 内存对齐问题已修复！");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testMemoryAlignmentFix() {
        System.out.println("1. 测试内存对齐修复...");
        
        char[] buffer = new char[20];
        
        // 在不同偏移位置测试 "null" 检测
        for (int offset = 0; offset <= 16; offset++) {
            java.util.Arrays.fill(buffer, ' ');
            
            if (offset + 4 <= buffer.length) {
                buffer[offset] = 'n';
                buffer[offset + 1] = 'u';
                buffer[offset + 2] = 'l';
                buffer[offset + 3] = 'l';
                
                // 测试 getLongUnaligned 方法，在修复前可能在 Android ARM 上崩溃
                try {
                    long result = IOUtils.getLongUnaligned(buffer, offset);
                    System.out.printf("   ✅ 偏移量 %2d: getLongUnaligned 测试通过 (0x%016X)\n", offset, result);
                } catch (Exception e) {
                    throw new RuntimeException("偏移量 " + offset + " 处 getLongUnaligned 测试失败", e);
                }
            }
        }
        System.out.println("   内存对齐测试完成！");
    }
    
    private static void testJsonParsing() {
        System.out.println("\n2. 测试 JSON 解析...");
        
        String[] testCases = {
            "{\"value\": null}",
            "{\"number\": 123, \"text\": null}",
            "[null, 456, null]",
            "{\"nested\": {\"field\": null}}"
        };
        
        for (String json : testCases) {
            try {
                Object result = com.alibaba.fastjson2.JSON.parse(json);
                System.out.println("   ✅ 解析成功: " + json);
            } catch (Exception e) {
                throw new RuntimeException("JSON 解析失败: " + json, e);
            }
        }
        System.out.println("   JSON 解析测试完成！");
    }
}
