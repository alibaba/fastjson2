package com.alibaba.fastjson.issue_1300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 29/07/2017.
 */
public class Issue1310_noasm {
    @Test
    public void test_trim() throws Exception {
        Model model = new Model();
        model.value = " a ";

        assertEquals("{\"value\":\"a\"}", JSON.toJSONString(model));

        Model model2 = JSON.parseObject("{\"value\":\" a \"}", Model.class);
        assertEquals("a", model2.value);
    }

    @Test
    public void test_trim_1() throws Exception {
        Model1 model = new Model1();
        model.value = " a ";

        assertEquals("{\"value\":\"a\"}", JSON.toJSONString(model));

        Model1 model2 = JSON.parseObject("{\"value\":\" a \"}", Model1.class);
        assertEquals("a", model2.value);
    }

    @Test
    public void test_trim_2() throws Exception {
        Model2 model = new Model2(1);
        model.value = " a ";

        assertEquals("{\"id\":1,\"value\":\"a\"}", JSON.toJSONString(model));

        Model2 model2 = JSON.parseObject("{\"value\":\" a \"}", Model2.class);
        assertEquals("a", model2.value);
    }

    private static class Model {
        @JSONField(format = "trim")
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class Model1 {
        @JSONField(format = "trim")
        public String value;
    }

    private static class Model2 {
        private int id;

        @JSONCreator
        public Model2(int id) {
            this.id = id;
        }

        @JSONField(format = "trim")
        private String value;

        public int getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
