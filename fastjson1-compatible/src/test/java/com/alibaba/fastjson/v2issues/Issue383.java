package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue383 {
    @Test
    public void test() {
        String str = "[{id:'1',name:'Dean',age:32,entryDate:'2016-06-29 22:23:00'},{id:'2',name:'Yang',age:31}]";
        {
            List<Employee> employees = JSON.parseArray(str, Employee.class, Feature.AllowUnQuotedFieldNames);
            assertEquals(2, employees.size());
        }
    }

    private static class Employee {
        private String id;
        private String name;
        private Integer age;
        private String birthday;
        private Date entryDate;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public Date getEntryDate() {
            return entryDate;
        }

        public void setEntryDate(Date entryDate) {
            this.entryDate = entryDate;
        }
    }
}
