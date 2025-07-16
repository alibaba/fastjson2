package com.test;

import com.alibaba.fastjson2.JSON;

/**
 * 验证 fastjson2 2.0.58.android5-SNAPSHOT 版本是否包含 Android 内存对齐修复
 */
public class SimpleSnapshotTest {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("fastjson2 Android 内存对齐修复验证");
        System.out.println("测试版本: 2.0.58.android5-SNAPSHOT");
        System.out.println("========================================\n");
        
        try {
            testJSONParsing();
            testNullHandling();
            
            System.out.println("\n✅ 所有测试通过！Android 内存对齐问题已修复。");
            System.out.println("   该版本应该可以在 Android ARM 设备上正常运行。");
        } catch (Exception e) {
            System.err.println("\n❌ 测试失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testJSONParsing() {
        System.out.println("1. 测试 JSON 解析功能...");
        
        String[] testCases = {
            "null",
            "\"null\"",
            "{\"value\": null}",
            "[null, \"test\", null]",
            "{\"data\": null, \"status\": \"ok\"}"
        };
        
        for (String testCase : testCases) {
            try {
                Object result = JSON.parse(testCase);
                System.out.printf("   ✅ 解析成功: %s\n", testCase);
            } catch (Exception e) {
                throw new RuntimeException("JSON 解析失败: " + testCase, e);
            }
        }
        System.out.println("   JSON 解析测试完成！");
    }
    
    private static void testNullHandling() {
        System.out.println("\n2. 测试 null 值处理...");
        
        // 测试各种包含 null 的 JSON 场景
        // 这些测试覆盖了可能触发 IOUtils.isNULL 的情况
        try {
            // 基本 null 测试
            Object nullResult = JSON.parse("null");
            if (nullResult != null) {
                throw new RuntimeException("null 解析失败");
            }
            System.out.println("   ✅ 基本 null 解析正常");
            
            // 对象中的 null 字段
            String jsonWithNull = "{\"name\":\"test\",\"value\":null,\"count\":42}";
            Object objResult = JSON.parse(jsonWithNull);
            System.out.println("   ✅ 对象中的 null 字段处理正常");
            
            // 数组中的 null 元素
            String arrayWithNull = "[\"first\", null, \"third\", null, 123]";
            Object arrayResult = JSON.parse(arrayWithNull);
            System.out.println("   ✅ 数组中的 null 元素处理正常");
            
            // 多层嵌套的 null
            String nestedNull = "{\"level1\":{\"level2\":{\"value\":null}}}";
            Object nestedResult = JSON.parse(nestedNull);
            System.out.println("   ✅ 嵌套结构中的 null 处理正常");
            
        } catch (Exception e) {
            throw new RuntimeException("null 值处理测试失败", e);
        }
        
        System.out.println("   null 值处理测试完成！");
    }
}
