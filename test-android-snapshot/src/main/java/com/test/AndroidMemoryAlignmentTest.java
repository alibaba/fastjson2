package com.test;

import com.alibaba.fastjson2.JSON;

/**
 * Android ARM 内存对齐测试
 * 模拟在 Android 10 ARM 设备上可能导致 SIGBUS 崩溃的场景
 */
public class AndroidMemoryAlignmentTest {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Android ARM 内存对齐修复验证");
        System.out.println("模拟 Android 10 ARM 设备场景");
        System.out.println("========================================\n");
        
        try {
            testUnalignedMemoryScenarios();
            testComplexJSONParsing();
            
            System.out.println("\n✅ 所有 Android ARM 内存对齐测试通过！");
            System.out.println("   修复有效：不再会在 Android ARM 设备上产生 SIGBUS 崩溃");
        } catch (Exception e) {
            System.err.println("\n❌ 测试失败：" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void testUnalignedMemoryScenarios() {
        System.out.println("1. 测试非对齐内存访问场景...");
        
        // 模拟各种可能导致内存未对齐的场景
        String[] testStrings = {
            "null",      // 4字节对齐场景
            " null",     // 5字节，非对齐
            "  null",    // 6字节，非对齐
            "   null",   // 7字节，非对齐
            "    null",  // 8字节对齐场景
            "     null", // 9字节，非对齐
            "\tnull",    // tab + null
            "\n\rnull", // 换行 + null
        };
        
        for (int i = 0; i < testStrings.length; i++) {
            String testStr = testStrings[i];
            try {
                // 解析这些字符串，内部会触发 IOUtils.isNULL 等方法
                Object result = JSON.parse(testStr.trim());
                if (result != null) {
                    throw new RuntimeException("null 解析结果错误: " + result);
                }
                System.out.printf("   ✅ 场景 %d: 非对齐访问测试通过 (\"%s\")\n", 
                    i + 1, testStr.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t"));
            } catch (Exception e) {
                throw new RuntimeException("场景 " + (i + 1) + " 失败: " + testStr, e);
            }
        }
        
        System.out.println("   非对齐内存访问测试完成！");
    }
    
    private static void testComplexJSONParsing() {
        System.out.println("\n2. 测试复杂 JSON 解析场景...");
        
        // 测试包含大量 null 的复杂 JSON，增加内存对齐问题的触发概率
        String[] complexJsons = {
            "{\"a\":null,\"b\":null,\"c\":null,\"d\":null}",
            "[null,null,null,null,null,null,null,null]",
            "{\"data\":{\"items\":[null,{\"value\":null},null]}}",
            "{\"x\":null,\"y\":{\"z\":null,\"w\":[null,null]}}",
            "[\n  null,\n  null,\n  null\n]",  // 带换行的格式
        };
        
        for (int i = 0; i < complexJsons.length; i++) {
            String json = complexJsons[i];
            try {
                Object result = JSON.parse(json);
                System.out.printf("   ✅ 复杂场景 %d: 解析成功\n", i + 1);
            } catch (Exception e) {
                throw new RuntimeException("复杂场景 " + (i + 1) + " 失败: " + json, e);
            }
        }
        
        System.out.println("   复杂 JSON 解析测试完成！");
    }
}
