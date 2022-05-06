package com.alibaba.fastjson;

import junit.framework.TestCase;
import org.junit.Assert;

public class JSONObjectTest_hashCode extends TestCase {

    public void test_hashCode() throws Exception {
        Assert.assertEquals(new JSONObject().hashCode(), new JSONObject().hashCode());
    }

    public void test_hashCode_1() throws Exception {
        Assert.assertEquals(JSON.parseObject("{a:1}"), JSON.parseObject("{'a':1}"));
    }
}
