package com.alibaba.fastjson;

import junit.framework.TestCase;
import org.junit.Assert;

public class JSONArrayTest_hashCode extends TestCase {

    public void test_hashCode() throws Exception {
        Assert.assertEquals(new JSONArray().hashCode(), new JSONArray().hashCode());
    }

    public void test_hashCode_1() throws Exception {
        Assert.assertEquals(JSON.parseArray("[]"), JSON.parseArray("[]"));
    }
}
