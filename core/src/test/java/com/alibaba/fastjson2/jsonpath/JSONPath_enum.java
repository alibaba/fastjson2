package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class JSONPath_enum {

    @Test
    public void test_name() throws Exception {
        Model model = new Model();
        model.size = Size.Small;

        Assert.assertEquals(Size.Small.name(), JSONPath.eval(model, "$.size.name"));
    }

    @Test
    public void test_orginal() throws Exception {
        Model model = new Model();
        model.size = Size.Small;

        Assert.assertEquals(Size.Small.ordinal(), JSONPath.eval(model, "$.size.ordinal"));
    }

    public static class Model {
        public Size size;
    }

    public enum Size {
        Big, Median, Small
    }
}
