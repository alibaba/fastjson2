package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringFieldTest_special_1 {
    @Test
    public void test_special() throws Exception {
        Model model = new Model();
        StringBuilder buf = new StringBuilder();
        for (int i = Character.MIN_VALUE; i < Character.MAX_VALUE; ++i) {
            buf.append((char) i);
        }
        model.name = buf.toString();

        String text = JSON.toJSONString(model);

        Model model2 = JSON.parseObject(text, Model.class);
        assertEquals(model.name, model2.name);
    }

    @Test
    public void test_special_browsecue() throws Exception {
        Model model = new Model();
        StringBuilder buf = new StringBuilder();
        for (int i = Character.MIN_VALUE; i < Character.MAX_VALUE; ++i) {
            buf.append((char) i);
        }
        model.name = buf.toString();

        String text = JSON.toJSONString(model, JSONWriter.Feature.BrowserSecure);
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        Model model2 = JSON.parseObject(text, Model.class);
        assertEquals(model.name, model2.name);
    }

    @Test
    public void test_special_browsecompatible() throws Exception {
        Model model = new Model();
        StringBuilder buf = new StringBuilder();
        for (int i = Character.MIN_VALUE; i < Character.MAX_VALUE; ++i) {
            buf.append((char) i);
        }
        model.name = buf.toString();

        String text = JSON.toJSONString(model, JSONWriter.Feature.BrowserCompatible);

        Model model2 = JSON.parseObject(text, Model.class);
        assertEquals(model.name, model2.name);
    }

    @Test
    public void test() throws Exception {
        Model model = new Model();
        model.name = "<>";
        String str = JSON.toJSONString(model, JSONWriter.Feature.BrowserSecure);
        assertEquals("{\"name\":\"\\u003c\\u003e\"}", str);
    }

    public static class Model {
        public String name;
    }
}
