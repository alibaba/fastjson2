package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2944 {
    @Test
    public void test() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(0, "John"));
        employees.add(new Employee(1, "Jane"));
        employees.add(new Employee(2, "Bob"));
        Department department = new Department("dev", employees);

        String payload = JSON.toJSONString(department);
        JSONObject jsonObject = JSON.parseObject(payload);
        Department parsed = jsonObject.toJavaObject(Department.class);
        assertEquals(payload, JSON.toJSONString(parsed));
    }
}

class Department {
    private final String name;
    private final List<Employee> employees;

    public String getName() {
        return name;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public Department(String name, List<Employee> employees) {
        this.name = name;
        this.employees = employees;
    }

    @Override
    public String toString() {
        return "Department{" +
                "name='" + name + '\'' +
                ", employees=" + employees +
                '}';
    }
}

class Employee {
    private final Integer id;
    private final String name;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
