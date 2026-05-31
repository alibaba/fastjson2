package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4007 {
    @Test
    public void test() {
        String json = "{\n" +
                "    \"activityId\": \"9260304192237310917\",\n" +
                "    \"activityRuleDtos\": [\n" +
                "        {\n" +
                "            \"awardId\": \"8260304192237381811\",\n" +
                "            \"promotionRuleDtos\": [\n" +
                "                {\n" +
                "                    \"field\": \"dataCenterUserTagLimit\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"field\": \"tradeTime\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"field\": \"skuLimit\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"awardId\": \"8260304192237401811\",\n" +
                "            \"promotionRuleDtos\": [\n" +
                "                {\n" +
                "                   \n" +
                "                    \"field\": \"tradeTime\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"field\": \"skuLimit\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        String jsonPathConfig1 = "$.activityRuleDtos[?(@.promotionRuleDtos[?(@.field == 'dataCenterUserTagLimit')])].awardId";
        JSONArray result1 = (JSONArray) JSONPath.extract(json, jsonPathConfig1);
        assertEquals(1, result1.size());
        assertEquals("8260304192237381811", result1.getString(0));

        // no match
        String jsonPathConfig2 = "$.activityRuleDtos[?(@.promotionRuleDtos[?(@.field == 'trade')])].awardId";
        JSONArray result2 = (JSONArray) JSONPath.extract(json, jsonPathConfig2);
        if (result2 != null) {
            assertEquals(0, result2.size());
        }

        // all match
        String jsonPathConfig3 = "$.activityRuleDtos[?(@.promotionRuleDtos[?(@.field == 'tradeTime')])].awardId";
        JSONArray result3 = (JSONArray) JSONPath.extract(json, jsonPathConfig3);
        assertEquals(2, result3.size());
        assertEquals("8260304192237381811", result3.getString(0));
        assertEquals("8260304192237401811", result3.getString(1));
    }

    // Java POJO 测试 (覆盖 ObjectWriterAdapter 分支)
    @Test
    public void testPojoNestedFilter() {
        Department dept1 = new Department("R&D", Arrays.asList(
                new Employee("Alice", 25),
                new Employee("Bob", 35)
        ), new int[]{101, 102});

        Department dept2 = new Department("HR", Arrays.asList(
                new Employee("Charlie", 28)
        ), new int[]{201});

        List<Department> depts = Arrays.asList(dept1, dept2);

        // 提取包含年龄 >= 30 岁员工的部门名称
        String path = "$[?(@.employees[?(@.age >= 30)])].name";
        Object result = JSONPath.eval(depts, path);
        assertEquals("[\"R&D\"]", JSON.toJSONString(result));
    }

    // 多层嵌套过滤器测试 (覆盖 fieldName2 路径提取逻辑)
    @Test
    public void testMultiLevelNestedFilter() {
        Department dept1 = new Department("Sales", Collections.emptyList(), null);
        dept1.company = new Company();
        dept1.company.projects = Arrays.asList(new Project("done"), new Project("active"));

        Department dept2 = new Department("Marketing", Collections.emptyList(), null);
        dept2.company = new Company();
        dept2.company.projects = Arrays.asList(new Project("done"));

        List<Department> depts = Arrays.asList(dept1, dept2);

        // 提取 company.projects 列表中包含 status == 'active' 的部门名称
        String path = "$[?(@.company.projects[?(@.status == 'active')])].name";
        Object result = JSONPath.eval(depts, path);
        assertEquals("[\"Sales\"]", JSON.toJSONString(result));
    }

    // 边界场景测试
    @Test
    public void testEdgeCases() {
        Department deptEmpty = new Department("EmptyDept", Collections.emptyList(), null);
        Department deptNull = new Department("NullDept", null, null);
        Department deptPrimitive = new Department("PrimDept", null, new int[]{1, 2, 3});

        List<Department> depts = Arrays.asList(deptEmpty, deptNull, deptPrimitive);

        // 测空数组和 null 字段
        String path1 = "$[?(@.employees[?(@.age == 20)])].name";
        assertEquals(null, JSONPath.eval(depts, path1));

        // 测基本类型数组
        String path2 = "$[?(@.codes[?(@.val == 1)])].name";
        assertEquals(null, JSONPath.eval(depts, path2));
    }

    public static class Department {
        public String name;
        public List<Employee> employees;
        public int[] codes;
        public Company company;

        public Department(String name, List<Employee> employees, int[] codes) {
            this.name = name;
            this.employees = employees;
            this.codes = codes;
        }
    }

    public static class Employee {
        public String empName;
        public int age;

        public Employee(String empName, int age) {
            this.empName = empName;
            this.age = age;
        }
    }

    public static class Company {
        public List<Project> projects;
    }

    public static class Project {
        public String status;

        public Project(String status) {
            this.status = status;
        }
    }
}
