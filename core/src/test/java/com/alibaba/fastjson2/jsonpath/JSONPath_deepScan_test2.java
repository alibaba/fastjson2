package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_deepScan_test2 {
    @SuppressWarnings("unchecked")
    @Test
    public void test_0() throws Exception {
        Root root = new Root();
        root.company = new Company();
        root.company.departs.add(new Department(1001));
        root.company.departs.add(new Department(1002));
        root.company.departs.add(new Department(1003));

        List<Object> ids = (List<Object>) JSONPath.eval(root, "$..id");
        assertEquals(3, ids.size());
        assertEquals(1001, ids.get(0));
        assertEquals(1002, ids.get(1));
        assertEquals(1003, ids.get(2));
    }

    public static class Root {
        public Company company;
    }

    public static class Company {
        public List<Department> departs = new ArrayList<Department>();
    }

    public static class Department {
        public int id;

        public Department() {
        }

        public Department(int id) {
            this.id = id;
        }
    }
}
