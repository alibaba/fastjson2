package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.annotation.JSONField;
import java.util.Date;

public class issue3654 {
    
    static class TestDTO {
        @JSONField(defaultValue = "default_name")
        private String name;
        
        @JSONField(defaultValue = "100")
        private int age;
        
        @JSONField(defaultValue = "2023-01-01 00:00:00")
        private Date birthDate;
        
        @JSONField(defaultValue = "true")
        private boolean active;
        
        @JSONField(defaultValue = "99.99")
        private double score;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Date getBirthDate() { return birthDate; }
        public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        
        @Override
        public String toString() {
            return String.format("name=%s, age=%d, birthDate=%s, active=%s, score=%.2f",
                    name, age, birthDate, active, score);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Comprehensive FastJSON Compatibility Test ===");
        
        
        System.out.println("--- Test 1: Without compatibility mode ---");
        JSONFactory.setJSONFieldDefaultValueCompatMode(false);
        try {
            TestDTO dto1 = JSON.parseObject("{}", TestDTO.class);
            System.out.println("UNEXPECTED: Should have failed but succeeded: " + dto1);
        } catch (Exception e) {
            System.out.println("EXPECTED: Failed as expected: " + e.getMessage());
        }
        
        System.out.println("--- Test 2: With compatibility mode ---");
        JSONFactory.setJSONFieldDefaultValueCompatMode(true);
        try {
            TestDTO dto2 = JSON.parseObject("{}", TestDTO.class);
            System.out.println("SUCCESS: Compatibility mode works: " + dto2);
        } catch (Exception e) {
            System.out.println("ERROR: Compatibility mode failed: " + e.getMessage());
            return;
        }
        
        System.out.println("--- Test 3: Partial JSON with existing data ---");
        try {
            String partialJson = "{\"name\":\"John\", \"age\":30}";
            TestDTO dto3 = JSON.parseObject(partialJson, TestDTO.class);
            System.out.println("SUCCESS: Partial parsing: " + dto3);
        } catch (Exception e) {
            System.out.println("ERROR: Partial parsing failed: " + e.getMessage());
            return;
        }
        
        System.out.println("--- Test 4: Empty JSON ---");
        try {
            TestDTO dto4 = JSON.parseObject("{}", TestDTO.class);
            System.out.println("SUCCESS: Empty JSON: " + dto4);
        } catch (Exception e) {
            System.out.println("ERROR: Empty JSON failed: " + e.getMessage());
            return;
        }
        
        System.out.println("--- Test 5: Null/Missing fields ---");
        try {
            String nullJson = "{\"name\":null, \"birthDate\":null}";
            TestDTO dto5 = JSON.parseObject(nullJson, TestDTO.class);
            System.out.println("SUCCESS: Null fields: " + dto5);
        } catch (Exception e) {
            System.out.println("ERROR: Null fields failed: " + e.getMessage());
            return;
        }
        
        System.out.println("--- Test 6: Reset compatibility mode ---");
        JSONFactory.setJSONFieldDefaultValueCompatMode(false);
        System.out.println("Compatibility mode reset to: " + JSONFactory.isJSONFieldDefaultValueCompatMode());
        
        System.out.println("=== All comprehensive tests completed! ===");
    }
}
