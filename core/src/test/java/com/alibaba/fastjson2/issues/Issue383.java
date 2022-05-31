package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue383 {
    @Test
    public void test() {
        String str = "[{id:'1',name:'Dean',age:32,entryDate:'2016-06-29 22:23:00'},{id:'2',name:'Yang',age:31}]";
        {
            List<Employee> employees = JSON.parseArray(str, Employee.class, JSONReader.Feature.AllowUnQuotedFieldNames);
            assertEquals(2, employees.size());
        }

        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        {
            List<Employee> employees = JSON.parseArray(utf8, Employee.class, JSONReader.Feature.AllowUnQuotedFieldNames);
            assertEquals(2, employees.size());
        }
        {
            List<Employee> employees = JSON.parseArray(utf8, 0, utf8.length, StandardCharsets.US_ASCII, Employee.class, JSONReader.Feature.AllowUnQuotedFieldNames);
            assertEquals(2, employees.size());
        }

        {
            JSONReader jsonReaderStr = TestUtils.createJSONReaderStr(str, JSONReader.Feature.AllowUnQuotedFieldNames);
            List<Employee> employees = jsonReaderStr.readArray(Employee.class);
            assertEquals(2, employees.size());
        }

        assertThrows(JSONException.class, () -> JSON.parseArray(str, Employee.class));
        assertThrows(JSONException.class, () -> JSON.parseArray(utf8, Employee.class));
        assertThrows(JSONException.class, () -> JSON.parseArray(utf8, 0, utf8.length, StandardCharsets.US_ASCII, Employee.class));
        assertThrows(JSONException.class, () -> TestUtils.createJSONReaderStr(str).readArray(Employee.class));
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
