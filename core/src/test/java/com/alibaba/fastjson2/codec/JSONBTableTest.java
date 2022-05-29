package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBTableTest {
    @Test
    public void test_jsonb_table() throws Exception {
        List<Employee> list = new ArrayList<>();
        list.add(new Employee(1));
        list.add(new Employee(2));
        list.add(new Employee(3));

        byte[] bytes = JSONB.toBytes(list);
        List<Employee> list2 = JSONB.parseObject(bytes, new TypeReference<List<Employee>>() {
        }.getType());
        assertEquals(3, list2.size());
        assertEquals(list, list2);
    }

    @Test
    public void test_jsonb_table_org() throws Exception {
        Org org = new Org();
        org.employees = new ArrayList<>();
        org.employees.add(new Employee(1));
        org.employees.add(new Employee(2));
        org.employees.add(new Employee(3));

        List<Org> orgs = Arrays.asList(org);
        byte[] bytes = JSONB.toBytes(orgs);

        JSONBDump.dump(bytes);

        List<Org> list2 = JSONB.parseObject(bytes, new TypeReference<List<Org>>() {
        }.getType());
        assertEquals(orgs.size(), list2.size());
        assertEquals(org, list2.get(0));
    }

    public static class Org {
        public List<Employee> employees;

        @Override
        public int hashCode() {
            return Objects.hash(employees);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Org org = (Org) o;

            return employees != null ? employees.equals(org.employees) : org.employees == null;
        }
    }

    public static class Employee {
        public int id;

        public Employee() {
        }

        public Employee(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Employee employee = (Employee) o;

            return id == employee.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}
