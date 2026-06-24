package com.alibaba.fastjson.issue_7600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue7655 {
    @Test
    public void toJSONShouldKeepRuntimeSubclassProperties() {
        ClassB classB = new ClassB();
        classB.setField1("value1");
        classB.setField2("value2");
        classB.setField3("value3");
        classB.setField4("value4");

        ClassC classC = new ClassC();
        classC.setFieldClassA(classB);

        JSONObject jsonObject = (JSONObject) JSON.toJSON(classC);
        JSONObject fieldClassA = jsonObject.getJSONObject("fieldClassA");

        assertEquals("value1", fieldClassA.getString("field1"));
        assertEquals("value2", fieldClassA.getString("field2"));
        assertEquals("value3", fieldClassA.getString("field3"));
        assertEquals("value4", fieldClassA.getString("field4"));
    }

    @Test
    public void toJSONShouldKeepRuntimeSubclassCycleSafe() {
        ClassB classB = new ClassB();
        classB.setField1("value1");
        classB.setField2("value2");
        classB.setField3("value3");
        classB.setField4("value4");

        ClassC classC = new ClassC();
        classC.setFieldClassA(classB);
        classB.setOwner(classC);

        JSONObject jsonObject = (JSONObject) JSON.toJSON(classC);
        JSONObject fieldClassA = jsonObject.getJSONObject("fieldClassA");

        assertEquals("value3", fieldClassA.getString("field3"));
        assertSame(jsonObject, fieldClassA.get("owner"));
    }

    public static class ClassA {
        private String field1;
        private String field2;

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public String getField2() {
            return field2;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }
    }

    public static class ClassB extends ClassA {
        private String field3;
        private String field4;
        private ClassC owner;

        public String getField3() {
            return field3;
        }

        public void setField3(String field3) {
            this.field3 = field3;
        }

        public String getField4() {
            return field4;
        }

        public void setField4(String field4) {
            this.field4 = field4;
        }

        public ClassC getOwner() {
            return owner;
        }

        public void setOwner(ClassC owner) {
            this.owner = owner;
        }
    }

    public static class ClassC {
        private ClassA fieldClassA;

        public ClassA getFieldClassA() {
            return fieldClassA;
        }

        public void setFieldClassA(ClassA fieldClassA) {
            this.fieldClassA = fieldClassA;
        }
    }
}
