package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONDirectTest {
    @Test
    public void test_feature() {
        Model model = new Model();
        model.id = 1001;
        model.value = "{}";

        String json = JSON.toJSONString(model);
        assertEquals("{\"id\":1001,\"value\":{}}", json);
    }

    public static class Model {
        public int id;

        @JSONField(jsonDirect = true)
        public String value;
    }

    @Test
    public void test_feature1() {
        Mode1l model = new Mode1l();
        model.id = 1001;
        model.value = "{}";

        String json = JSON.toJSONString(model);
        assertEquals("{\"id\":1001,\"value\":{}}", json);
    }

    public static class Mode1l {
        public int id;

        @JsonRawValue
        public String value;
    }

    @Test
    public void test_feature2() {
        Mode12 model = new Mode12();
        model.id = 1001;
        model.value = "{}";

        String json = JSON.toJSONString(model);
        assertEquals("{\"id\":1001,\"value\":{}}", json);
    }

    public static class Mode12 {
        public int id;

        @com.alibaba.fastjson.annotation.JSONField(jsonDirect = true)
        public String value;
    }

    @Test
    public void test_feature3() {
        Model3 model = new Model3();
        model.id = 1001;
        model.value = "{}";

        String json = JSON.toJSONString(model);
        assertEquals("{\"id\":1001,\"value\":{}}", json);
    }

    public static class Model3 {
        private int id;

        private String value;

        public int getId() {
            return id;
        }

        @JSONField(jsonDirect = true)
        public String getValue() {
            return value;
        }
    }

    @Test
    public void test_feature4() {
        Model4 model = new Model4();
        model.value = "{}";

        String json = JSON.toJSONString(model);
        assertEquals("{}", json);
    }

    public static class Model4 {
        private String value;

        @JsonValue
        @JsonRawValue
        public String value() {
            return value;
        }
    }

    @Test
    public void test_feature5() {
        Model5 model = new Model5();
        model.value = "{}";

        String json = JSON.toJSONString(model);
        assertEquals("{}", json);
    }

    public static class Model5 {
        private String value;

        @JSONField(jsonDirect = true, value = true)
        public String value() {
            return value;
        }
    }
}
