package com.alibaba.fastjson2.odps;

import com.alibaba.fastjson2.support.odps.JSONExtract2;
import com.alibaba.fastjson2.support.odps.JSONExtractInt32;
import com.alibaba.fastjson2.support.odps.JSONExtractInt64;
import com.aliyun.odps.io.Text;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSONExtract2Test {
    @Test
    public void test() {
        JSONExtract2 udf = new JSONExtract2();
        assertNull(udf.evaluate(null, "$"));
        assertNull(udf.evaluate("", "$"));
        assertNull(udf.evaluate("null", "$"));
        assertEquals("123", udf.evaluate("123", "$"));
    }

    @Test
    public void test2() {
        JSONExtractInt32 udf = new JSONExtractInt32("$");
        assertNull(udf.eval(new Text("null")));
        assertNotNull(udf.eval(new Text("123")));
        assertEquals("123", udf.eval(new Text("123")).toString());
    }

    @Test
    public void test23() {
        JSONExtractInt64 udf = new JSONExtractInt64("$");
        assertNull(udf.eval(new Text("null")));
        assertNotNull(udf.eval(new Text("123")));
        assertEquals("123", udf.eval(new Text("123")).toString());
    }
}
