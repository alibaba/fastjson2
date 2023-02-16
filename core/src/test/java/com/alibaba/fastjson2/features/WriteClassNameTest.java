package com.alibaba.fastjson2.features;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class WriteClassNameTest {
    @Test
    public void test() {
        Http http = new Http();
        String json = "{\"type\":\"HTTP\"}";
        assertEquals(json, JSON.toJSONString(http));

        Checker checker = JSON.parseObject(json, Checker.class);
        assertSame(Http.class, checker.getClass());
    }

    @Test
    public void testDefault() {
        Checker checker = JSON.parseObject("{\"type\":\"no-exist\"}", Checker.class);
        assertSame(None.class, checker.getClass());
    }

    @Test
    public void testRegisterSubType() {
        JSON.registerSeeAlsoSubType(TestChecker.class);
        Checker checker = JSON.parseObject("{\"type\":\"Test\"}", Checker.class);
        assertSame(TestChecker.class, checker.getClass());
    }

    @Test
    public void testRegisterSubType1() {
        JSON.registerSeeAlsoSubType(XChecker.class, "X");
        Checker checker = JSON.parseObject("{\"type\":\"X\"}", Checker.class);
        assertSame(XChecker.class, checker.getClass());
    }

    @JSONType(typeName = "Test")
    public static class TestChecker
            extends Checker {
    }

    public static class XChecker
            extends Checker {
    }

    @JSONType(
            typeKey = "type",
            serializeFeatures = JSONWriter.Feature.WriteClassName,
            seeAlso = {None.class, Http.class, MySql.class},
            seeAlsoDefault = None.class
    )
    public abstract static class Checker {
    }

    @JSONType(typeName = "None")
    public static class None
            extends Checker {
    }

    @JSONType(typeName = "HTTP")
    public static class Http
            extends Checker {
    }

    @JSONType(typeName = "MySQL")
    public static class MySql
            extends Checker {
    }
}
