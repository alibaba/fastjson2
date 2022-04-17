package com.alibaba.json.bvt.issue_3300;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;
import junit.framework.TestCase;

public class Issue3330 extends TestCase {
    private char[] chars;
    protected void setUp() throws Exception {
        chars = new char[IOUtils.DIGITS.length];
        System.arraycopy(IOUtils.DIGITS, 0, chars, 0, IOUtils.DIGITS.length);

        IOUtils.DIGITS[10] = 'a';
        IOUtils.DIGITS[11] = 'b';
        IOUtils.DIGITS[12] = 'c';
        IOUtils.DIGITS[13] = 'd';
        IOUtils.DIGITS[14] = 'e';
        IOUtils.DIGITS[15] = 'f';
    }

    protected void tearDown() throws Exception {
        System.arraycopy(chars, 0, IOUtils.DIGITS, 0, chars.length);
    }

    public void test_for_issue() throws Exception {
        String str = JSON.toJSONString("中国", SerializerFeature.BrowserCompatible);
        assertEquals("\"中国\"", str);
    }
}
