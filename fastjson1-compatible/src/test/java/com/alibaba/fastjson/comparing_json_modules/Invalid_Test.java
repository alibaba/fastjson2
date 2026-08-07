package com.alibaba.fastjson.comparing_json_modules;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 25/03/2017.
 */
public class Invalid_Test {
    @Test
    public void test_6_1() throws Exception {
        assertEquals(0, JSON.parse("+0"));
    }

//    public void test_6_5() throws Exception {
//        assertEquals(28, JSON.parse("034"));
//    }
}
