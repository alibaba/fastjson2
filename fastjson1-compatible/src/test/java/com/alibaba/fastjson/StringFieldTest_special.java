package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringFieldTest_special {
    @Test
    public void test_special() throws Exception {
        Model model = new Model();
        model.name = "a\\bc";
        String text = JSON.toJSONString(model);
        assertEquals("{\"name\":\"a\\\\bc\"}", text);

        JSONReader reader = new JSONReader(new StringReader(text));
        Model model2 = reader.readObject(Model.class);
        assertEquals(model.name, model2.name);
        reader.close();
    }

    @Test
    public void test_special_2() throws Exception {
        Model model = new Model();
        model.name = "a\\bc\"";
        String text = JSON.toJSONString(model);
        assertEquals("{\"name\":\"a\\\\bc\\\"\"}", text);

        JSONReader reader = new JSONReader(new StringReader(text));
        Model model2 = reader.readObject(Model.class);
        assertEquals(model.name, model2.name);
        reader.close();
    }

    public static class Model {
        public String name;
    }
}
