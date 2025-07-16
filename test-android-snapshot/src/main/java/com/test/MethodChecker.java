package com.test;

import com.alibaba.fastjson2.util.IOUtils;
import java.lang.reflect.Method;

public class MethodChecker {
    public static void main(String[] args) {
        System.out.println("IOUtils 类的可用方法：");
        Method[] methods = IOUtils.class.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.getName().contains("Long") || method.getName().contains("NULL") || 
                method.getName().contains("null") || method.getName().contains("Unaligned")) {
                System.out.printf("   %s: %s\n", method.getName(), method.toString());
            }
        }
        
        // 测试 isNULL 相关方法
        System.out.println("\n测试 isNULL 相关方法：");
        try {
            for (Method method : methods) {
                if (method.getName().equals("isNULL")) {
                    System.out.printf("   找到 isNULL 方法: %s\n", method.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
