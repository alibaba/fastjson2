package com.alibaba.fastjson;

import junit.framework.TestCase;
import org.junit.Assert;

public class JSONObjectTest_getDate extends TestCase {

    public void test_get_empty() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", "");
        Assert.assertEquals("", obj.get("value"));
        Assert.assertNull(obj.getDate("value"));
    }
}
