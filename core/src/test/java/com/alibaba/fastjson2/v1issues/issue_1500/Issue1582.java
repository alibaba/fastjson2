package com.alibaba.fastjson2.v1issues.issue_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue1582 {
    @Test
    public void test_for_issue() throws Exception {
        assertSame(Size.Big, JSON.parseObject("\"Big\"", Size.class));
        assertSame(Size.Big, JSON.parseObject("\"big\"", Size.class));
        assertNull(JSON.parseObject("\"Large\"", Size.class));
        assertSame(Size.LL, JSON.parseObject("\"L3\"", Size.class));

        assertSame(Size.Small, JSON.parseObject("\"Little\"", Size.class));
    }

    @Test
    public void test_for_issue_1() throws Exception {
        JSONObject object = JSON.parseObject("{\"size\":\"Little\"}");
        Model model = object.toJavaObject(Model.class);
        assertSame(Size.Small, model.size);
    }

    public static class Model {
        public Size size;
    }

    public static enum Size {
        Big,

        @JSONField(alternateNames = "Little")
        Small,

        @JSONField(name = "L3")
        LL
    }
}
