package com.alibaba.fastjson.issue_3000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3330 {
    private char[] chars;

    @BeforeEach
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

    @AfterEach
    public void tearDown() throws Exception {
        System.arraycopy(chars, 0, IOUtils.DIGITS, 0, chars.length);
    }

    @Test
    public void test_for_issue() throws Exception {
        String str = JSON.toJSONString("中国", SerializerFeature.BrowserCompatible);
        assertEquals("\"中国\"", str);
    }
}
