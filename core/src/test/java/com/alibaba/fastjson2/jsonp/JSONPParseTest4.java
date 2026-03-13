package com.alibaba.fastjson2.jsonp;

import com.alibaba.fastjson2.JSONPObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 21/02/2017.
 */
@Tag("compat-jsonp")
public class JSONPParseTest4 {
    @Test
    public void test_f() throws Exception {
        JSONPObject p = new JSONPObject();
        p.setFunction("f");
        assertEquals("f()", p.toString());
    }

    @Test
    public void test_1() throws Exception {
        JSONPObject p = new JSONPObject("f");
        assertEquals("f()", p.toString());
    }
}
