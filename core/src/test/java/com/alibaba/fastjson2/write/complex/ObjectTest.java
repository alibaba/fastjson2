package com.alibaba.fastjson2.write.complex;

import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

public class ObjectTest {
    @Test
    public void test_0() throws Exception {
        Employee emp = new Employee();
        emp.id = 1001;
        Org org = new Org();
        org.id = 2001;
        emp.org = org;

        JSONWriter jw = JSONWriter.of();
        jw.writeAny(emp);
        String json = jw.toString();
        jw.close();

        System.out.println(json);
    }

    public static class Org {
        public int id;
    }

    public static class Employee {
        public int id;
        public Org org;
    }
}
