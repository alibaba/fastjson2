package com.alibaba.fastjson2.android;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

import org.junit.jupiter.api.Test;

public class Issue715 {
    @Test
    public void test() {
        Student student = new Student("bin",12,"湖北");
        byte[] bytes = JSONB.toBytes(student, JSONWriter.Feature.FieldBased);
        Student student2 = JSONB.parseObject(bytes, Student.class, JSONReader.Feature.FieldBased);
    }

    public static class Student {
        private String name;
        private int sge;
        private String address;

        public Student(String name, int sge, String address) {
            this.name = name;
            this.sge = sge;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSge() {
            return sge;
        }

        public void setSge(int sge) {
            this.sge = sge;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    ", sge=" + sge +
                    ", address='" + address + '\'' +
                    '}';
        }
    }
}
