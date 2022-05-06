package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import junit.framework.TestCase;
import org.junit.Assert;

import java.lang.reflect.Type;

public class JSONObjectTest_getObj_2 extends TestCase {

    public void test_get_empty() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", "");
        Assert.assertEquals("", obj.get("value"));
        Assert.assertNull(obj.getObject("value", Model.class));
    }

    public void test_get_null() throws Exception {
        TypeUtils.cast("null", getType(), ParserConfig.getGlobalInstance());
        TypeUtils.cast("", getType(), ParserConfig.getGlobalInstance());
    }

    public static class Model {

    }

    public static <T> Type getType() {
        return new TypeReference<T[]>() {}.getType();
    }
}
