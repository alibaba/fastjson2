package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue715 {
    @Test
    public void test() {
        Student student = new Student("bin", 12, "湖北");
        byte[] bytes = JSONB.toBytes(student);
        Student student2 = JSONB.parseObject(bytes, Student.class);
        assertEquals(student.address, student2.address);
        assertEquals(student.sge, student2.sge);
        assertEquals(student.address, student2.address);
    }

    public class Student {
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
